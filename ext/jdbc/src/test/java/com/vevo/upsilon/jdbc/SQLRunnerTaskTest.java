package com.vevo.upsilon.jdbc;

import com.vevo.upsilon.Upsilon;
import com.vevo.upsilon.di.Dependencies;
import com.vevo.upsilon.execute.UpgradeStatus;
import com.vevo.upsilon.task.load.ClasspathTasksLoader;
import org.apache.commons.dbcp2.BasicDataSource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class SQLRunnerTaskTest {

    private BasicDataSource dataSource;

    @BeforeClass
    public void before() {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
        dataSource.setUsername("SA");
        dataSource.setPassword("");
        dataSource.setUrl("jdbc:hsqldb:mem:mymemdb");
    }

    @Test
    public void upgrade() throws SQLException, InterruptedException {
        Upsilon upsilon = Upsilon.newBuilder()
                .tasksLoader(ClasspathTasksLoader.from("sql_runner.up"))
                .register(new Dependencies() {
                    @Override
                    public void configure() {
                        bind(dataSource, DataSource.class);
                    }
                })
                .build();

        upsilon.upgrade();

        while(upsilon.getStatus().equals(UpgradeStatus.IN_PROGRESS) || upsilon.getStatus().equals(UpgradeStatus.INITIALIZING)) {
            Thread.sleep(200);
        }

        //after our upgrade, we should make sure our record is UPDATED
        ResultSet rs = dataSource.getConnection().createStatement()
                .executeQuery("SELECT * FROM test");

        assertTrue(rs.next());

        assertEquals(rs.getString("thing"), "UPDATED");

        assertTrue(rs.next());

        assertEquals(rs.getString("thing"), "UPDATED AGAIN");
    }


    @Test(expectedExceptions = SQLSyntaxErrorException.class, expectedExceptionsMessageRegExp = ".* object not found: TEST1.*")
    public void rollback() throws SQLException, InterruptedException {
        Upsilon upsilon = Upsilon.newBuilder()
                .tasksLoader(ClasspathTasksLoader.from("sql_runner_fail.up"))
                .register(new Dependencies() {
                    @Override
                    public void configure() {
                        bind(dataSource, DataSource.class);
                    }
                })
                .build();

        upsilon.upgrade();

        while(upsilon.getStatus().equals(UpgradeStatus.IN_PROGRESS) || upsilon.getStatus().equals(UpgradeStatus.INITIALIZING)) {
            Thread.sleep(200);
        }

        //sql should have been rolled back so this table doesnt exist
        dataSource.getConnection().createStatement()
                .executeQuery("SELECT * FROM test1");

        fail("SQL should have been rolled back");
    }
}