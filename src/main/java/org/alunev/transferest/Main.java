package org.alunev.transferest;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new TransfersModule());
        SparkApp sparkApp = injector.getInstance(SparkApp.class);
        sparkApp.start();
    }
}
