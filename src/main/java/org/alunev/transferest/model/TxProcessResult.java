package org.alunev.transferest.model;

import lombok.Getter;
import lombok.ToString;

@ToString
public class TxProcessResult {
    @Getter
    private final TxStatus status;

    @Getter
    private final String message;

    public TxProcessResult(TxStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
