package com.betting.server;

import com.betting.annotation.Route;
import com.betting.util.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RequestRouter implements HttpHandler {
    private final List<RouteMapping> routes;

    public RequestRouter() {
        this.routes = new ArrayList<>();
    }

    public RequestRouter register(Object handler) {
        scanHandlerRoutes(handler);
        return this;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        Optional<RouteMapping> matchingRoute = findMatchingRoute(method, path);

        if (matchingRoute.isPresent()) {
            try {
                matchingRoute.get().invoke(exchange, path);
            } catch (Exception e) {
                handleError(exchange, e);
            }
        } else {
            handleNotFound(exchange, method, path);
        }
    }

    private void scanHandlerRoutes(Object handler) {
        for (Method method : handler.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Route.class)) {
                registerRoute(handler, method);
            }
        }
    }

    private void registerRoute(Object handler, Method method) {
        Route route = method.getAnnotation(Route.class);
        RouteMapping mapping = new RouteMapping(route.method(), route.pattern(), handler, method);
        routes.add(mapping);
    }

    private Optional<RouteMapping> findMatchingRoute(String method, String path) {
        return routes.stream()
                .filter(route -> route.matches(method, path))
                .findFirst();
    }

    private void handleError(HttpExchange exchange, Exception error) throws IOException {
        System.err.println("Error handling request: " + error.getMessage());
        HttpUtils.sendEmptyResponse(exchange, 500);
    }

    private void handleNotFound(HttpExchange exchange, String method, String path) throws IOException {
        System.out.println("Route not found: " + method + " " + path);
        HttpUtils.sendEmptyResponse(exchange, 404);
    }
}
