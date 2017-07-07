package org.alunev.transferest;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.alunev.transferest.service.UserService;

import static spark.Spark.get;

public class Main {
    public static void main(String[] args) {
        get("/ping", (req, res) -> "pong");

        Injector injector = Guice.createInjector(new TransfersModule());


        UserService userService = injector.getInstance(UserService.class);
    }
}
