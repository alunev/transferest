package org.alunev.transferest.service;

import com.google.inject.Inject;
import org.alunev.transferest.db.Sql2oFactory;
import org.alunev.transferest.model.User;
import org.alunev.transferest.model.User.UserBuilder;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

public class UserService {

    private final Sql2o sql2o;

    @Inject
    public UserService(Sql2oFactory sql2oFactory) {
        this.sql2o = sql2oFactory.createSql2o();
    }

    public User save(User user) {
        long key;
        try (Connection con = sql2o.open()) {
            key = (Long) con.createQuery("insert into users(name) values (:name)",
                    "insert_user",
                    true)
                    .addParameter("name", user.getName())
                    .executeUpdate()
                    .getKey();
        }

        return new UserBuilder(user)
                .setId(key)
                .build();
    }
}
