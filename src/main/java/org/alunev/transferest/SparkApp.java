package org.alunev.transferest;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.alunev.transferest.db.DbInitializer;
import org.alunev.transferest.route.RestRoutes;
import org.alunev.transferest.route.WebRoutes;

@Slf4j
public class SparkApp {
    private final DbInitializer dbInitializer;

    private final RestRoutes restRoutes;
    private final WebRoutes webRoutes;

    @Inject
    public SparkApp(DbInitializer dbInitializer,
                    RestRoutes restRoutes,
                    WebRoutes webRoutes) {
        this.dbInitializer = dbInitializer;
        this.restRoutes = restRoutes;
        this.webRoutes = webRoutes;
    }

    public void start() {
        initDb();

        webRoutes.addRoutes();
        restRoutes.addRoutes();
    }


    private void initDb() {
        dbInitializer.initSchema();
    }
}
