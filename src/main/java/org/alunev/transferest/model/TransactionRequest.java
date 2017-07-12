package org.alunev.transferest.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Currency;

@Builder(toBuilder = true)
public class TransactionRequest {
    @Getter
    private final String senderName;

    @Getter
    private final String receiverName;

    @Getter
    private final BigDecimal amount;

    @Getter
    private final Currency currency;
}
