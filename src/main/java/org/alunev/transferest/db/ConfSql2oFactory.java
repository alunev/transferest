package org.alunev.transferest.db;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import org.sql2o.Sql2o;

/**
 * Create sql2o tool from config
 */
public class ConfSql2oFactory implements Sql2oFactory {
    private final Config conf;

    @Inject
    public ConfSql2oFactory(Config conf) {
        this.conf = conf;
    }

    @Override
    public Sql2o createSql2o() {
        return new Sql2o(conf.getString("db.url"), conf.getString("db.user"), conf.getString("db.pass"));
    }
}
