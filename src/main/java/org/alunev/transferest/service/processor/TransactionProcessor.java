package org.alunev.transferest.service.processor;

import com.google.inject.Inject;
import org.alunev.transferest.db.Sql2oFactory;
import org.alunev.transferest.model.Transaction;
import org.alunev.transferest.model.error.RestException;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.math.BigDecimal;

public class TransactionProcessor {
    private final Sql2o sql2o;

    @Inject
    public TransactionProcessor(Sql2oFactory sql2oFactory) {
        this.sql2o = sql2oFactory.createSql2o();;
    }

    public TxProcessResult process(Transaction tx) throws RestException {
        try (Connection con = sql2o.beginTransaction()) {
            BigDecimal senderBalance = getAccountBalance(tx.getSenderAccId(), con);

            BigDecimal resultingBalance = senderBalance.subtract(tx.getSendAmount());
            if (resultingBalance.compareTo(BigDecimal.ZERO) < 0) {
                return new TxProcessResult(TxStatus.DECLINED, "insufficient balance on account " + tx.getSenderAccId());
            }

            con.createQuery("update accounts set balance = :balance where id = :id")
               .addParameter("id", tx.getSenderAccId())
               .addParameter("balance", resultingBalance)
               .executeUpdate();

            BigDecimal receiverBalance = getAccountBalance(tx.getSenderAccId(), con);

            con.createQuery("update accounts set balance = :balance where id = :id")
               .addParameter("id", tx.getReceiverAccId())
               .addParameter("balance", receiverBalance.add(tx.getReceiveAmount()))
               .executeUpdate();

            Long key = (Long) con.createQuery(
                    "insert into transactions "
                    + "(senderAccId, receiverAccId, sendAmount, receiveAmount, updateTs)"
                    + " values(:senderAccId, :receiverAccId, :sendAmount, :receiveAmount, :updateTs)",
                    "insert_transaction",
                    true
            )
                                 .bind(tx)
                                 .executeUpdate()
                                 .getKey();

            con.commit();
        }

        return new TxProcessResult(TxStatus.ACCEPTED, "OK");
    }

    private BigDecimal getAccountBalance(long accId, Connection con) throws RestException {
        return con.createQuery("select balance from accounts where id = :id")
                                      .addParameter("id", accId)
                                      .executeAndFetch(BigDecimal.class)
                                      .stream()
                                      .findFirst()
                                      .orElseThrow(
                                              () -> new RestException("no account with id = " + accId)
                                      );
    }
}
