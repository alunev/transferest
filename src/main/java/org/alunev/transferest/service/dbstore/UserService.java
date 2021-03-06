package org.alunev.transferest.service.dbstore;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.alunev.transferest.db.Sql2oFactory;
import org.alunev.transferest.model.User;
import org.alunev.transferest.util.RetryUtil;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.List;
import java.util.Optional;

import static java.sql.Connection.TRANSACTION_SERIALIZABLE;

@Slf4j
public class UserService {

    private final Sql2o sql2o;

    @Inject
    public UserService(Sql2oFactory sql2oFactory) {
        this.sql2o = sql2oFactory.createSql2o();
    }

    public List<User> getAll() {
        List<User> allUsers;
        try (Connection con = sql2o.open()) {
            allUsers = con.createQuery(
                    "select * from users",
                    "select_all_users"
            ).executeAndFetch(User.class);
        }

        return allUsers;
    }

    public Optional<User> getById(long id) {
        Optional<User> user;
        try (Connection con = sql2o.open()) {
            user = getById(id, con);
        }

        return user;
    }

    public Optional<User> getById(long id, Connection con) {
        return con.createQuery(
                "select * from users where id = :id",
                "select_user"
        )
                .addParameter("id", id)
                .executeAndFetch(User.class)
                .stream()
                .findFirst();
    }

    public Optional<User> getByName(String name, Connection con) {
        return con.createQuery(
                "select * from users where name = :name",
                "select_user_by_name"
        )
                .addParameter("name", name)
                .executeAndFetch(User.class)
                .stream()
                .findFirst();
    }

    public User create(User user) {
        long key;
        try (Connection con = sql2o.open()) {
            key = (Long) con.createQuery("insert into users(name) values (:name)",
                    "insert_user",
                    true
            )
                    .addParameter("name", user.getName())
                    .executeUpdate()
                    .getKey();
        }

        return user.toBuilder()
                .id(key)
                .build();
    }

    public Optional<User> update(User user) {
        return RetryUtil.getWithRetry(() -> {
            try (Connection con = sql2o.beginTransaction(TRANSACTION_SERIALIZABLE)) {
                con.createQuery(
                        "update users set name = :name where id = :id",
                        "update_user"
                )
                        .addParameter("name", user.getName())
                        .addParameter("id", user.getId())
                        .executeUpdate();

                Optional<User> updated = getById(user.getId(), con);

                con.commit();

                return updated;
            }
        });
    }

    public Optional<User> delete(long id) {
        Optional<User> user = getById(id);

        try (Connection con = sql2o.open()) {
            con.createQuery(
                    "delete from users where id = :id",
                    "delete_user"
            )
                    .addParameter("id", id)
                    .executeUpdate();
        }

        return user;
    }
}
