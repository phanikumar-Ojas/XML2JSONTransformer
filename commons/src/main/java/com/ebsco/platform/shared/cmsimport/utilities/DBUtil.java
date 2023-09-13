package com.ebsco.platform.shared.cmsimport.utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
	 
	private static final String CONNECTION_STRING = AppPropertiesUtil.getProperty("META_CONNECTION_STRING");
	 private static final String CONNECTION_USER = AppPropertiesUtil.getProperty("META_CONNECTION_USER");
	 private static final String CONNECTION_PWD =  AppPropertiesUtil.getProperty("META_CONNECTION_PWD");

	 
	  private static final String MFS_CONNECTION_URL = AppPropertiesUtil.getProperty("MFS_CONNECTION_URL");

	
	public static Connection getPostgresConn() throws SQLException {
		// TODO Auto-generated method stub
		Connection conn =  DriverManager.getConnection(CONNECTION_STRING, CONNECTION_USER, CONNECTION_PWD);
		return conn;
	}
	
	
	public static Connection getMFSConn() throws SQLException {
		java.util.Properties props = new java.util.Properties();


		props.put("v$session.program", "MFS_CMS_SYNCHER");
		String dbURL = MFS_CONNECTION_URL;
		Connection conn = DriverManager.getConnection(dbURL, props);
		return conn;
	}
}
