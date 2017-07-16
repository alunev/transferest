package org.alunev.transferest.db;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

/**
 * Create tables on in-memory DB
 */

@Slf4j
public class DbInitializer {
    private final Sql2o sql2o;

    @Inject
    public DbInitializer(Sql2oFactory sql2oFactory) {
        this.sql2o = sql2oFactory.createSql2o();
    }

    public void initSchema() {
        try (Connection con = sql2o.open()) {
            con.createQuery("create table users ("
                            + "id bigint identity primary key, "
                            + "name varchar(256) not null unique"
                            + ")",
                    "create table users"
            ).executeUpdate();

            con.createQuery("create table accounts (" +
                            "id bigint identity primary key, " +
                            "ownerId bigint," +
                            "number varchar(256), " +
                            "balance numeric(20, 2), " +
                            "currency varchar(3), " +
                            "updateTs timestamp(3) default now" +
                            ")",
                    "create table accounts"
            ).executeUpdate();

            con.createQuery("create table transactions (" +
                            "id bigint identity primary key, " +
                            "senderAccId bigint, " +
                            "receiverAccId bigint, " +
                            "sendAmount numeric(20, 2), " +
                            "receiveAmount numeric(20, 2), " +
                            "updateTs timestamp(3) default now" +
                            ")",
                    "create table transactions"
            ).executeUpdate();
        }
    }

    public void dropSchema() {
        try {
            try (Connection con = sql2o.open()) {
                con.createQuery("drop table users")
                        .executeUpdate();

                con.createQuery("drop table transactions")
                        .executeUpdate();

                con.createQuery("drop table accounts")
                        .executeUpdate();
            }
        } catch (Sql2oException e) {
            log.error("failed to drop tables", e);
        }
    }

    public void recreateSchema() {
        dropSchema();
        initSchema();
    }
}
