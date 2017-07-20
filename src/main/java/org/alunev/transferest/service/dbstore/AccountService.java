package org.alunev.transferest.service.dbstore;

import com.google.inject.Inject;
import org.alunev.transferest.db.Sql2oFactory;
import org.alunev.transferest.model.Account;
import org.alunev.transferest.model.error.TransferException;
import org.alunev.transferest.util.RetryUtil;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.List;
import java.util.Optional;

import static java.sql.Connection.TRANSACTION_SERIALIZABLE;

public class AccountService {

    private final Sql2o sql2o;

    private final UserService userService;

    @Inject
    public AccountService(Sql2oFactory sql2oFactory, UserService userService) {
        this.sql2o = sql2oFactory.createSql2o();
        this.userService = userService;
    }

    public List<Account> getByUserId(long userId) {
        List<Account> accounts;
        try (Connection con = sql2o.open()) {
            accounts = getByUserId(userId, con);
        }

        return accounts;
    }

    public List<Account> getByUserId(long userId, Connection con) {
        List<Account> accounts;
        accounts = con.createQuery(
                "select * from accounts where ownerId = :ownerId",
                "select_accounts_for_user"
        )
                .addParameter("ownerId", userId)
                .executeAndFetch(Account.class);

        return accounts;
    }

    public Optional<Account> getById(long id) {
        Optional<Account> account;
        try (Connection con = sql2o.open()) {
            account = getById(id, con);
        }

        return account;
    }

    public Optional<Account> getById(long id, Connection con) {
        return con.createQuery(
                "select * from accounts where id = :id",
                "select_account"
        )
                .addParameter("id", id)
                .executeAndFetch(Account.class)
                .stream()
                .findFirst();
    }

    public Account create(Account account) throws TransferException {
        long key = RetryUtil.getWithRetry(() -> {
            try (Connection con = sql2o.beginTransaction(TRANSACTION_SERIALIZABLE)) {
                userService.getById(account.getOwnerId(), con)
                        .orElseThrow(() -> new TransferException("no user with id = " + account.getOwnerId()));

                Long k = (Long) con.createQuery(
                        "insert into accounts (ownerId, number, balance, currency)"
                                + " values(:ownerId, :number, :balance, :currency)",
                        "insert_account",
                        true
                )
                        .bind(account)
                        .executeUpdate()
                        .getKey();

                con.commit();

                return k;
            }
        });

        return account.toBuilder()
                .id(key)
                .build();
    }

    public Optional<Account> update(Account account) {
        try (Connection con = sql2o.open()) {
            update(account, con);
        }

        return getById(account.getId());
    }

    public void update(Account account, Connection con) {
        con.createQuery(
                "update accounts set ownerId = :ownerId, number = :number, balance = :balance, " +
                        "currency = :currency where id = :id",
                "update_account " + account.getOwnerId() + " " + account.getBalance()
        )
                .bind(account)
                .executeUpdate();
    }

    public Optional<Account> delete(long id) {
        Optional<Account> account = getById(id);

        try (Connection con = sql2o.open()) {
            con.createQuery(
                    "delete from accounts where id = :id",
                    "delete_account"
            )
                    .addParameter("id", id)
                    .executeUpdate();
        }

        return account;
    }
}
