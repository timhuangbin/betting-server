package com.betting;

import com.betting.handlers.HighStakesHandler;
import com.betting.handlers.SessionHandler;
import com.betting.handlers.StakeHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class BettingServer {
    private static final int PORT = 8001;
    private static final int BACKLOG = 0;
    private static final int THREAD_POOL_SIZE = 10;

    private HttpServer server;
    private SessionManager sessionManager;
    private BettingDataStore dataStore;

    public BettingServer() {
        this.sessionManager = new SessionManager();
        this.dataStore = new BettingDataStore();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), BACKLOG);

        // Set up the context processor - using suffix matching
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();

            if (path.endsWith("/session")) {
                new SessionHandler(sessionManager).handle(exchange);
            } else if (path.contains("/stake")) {
                new StakeHandler(sessionManager, dataStore).handle(exchange);
            } else if (path.endsWith("/highstakes")) {
                new HighStakesHandler(dataStore).handle(exchange);
            } else {
                exchange.sendResponseHeaders(404, -1); // Not Found
            }
        });

        // Set up the thread pool
        server.setExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE));

        // Start server
        server.start();
        System.out.println("Server started on port " + PORT);

        // Start the session cleanup thread
        startSessionCleanupThread();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Server stopped");
        }
    }

    private void startSessionCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000); // Check once per minute
                    sessionManager.cleanupExpiredSessions();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    public static void main(String[] args) {
        BettingServer bettingServer = new BettingServer();
        try {
            bettingServer.start();

            // Add a shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(bettingServer::stop));

            // Keep the server running
            Thread.currentThread().join();
        } catch (IOException | InterruptedException e) {
            System.err.println("Server error: " + e.getMessage());
            bettingServer.stop();
        }
    }
}
