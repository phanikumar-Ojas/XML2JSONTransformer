package com.ebsco.platform.shared.cmsimport.export.utils;

import java.nio.file.Files;
import java.nio.file.Paths;

public class TestUtils {
	
	public static String readResourceFile(String relativePath) {
        try {
            return Files.readString(Paths.get(TestUtils.class.getResource(relativePath).toURI()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
