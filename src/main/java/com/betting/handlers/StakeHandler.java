package com.betting.handlers;

import com.betting.BettingDataStore;
import com.betting.SessionManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class StakeHandler implements HttpHandler {
    private final SessionManager sessionManager;
    private final BettingDataStore dataStore;

    public StakeHandler(SessionManager sessionManager, BettingDataStore dataStore) {
        this.sessionManager = sessionManager;
        this.dataStore = dataStore;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        // 解析路径获取投注方案ID
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        if (pathParts.length < 2) {
            exchange.sendResponseHeaders(400, -1); // Bad Request
            return;
        }

        try {
            int offerId = Integer.parseInt(pathParts[1]);

            // 检查会话密钥
            URI uri = exchange.getRequestURI();
            String query = uri.getQuery();
            if (query == null || !query.startsWith("sessionkey=")) {
                exchange.sendResponseHeaders(400, -1); // Bad Request
                return;
            }

            String sessionKey = query.substring("sessionkey=".length());
            if (!sessionManager.isValidSession(sessionKey)) {
                exchange.sendResponseHeaders(401, -1); // Unauthorized
                return;
            }

            // 获取客户ID
            Integer customerId = sessionManager.getCustomerId(sessionKey);
            if (customerId == null) {
                exchange.sendResponseHeaders(401, -1); // Unauthorized
                return;
            }

            // 读取请求体中的投注金额
            try (InputStream is = exchange.getRequestBody()) {
                byte[] requestBody = is.readAllBytes();
                String stakeStr = new String(requestBody);
                int stake = Integer.parseInt(stakeStr.trim());

                // 存储投注数据
                dataStore.addStake(offerId, customerId, stake);

                // 返回空响应
                exchange.sendResponseHeaders(200, -1);
            } catch (NumberFormatException e) {
                exchange.sendResponseHeaders(400, -1); // Bad Request
            }
        } catch (NumberFormatException e) {
            exchange.sendResponseHeaders(400, -1); // Bad Request
        }
    }
}
