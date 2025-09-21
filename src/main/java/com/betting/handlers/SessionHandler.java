package com.betting.handlers;

import com.betting.SessionManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class SessionHandler implements HttpHandler {
    private final SessionManager sessionManager;

    public SessionHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Isn't Allowed
            return;
        }

        String path = exchange.getRequestURI().getPath();
        // 路径格式应该是 /{customerId}/session
        String[] pathParts = path.split("/");

        if (pathParts.length < 3) {
            exchange.sendResponseHeaders(400, -1); // Bad Request
            return;
        }

        try {
            // The customer ID is the second part of the path
            int customerId = Integer.parseInt(pathParts[1]);
            String sessionKey = sessionManager.getOrCreateSession(customerId);

            exchange.sendResponseHeaders(200, sessionKey.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(sessionKey.getBytes());
            }
        } catch (NumberFormatException e) {
            exchange.sendResponseHeaders(400, -1); // Bad Request
        }
    }
}
