package org.alunev.transferest.db;

import org.alunev.transferest.service.TestSql2oFactory;
import org.junit.Test;

/**
 * @author red
 * @since 0.0.1
 */
public class DbInitializerIT {
    @Test
    public void initSchemaWorksWithNoExceptions() throws Exception {
        new DbInitializer(new TestSql2oFactory()).initSchema();
    }

}