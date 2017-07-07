package org.alunev.transferest.model;

/**
 * User, how may have some accounts
 */
public class User {

    private final long id;

    private final String name;

    private User(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static class UserBuilder {
        private long id = -1;
        private String name;

        public UserBuilder() {
        }

        public UserBuilder(User user) {
            this.id = user.getId();
            this.name = user.getName();
        }

        public UserBuilder setId(long id) {
            this.id = id;
            return this;
        }

        public UserBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public User build() {
            return new User(id, name);
        }


        public static User userWithName(String name) {
            return new UserBuilder().setName(name).build();
        }
    }
}
