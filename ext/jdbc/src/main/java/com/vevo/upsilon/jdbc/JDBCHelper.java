package com.vevo.upsilon.jdbc;

import org.apache.commons.dbutils.QueryRunner;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class JDBCHelper {

    private final DataSource dataSource;

    public JDBCHelper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeSQLFile(String classpathPath) {

    }

    public void executeSQLFile(InputStream fileStream) {
        Object ob = "sup";

        QueryRunner run = new QueryRunner(dataSource);
//
//        dataSource.getConnection().createStatement()
//                .execute("select * from vevoaurora where thing = 'wat'");
    }
}
