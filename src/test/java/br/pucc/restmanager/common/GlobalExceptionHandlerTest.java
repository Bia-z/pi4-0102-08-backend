package br.pucc.restmanager.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void falhaDeBancoRetornaErroInternoDaFicha() {
        ResponseEntity<ApiError> response = handler.tratarFalhaDeBanco(
                new DataAccessResourceFailureException("database unavailable"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("FALHA_INTERNA_SERVIDO", response.getBody().erro());
    }
}
