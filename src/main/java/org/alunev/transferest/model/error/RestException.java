package org.alunev.transferest.model.error;


public class RestException extends Exception {
    private final String message;

    public RestException(String message) {
        this.message = message;
    }
}
