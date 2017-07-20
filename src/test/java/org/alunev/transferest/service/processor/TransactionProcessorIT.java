package org.alunev.transferest.service.processor;

import lombok.extern.slf4j.Slf4j;
import org.alunev.transferest.model.Account;
import org.alunev.transferest.model.Transaction;
import org.alunev.transferest.model.TransactionRequest;
import org.alunev.transferest.model.User;
import org.alunev.transferest.model.error.TransferException;
import org.alunev.transferest.service.ServiceIT;
import org.alunev.transferest.service.dbstore.AccountService;
import org.alunev.transferest.service.dbstore.TransactionService;
import org.alunev.transferest.service.dbstore.UserService;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class TransactionProcessorIT extends ServiceIT {
    private TransactionProcessor processor;

    private UserService userService;

    private AccountService accountService;

    private TransactionService transactionService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        userService = new UserService(sql2oFactory);
        accountService = new AccountService(sql2oFactory, userService);
        transactionService = new TransactionService(sql2oFactory);

        processor = new TransactionProcessor(sql2oFactory, userService, accountService, transactionService);
    }

    @Test
    public void success() throws Exception {
        User bob = userService.create(User.withName("Bob"));
        User alice = userService.create(User.withName("Alice"));

        Account rubAcc = createAcc(bob, 100.00, "RUB");
        Account usdAcc = createAcc(alice, 5.00, "USD");

        Transaction transaction = processor.process(Transaction.builder()
                .senderAccId(rubAcc.getId())
                .sendAmount(BigDecimal.valueOf(60.00))
                .receiverAccId(usdAcc.getId())
                .receiveAmount(BigDecimal.valueOf(1.00))
                .build());

        assertThat(transaction).isNotNull();
    }

    @Test
    public void testMultiThreadedTxConsistency() throws Exception {
        User bob = userService.create(User.withName("Bob"));
        User alice = userService.create(User.withName("Alice"));

        Account bobAcc = createAcc(bob, 100.00, "USD");
        Account aliceAcc = createAcc(alice, 0.00, "USD");

        ExecutorService executor = Executors.newFixedThreadPool(4);
        Set<Future> futures = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            futures.add(executor.submit(() -> {
                try {
                    processor.process(TransactionRequest.builder()
                            .senderName("Bob")
                            .receiverName("Alice")
                            .amount(BigDecimal.valueOf(1.0))
                            .currency(Currency.getInstance("USD"))
                            .build());
                } catch (TransferException e) {
                    log.error("Error: ", e);
                }
            }));
        }

        for (Future future : futures) {
            future.get();
        }

        assertThat(accountService.getById(bobAcc.getId()).get().getBalance()).isEqualTo(new BigDecimal("0.00"));
        assertThat(accountService.getById(aliceAcc.getId()).get().getBalance()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test(expected = TransferException.class)
    public void insufficientBalance() throws Exception {
        User bob = userService.create(User.withName("Bob"));
        User alice = userService.create(User.withName("Alice"));

        Account rubAcc = createAcc(bob, 10.00, "RUB");
        Account usdAcc = createAcc(alice, 5.00, "USD");

        processor.process(Transaction.builder()
                .senderAccId(rubAcc.getId())
                .sendAmount(BigDecimal.valueOf(60.00))
                .receiverAccId(usdAcc.getId())
                .receiveAmount(BigDecimal.valueOf(1.00))
                .build());
    }

    @Test(expected = TransferException.class)
    public void errorOnUnknownAccount() throws Exception {
        processor.process(Transaction.builder()
                .senderAccId(5)
                .sendAmount(BigDecimal.valueOf(60.00))
                .receiverAccId(6)
                .receiveAmount(BigDecimal.valueOf(1.00))
                .build());
    }

    @Test
    public void transactionSavedInLog() throws Exception {
        User bob = userService.create(User.withName("Bob"));
        User alice = userService.create(User.withName("Alice"));

        Account rubAcc = createAcc(bob, 100.00, "RUB");
        Account usdAcc = createAcc(alice, 1.00, "USD");

        processor.process(Transaction.builder()
                .senderAccId(rubAcc.getId())
                .sendAmount(BigDecimal.valueOf(60.00))
                .receiverAccId(usdAcc.getId())
                .receiveAmount(BigDecimal.valueOf(1.00))
                .build());

        Optional<Transaction> transaction = transactionService.getById(0);

        assertThat(transaction).isNotEmpty();
    }

    @Test
    public void successTransferByName() throws Exception {
        User bob = userService.create(User.withName("Bob"));
        User alice = userService.create(User.withName("Alice"));

        Account bobAcc = createAcc(bob, 100.00, "USD");
        Account aliceAcc = createAcc(alice, 1.00, "USD");

        Transaction transaction = processor.process(TransactionRequest.builder()
                .senderName("Bob")
                .receiverName("Alice")
                .amount(BigDecimal.valueOf(5.05))
                .currency(Currency.getInstance("USD"))
                .build());

        assertThat(transaction).isNotNull();
        assertThat(transaction.getSenderAccId()).isEqualTo(bobAcc.getId());
        assertThat(transaction.getReceiverAccId()).isEqualTo(aliceAcc.getId());
    }

    @Test(expected = TransferException.class)
    public void wrongNameInTransferRequest() throws Exception {
        User bob = userService.create(User.withName("Bob"));

        createAcc(bob, 100.00, "USD");

        processor.process(TransactionRequest.builder()
                .senderName("Bob")
                .receiverName("Alice")
                .amount(BigDecimal.valueOf(5.05))
                .currency(Currency.getInstance("USD"))
                .build());
    }

    @Test(expected = TransferException.class)
    public void wrongAccountCurrencyForTransferRequest() throws Exception {
        User bob = userService.create(User.withName("Bob"));
        User alice = userService.create(User.withName("Alice"));

        createAcc(bob, 100.00, "USD");
        createAcc(alice, 1.00, "EUR");

        processor.process(TransactionRequest.builder()
                .senderName("Bob")
                .receiverName("Alice")
                .amount(BigDecimal.valueOf(5.05))
                .currency(Currency.getInstance("USD"))
                .build());
    }

    @Test(expected = TransferException.class)
    public void negativeAmount() throws Exception {
        User bob = userService.create(User.withName("Bob"));
        User alice = userService.create(User.withName("Alice"));

        Account rubAcc = createAcc(bob, 10.00, "RUB");
        Account usdAcc = createAcc(alice, 5.00, "USD");

        processor.process(Transaction.builder()
                .senderAccId(rubAcc.getId())
                .sendAmount(BigDecimal.valueOf(-1.00))
                .receiverAccId(usdAcc.getId())
                .receiveAmount(BigDecimal.valueOf(1.00))
                .build());
    }

    private Account createAcc(User bob, double balance, String ccy) throws TransferException {
        return accountService.create(Account.builder()
                .ownerId(bob.getId())
                .number("456-4444")
                .balance(BigDecimal.valueOf(balance))
                .currency(ccy)
                .updateTs(Timestamp.valueOf(LocalDateTime.now()))
                .build());
    }

}