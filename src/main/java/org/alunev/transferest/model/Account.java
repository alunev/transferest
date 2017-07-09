package org.alunev.transferest.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * User account - has single currency, balance can't be < 0
 * One User may have many Accounts. One Account belongs to exactly one User.
 */

@Builder(toBuilder = true)
public class Account {
    @Getter
    private final long id;

    @Getter
    private final long ownerId;

    @Getter
    private final String number;

    @Getter
    private final BigDecimal balance;

    @Getter
    private final String currency;

    @Getter
    private final Timestamp updateTs;

    private Account(long id, long ownerId, String number, BigDecimal balance, String currency, Timestamp updateTs) {
        this.id = id;
        this.ownerId = ownerId;
        this.number = number;
        this.balance = balance;
        this.currency = currency;
        this.updateTs = updateTs;
    }
}
