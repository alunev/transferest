package org.alunev.transferest.service.dbstore;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.alunev.transferest.db.Sql2oFactory;
import org.alunev.transferest.model.User;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class UserService {

    private final Sql2o sql2o;

    @Inject
    public UserService(Sql2oFactory sql2oFactory) {
        this.sql2o = sql2oFactory.createSql2o();
    }

    public List<User> getAll() {
        List<User> allUsers = Collections.emptyList();
        try (Connection con = sql2o.open()) {
            allUsers = con.createQuery(
                    "select * from users",
                    "select_all_users"
            ).executeAndFetch(User.class);
        }

        return allUsers;
    }

    public User save(User user) {
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

    public Optional<User> getById(String id) {
        List<User> users;
        try (Connection con = sql2o.open()) {
            users = con.createQuery(
                    "select * from users where id = :id",
                    "select_user"
            )
                       .addParameter("id", Long.parseLong(id))
                       .executeAndFetch(User.class);
        }

        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }
}
