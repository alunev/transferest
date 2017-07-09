package org.alunev.transferest.service.processor;

import org.alunev.transferest.model.Account;
import org.alunev.transferest.model.Transaction;
import org.alunev.transferest.model.User;
import org.alunev.transferest.model.error.RestException;
import org.alunev.transferest.service.ServiceIT;
import org.alunev.transferest.service.dbstore.AccountService;
import org.alunev.transferest.service.dbstore.TransactionService;
import org.alunev.transferest.service.dbstore.UserService;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class TransactionProcessorIT extends ServiceIT {
    private TransactionProcessor processor;

    private UserService userService;

    private AccountService accountService;

    private TransactionService transactionService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        processor = new TransactionProcessor(sql2oFactory);
        userService = new UserService(sql2oFactory);
        accountService = new AccountService(sql2oFactory);
        transactionService = new TransactionService(sql2oFactory);
    }

    @Test
    public void statusOK() throws Exception {
        User bob = userService.save(User.withName("Bob"));
        User alice = userService.save(User.withName("Alice"));

        Account rubAcc = createRubAcc(bob, 100.00, "RUB");
        Account usdAcc = createRubAcc(alice, 5.00, "USD");

        TxProcessResult result = processor.process(Transaction.builder()
                                                              .senderAccId(rubAcc.getId())
                                                              .sendAmount(BigDecimal.valueOf(60.00))
                                                              .receiverAccId(usdAcc.getId())
                                                              .receiveAmount(BigDecimal.valueOf(1.00))
                                                              .build());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TxStatus.ACCEPTED);
    }

    @Test
    public void statusDeclined() throws Exception {
        User bob = userService.save(User.withName("Bob"));
        User alice = userService.save(User.withName("Alice"));

        Account rubAcc = createRubAcc(bob, 10.00, "RUB");
        Account usdAcc = createRubAcc(alice, 5.00, "USD");

        TxProcessResult result = processor.process(Transaction.builder()
                                                              .senderAccId(rubAcc.getId())
                                                              .sendAmount(BigDecimal.valueOf(60.00))
                                                              .receiverAccId(usdAcc.getId())
                                                              .receiveAmount(BigDecimal.valueOf(1.00))
                                                              .build());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TxStatus.DECLINED);
    }

    @Test(expected = RestException.class)
    public void errorOnUnknownAccount() throws Exception {
        TxProcessResult result = processor.process(Transaction.builder()
                                                              .senderAccId(5)
                                                              .sendAmount(BigDecimal.valueOf(60.00))
                                                              .receiverAccId(6)
                                                              .receiveAmount(BigDecimal.valueOf(1.00))
                                                              .build());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TxStatus.DECLINED);
    }

    @Test
    public void transactionSavedInLog() throws Exception {
        User bob = userService.save(User.withName("Bob"));
        User alice = userService.save(User.withName("Alice"));

        Account rubAcc = createRubAcc(bob, 100.00, "RUB");
        Account usdAcc = createRubAcc(alice, 1.00, "USD");

        processor.process(Transaction.builder()
                                     .senderAccId(rubAcc.getId())
                                     .sendAmount(BigDecimal.valueOf(60.00))
                                     .receiverAccId(usdAcc.getId())
                                     .receiveAmount(BigDecimal.valueOf(1.00))
                                     .build());

        Optional<Transaction> transaction = transactionService.getById(0);

        assertThat(transaction).isNotEmpty();
    }


    private Account createRubAcc(User bob, double balance, String ccy) {
        return accountService.save(Account.builder()
                                          .ownerId(bob.getId())
                                          .number("456-4444")
                                          .balance(BigDecimal.valueOf(balance))
                                          .currency(ccy)
                                          .updateTs(Timestamp.valueOf(LocalDateTime.now()))
                                          .build());
    }

}