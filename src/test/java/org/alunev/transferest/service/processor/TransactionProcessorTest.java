package org.alunev.transferest.service.processor;

import com.google.common.collect.ImmutableList;
import org.alunev.transferest.db.Sql2oFactory;
import org.alunev.transferest.model.Account;
import org.alunev.transferest.model.Transaction;
import org.alunev.transferest.model.TransactionRequest;
import org.alunev.transferest.model.User;
import org.alunev.transferest.service.dbstore.AccountService;
import org.alunev.transferest.service.dbstore.TransactionService;
import org.alunev.transferest.service.dbstore.UserService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class TransactionProcessorTest {

    private TransactionProcessor processor;
    private UserService userService;
    private AccountService accountService;
    private Sql2oFactory sql2oFactory;

    @Before
    public void setUp() throws Exception {
        userService = mock(UserService.class);
        accountService = mock(AccountService.class);
        sql2oFactory = mock(Sql2oFactory.class);

        Sql2o sql2o = mock(Sql2o.class);
        when(sql2oFactory.createSql2o()).thenReturn(sql2o);

        when(sql2o.beginTransaction()).thenReturn(mock(Connection.class));
        when(sql2o.open()).thenReturn(mock(Connection.class));

        TransactionService transactionService = mock(TransactionService.class);
        when(transactionService.save(any(Transaction.class))).then(returnsFirstArg());
        when(transactionService.save(any(Transaction.class), any(Connection.class))).then(returnsFirstArg());

        processor = new TransactionProcessor(
                sql2oFactory,
                userService,
                accountService,
                transactionService
        );
    }

    @Test
    public void successTransferByName() throws Exception {
        mockUser(User.builder().id(1).name("Bob").build());
        Account bobAcc = mockAcc(Account.builder()
                .id(1)
                .ownerId(1)
                .balance(BigDecimal.valueOf(100.00))
                .currency("USD")
                .build());

        mockUser(User.builder().id(2).name("Alice").build());
        Account aliceAcc = mockAcc(Account.builder()
                .id(2)
                .ownerId(2)
                .balance(BigDecimal.valueOf(1.00))
                .currency("USD")
                .build());

        Transaction transaction = processor.process(TransactionRequest.builder()
                .senderName("Bob")
                .receiverName("Alice")
                .amount(BigDecimal.valueOf(5.05))
                .currency(Currency.getInstance("USD"))
                .build());

        assertThat(transaction).isNotNull();
        assertThat(transaction.getSenderAccId()).isEqualTo(bobAcc.getId());
        assertThat(transaction.getReceiverAccId()).isEqualTo(aliceAcc.getId());
        assertThat(transaction.getSendAmount()).isEqualTo(BigDecimal.valueOf(5.05));
        assertThat(transaction.getReceiveAmount()).isEqualTo(BigDecimal.valueOf(5.05));
    }

    private void mockUser(User user) {
        when(userService.getById(eq(user.getId()))).thenReturn(Optional.of(user));
        when(userService.getById(eq(user.getId()), any(Connection.class))).thenReturn(Optional.of(user));

        when(userService.getByName(eq(user.getName()), any(Connection.class))).thenReturn(Optional.of(user));
    }

    private Account mockAcc(Account account) {
        when(accountService.getById(eq(account.getId()))).thenReturn(Optional.of(account));
        when(accountService.getById(eq(account.getId()), any(Connection.class))).thenReturn(Optional.of(account));

        when(accountService.getByUserId(eq(account.getOwnerId()))).thenReturn(ImmutableList.of(account));
        when(accountService.getByUserId(eq(account.getOwnerId()), any(Connection.class))).thenReturn(ImmutableList.of(account));

        return account;
    }
}