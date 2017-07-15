package org.alunev.transferest;

import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.alunev.transferest.db.ConfSql2oFactory;
import org.alunev.transferest.db.DbInitializer;
import org.alunev.transferest.db.Sql2oFactory;
import org.alunev.transferest.route.RestRoutes;
import org.alunev.transferest.route.WebRoutes;
import org.alunev.transferest.service.GenerationService;
import org.alunev.transferest.service.dbstore.UserService;
import org.alunev.transferest.service.processor.TransactionProcessor;


public class TransfersModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Config.class).toInstance(ConfigFactory.load());
        bind(Sql2oFactory.class).to(ConfSql2oFactory.class);

        bind(DbInitializer.class);
        bind(SparkApp.class);

        bind(UserService.class);
        bind(GenerationService.class);
        bind(TransactionProcessor.class);

        bind(WebRoutes.class);
        bind(RestRoutes.class);
    }
}
