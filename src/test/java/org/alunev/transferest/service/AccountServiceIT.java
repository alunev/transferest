package org.alunev.transferest.service;

import org.alunev.transferest.db.DbInitializer;
import org.alunev.transferest.model.Account;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AccountServiceIT {
    private TestSql2oFactory sql2oFactory = new TestSql2oFactory();

    private AccountService accountService;

    @Before
    public void setUp() throws Exception {
        new DbInitializer(sql2oFactory).initSchema();

        accountService = new AccountService(sql2oFactory);
    }

    @Test
    public void canSaveAccounts() throws Exception {
        Account newAccount = new Account.Builder()
                .setNumber("123-123-123-123")
                .setBalance(BigDecimal.valueOf(167.02))
                .setCurrency("USD")
                .setUpdateTs(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        Account account = accountService.save(newAccount);

        assertThat(account.getId()).isEqualTo(0);
        assertAccFields(newAccount, account);

        newAccount = new Account.Builder()
                .setNumber("456-4444")
                .setBalance(BigDecimal.valueOf(767.002))
                .setCurrency("RUB")
                .setUpdateTs(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        account = accountService.save(newAccount);

        assertThat(account.getId()).isEqualTo(1);
        assertAccFields(newAccount, account);
    }

    private void assertAccFields(Account acc, Account savedAcc) {
        assertThat(savedAcc.getNumber()).isEqualTo(acc.getNumber());
        assertThat(savedAcc.getBalance()).isEqualTo(acc.getBalance());
        assertThat(savedAcc.getUpdateTs()).isEqualTo(acc.getUpdateTs());
    }

}