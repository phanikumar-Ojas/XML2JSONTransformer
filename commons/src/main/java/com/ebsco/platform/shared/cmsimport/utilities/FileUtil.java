package com.ebsco.platform.shared.cmsimport.utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
	
	public static String readStringFromFile(String path) {

		String fileContent = "";

		StringBuilder contentBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) 
		{

		    String sCurrentLine;
		    while ((sCurrentLine = br.readLine()) != null) 
		    {
		        contentBuilder.append(sCurrentLine).append("\n");
		    }
		} 
		catch (IOException e) 
		{
		    e.printStackTrace();
		}

		fileContent = contentBuilder.toString();
		return fileContent;
	}
	
	
	public static List<String> getFilePaths(String rootDir) {
		// TODO Auto-generated method stub
		List<String>filePaths = new ArrayList<String>();
		  String[] pathnames;

	        // Creates a new File instance by converting the given pathname string
	        // into an abstract pathname
	        File f = new File(rootDir);

	        // Populates the array with names of files and directories
	        pathnames = f.list();

	        // For each pathname in the pathnames array
	        for (String pathname : pathnames) {
	            filePaths.add(rootDir + getFileSeperator() + pathname);
	        }
	        return filePaths;
	}
	
	
	public static String getFileSeperator() {
		return FileSystems.getDefault().getSeparator();
	}

	
	public static void createDir(String path) {
		try {
			Files.createDirectories(Paths.get(path));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	public static void writeStringToFileWithUTF8(String fullPath, String str) {
	    try {
		   // BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fullPath)));
		    BufferedWriter writer = new BufferedWriter
		    	    (new OutputStreamWriter(new FileOutputStream(fullPath), StandardCharsets.UTF_8));
			writer.write(str);
			 writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		
	}
	
	public static void writeStringToFile(String fullPath, String str) {
	    try {
		    BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fullPath)));
			writer.write(str);
			 writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		
	}
}
