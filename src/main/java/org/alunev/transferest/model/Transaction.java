package org.alunev.transferest.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Money transfer event info. Transfer may happen between exactly 2 accounts.
 */
public class Transaction {

    private final long id;

    private final long senderAccId;

    private final long receiverAccId;

    private final BigDecimal sendAmount;

    private final BigDecimal receiveAmount;

    private final Timestamp updateTs;

    private Transaction(long id,
                        long senderAccId,
                        long receiverAccId,
                        BigDecimal sendAmount,
                        BigDecimal receiveAmount,
                        Timestamp updateTs) {
        this.id = id;
        this.senderAccId = senderAccId;
        this.receiverAccId = receiverAccId;
        this.sendAmount = sendAmount;
        this.receiveAmount = receiveAmount;
        this.updateTs = updateTs;
    }

    public long getId() {
        return id;
    }

    public long getSenderAccId() {
        return senderAccId;
    }

    public long getReceiverAccId() {
        return receiverAccId;
    }

    public BigDecimal getSendAmount() {
        return sendAmount;
    }

    public BigDecimal getReceiveAmount() {
        return receiveAmount;
    }

    public Timestamp getUpdateTs() {
        return updateTs;
    }

    public static class Builder {
        private long id = -1;
        private long senderAccId;
        private long receiverAccId;
        private BigDecimal sendAmount;
        private BigDecimal receiveAmount;
        private Timestamp updateTs;

        public Builder() {
        }

        public Builder(Transaction tx) {
            this.id = tx.id;
            this.senderAccId = tx.senderAccId;
            this.receiverAccId = tx.receiverAccId;
            this.sendAmount = tx.sendAmount;
            this.receiveAmount = tx.receiveAmount;
            this.updateTs = tx.updateTs;
        }

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setSenderAccId(long senderAccId) {
            this.senderAccId = senderAccId;
            return this;
        }

        public Builder setReceiverAccId(long receiverAccId) {
            this.receiverAccId = receiverAccId;
            return this;
        }

        public Builder setSendAmount(BigDecimal sendAmount) {
            this.sendAmount = sendAmount;
            return this;
        }

        public Builder setReceiveAmount(BigDecimal receiveAmount) {
            this.receiveAmount = receiveAmount;
            return this;
        }

        public Builder setUpdateTs(Timestamp updateTs) {
            this.updateTs = updateTs;
            return this;
        }

        public Transaction build() {
            return new Transaction(id, senderAccId, receiverAccId, sendAmount, receiveAmount, updateTs);
        }
    }
}
