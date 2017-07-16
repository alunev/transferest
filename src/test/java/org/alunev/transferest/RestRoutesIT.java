package org.alunev.transferest;

import com.despegar.http.client.HttpClientException;
import com.despegar.http.client.HttpResponse;
import com.despegar.sparkjava.test.SparkServer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.alunev.transferest.db.DbInitializer;
import org.alunev.transferest.model.Account;
import org.alunev.transferest.model.TransactionRequest;
import org.alunev.transferest.model.User;
import org.alunev.transferest.service.TestSql2oFactory;
import org.alunev.transferest.service.dbstore.AccountService;
import org.alunev.transferest.service.dbstore.UserService;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import spark.servlet.SparkApplication;

import java.math.BigDecimal;
import java.util.Currency;

import static org.alunev.transferest.util.JsonUtil.toJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class RestRoutesIT {

    @ClassRule
    public final static SparkServer<TestSparkApplication> testServer = new SparkServer<>(TestSparkApplication.class,
            5678);

    private UserService userService;
    private AccountService accountService;
    private DbInitializer dbInitializer;

    @Before
    public void setUp() throws Exception {
        TestSql2oFactory sql2oFactory = new TestSql2oFactory();
        dbInitializer = new DbInitializer(sql2oFactory);
        userService = new UserService(sql2oFactory);
        accountService = new AccountService(sql2oFactory, userService);

        dbInitializer.recreateSchema();
    }

    @After
    public void tearDown() throws Exception {
        dbInitializer.dropSchema();
    }

    @Test
    public void error400IfException() throws Exception {
        HttpResponse httpResponse = sendTxRequest();

        assertThat(httpResponse.code()).isEqualTo(400);
    }

    @Test
    public void putUserGives200() throws Exception {
        User user = userService.create(User.withName("Bob"));

        assertThat(put("/v1/users/0",
                toJson(User.builder().id(0).name("Bob-1").build())
        ).code()).isEqualTo(200);
    }

    @Test
    public void deleteUserGives200() throws Exception {
        userService.create(User.withName("Bob"));

        assertThat(delete("/v1/users/0").code()).isEqualTo(200);
    }

    @Test
    public void accountMethodsGive200() throws Exception {
        userService.create(User.withName("Bob"));

        Account account = Account.builder()
                .ownerId(0)
                .number("123-456")
                .balance(BigDecimal.valueOf(12.34))
                .currency("USD")
                .build();

        assertThat(post("/v1/accounts", toJson(account)).code()).isEqualTo(200);
        assertThat(put("/v1/accounts/0", toJson(account.toBuilder().number("555").build())).code()).isEqualTo(200);
        assertThat(get("/v1/accounts/0").code()).isEqualTo(200);
        assertThat(delete("/v1/accounts/0").code()).isEqualTo(200);
    }

    @Test
    public void postTxRequestGives200() throws Exception {
        userService.create(User.withName("Bob"));
        userService.create(User.withName("Alice"));

        accountService.create(Account.builder()
                .ownerId(0)
                .balance(BigDecimal.TEN)
                .currency("USD")
                .build());

        accountService.create(Account.builder()
                .ownerId(1)
                .balance(BigDecimal.ONE)
                .currency("USD")
                .build());


        HttpResponse httpResponse = sendTxRequest();

        assertThat(httpResponse.code()).isEqualTo(200);
    }

    private HttpResponse sendTxRequest() throws com.despegar.http.client.HttpClientException {
        TransactionRequest request = TransactionRequest.builder()
                .senderName("Bob")
                .receiverName("Alice")
                .amount(BigDecimal.valueOf(5))
                .currency(Currency.getInstance("USD"))
                .build();


        return post("/v1/transactions/request", toJson(request));
    }

    private HttpResponse post(String path, String body) throws HttpClientException {
        return testServer.execute(
                testServer.post(path, body, false)
        );
    }

    private HttpResponse put(String path, String body) throws HttpClientException {
        return testServer.execute(
                testServer.put(path, body, false)
        );
    }

    private HttpResponse get(String path) throws HttpClientException {
        return testServer.execute(
                testServer.get(path, false)
        );
    }

    private HttpResponse delete(String path) throws HttpClientException {
        return testServer.execute(
                testServer.delete(path, false)
        );
    }


    public static class TestSparkApplication implements SparkApplication {
        @Override
        public void init() {
            Injector injector = Guice.createInjector(new TransfersModule());
            SparkApp sparkApp = injector.getInstance(SparkApp.class);
            sparkApp.start();
        }
    }
}