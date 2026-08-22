package ir.moke.test.rest;

import ir.moke.microfox.api.http.Request;
import ir.moke.microfox.api.http.Response;
import ir.moke.microfox.api.http.annotation.*;
import ir.moke.microfox.http.SecurityContext;
import ir.moke.test.UserDTO;
import ir.moke.test.http.security.BasicPrincipal;
import ir.moke.test.http.security.TokenProvider;

import java.util.List;

@Path("/api/")
public class SampleRestApi {

    @Path("/hello")
    @GET
    public static void checkQueryParam(@QueryParam("name") String name) {
        System.out.println(name);
    }

    @Path("/person/{test}")
    @POST
    public static void checkRequestDTO(@PostBody UserDTO dto, @PathParam("test") String test) {
        System.out.println(dto);
        System.out.println("test value: " + test);
    }

    @Path("/bean-param")
    @GET
    public static void checkBeanParam(@BeanParam UserDTO dto, Request request, Response response) {
        System.out.println("Request URI : " + request.uri());
        System.out.println(response.status());
        System.out.println(dto);
    }

    @Path("/response")
    @GET
    @Scope("read:users")
    @Role(value = {"ADMIN", "MEMBER"})
    public static UserDTO checkResponseDTO(@QueryParam("id") Long id, @QueryParam("name") String name) {
        return new UserDTO(id, name);
    }

    @Path("/login")
    @GET
    public static void login(Response response) {
        BasicPrincipal principal = (BasicPrincipal) SecurityContext.principal();
        String token = TokenProvider.create(principal.getName(), List.of("ADMIN"), List.of("read:users"));
        response.header("Authorization", "Bearer " + token);
    }

    @Path("/security")
    @GET
    @Scope("read:users")
    @Role(value = {"ADMIN", "MEMBER"})
    public static void checkSecurity() {
        System.out.println("Security Ok");
    }
}
