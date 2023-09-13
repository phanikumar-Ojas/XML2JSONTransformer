package com.ebsco.platform.shared.cmsimport.rs.repository;

import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;

import org.postgresql.ds.PGSimpleDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseClient {

    private final PGSimpleDataSource dataSource;


    public DatabaseClient() {
        String dbUrl = AppPropertiesUtil.getProperty("db.url");
        String dbUser = AppPropertiesUtil.getProperty("db.user");
        String dbPassword = AppPropertiesUtil.getProperty("db.password");
        int dbPort = Integer.parseInt(AppPropertiesUtil.getProperty("db.port"));
        String dbName = AppPropertiesUtil.getProperty("db.name");
        dataSource = new PGSimpleDataSource();
        dataSource.setUser(dbUser);
        dataSource.setPassword(dbPassword);
        dataSource.setDatabaseName(dbName);
        dataSource.setPortNumbers(new int[]{dbPort});
        dataSource.setServerNames(new String[]{dbUrl});
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public PGSimpleDataSource getDataSource() {
		return dataSource;
	}
}
