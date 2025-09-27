package com.betting.server;

import com.betting.annotation.BodyParam;
import com.betting.annotation.PathParam;
import com.betting.annotation.QueryParam;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RouteMapping {
    private final String httpMethod;
    private final Pattern pattern;
    private final Object handler;
    private final Method method;
    private final Parameter[] parameters;

    public RouteMapping(String httpMethod, String pattern, Object handler, Method method) {
        this.httpMethod = httpMethod.toUpperCase();
        this.pattern = Pattern.compile(pattern);
        this.handler = handler;
        this.method = method;
        this.parameters = method.getParameters();

        method.setAccessible(true);
    }

    public boolean matches(String method, String path) {
        return this.httpMethod.equalsIgnoreCase(method) &&
                pattern.matcher(path).matches();
    }

    public void invoke(HttpExchange exchange, String path) throws Exception {
        Object[] args = resolveParameters(exchange, path);
        method.invoke(handler, args);
    }

    private Object[] resolveParameters(HttpExchange exchange, String path) throws IOException {
        Object[] args = new Object[parameters.length];
        String[] pathParts = path.split("/");
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> queryParams = parseQueryParams(query);
        String body = new String(exchange.getRequestBody().readAllBytes());

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Class<?> type = param.getType();

            // 处理 HttpExchange 参数
            if (type == HttpExchange.class) {
                args[i] = exchange;
                continue;
            }

            // 处理查询参数
            if (param.isAnnotationPresent(QueryParam.class)) {
                QueryParam annotation = param.getAnnotation(QueryParam.class);
                String value = queryParams.get(annotation.value());
                args[i] = convertValue(value, type);
                continue;
            }

            // 处理路径参数
            if (param.isAnnotationPresent(PathParam.class)) {
                PathParam annotation = param.getAnnotation(PathParam.class);
                int index = annotation.index() != -1 ? annotation.index() : i;
                if (index < pathParts.length) {
                    args[i] = convertValue(pathParts[index], type);
                }
                continue;
            }

            // 处理请求体参数
            if (param.isAnnotationPresent(BodyParam.class)) {
                args[i] = convertValue(body, type);
                continue;
            }

            // 默认按顺序处理路径参数
            if (i < pathParts.length) {
                args[i] = convertValue(pathParts[i], type);
            }
        }

        return args;
    }

    private Object convertValue(String value, Class<?> targetType) {
        if (value == null) return null;

        if (targetType == String.class) {
            return value;
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }

        return value; // 默认返回字符串
    }

    private Map<String, String> parseQueryParams(String query) {
        if (query == null) return Collections.emptyMap();

        Map<String, String> params = new HashMap<>();
        for (String pair : query.split("&")) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }
        return params;
    }
}
