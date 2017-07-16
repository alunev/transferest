package org.alunev.transferest.model.error;


public class UnexpectedException extends Exception {
    public UnexpectedException(String message, Exception exception) {
        super(message, exception);
    }
}
