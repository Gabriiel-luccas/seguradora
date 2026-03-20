package com.acme.seguradora.domain.exception;

public class QuoteValidationException extends RuntimeException {

    public QuoteValidationException(String message) {
        super(message);
    }
}
