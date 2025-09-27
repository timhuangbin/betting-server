package com.betting.handlers;

import com.betting.annotation.PathParam;
import com.betting.annotation.Route;
import com.betting.service.SessionService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class SessionHandler extends BaseHandler {
    private final SessionService sessionService;

    public SessionHandler(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Route(method = "GET", pattern = "/\\d+/session")
    public void handleGetSession(@PathParam(index = 1) int customerId, HttpExchange exchange)
            throws IOException {
        try {
            String sessionKey = sessionService.getOrCreateSession(customerId);
            sendResponse(exchange, sessionKey);
        } catch (Exception e) {
            sendError(exchange, 500);
        }
    }
}
