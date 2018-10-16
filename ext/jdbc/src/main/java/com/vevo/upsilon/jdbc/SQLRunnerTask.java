package com.vevo.upsilon.jdbc;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import com.vevo.upsilon.except.UpsilonUpgradeException;
import com.vevo.upsilon.task.Task;
import lombok.ToString;
import org.apache.commons.dbutils.QueryRunner;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

@ToString
public class SQLRunnerTask implements Task {

    private final QueryRunner queryRunner;

    private final String upgrade;
    private final String rollback;

    @Inject
    public SQLRunnerTask(DataSource dataSource,
                         String upgrade,
                         String rollback) {
        this.upgrade = upgrade;
        this.rollback = rollback;

        this.queryRunner = new QueryRunner(dataSource);
    }

    @Override
    public void upgrade() throws Exception {
        executeScript(upgrade);
    }

    @Override
    public void rollback() throws Exception {
        executeScript(rollback);
    }

    private void executeScript(String filePath) throws IOException, SQLException {
        filePath = filePath.startsWith("/") ? filePath : "/" + filePath;

        InputStream fileStream = this.getClass().getResourceAsStream(filePath);

        if (fileStream == null) {
            throw new FileNotFoundException("Cannot find provided SQL file for upgrade: " + filePath);
        }

        ByteSource byteSource = new ByteSource() {
            @Override
            public InputStream openStream() {
                return fileStream;
            }
        };

        String sql = byteSource.asCharSource(Charsets.UTF_8).read();

        try (Connection connection = this.queryRunner.getDataSource().getConnection()) {
            for (String statement : sql.split(";\n")) {
                queryRunner.execute(connection, statement);
            }
        }
    }
}
