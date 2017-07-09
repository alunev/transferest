package org.alunev.transferest.service.dbstore;

import com.google.inject.Inject;
import org.alunev.transferest.db.Sql2oFactory;
import org.alunev.transferest.model.Account;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.List;
import java.util.Optional;

public class AccountService {

    private final Sql2o sql2o;

    @Inject
    public AccountService(Sql2oFactory sql2oFactory) {
        this.sql2o = sql2oFactory.createSql2o();
    }

    public List<Account> getByUserId(String userId) {
        List<Account> accounts;
        try (Connection con = sql2o.open()) {
            accounts = con.createQuery(
                    "select * from accounts where ownerId = :ownerId",
                    "select_accounts_for_user"
            )
                          .addParameter("ownerId", userId)
                          .executeAndFetch(Account.class);
        }

        return accounts;
    }

    public Account save(Account account) {
        long key;
        try (Connection con = sql2o.open()) {
            key = (Long) con.createQuery(
                    "insert into accounts (ownerId, number, balance, currency, updateTs)"
                    + " values(:ownerId, :number, :balance, :currency, :updateTs)",
                    "insert_account",
                    true
            )
                            .bind(account)
                            .executeUpdate()
                            .getKey();
        }

        return account.toBuilder()
                .id(key)
                .build();
    }

    public Optional<Account> getById(String userId, String accId) {
        return getById(accId);
    }

    public Optional<Account> getById(String id) {
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
