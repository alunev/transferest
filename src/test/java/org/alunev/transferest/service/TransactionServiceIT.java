package org.alunev.transferest.service;

import org.alunev.transferest.db.DbInitializer;
import org.alunev.transferest.model.Transaction;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TransactionServiceIT {
    private TestSql2oFactory sql2oFactory = new TestSql2oFactory();
    private TransactionService transactionService;

    @Before
    public void setUp() throws Exception {
        new DbInitializer(sql2oFactory).initSchema();

        transactionService = new TransactionService(sql2oFactory);
    }

    @Test
    public void canSaveTransactions() throws Exception {
        sql2oFactory = new TestSql2oFactory();

        Transaction newTx = new Transaction.Builder()
                .setReceiveAmount(BigDecimal.valueOf(60.3))
                .setSendAmount(BigDecimal.valueOf(1.02))
                .setUpdateTs(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        Transaction tx = transactionService.save(newTx);

        assertThat(tx.getId()).isEqualTo(0);
        assertTxFields(newTx, tx);


        newTx = new Transaction.Builder()
                .setReceiveAmount(BigDecimal.valueOf(60.3))
                .setSendAmount(BigDecimal.valueOf(1.02))
                .setUpdateTs(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        tx = transactionService.save(newTx);

        assertThat(tx.getId()).isEqualTo(1);
        assertTxFields(newTx, tx);
    }

    private void assertTxFields(Transaction newTx, Transaction tx) {
        assertThat(tx.getSendAmount()).isEqualTo(newTx.getSendAmount());
        assertThat(tx.getReceiveAmount()).isEqualTo(newTx.getReceiveAmount());
        assertThat(tx.getUpdateTs()).isEqualTo(newTx.getUpdateTs());
    }

}