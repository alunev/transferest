package org.alunev.transferest.route;


import com.google.inject.Inject;
import org.alunev.transferest.model.Account;
import org.alunev.transferest.model.User;
import org.alunev.transferest.service.GenerationService;
import org.alunev.transferest.service.dbstore.AccountService;
import org.alunev.transferest.service.dbstore.TransactionService;
import org.alunev.transferest.service.dbstore.UserService;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static spark.Spark.get;
import static spark.Spark.staticFileLocation;

public class WebRoutes {
    private final UserService userService;

    private final AccountService accountService;

    private final TransactionService transactionService;

    private final GenerationService generationService;

    @Inject
    public WebRoutes(UserService userService,
                     AccountService accountService,
                     TransactionService transactionService,
                     GenerationService generationService) {
        this.userService = userService;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.generationService = generationService;
    }

    public void addRoutes() {
        staticFileLocation("/public");

        get("/", (req, res) -> {
                    Map<String, Object> model = new HashMap<>();

                    model.put("users", userService.getAll());
                    model.put("userAccounts", getUserAccountsMap());
                    model.put("transactions", transactionService.getAll());

                    return new VelocityTemplateEngine().render(
                            new ModelAndView(model, "index.vm")
                    );
                }
        );

        get("/ping", (req, res) -> "pong");
        get("/generate/data", (req, res) -> {
            generationService.generateSomeUsersWithAccounts();
            res.redirect("/");

            return "";
        });
    }

    private Map<User, List<Account>> getUserAccountsMap() {
        return userService.getAll().stream()
                .collect(Collectors.toMap(user -> user, user ->accountService.getByUserId(user.getId())));
    }
}
