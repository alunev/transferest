package org.alunev.transferest.service;

import org.alunev.transferest.db.DbInitializer;
import org.alunev.transferest.service.dbstore.AccountService;
import org.junit.After;
import org.junit.Before;

/**
 * @author Anton Lunev antonluneyv@gmail.com
 */
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
