package com.betting.handlers;

import com.betting.util.HttpUtils;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public abstract class BaseHandler {

    protected void sendResponse(HttpExchange exchange, String response) throws IOException {
        HttpUtils.sendResponse(exchange, 200, response);
    }

    protected void sendEmptyResponse(HttpExchange exchange) throws IOException {
        HttpUtils.sendEmptyResponse(exchange, 200);
    }

    protected void sendError(HttpExchange exchange, int statusCode) throws IOException {
        HttpUtils.sendEmptyResponse(exchange, statusCode);
    }
}
