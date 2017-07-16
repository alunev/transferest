package org.alunev.transferest.service;

import org.alunev.transferest.model.Account;
import org.alunev.transferest.model.User;
import org.alunev.transferest.model.error.TransferException;
import org.alunev.transferest.service.dbstore.AccountService;
import org.alunev.transferest.service.dbstore.UserService;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Connection;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccountServiceIT extends ServiceIT {

    private AccountService accountService;
    private UserService userService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        userService = mock(UserService.class);
        when(userService.getById(anyLong(), any(Connection.class))).thenReturn(Optional.of(User.builder().build()));

        accountService = new AccountService(sql2oFactory, userService);
    }

    @Test
    public void canSaveAccounts() throws Exception {
        Account newAccount = Account.builder()
                                    .number("123-123-123-123")
                                    .balance(BigDecimal.valueOf(167.02))
                                    .currency("USD")
                                    .build();

        Account account = accountService.create(newAccount);
        account = accountService.getById(account.getId()).get();

        assertThat(account.getId()).isEqualTo(0);
        assertAccFields(newAccount, account);

        newAccount = Account.builder()
                            .number("456-4444")
                            .balance(BigDecimal.valueOf(767.02))
                            .currency("RUB")
                            .build();

        account = accountService.create(newAccount);
        account = accountService.getById(account.getId()).get();

        assertThat(account.getId()).isEqualTo(1);
        assertAccFields(newAccount, account);
    }

    @Test
    public void canGetById() throws Exception {
        accountService.create(Account.builder()
                                   .number("123-123-123-123")
                                   .ownerId(1)
                                   .balance(BigDecimal.valueOf(167.02))
                                   .currency("USD")
                                   .build());

        accountService.create(Account.builder()
                                   .number("45-7896")
                                   .ownerId(1)
                                   .balance(BigDecimal.valueOf(6.02))
                                   .currency("USD")
                                   .build());

        List<Account> accounts = accountService.getByUserId(1);

        assertThat(accounts.size()).isEqualTo(2);
    }


    @Test
    public void canUpdate() throws Exception {
        Account account = Account.builder()
                .number("123-123-123-123")
                .ownerId(1)
                .balance(BigDecimal.valueOf(167.02))
                .currency("USD")
                .build();
        account = accountService.create(account);

        Account updatedAccount = account.toBuilder()
                .number("456")
                .balance(BigDecimal.valueOf(1050, 2))
                .currency("EUR")
                .build();
        accountService.update(updatedAccount);

        Account newAccount = accountService.getById(0).get();

        assertThat(newAccount.getNumber()).isEqualTo(updatedAccount.getNumber());
        assertThat(newAccount.getBalance()).isEqualTo(updatedAccount.getBalance());
    }

    @Test(expected = TransferException.class)
    public void exceptionIfOwnerNotFound() throws Exception {
        when(userService.getById(anyLong(), any(Connection.class))).thenReturn(Optional.empty());

        accountService.create(Account.builder()
                .number("123-123-123-123")
                .ownerId(1)
                .balance(BigDecimal.valueOf(167.02))
                .currency("USD")
                .build());
    }

    private void assertAccFields(Account acc, Account savedAcc) {
        assertThat(savedAcc.getNumber()).isEqualTo(acc.getNumber());
        assertThat(savedAcc.getBalance()).isEqualTo(acc.getBalance());
    }

}