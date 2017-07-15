package org.alunev.transferest.service;

import org.alunev.transferest.db.DbInitializer;
import org.junit.After;
import org.junit.Before;

public class ServiceIT {
    protected TestSql2oFactory sql2oFactory = new TestSql2oFactory();

    private DbInitializer dbInitializer;

    @Before
    public void setUp() throws Exception {
        dbInitializer = new DbInitializer(sql2oFactory);
        dbInitializer.initSchema();
    }

    @After
    public void tearDown() throws Exception {
        dbInitializer.dropSchema();
    }
}
