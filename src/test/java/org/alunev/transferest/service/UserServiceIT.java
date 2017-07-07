package org.alunev.transferest.service;

import org.alunev.transferest.db.DbInitializer;
import org.alunev.transferest.model.User;
import org.alunev.transferest.model.User.UserBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class UserServiceIT {

    private TestSql2oFactory sql2oFactory = new TestSql2oFactory();
    private UserService userService;

    @Before
    public void setUp() throws Exception {
        new DbInitializer(sql2oFactory).initSchema();

        userService = new UserService(sql2oFactory);
    }

    @Test
    public void canSaveUsers() throws Exception {
        sql2oFactory = new TestSql2oFactory();

        User user = userService.save(UserBuilder.userWithName("Bob"));

        assertThat(user.getId()).isEqualTo(0);
        assertThat(user.getName()).isEqualTo("Bob");


        user = userService.save(UserBuilder.userWithName("Bob-1"));

        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getName()).isEqualTo("Bob-1");

        user = userService.save(UserBuilder.userWithName("Bob-2"));

        assertThat(user.getId()).isEqualTo(2);
        assertThat(user.getName()).isEqualTo("Bob-2");
    }

}