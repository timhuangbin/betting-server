package com.betting.handlers;

import com.betting.annotation.PathParam;
import com.betting.annotation.Route;
import com.betting.service.BettingService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class HighStakesHandler extends BaseHandler {
    private final BettingService bettingService;

    public HighStakesHandler(BettingService bettingService) {
        this.bettingService = bettingService;
    }

    @Route(method = "GET", pattern = "/\\d+/highstakes")
    public void handleGetHighStakes(@PathParam(index = 1) int offerId, HttpExchange exchange)
            throws IOException {
        try {
            String highStakes = bettingService.getHighStakes(offerId);
            sendResponse(exchange, highStakes);
        } catch (Exception e) {
            sendError(exchange, 500);
        }
    }
}
