package org.alunev.transferest.service.dbstore;

import com.google.inject.Inject;
import org.alunev.transferest.db.Sql2oFactory;
import org.alunev.transferest.model.Transaction;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.List;
import java.util.Optional;

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
                    true
            )
                            .bind(tx)
                            .executeUpdate()
                            .getKey();
        }

        return tx.toBuilder()
                 .id(key)
                 .build();
    }

    public Optional<Transaction> getById(long id) {
        List<Transaction> transactions;
        try (Connection con = sql2o.open()) {
            transactions = con.createQuery(
                    "select * from transactions where id = :id",
                    "select_transaction"
            )
                              .addParameter("id", id)
                              .executeAndFetch(Transaction.class);
        }

        return transactions.isEmpty() ? Optional.empty() : Optional.of(transactions.get(0));
    }

    public List<Transaction> getAll() {
        List<Transaction> transactions;
        try (Connection con = sql2o.open()) {
            transactions = con.createQuery(
                    "select * from transactions order by id desc",
                    "select_all_transaction"
            )
                    .executeAndFetch(Transaction.class);
        }

        return transactions;
    }
}
