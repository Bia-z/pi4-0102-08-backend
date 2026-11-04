package br.pucc.restmanager.auth;

import br.pucc.restmanager.common.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;

final class SecurityErrorResponse {

    private SecurityErrorResponse() {
    }

    static void write(HttpServletResponse response, ErrorCode error) throws IOException {
        response.setStatus(error.getStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"erro\":\"" + error.name() + "\"}");
    }
}
