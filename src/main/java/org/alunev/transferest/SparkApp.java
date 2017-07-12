package org.alunev.transferest;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.alunev.transferest.db.DbInitializer;
import org.alunev.transferest.model.Account;
import org.alunev.transferest.model.Transaction;
import org.alunev.transferest.model.TransactionRequest;
import org.alunev.transferest.model.User;
import org.alunev.transferest.model.error.TransferException;
import org.alunev.transferest.model.error.UnexpectedException;
import org.alunev.transferest.service.GenerationService;
import org.alunev.transferest.service.dbstore.AccountService;
import org.alunev.transferest.service.dbstore.TransactionService;
import org.alunev.transferest.service.dbstore.UserService;
import org.alunev.transferest.service.processor.TransactionProcessor;

import java.math.BigDecimal;

import static com.google.common.base.Throwables.getStackTraceAsString;
import static org.alunev.transferest.util.JsonUtil.fromJson;
import static org.alunev.transferest.util.JsonUtil.toJson;
import static spark.Spark.*;

@Slf4j
public class SparkApp {
    private final GenerationService generationService;

    private final DbInitializer dbInitializer;

    private final UserService userService;

    private final AccountService accountService;

    private final TransactionProcessor transactionProcessor;

    @Inject
    public SparkApp(UserService userService,
                    GenerationService generationService,
                    DbInitializer dbInitializer,
                    AccountService accountService,
                    TransactionService transactionService,
                    TransactionProcessor transactionProcessor) {
        this.userService = userService;
        this.generationService = generationService;
        this.dbInitializer = dbInitializer;
        this.accountService = accountService;
        this.transactionProcessor = transactionProcessor;
    }

    public void start() {
        initDb();

        staticFileLocation("/public");

        addExceptionHandlers();
        addHelperPages();
        addRestHandlers();
    }

    private void addHelperPages() {
        get("/index", (req, res) -> "pong");
        get("/ping", (req, res) -> "pong");
        get("/generate/data", (req, res) -> "generated: " + generationService.generateSomeUsersWithAccounts());
    }

    private void addRestHandlers() {
        get("/v1/users", (req, res) -> userService.getAll(), toJson());
        get("/v1/users/:id",
            (req, res) -> userService
                    .getById(Long.parseLong(req.params(":id")))
                    .orElseThrow(() -> new TransferException("user not found")),
            toJson()
        );
        post("/v1/users",
             (req, res) -> userService.save(User.withName(req.queryParams("name"))),
             toJson()
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
             (req, res) -> accountService.save(
                     Account.builder()
                            .ownerId(Long.parseLong(req.params(":id")))
                            .number(req.queryParams("number"))
                            .balance(parseBigDecimal(req.queryParams("balance")))
                            .currency(req.queryParams("currency"))
                            .build()),
             toJson()
        );

        post("/v1/transactions",
             (req, res) -> transactionProcessor.process(
                     Transaction.builder()
                                .senderAccId(Long.parseLong(req.queryParams("senderAccId")))
                                .receiverAccId(Long.parseLong(req.queryParams("receiverAccId")))
                                .sendAmount(parseBigDecimal(req.queryParams("sendAmount")))
                                .receiveAmount(parseBigDecimal(req.queryParams("receiveAmount")))
                                .build()),
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

    private BigDecimal parseBigDecimal(String s) {
        return BigDecimal.valueOf(Double.parseDouble(s));
    }

    private void initDb() {
        dbInitializer.initSchema();
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
