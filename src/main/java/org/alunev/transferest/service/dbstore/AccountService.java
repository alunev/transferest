package org.alunev.transferest.service.dbstore;

import com.google.inject.Inject;
import org.alunev.transferest.db.Sql2oFactory;
import org.alunev.transferest.model.Account;
import org.alunev.transferest.model.error.TransferException;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.List;
import java.util.Optional;

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

    private List<Account> getByUserId(long userId, Connection con) {
        List<Account> accounts;
        accounts = con.createQuery(
                "select * from accounts where ownerId = :ownerId",
                "select_accounts_for_user"
        )
                      .addParameter("ownerId", userId)
                      .executeAndFetch(Account.class);

        return accounts;
    }

    public Account save(Account account) throws TransferException {
        long key;
        try (Connection con = sql2o.beginTransaction()) {
            userService.getById(account.getOwnerId(), con)
                    .orElseThrow(() -> new TransferException("no user with id = " + account.getOwnerId()));

            key = (Long) con.createQuery(
                    "insert into accounts (ownerId, number, balance, currency, updateTs)"
                    + " values(:ownerId, :number, :balance, :currency, :updateTs)",
                    "insert_account",
                    true
            )
                            .bind(account)
                            .executeUpdate()
                            .getKey();

            con.commit();
        }

        return account.toBuilder()
                      .id(key)
                      .build();
    }

    public Optional<Account> getById(long id) {
        List<Account> accounts;
        try (Connection con = sql2o.open()) {
            accounts = con.createQuery(
                    "select * from accounts where id = :id",
                    "select_account"
            )
                          .addParameter("id", id)
                          .executeAndFetch(Account.class);
        }

        return accounts.isEmpty() ? Optional.empty() : Optional.of(accounts.get(0));
    }
}
