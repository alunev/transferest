package org.alunev.transferest.service.processor;

import com.google.inject.Inject;
import org.alunev.transferest.db.Sql2oFactory;
import org.alunev.transferest.model.Account;
import org.alunev.transferest.model.Transaction;
import org.alunev.transferest.model.TransactionRequest;
import org.alunev.transferest.model.User;
import org.alunev.transferest.model.error.TransferException;
import org.alunev.transferest.service.dbstore.AccountService;
import org.alunev.transferest.service.dbstore.TransactionService;
import org.alunev.transferest.service.dbstore.UserService;
import org.alunev.transferest.util.RetryUtil;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.math.BigDecimal;
import java.util.Currency;

import static java.sql.Connection.TRANSACTION_SERIALIZABLE;

public class TransactionProcessor {
    private final Sql2o sql2o;

    private final UserService userService;

    private final AccountService accountService;

    private final TransactionService transactionService;

    @Inject
    public TransactionProcessor(Sql2oFactory sql2oFactory,
                                UserService userService,
                                AccountService accountService,
                                TransactionService transactionService) {
        this.sql2o = sql2oFactory.createSql2o();
        this.userService = userService;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    public Transaction process(Transaction tx) throws TransferException {
        return RetryUtil.getWithRetry(() -> {
            try (Connection con = sql2o.beginTransaction(TRANSACTION_SERIALIZABLE)) {
                Transaction transaction = processOnConnection(tx, con);

                con.commit();

                return transaction;
            }
        });
    }

    public Transaction process(TransactionRequest request) throws TransferException {
        return RetryUtil.getWithRetry(() -> {
            try (Connection con = sql2o.beginTransaction(TRANSACTION_SERIALIZABLE)) {
                Currency ccy = request.getCurrency();

                Account senderAccount = getUserAccountOfCurrency(con, request.getSenderName(), ccy);
                Account receiverAccount = getUserAccountOfCurrency(con, request.getReceiverName(), ccy);

                Transaction transaction = processOnConnection(
                        Transaction.builder()
                                .senderAccId(senderAccount.getId())
                                .receiverAccId(receiverAccount.getId())
                                .sendAmount(request.getAmount())
                                .receiveAmount(request.getAmount())
                                .build(),
                        con
                );

                con.commit();

                return transaction;
            }
        });
    }

    private Transaction processOnConnection(Transaction tx, Connection con) throws TransferException {
        Account senderAccount = getAccount(tx.getSenderAccId(), con);
        BigDecimal senderBalance = senderAccount.getBalance();

        checkNegativeAmount(tx);

        checkSenderBalance(tx, senderBalance);

        accountService.update(
                senderAccount.toBuilder()
                        .balance(senderAccount.getBalance().subtract(tx.getSendAmount()))
                        .build(),
                con
        );

        Account receiverAccount = getAccount(tx.getReceiverAccId(), con);
        accountService.update(
                receiverAccount.toBuilder()
                        .balance(receiverAccount.getBalance().add(tx.getReceiveAmount()))
                        .build(),
                con
        );

        return transactionService.save(tx, con);
    }

    private void checkSenderBalance(Transaction tx, BigDecimal senderBalance) throws TransferException {
        BigDecimal remainingBalance = senderBalance.subtract(tx.getSendAmount());
        if (remainingBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new TransferException("insufficient balance on account " + tx.getSenderAccId());
        }
    }

    private void checkNegativeAmount(Transaction tx) throws TransferException {
        if (tx.getSendAmount().compareTo(BigDecimal.ZERO) < 0
                || tx.getReceiveAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new TransferException("negative amount not allowed");
        }
    }

    private Account getAccount(long id, Connection con) throws TransferException {
        return accountService.getById(id, con)
                .orElseThrow(
                        () -> new TransferException("no account with id = " + id)
                );
    }

    private Account getUserAccountOfCurrency(Connection con, String name, Currency ccy) throws TransferException {
        User sender = userService.getByName(name, con).orElseThrow(
                () -> new TransferException("no user with name = " + name)
        );

        return accountService.getByUserId(sender.getId(), con).stream()
                .filter(account -> account.getCurrency().equals(ccy.getCurrencyCode()))
                .findFirst()
                .orElseThrow(
                        () -> new TransferException("no account with ccy = "
                                + ccy
                                + " for user with name = "
                                + name)
                );
    }
}
