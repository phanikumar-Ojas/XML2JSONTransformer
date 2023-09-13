package com.ebsco.platform.shared.cmsimport.utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;


public class AppPropertiesUtil {
	
    private static final String PROPS_FILE_NAME = "cms.properties";
	private static Properties config;
	private static final String CFG_DIR_PROP = "cfg.dir";

	public static String getProperty(String key) {
		if (config == null) {
		    setProperties(loadProperties());
		}
	    return config.getProperty(key);
	}
	
	public static void setProperties(Properties props) {
	    config = props;
	}
	
	public static Properties loadProperties() {
		boolean isWindows = SystemUtil.isWindows();
		String localBaseDir = "C:/java/";
		if (System.getProperty(CFG_DIR_PROP) != null && !System.getProperty(CFG_DIR_PROP).isBlank()) {
			localBaseDir = System.getProperty(CFG_DIR_PROP) + File.separatorChar;
		} else if (!isWindows) {
			localBaseDir = "/usr/local/app/java/";
		} else {
			localBaseDir = "C:\\app\\java\\";
		}
		String fileName = localBaseDir + PROPS_FILE_NAME;
		

		Properties props = new Properties();
		try {
			System.out.println("cms.properties=" + fileName);
			props.load(Files.newInputStream(Paths.get(fileName)));
		} catch (IOException ex) {
			try {
				System.out.println("No cms.properties found, using it from classpath");
				props.load(AppPropertiesUtil.class.getClassLoader().getResourceAsStream(PROPS_FILE_NAME));
			} catch (IOException e) {
				System.out.println("Could not find cms.properties file");
			}
		}
		return props;
	}

}

