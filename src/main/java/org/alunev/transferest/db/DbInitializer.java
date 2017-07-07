package org.alunev.transferest.db;

import com.google.inject.Inject;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

/**
 * Create tables on in-memory DB
 */
public class DbInitializer {
    private final Sql2o sql2o;

    @Inject
    public DbInitializer(Sql2oFactory sql2oFactory) {
        this.sql2o = sql2oFactory.createSql2o();
    }

    public void initSchema() {
        try (Connection con = sql2o.open()) {
            con.createQuery("create table users (id bigint identity primary key, name varchar(256))",
                    "create table users")
                    .executeUpdate();

            con.createQuery("create table transactions (" +
                            "id bigint identity primary key, " +
                            "name varchar(256), " +
                            "senderAccId bigint, " +
                            "receiverAccId bigint, " +
                            "sendAmount numeric, " +
                            "receiveAmount numeric, " +
                            "updateTs timestamp(3)" +
                            ")",
                    "create table transactions")
                    .executeUpdate();

            con.createQuery("create table accounts (" +
                            "id bigint identity primary key, " +
                            "number varchar(256), " +
                            "balance bigint, " +
                            "currency varchar(3), " +
                            "updateTs timestamp(3)" +
                            ")",
                    "create table accounts")
                    .executeUpdate();

        }
    }
}
