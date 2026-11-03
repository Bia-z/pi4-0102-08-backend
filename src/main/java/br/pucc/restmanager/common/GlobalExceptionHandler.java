package br.pucc.restmanager.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Transforma BusinessException em resposta HTTP: {"erro": "NOME_DO_ERRO"}
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> tratarNegocio(BusinessException ex) {
        return ResponseEntity.status(ex.getErro().getStatus())
                .body(new ApiError(ex.getErro().name()));
    }
}