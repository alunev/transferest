package org.alunev.transferest.model;

import lombok.Builder;
import lombok.Getter;

/**
 * User, how may have some accounts
 */
@Builder(toBuilder = true)
public class User {
    @Getter
    private final long id;

    @Getter
    private final String name;

    private User(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static User withName(String name) {
        return User.builder().name(name).build();
    }
}
