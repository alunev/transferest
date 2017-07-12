package org.alunev.transferest.service;

import org.alunev.transferest.model.Account;
import org.alunev.transferest.service.dbstore.AccountService;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AccountServiceIT extends ServiceIT {

    private AccountService accountService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        accountService = new AccountService(sql2oFactory);
    }

    @Test
    public void canSaveAccounts() throws Exception {
        Account newAccount = Account.builder()
                                    .number("123-123-123-123")
                                    .balance(BigDecimal.valueOf(167.02))
                                    .currency("USD")
                                    .updateTs(Timestamp.valueOf(LocalDateTime.now()))
                                    .build();

        Account account = accountService.save(newAccount);
        account = accountService.getById(account.getId()).get();

        assertThat(account.getId()).isEqualTo(0);
        assertAccFields(newAccount, account);

        newAccount = Account.builder()
                            .number("456-4444")
                            .balance(BigDecimal.valueOf(767.02))
                            .currency("RUB")
                            .updateTs(Timestamp.valueOf(LocalDateTime.now()))
                            .build();

        account = accountService.save(newAccount);
        account = accountService.getById(account.getId()).get();

        assertThat(account.getId()).isEqualTo(1);
        assertAccFields(newAccount, account);
    }

    @Test
    public void canGetById() throws Exception {
        accountService.save(Account.builder()
                                   .number("123-123-123-123")
                                   .ownerId(1)
                                   .balance(BigDecimal.valueOf(167.02))
                                   .currency("USD")
                                   .updateTs(Timestamp.valueOf(LocalDateTime.now()))
                                   .build());

        accountService.save(Account.builder()
                                   .number("45-7896")
                                   .ownerId(1)
                                   .balance(BigDecimal.valueOf(6.02))
                                   .currency("USD")
                                   .updateTs(Timestamp.valueOf(LocalDateTime.now()))
                                   .build());

        List<Account> accounts = accountService.getByUserId(1);

        assertThat(accounts.size()).isEqualTo(2);
    }

    private void assertAccFields(Account acc, Account savedAcc) {
        assertThat(savedAcc.getNumber()).isEqualTo(acc.getNumber());
        assertThat(savedAcc.getBalance()).isEqualTo(acc.getBalance());
        assertThat(savedAcc.getUpdateTs()).isEqualTo(acc.getUpdateTs());
    }

}