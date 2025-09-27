package com.betting.server;

import com.betting.handlers.HighStakesHandler;
import com.betting.handlers.SessionHandler;
import com.betting.handlers.StakeHandler;
import com.betting.service.BettingService;
import com.betting.service.SessionService;
import com.betting.store.BettingStore;
import com.betting.store.SessionStore;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BettingServer {
    private static final int PORT = 8001;
    private static final int THREAD_POOL_SIZE = 10;
    private static final int HIGH_STAKES_LIMIT = 20;
    private static final long SESSION_CLEANUP_INTERVAL_MINUTES = 1;

    private HttpServer server;
    private ScheduledExecutorService scheduler;

    private final SessionService sessionService;
    private final BettingService bettingService;

    public BettingServer() {
        SessionStore sessionStore = new SessionStore();
        BettingStore bettingStore = new BettingStore(HIGH_STAKES_LIMIT);

        this.sessionService = new SessionService(sessionStore);
        this.bettingService = new BettingService(bettingStore);
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        RequestRouter router = new RequestRouter()
                .register(new SessionHandler(sessionService))
                .register(new StakeHandler(sessionService, bettingService))
                .register(new HighStakesHandler(bettingService));
        server.createContext("/", router);

        server.setExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE));

        server.start();
        System.out.println("Server started on port " + PORT);

        startSessionCleanupTask();

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("HTTP server stopped");
        }

        if (scheduler != null) {
            scheduler.shutdown();
            System.out.println("Scheduler stopped");
        }
    }

    private void startSessionCleanupTask() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(
                sessionService::cleanupExpiredSessions,
                SESSION_CLEANUP_INTERVAL_MINUTES,
                SESSION_CLEANUP_INTERVAL_MINUTES,
                TimeUnit.MINUTES
        );
    }

    public static void main(String[] args) {
        BettingServer bettingServer = new BettingServer();
        try {
            bettingServer.start();
            System.out.println("Press Ctrl+C to stop the server");

            Thread.currentThread().join();
        } catch (IOException | InterruptedException e) {
            System.err.println("Server error: " + e.getMessage());
            bettingServer.stop();
        }
    }
}
