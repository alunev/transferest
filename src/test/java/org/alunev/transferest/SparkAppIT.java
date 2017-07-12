package org.alunev.transferest;

import com.despegar.http.client.HttpResponse;
import com.despegar.http.client.PostMethod;
import com.despegar.sparkjava.test.SparkServer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.alunev.transferest.model.Account;
import org.alunev.transferest.model.TransactionRequest;
import org.alunev.transferest.model.User;
import org.alunev.transferest.service.ServiceIT;
import org.alunev.transferest.service.TestSql2oFactory;
import org.alunev.transferest.service.dbstore.AccountService;
import org.alunev.transferest.service.dbstore.UserService;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import spark.servlet.SparkApplication;

import java.math.BigDecimal;
import java.util.Currency;

import static org.alunev.transferest.util.JsonUtil.toJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class SparkAppIT {

    @ClassRule
    public static SparkServer<TestSparkApplication> testServer = new SparkServer<>(TestSparkApplication.class, 4567);
    private UserService userService;
    private AccountService accountService;

    @Before
    public void setUp() throws Exception {
        userService = new UserService(new TestSql2oFactory());
        accountService = new AccountService(new TestSql2oFactory());
    }

    @Test
    public void error400IfException() throws Exception {
        HttpResponse httpResponse = sendTxRequest();

        log.info("Response: {}", new String(httpResponse.body()));

        assertThat(httpResponse.code()).isEqualTo(400);
    }

    @Test
    public void jsonTransactionForTxRequest() throws Exception {
        userService.save(User.withName("Bob"));
        userService.save(User.withName("Alice"));

        accountService.save(Account.builder()
                                   .ownerId(0)
                                   .balance(BigDecimal.TEN)
                                   .currency("USD")
                                   .build());

        accountService.save(Account.builder()
                                   .ownerId(1)
                                   .balance(BigDecimal.ONE)
                                   .currency("USD")
                                   .build());


        HttpResponse httpResponse = sendTxRequest();

        log.info("Response: {}", new String(httpResponse.body()));

        assertThat(httpResponse.code()).isEqualTo(200);
    }

    private HttpResponse sendTxRequest() throws com.despegar.http.client.HttpClientException {
        TransactionRequest request = TransactionRequest.builder()
                                                       .senderName("Bob")
                                                       .receiverName("Alice")
                                                       .amount(BigDecimal.valueOf(5))
                                                       .currency(Currency.getInstance("USD"))
                                                       .build();


        PostMethod post = testServer.post("/v1/transactions/request",
                                          toJson(request),
                                          false
        );
        post.addHeader("Test-Header", "test");

        return testServer.execute(post);
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