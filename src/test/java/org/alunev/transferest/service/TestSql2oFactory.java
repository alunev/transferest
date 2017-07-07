package org.alunev.transferest.service;

import org.alunev.transferest.db.Sql2oFactory;
import org.sql2o.Sql2o;


public class TestSql2oFactory implements Sql2oFactory {
    @Override
    public Sql2o createSql2o() {
        return new Sql2o("jdbc:hsqldb:mem:testmemdb", "SA", "");
    }
}
