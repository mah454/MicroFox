package ir.moke.microfox.api.http;

import java.util.List;

@FunctionalInterface
public interface RestConsumer {

    void accept(String path, HttpMethod method, Route route, List<String> roles, List<String> scopes);
}
