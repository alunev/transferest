package org.alunev.transferest.model.error;


public class TransferException extends RuntimeException {

    private final String message;

    public TransferException(String message) {
        super(message);

        this.message = message;
    }
}
