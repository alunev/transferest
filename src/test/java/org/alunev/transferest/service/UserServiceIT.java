package org.alunev.transferest.service;

import org.alunev.transferest.model.User;
import org.alunev.transferest.service.dbstore.UserService;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Sql2oException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class UserServiceIT extends ServiceIT {

    private UserService userService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        userService = new UserService(sql2oFactory);
    }

    @Test
    public void canSaveUsers() throws Exception {
        User user = userService.save(User.withName("Bob"));

        assertThat(user.getId()).isEqualTo(0);
        assertThat(user.getName()).isEqualTo("Bob");


        user = userService.save(User.withName("Bob-1"));

        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getName()).isEqualTo("Bob-1");

        user = userService.save(User.withName("Bob-2"));

        assertThat(user.getId()).isEqualTo(2);
        assertThat(user.getName()).isEqualTo("Bob-2");
    }

    @Test(expected = Sql2oException.class)
    public void errorOnDuplicateUser() throws Exception {
        userService.save(User.withName("Bob"));
        userService.save(User.withName("Bob"));
    }
}