package br.pucc.restmanager.auth;

import br.pucc.restmanager.common.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        ErrorCode error = authException instanceof BadCredentialsException
                ? ErrorCode.TOKEN_INVALIDO
                : ErrorCode.TOKEN_AUSENTE;
        SecurityErrorResponse.write(response, error);
    }
}
