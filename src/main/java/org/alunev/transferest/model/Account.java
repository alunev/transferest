package org.alunev.transferest.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * User account - has single currency, balance can't be < 0
 * One User may have many Accounts. One Account belongs to exactly one User.
 */
public class Account {
    private final long id;

    private final String number;

    private final BigDecimal balance;

    private final String currency;

    private final Timestamp updateTs;

    private Account(long id, String number, BigDecimal balance, String currency, Timestamp updateTs) {
        this.id = id;
        this.number = number;
        this.balance = balance;
        this.currency = currency;
        this.updateTs = updateTs;
    }

    public long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public Timestamp getUpdateTs() {
        return updateTs;
    }

    public static class Builder {
        private long id;
        private String number;
        private BigDecimal balance;
        private String currency;
        private Timestamp updateTs;

        public Builder() {
        }

        public Builder(Account account) {
            this.id = account.id;
            this.number = account.number;
            this.balance = account.balance;
            this.currency = account.currency;
            this.updateTs = account.updateTs;
        }

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setNumber(String number) {
            this.number = number;
            return this;
        }

        public Builder setBalance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public Builder setCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder setUpdateTs(Timestamp updateTs) {
            this.updateTs = updateTs;
            return this;
        }

        public Account build() {
            return new Account(id, number, balance, currency, updateTs);
        }
    }
}
