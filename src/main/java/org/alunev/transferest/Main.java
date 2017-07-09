package org.alunev.transferest;

import com.google.inject.Guice;
import com.google.inject.Injector;

import static org.alunev.transferest.util.JsonUtil.toJson;
import static spark.Spark.get;

public class Main {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new TransfersModule());
        SparkApp sparkApp = injector.getInstance(SparkApp.class);
        sparkApp.start();
    }
}
