package com.coworking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        Map<String, String> campos
) {

    public ErroResponse(int status, String erro, String mensagem) {
        this(LocalDateTime.now(), status, erro, mensagem, null);
    }

    public ErroResponse(int status, String erro, String mensagem, Map<String, String> campos) {
        this(LocalDateTime.now(), status, erro, mensagem, campos);
    }
}
