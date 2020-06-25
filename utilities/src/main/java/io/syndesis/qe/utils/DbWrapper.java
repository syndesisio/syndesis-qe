package io.syndesis.qe.utils;

import java.sql.Connection;

import io.fabric8.kubernetes.client.LocalPortForward;
import lombok.Data;

@Data
public class DbWrapper {
    private String dbType;
    private LocalPortForward localPortForward;
    private Connection dbConnection;

    public DbWrapper(String dbType) {
        setDbType(dbType);
    }
}
