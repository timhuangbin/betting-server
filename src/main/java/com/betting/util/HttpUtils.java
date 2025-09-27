package com.betting.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class HttpUtils {
    public static void sendResponse(HttpExchange exchange, int statusCode, String response)
            throws IOException {
        byte[] responseBytes = response.getBytes();
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    public static void sendEmptyResponse(HttpExchange exchange, int statusCode)
            throws IOException {
        exchange.sendResponseHeaders(statusCode, -1);
    }
}
