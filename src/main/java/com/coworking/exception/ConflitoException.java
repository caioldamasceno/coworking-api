package com.coworking.exception;

public abstract class ConflitoException extends RuntimeException {

    protected ConflitoException(String mensagem) {
        super(mensagem);
    }
}
