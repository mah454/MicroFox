package ir.moke.microfox.http.proxy;

import ir.moke.microfox.api.http.*;
import ir.moke.microfox.api.http.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RestRouter {

    private static final Map<Class<?>, HttpMethod> HTTP_METHOD_ANNOTATIONS = Map.of(
            GET.class, HttpMethod.GET,
            POST.class, HttpMethod.POST,
            PUT.class, HttpMethod.PUT,
            DELETE.class, HttpMethod.DELETE,
            PATCH.class, HttpMethod.PATCH
    );

    public static void registerRoutes(Class<?> restClass, RestConsumer consumer) {
        String basePath = Optional.ofNullable(restClass.getAnnotation(Path.class))
                .map(Path::value)
                .orElse("");

        for (Method method : restClass.getDeclaredMethods()) {
            HttpMethod httpMethod = resolveHttpMethod(method);
            if (httpMethod == null) continue;

            String methodPath = Optional.ofNullable(method.getAnnotation(Path.class))
                    .map(Path::value)
                    .orElse("");

            List<String> roles = Arrays.stream(Optional.ofNullable(method.getAnnotation(Role.class))
                            .map(Role::value)
                            .orElse(new String[0]))
                    .toList();

            List<String> scopes = Arrays.stream(Optional.ofNullable(method.getAnnotation(Scope.class))
                            .map(Scope::value)
                            .orElse(new String[0]))
                    .toList();

            String fullPath = HttpUtils.normalizePath(basePath) + HttpUtils.normalizePath(methodPath);
            method.setAccessible(true);

            Route route = (req, resp) -> {
                Object[] args = resolveArgs(method, req, resp);
                Object result = method.invoke(null, args); // static methods
                if (result != null) resp.body(result); // assumes Response has json()
            };

            consumer.accept(fullPath, httpMethod, route, roles, scopes);
        }
    }

    @SuppressWarnings("unchecked")
    private static HttpMethod resolveHttpMethod(Method method) {
        return HTTP_METHOD_ANNOTATIONS.entrySet().stream()
                .filter(e -> method.isAnnotationPresent((Class<? extends Annotation>) e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private static Object[] resolveArgs(Method method, Request req, Response response) {
        Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            args[i] = resolveParam(params[i], req, response);
        }
        return args;
    }

    private static Object resolveParam(Parameter param, Request req, Response resp) {
        if (param.isAnnotationPresent(PostBody.class)) {
            return req.body(param.getType());
        } else if (param.isAnnotationPresent(BeanParam.class)) {
            return req.bean(param.getType());
        } else if (param.getType().isAssignableFrom(Request.class)) {
            return req;
        } else if (param.getType().isAssignableFrom(Response.class)) {
            return resp;
        } else {
            return BeanParamBinder.resolveParameterValue(param, req);
        }
    }
}
