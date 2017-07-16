package org.alunev.transferest.service;

import org.alunev.transferest.model.Transaction;
import org.alunev.transferest.service.dbstore.TransactionService;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TransactionServiceIT extends ServiceIT {

    private TransactionService transactionService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        transactionService = new TransactionService(sql2oFactory);
    }

    @Test
    public void canSaveTransactions() throws Exception {
        sql2oFactory = new TestSql2oFactory();

        Transaction newTx = Transaction.builder()
                .receiveAmount(BigDecimal.valueOf(60.3))
                .sendAmount(BigDecimal.valueOf(1.02))
                .updateTs(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        Transaction tx = transactionService.save(newTx);

        assertThat(tx.getId()).isEqualTo(0);
        assertTxFields(newTx, tx);


        newTx = Transaction.builder()
                .receiveAmount(BigDecimal.valueOf(60.3))
                .sendAmount(BigDecimal.valueOf(1.02))
                .updateTs(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        tx = transactionService.save(newTx);

        assertThat(tx.getId()).isEqualTo(1);
        assertTxFields(newTx, tx);
    }

    @Test
    public void canGetTransactions() throws Exception {
        sql2oFactory = new TestSql2oFactory();

        Transaction newTx = Transaction.builder()
                                       .sendAmount(BigDecimal.valueOf(1.02))
                                       .receiveAmount(BigDecimal.valueOf(6030, 2))
                                       .updateTs(Timestamp.valueOf(LocalDateTime.now()))
                                       .build();

        transactionService.save(newTx);

        Optional<Transaction> tx = transactionService.getById(newTx.getId());

        assertThat(tx).isNotEmpty();
        assertThat(tx.get().getId()).isEqualTo(newTx.getId());
        assertTxFields(newTx, tx.get());
    }

    private void assertTxFields(Transaction newTx, Transaction tx) {
        assertThat(tx.getSendAmount()).isEqualTo(newTx.getSendAmount());
        assertThat(tx.getReceiveAmount()).isEqualTo(newTx.getReceiveAmount());
    }

}