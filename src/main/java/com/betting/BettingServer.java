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

        // 设置上下文处理器 - 使用前缀匹配
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

        // 设置线程池
        server.setExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE));

        // 启动服务器
        server.start();
        System.out.println("Server started on port " + PORT);

        // 启动会话清理线程
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
                    Thread.sleep(60000); // 每分钟检查一次
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

            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(bettingServer::stop));

            // 保持服务器运行
            Thread.currentThread().join();
        } catch (IOException | InterruptedException e) {
            System.err.println("Server error: " + e.getMessage());
            bettingServer.stop();
        }
    }
}
