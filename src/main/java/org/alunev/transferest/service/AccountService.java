package org.alunev.transferest.service;

import com.google.inject.Inject;
import org.alunev.transferest.db.Sql2oFactory;
import org.alunev.transferest.model.Account;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

public class AccountService {

    private final Sql2o sql2o;

    @Inject
    public AccountService(Sql2oFactory sql2oFactory) {
        this.sql2o = sql2oFactory.createSql2o();
    }

    public Account save(Account account) {
        long key;
        try (Connection con = sql2o.open()) {
            key = (Long) con.createQuery(
                    "insert into accounts (number, balance, currency, updateTs)"
                            + " values(:number, :balance, :currency, :updateTs)",
                    "insert_account",
                    true)
                    .bind(account)
                    .executeUpdate()
                    .getKey();
        }

        return new Account.Builder(account)
                .setId(key)
                .build();
    }
}
