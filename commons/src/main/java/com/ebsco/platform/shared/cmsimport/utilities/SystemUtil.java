package com.ebsco.platform.shared.cmsimport.utilities;

public class SystemUtil {

	public static boolean isWindows()
	{
		return getOsName().startsWith("Windows");
	}

	public static String getOsName()
	{
		return  System.getProperty("os.name"); 
	}
}
