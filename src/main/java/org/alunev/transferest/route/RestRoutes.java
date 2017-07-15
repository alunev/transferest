package org.alunev.transferest.route;


import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.alunev.transferest.model.Account;
import org.alunev.transferest.model.Transaction;
import org.alunev.transferest.model.TransactionRequest;
import org.alunev.transferest.model.User;
import org.alunev.transferest.model.error.TransferException;
import org.alunev.transferest.model.error.UnexpectedException;
import org.alunev.transferest.service.dbstore.AccountService;
import org.alunev.transferest.service.dbstore.UserService;
import org.alunev.transferest.service.processor.TransactionProcessor;

import java.math.BigDecimal;

import static com.google.common.base.Throwables.getStackTraceAsString;
import static org.alunev.transferest.util.JsonUtil.fromJson;
import static org.alunev.transferest.util.JsonUtil.toJson;
import static spark.Spark.delete;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

@Slf4j
public class RestRoutes {

    private final UserService userService;
    private final AccountService accountService;
    private final TransactionProcessor transactionProcessor;

    @Inject
    public RestRoutes(UserService userService,
                      AccountService accountService,
                      TransactionProcessor transactionProcessor) {
        this.userService = userService;
        this.accountService = accountService;
        this.transactionProcessor = transactionProcessor;
    }

    public void addRoutes() {
        addExceptionHandlers();

        get("/v1/users", (req, res) -> userService.getAll(), toJson());
        get("/v1/users/:id",
                (req, res) -> userService
                        .getById(Long.parseLong(req.params(":id")))
                        .orElseThrow(() -> new TransferException("user not found")),
                toJson()
        );
        post("/v1/users",
                (req, res) -> userService.save(fromJson(req.body(), User.class)),
                toJson()
        );
        put("/v1/users/:id",
                (req, res) -> {
                    User user = fromJson(req.body(), User.class).toBuilder()
                            .id(Long.parseLong(req.params("id")))
                            .build();

                    userService.update(user)
                            .orElseThrow(() -> new TransferException("user not found"));

                    return "{}";
                }
        );
        delete("/v1/users/:id",
                (req, res) -> {
                    userService.delete(Long.parseLong(req.params(":id")))
                            .orElseThrow(() -> new TransferException("user not found"));

                    return "{}";
                }
        );


        get("/v1/users/:id/accounts", (req, res) ->
                accountService.getByUserId(Long.parseLong(req.params(":id"))), toJson());
        get("/v1/users/:id/accounts/:accId", (req, res) ->
                accountService.getById(Long.parseLong(req.params(":accId"))), toJson());

        get("/v1/accounts/:accId", (req, res) ->
                accountService.getById(Long.parseLong(req.params(":accId"))).orElseThrow(
                        () -> new TransferException("account not found")
                ), toJson()
        );

        post("/v1/users/:id/accounts",
                (req, res) -> accountService.save(fromJson(req.body(), Account.class)),
                toJson()
        );

        post("/v1/transactions",
                (req, res) -> transactionProcessor.process(fromJson(req.body(), Transaction.class)),
                toJson()
        );

        post("/v1/transactions/request",
                (req, res) -> {
                    TransactionRequest request = fromJson(req.body(), TransactionRequest.class);
                    return transactionProcessor.process(request);
                },
                toJson()
        );
    }

    private void addExceptionHandlers() {
        exception(TransferException.class, (exception, request, response) -> {
            response.status(400);
            response.body(toJson(exception));
        });
        exception(Exception.class, (exception, request, response) -> {
            log.error("Exception on " + request.url(), exception);

            response.status(500);
            response.body(toJson(new UnexpectedException(getStackTraceAsString(exception))));
        });
    }
}
