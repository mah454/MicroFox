package ir.moke.test.rest;

import ir.moke.microfox.MicroFox;
import ir.moke.microfox.api.http.SecurityInfo;
import ir.moke.microfox.exception.MicroFoxException;
import ir.moke.test.exception.ExceptionController;
import ir.moke.test.http.security.BasicAuthSecurity;
import ir.moke.test.http.security.JwtSecurity;

import static ir.moke.microfox.MicroFox.exceptionMapperRegister;

public class RestMainClass {
    static void main() {
//        exceptionMapperRegister(MicroFoxException.class, ExceptionController::handleMicroFoxException);
        MicroFox.security(new SecurityInfo("/api/login", new BasicAuthSecurity(), 1));
        MicroFox.security(new SecurityInfo("/api/security", new JwtSecurity(), 2));

        MicroFox.rest(SampleRestApi.class);
    }
}
