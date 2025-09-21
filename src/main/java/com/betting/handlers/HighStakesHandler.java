package com.betting.handlers;

import com.betting.BettingDataStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class HighStakesHandler implements HttpHandler {
    private final BettingDataStore dataStore;

    public HighStakesHandler(BettingDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Isn't Allowed
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        if (pathParts.length < 2) {
            exchange.sendResponseHeaders(400, -1); // Bad Request
            return;
        }

        try {
            int offerId = Integer.parseInt(pathParts[1]);
            String highStakes = dataStore.getHighStakes(offerId);

            exchange.sendResponseHeaders(200, highStakes.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(highStakes.getBytes());
            }
        } catch (NumberFormatException e) {
            exchange.sendResponseHeaders(400, -1); // Bad Request
        }
    }
}
