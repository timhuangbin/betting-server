package com.betting.handlers;

import com.betting.annotation.BodyParam;
import com.betting.annotation.PathParam;
import com.betting.annotation.QueryParam;
import com.betting.annotation.Route;
import com.betting.service.BettingService;
import com.betting.service.SessionService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class StakeHandler extends BaseHandler {
    private final SessionService sessionService;
    private final BettingService bettingService;

    public StakeHandler(SessionService sessionService, BettingService bettingService) {
        this.sessionService = sessionService;
        this.bettingService = bettingService;
    }

    @Route(method = "POST", pattern = "/\\d+/stake")
    public void handlePostStake(
            @PathParam(index = 1) int offerId,
            @QueryParam("sessionkey") String sessionKey,
            @BodyParam int stake,
            HttpExchange exchange) throws IOException {

        // 验证会话
        if (!sessionService.isValidSession(sessionKey)) {
            sendError(exchange, 401);
            return;
        }

        // 获取客户ID
        Integer customerId = sessionService.getCustomerId(sessionKey).orElse(null);
        if (customerId == null) {
            sendError(exchange, 401);
            return;
        }

        try {
            bettingService.addStake(offerId, customerId, stake);
            sendEmptyResponse(exchange);
        } catch (Exception e) {
            sendError(exchange, 500);
        }
    }
}
