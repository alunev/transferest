package org.alunev.transferest.util;


import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.sql2o.Sql2oException;

import java.sql.SQLTransactionRollbackException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class RetryUtil {
    public static <T> T getWithRetry(Callable<T> callable) {
        RetryPolicy retryPolicy = new RetryPolicy()
                .retryOn(e -> e instanceof Sql2oException && e.getCause() instanceof SQLTransactionRollbackException)
                .withDelay(50, TimeUnit.MILLISECONDS)
                .withMaxRetries(10);

        return Failsafe.with(retryPolicy).get(callable);
    }
}
