package org.justice.modelapicorereloaded.restapi;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.justice.modelapicorereloaded.annotation.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestApiServer {
    private final HttpServer server;
    private final Gson gson = new Gson();
    private final List<ControllerDefinition> registeredControllers = new ArrayList<>();

    public RestApiServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/", this::handleRequest);
    }

    record RouteHandler(String method, String path, HandlerFunction handler, Method handlerMethod) {}
    private final List<RouteHandler> routeHandlers = new ArrayList<>();
    record RouteDefinition(String httpMethod, String originalPath, Pattern pattern, HttpHandler handler, Method method) {}
    List<RouteDefinition> routes = new ArrayList<>();

    private Pattern pathToRegex(String pathTemplate) {
        String regex = "^" + pathTemplate.replaceAll("\\{([a-zA-Z][a-zA-Z0-9_]*)}", "(?<$1>[^/]+)") + "$";
        return Pattern.compile(regex);
    }

    private void handleRequest(HttpExchange exchange) {
        addCorsHeaders(exchange);
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            try {
                respond(exchange, 204, "");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        String requestPath = exchange.getRequestURI().getPath();
        String requestMethod = exchange.getRequestMethod();

        for (RouteHandler handler : routeHandlers) {
            if (handler.method().equalsIgnoreCase(requestMethod) && pathMatches(handler.path(), requestPath)) {
                try {
                    Method matchedMethod = handler.handlerMethod();
                    if (matchedMethod.isAnnotationPresent(AuthenticationRequired.class)) {
                        if (!isAuthenticated(exchange)) {
                            respond(exchange, 401, "Unauthorized");
                            return;
                        }
                    }
                    if (matchedMethod.isAnnotationPresent(Permission.class)) {
                        String requiredPermission = matchedMethod.getAnnotation(Permission.class).value();
                        if (!hasPermission(exchange, requiredPermission)) {
                            respond(exchange, 403, "Forbidden");
                            return;
                        }
                    }

                    handler.handler().handle(exchange);
                    return;

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        respond(exchange, 500, "Internal Server Error: " + e.getMessage());
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    return;
                }
            }
        }

        try {
            respond(exchange, 404, "Not Found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void registerHandler(String httpMethod, String path, HandlerFunction handler, Method handlerMethod) {
        routeHandlers.add(new RouteHandler(httpMethod, path, handler, handlerMethod));
    }

    public void registerController(Object controller) {
        Class<?> clazz = controller.getClass();
        if (!clazz.isAnnotationPresent(RestController.class)) return;
        String basePath = clazz.getAnnotation(RestController.class).path();

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GetMapping.class) || method.isAnnotationPresent(PostMapping.class)) {
                String endpointTemplate = basePath + (method.isAnnotationPresent(GetMapping.class)
                        ? method.getAnnotation(GetMapping.class).value()
                        : method.getAnnotation(PostMapping.class).value());

                Pattern pattern = pathToRegex(endpointTemplate);

                String httpMethod = method.isAnnotationPresent(GetMapping.class) ? "GET" : "POST";
                routes.add(new RouteDefinition(httpMethod, endpointTemplate, pattern, exchange -> {
                    Object[] args = resolveMethodArguments(method, exchange, pattern);
                    Object response = null;
                    try {
                        response = method.invoke(controller, args);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                    respond(exchange, 200, gson.toJson(response));
                }, method));

                registerHandler(httpMethod, endpointTemplate, exchange -> {
                    Object[] args = resolveMethodArguments(method, exchange, pattern);
                    Object response = method.invoke(controller, args);
                    respond(exchange, 200, gson.toJson(response));
                }, method);
            }
        }
        registeredControllers.add(new ControllerDefinition(clazz, controller));
    }

    private void addCorsHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        headers.add("Access-Control-Max-Age", "86400");
    }

    private Map<String, String> extractPathParams(Pattern pattern, String actualPath) {
        Matcher matcher = pattern.matcher(actualPath);
        Map<String, String> pathParams = new HashMap<>();
        if (matcher.matches()) {
            matcher.groupCount();
            pattern.pattern().split("\\(\\?<");
            for (String groupName : pattern.pattern().split("\\(\\?<")) {
                if (groupName.contains(">")) {
                    String name = groupName.substring(0, groupName.indexOf(">"));
                    pathParams.put(name, matcher.group(name));
                }
            }
        }
        return pathParams;
    }

    private Object[] resolveMethodArguments(Method method, HttpExchange exchange, Pattern pattern) throws IOException {
        Parameter[] parameters = method.getParameters();
        Map<String, String> pathParams = extractPathParams(pattern, exchange.getRequestURI().getPath());
        List<Object> args = new ArrayList<>();

        for (Parameter param : parameters) {
            if (param.isAnnotationPresent(RequestBody.class)) {
                InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                args.add(gson.fromJson(reader, param.getType()));
            } else if (param.isAnnotationPresent(PathParam.class)) {
                String key = param.getAnnotation(PathParam.class).value();
                args.add(convert(pathParams.get(key), param.getType()));
            } else if (param.isAnnotationPresent(HeaderParam.class)) {
                String key = param.getAnnotation(HeaderParam.class).value();
                String value = exchange.getRequestHeaders().getFirst(key);
                args.add(convert(value, param.getType()));
            } else {
                args.add(null);
            }
        }

        return args.toArray();
    }

    private boolean pathMatches(String mappingPath, String requestPath) {
        mappingPath = mappingPath.replaceAll("/+$", "");
        requestPath = requestPath.replaceAll("/+$", "");

        String[] mappingParts = mappingPath.split("/");
        String[] requestParts = requestPath.split("/");

        if (mappingParts.length != requestParts.length) return false;

        for (int i = 0; i < mappingParts.length; i++) {
            if (mappingParts[i].startsWith("{") && mappingParts[i].endsWith("}")) continue;
            if (!mappingParts[i].equals(requestParts[i])) return false;
        }
        return true;
    }

    private boolean isAuthenticated(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
        String token = authHeader.substring(7);
        return JwtUtil.validateToken(token);
    }

    private boolean hasPermission(HttpExchange exchange, String permission) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
        String token = authHeader.substring(7);
        if (!JwtUtil.validateToken(token)) return false;
        String username = JwtUtil.getUsernameFromToken(token);
        return "admin".equals(username);
    }

    private Object convert(String value, Class<?> type) {
        if (type == String.class) return value;
        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == long.class || type == Long.class) return Long.parseLong(value);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
        return null;
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    @FunctionalInterface
    interface HandlerFunction {
        void handle(HttpExchange exchange) throws Exception;
    }

    public void start() {
        server.start();
        registerOpenApiEndpoint();
        System.out.println("[RestApiServer] Running on port " + server.getAddress().getPort());
    }


    private void registerOpenApiEndpoint() {
        server.createContext("/openapi.json", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                respond(exchange, 405, "Method Not Allowed");
                return;
            }

            Map<String, Object> openApi = new LinkedHashMap<>();
            openApi.put("openapi", "3.0.0");

            Map<String, Object> info = Map.of(
                    "title", "Minecraft Plugin API",
                    "version", "1.0.0"
            );
            openApi.put("info", info);

            Map<String, Object> paths = new LinkedHashMap<>();

            for (ControllerDefinition def : registeredControllers) {
                String basePath = def.controllerClass.getAnnotation(RestController.class).path();

                for (Method method : def.controllerClass.getDeclaredMethods()) {
                    String httpMethod = null;
                    String path = null;
                    boolean hasBody = false;

                    if (method.isAnnotationPresent(GetMapping.class)) {
                        httpMethod = "get";
                        path = basePath + method.getAnnotation(GetMapping.class).value();
                    } else if (method.isAnnotationPresent(PostMapping.class)) {
                        httpMethod = "post";
                        path = basePath + method.getAnnotation(PostMapping.class).value();
                        hasBody = true;

                    } else if (method.isAnnotationPresent(PutMapping.class)) {
                        httpMethod = "put";
                        path = basePath + method.getAnnotation(PutMapping.class).value();
                        hasBody = true;

                    } else if (method.isAnnotationPresent(DeleteMapping.class)) {
                        httpMethod = "delete";
                        path = basePath + method.getAnnotation(DeleteMapping.class).value();
                        hasBody = true;
                    }

                    if (httpMethod != null && path != null) {
                        Map<String, Object> methodObj = new LinkedHashMap<>();
                        methodObj.put("summary", method.getName());
                        methodObj.put("responses", Map.of("200", Map.of("description", "OK")));

                        if (hasBody) {
                            methodObj.put("requestBody", Map.of(
                                    "required", true,
                                    "content", Map.of(
                                            "application/json", Map.of(
                                                    "schema", Map.of("type", "object")
                                            )
                                    )
                            ));
                        }

                        paths.putIfAbsent(path, new LinkedHashMap<>());
                        ((Map<String, Object>) paths.get(path)).put(httpMethod, methodObj);
                    }
                }
            }

            openApi.put("paths", paths);
            respond(exchange, 200, gson.toJson(openApi));
        });
    }



}
