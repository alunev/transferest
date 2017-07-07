package org.alunev.transferest.service;

import com.google.inject.Inject;
import org.alunev.transferest.db.Sql2oFactory;
import org.alunev.transferest.model.Transaction;
import org.sql2o.Connection;
import org.sql2o.Sql2o;


public class TransactionService {

    private final Sql2o sql2o;

    @Inject
    public TransactionService(Sql2oFactory sql2oFactory) {
        this.sql2o = sql2oFactory.createSql2o();
    }

    public Transaction save(Transaction tx) {
        long key;

        try (Connection con = sql2o.open()) {
            key = (Long) con.createQuery(
                    "insert into transactions (senderAccId, receiverAccId, sendAmount, receiveAmount, updateTs)"
                            + " values(:senderAccId, :receiverAccId, :sendAmount, :receiveAmount, :updateTs)",
                    "insert_transaction",
                    true)
                    .bind(tx)
                    .executeUpdate()
                    .getKey();
        }

        return new Transaction.Builder(tx)
                .setId(key)
                .build();
    }
}
