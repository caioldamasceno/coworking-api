package com.coworking.exception;

import com.coworking.dto.response.ErroResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(RecursoNaoEncontradoException ex) {
        ErroResponse corpo = new ErroResponse(HttpStatus.NOT_FOUND.value(), "Nao encontrado", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpo);
    }

    @ExceptionHandler(ConflitoDeHorarioException.class)
    public ResponseEntity<ErroResponse> tratarConflito(ConflitoDeHorarioException ex) {
        ErroResponse corpo = new ErroResponse(HttpStatus.CONFLICT.value(), "Conflito de horario", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(corpo);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                campos.put(erro.getField(), erro.getDefaultMessage()));
        ErroResponse corpo = new ErroResponse(HttpStatus.BAD_REQUEST.value(), "Validacao", "Campos invalidos", campos);
        return ResponseEntity.badRequest().body(corpo);
    }
}
