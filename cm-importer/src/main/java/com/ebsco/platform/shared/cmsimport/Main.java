package com.ebsco.platform.shared.cmsimport;

import java.lang.reflect.Method;

import com.ebsco.platform.shared.cmsimport.rs.ResearchStarterImporter;

public class Main {
	
	public static void main(String[] args) {
		try {
			String classNameToRun = null;
			if(args.length == 0 || args[0] == null || args[0].isBlank()) {
				System.out.println("Please specify fully qualified class name containing main method as a first parameter");
				System.out.println("Using default class: " + ResearchStarterImporter.class.getName());
				classNameToRun = ResearchStarterImporter.class.getName();
			} else {
				classNameToRun = args[0];
			}
			Class<?> classWithMainMethodToRun = Class.forName(classNameToRun);
			Method main = classWithMainMethodToRun.getDeclaredMethod("main", String[].class);
			final Object[] argsWrapper = new Object[1];
			argsWrapper[0] = args;
			main.invoke(null, argsWrapper);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
