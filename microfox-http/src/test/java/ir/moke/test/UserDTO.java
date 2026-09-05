package ir.moke.test;

import ir.moke.microfox.api.http.annotation.QueryParam;

public record UserDTO(@QueryParam("id") long id,
                      @QueryParam("name") String name) {
}
