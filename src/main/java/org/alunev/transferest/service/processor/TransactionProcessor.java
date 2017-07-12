package org.alunev.transferest.service.processor;

import com.google.inject.Inject;
import org.alunev.transferest.db.Sql2oFactory;
import org.alunev.transferest.model.*;
import org.alunev.transferest.model.error.TransferException;
import org.alunev.transferest.service.dbstore.AccountService;
import org.alunev.transferest.service.dbstore.UserService;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.math.BigDecimal;
import java.util.Currency;

public class TransactionProcessor {
    private final Sql2o sql2o;

    private final UserService userService;

    private final AccountService accountService;

    @Inject
    public TransactionProcessor(Sql2oFactory sql2oFactory,
                                UserService userService,
                                AccountService accountService) {
        this.sql2o = sql2oFactory.createSql2o();
        this.userService = userService;
        this.accountService = accountService;
    }

    public Transaction process(Transaction tx) throws TransferException {
        Transaction transaction;
        try (Connection con = sql2o.beginTransaction()) {
            transaction = process(tx, con);

            con.commit();
        }

        return transaction;
    }

    public Transaction process(TransactionRequest request) throws TransferException {
        Transaction transaction;
        try (Connection con = sql2o.beginTransaction()) {
            Currency ccy = request.getCurrency();

            Account senderAccount = getUserAccountOfCurrency(con, request.getSenderName(), ccy);
            Account receiverAccount = getUserAccountOfCurrency(con, request.getReceiverName(), ccy);

            transaction = process(Transaction.builder()
                                             .senderAccId(senderAccount.getId())
                                             .receiverAccId(receiverAccount.getId())
                                             .sendAmount(request.getAmount())
                                             .receiveAmount(request.getAmount())
                                             .build()
            );

            con.commit();
        }

        return transaction;
    }

    private Transaction process(Transaction tx, Connection con) throws TransferException {
        BigDecimal senderBalance = getAccountBalance(tx.getSenderAccId(), con);

        if (tx.getSendAmount().compareTo(BigDecimal.ZERO) < 0
            || tx.getReceiveAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new TransferException("negative amount not allowed");
        }

        BigDecimal resultingBalance = senderBalance.subtract(tx.getSendAmount());
        if (resultingBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new TransferException("insufficient balance on account " + tx.getSenderAccId());
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

        return tx.toBuilder()
                 .id(key)
                 .build();
    }

    private BigDecimal getAccountBalance(long accId, Connection con) throws TransferException {
        return con.createQuery("select balance from accounts where id = :id")
                  .addParameter("id", accId)
                  .executeAndFetch(BigDecimal.class)
                  .stream()
                  .findFirst()
                  .orElseThrow(
                          () -> new TransferException("no account with id = " + accId)
                  );
    }

    private Account getUserAccountOfCurrency(Connection con, String name, Currency ccy) throws TransferException {
        User sender = userService.getByName(name, con).orElseThrow(
                () -> new TransferException("no user with name = " + name)
        );

        return accountService.getByUserId(sender.getId()).stream()
                             .filter(account -> account.getCurrency().equals(ccy.getCurrencyCode()))
                             .findFirst()
                             .orElseThrow(
                                     () -> new TransferException("no account with ccy = "
                                                                 + ccy
                                                                 + "for user with name = "
                                                                 + name)
                             );
    }
}
