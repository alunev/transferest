package org.alunev.transferest.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Money transfer event info. Transfer may happen between exactly 2 accounts.
 */
@Builder(toBuilder = true)
public class Transaction {
    @Getter
    private final long id;

    @Getter
    private final long senderAccId;

    @Getter
    private final long receiverAccId;

    @Getter
    private final BigDecimal sendAmount;

    @Getter
    private final BigDecimal receiveAmount;

    @Getter
    private final Timestamp updateTs;
}
