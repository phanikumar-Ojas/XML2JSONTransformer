package com.ebsco.platform.shared.cmsimport.xml;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ebsco.platform.shared.cmsimport.utilities.FileUtil;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class ExtractXMLSchema {
	
	 private static final String PATH_TO_XML_FILES_DIR = "/Users/mpamuk/Desktop/cms_pov/reimport/XML";
	 private static Pattern regex = Pattern.compile("<(.*?)\\>(.*?)</(.*?)>", Pattern.MULTILINE | Pattern.DOTALL);
	 private static Pattern regex2 = Pattern.compile("<(.*?)>", Pattern.MULTILINE);
	 private static Pattern bookSeriesMetaRegex = Pattern.compile("<book-series-meta>(.*?)</book-series-meta>", Pattern.MULTILINE| Pattern.DOTALL);
	 private static Pattern bookMetaRegex = Pattern.compile("<book-meta>(.*?)</book-meta>", Pattern.MULTILINE| Pattern.DOTALL);

	 private static Pattern bookPartMetaRegex = Pattern.compile("<book-part-meta>(.*?)</book-part-meta>", Pattern.MULTILINE| Pattern.DOTALL);

	 
	 private static Pattern metaNameRegex = Pattern.compile("<meta-name>(.*?)</meta-name>", Pattern.MULTILINE| Pattern.DOTALL);
	 private static Pattern metaValueRegex = Pattern.compile("<meta-value>(.*?)</meta-value>", Pattern.MULTILINE| Pattern.DOTALL);
	 private static Pattern customMetaRegex = Pattern.compile("<custom-meta>(.*?)</custom-meta>", Pattern.MULTILINE| Pattern.DOTALL);
	 private static Pattern reflistRegex = Pattern.compile("<ref-list>(.*?)</ref-list>", Pattern.MULTILINE| Pattern.DOTALL);

	 
	 
	 
	 
	 public static void mainCustomMeta (String [] args) {

			Map<String,Integer>map = new HashMap<String,Integer>();
			Set<String>xmlElements = new HashSet<String>();
			Set<String>xmlVals = new HashSet<String>();

			String rootDir = PATH_TO_XML_FILES_DIR;
			//String rootDir = "/Users/mpamuk/Desktop/cms_pov/xml_files/example";
			List<String>listOfAll = new ArrayList<String>();
			List<String>filePaths = getFilePaths(rootDir);
			int count=0;
			for (String fileP : filePaths) {
				//System.out.println(fileP);
				System.out.println(fileP.substring(fileP.lastIndexOf("/")+1, fileP.length()-4));
				String xmlContents = FileUtil.readStringFromFile(fileP);
				
				 Matcher regexMatcher = customMetaRegex.matcher(xmlContents);
				 
				 while (regexMatcher.find()) {//Finds Matching Pattern in String
						
					 String tagS= regexMatcher.group(1);
					 System.out.println("group " + tagS);
					 Matcher matcher = metaNameRegex.matcher(tagS);
					 while (matcher.find()) {
						 System.out.println("meta name " + matcher.group(1));
						 xmlElements.add(matcher.group(1));
					 }

					 matcher = metaValueRegex.matcher(tagS);
					 while (matcher.find()) {
						 System.out.println("meta value " + matcher.group(1));
						 xmlVals.add(matcher.group(1));
					 }
					 
				 }
			//	Set<String>elements = getElements(bookSeriesMeta);
			//	xmlElements.addAll(elements);


			}
			
			for (String xmlElement: xmlElements) {
				System.out.println("ELEMENT " + xmlElement);
			}
			
			for (String xmlElement: xmlVals) {
				System.out.println("ELEMENT " + xmlElement);
			}
	 }
	 
	 
	 
	 
	 
	 
	 
	
	 


		public static void mainCitation (String [] args) {

			Map<String,Integer>map = new HashMap<String,Integer>();
			Set<String>xmlElements = new HashSet<String>();

			String rootDir = PATH_TO_XML_FILES_DIR;
			//String rootDir = "/Users/mpamuk/Desktop/cms_pov/xml_files/example";
			List<String>listOfAll = new ArrayList<String>();
			List<String>filePaths = getFilePaths(rootDir);
			int count=0;
			for (String fileP : filePaths) {
				//System.out.println(fileP);
				//System.out.println(fileP.substring(fileP.lastIndexOf("/")+1, fileP.length()-4));
				String xmlContents = FileUtil.readStringFromFile(fileP);
				
				String reflist = extractReflist( xmlContents);
				if (reflist== null) {
					//System.out.println("Missing cits " + fileP);
				}
				else {
				Set<String>elements = getElements(reflist);
				if (elements.contains("sup")) {
					System.out.println(fileP);
				}
				xmlElements.addAll(elements);
				}

			}
			
			for (String xmlElement: xmlElements) {
				System.out.println("ELEMENT " + xmlElement);
			}
	 }








	public static void mainBookPartMeta (String [] args) {

			Map<String,Integer>map = new HashMap<String,Integer>();
			Set<String>xmlElements = new HashSet<String>();

			String rootDir = PATH_TO_XML_FILES_DIR;
			//String rootDir = "/Users/mpamuk/Desktop/cms_pov/xml_files/example";
			List<String>listOfAll = new ArrayList<String>();
			List<String>filePaths = getFilePaths(rootDir);
			int count=0;
			for (String fileP : filePaths) {
				//System.out.println(fileP);
				System.out.println(fileP.substring(fileP.lastIndexOf("/")+1, fileP.length()-4));
				String xmlContents = FileUtil.readStringFromFile(fileP);
				
				String bookSeriesMeta = extractBookPartMeta( xmlContents);
				Set<String>elements = getElements(bookSeriesMeta);
				xmlElements.addAll(elements);


			}
			
			for (String xmlElement: xmlElements) {
				System.out.println("ELEMENT " + xmlElement);
			}
	 }
	 
	 
	 
	 
	 public static void mainBookSeriesMeta (String [] args) {

			Map<String,Integer>map = new HashMap<String,Integer>();
			Set<String>xmlElements = new HashSet<String>();

			String rootDir = PATH_TO_XML_FILES_DIR;
			//String rootDir = "/Users/mpamuk/Desktop/cms_pov/xml_files/example";
			List<String>listOfAll = new ArrayList<String>();
			List<String>filePaths = getFilePaths(rootDir);
			int count=0;
			for (String fileP : filePaths) {
				String xmlContents = FileUtil.readStringFromFile(fileP);
				
				String bookSeriesMeta = extractBookSeriesMeta( xmlContents);
				Set<String>elements = getElements(bookSeriesMeta);
				xmlElements.addAll(elements);


			}
			
			for (String xmlElement: xmlElements) {
				System.out.println("ELEMENT " + xmlElement);
			}
	 }
	 
	 public static void mainBookMeta (String [] args) {

			Map<String,Integer>map = new HashMap<String,Integer>();
			Set<String>xmlElements = new HashSet<String>();

			String rootDir = PATH_TO_XML_FILES_DIR;
			//String rootDir = "/Users/mpamuk/Desktop/cms_pov/xml_files/example";
			List<String>listOfAll = new ArrayList<String>();
			List<String>filePaths = getFilePaths(rootDir);
			int count=0;
			for (String fileP : filePaths) {
				String xmlContents = FileUtil.readStringFromFile(fileP);
				
				String bookSeriesMeta = extractBookMeta( xmlContents);
				Set<String>elements = getElements(bookSeriesMeta);
				xmlElements.addAll(elements);


			}
			
			for (String xmlElement: xmlElements) {
				System.out.println("ELEMENT " + xmlElement);
			}
	 }
	 
		private static String extractBookPartMeta(String xmlContents) {
			
			 Matcher regexMatcher = bookPartMetaRegex.matcher(xmlContents);

			 while (regexMatcher.find()) {//Finds Matching Pattern in String
				
				 String tagS= regexMatcher.group(1);
				 System.out.println("group " + tagS);
				 return tagS;
			 }
			 return null;
			 
		}
		
		private static String extractReflist(String xmlContents) {
			
			 Matcher regexMatcher = reflistRegex.matcher(xmlContents);

			 while (regexMatcher.find()) {//Finds Matching Pattern in String
				
				 String tagS= regexMatcher.group(1);
				 //System.out.println("group " + tagS);
				 return tagS;
			 }
			 return null;
			 
		}
	 
	private static String extractBookSeriesMeta(String xmlContents) {
		
		 Matcher regexMatcher = bookSeriesMetaRegex.matcher(xmlContents);

		 while (regexMatcher.find()) {//Finds Matching Pattern in String
			
			 String tagS= regexMatcher.group(1);
			 System.out.println("group " + tagS);
			 return tagS;
		 }
		 return null;
		 
	}
	
	private static String extractBookMeta(String xmlContents) {
		
		 Matcher regexMatcher = bookMetaRegex.matcher(xmlContents);

		 while (regexMatcher.find()) {//Finds Matching Pattern in String
			
			 String tagS= regexMatcher.group(1);
			 System.out.println("group " + tagS);
			 return tagS;
		 }
		 return null;
		 
	}
	
	
	//mainFindTagFile
	public static void main(String[] args) {
		
		Map<String,Integer>map = new HashMap<String,Integer>();
		Set<String>xmlElements = new HashSet<String>();

		String rootDir = PATH_TO_XML_FILES_DIR;
		//String rootDir = "/Users/mpamuk/Desktop/cms_pov/xml_files/example";
		List<String>listOfAll = new ArrayList<String>();
		List<String>filePaths = getFilePaths(rootDir);
		int count=0;
		for (String fileP : filePaths) {
			  

			System.out.println(count++);
			String xmlContents = FileUtil.readStringFromFile(fileP);
			Set<String>elements = getElements(xmlContents);
			
			for (String el : elements) {
				Integer times = map.get(el);
				if(times==null) {
					times=0;
				}
				times++;
				if (el.equals("ext-link ext-link-type=\"uri\" xlink:type=\"simple\"")) {
					  System.out.println(fileP);

				}
				map.put(el,times);
			}
			xmlElements.addAll(elements);
		}


		for (String el : xmlElements) {

			//System.out.println("ELEE" + el);
			listOfAll.add(el);
		}
		
		
		
		Collections.sort(listOfAll);
		for (String l : listOfAll)
			System.out.println(l);
		
		for (String key: map.keySet()) {
			System.out.println("key: " + key + " " + map.get(key));
		}
		

		
	}

	
	
	

	   private static Set<String> getElements(String xmlContents) {
		// TODO Auto-generated method stub
		     Set<String>set = new HashSet<String>();
			 
			 List<String> matchList = new ArrayList<String>();
			
			 Matcher regexMatcher = regex.matcher(xmlContents);

			 while (regexMatcher.find()) {//Finds Matching Pattern in String
				
				 String tagS= regexMatcher.group(3);
				// System.out.println("tahs with no sp " +tagS);
				 if (tagS.contains(" ")) {
					 //System.out.println("tahs with space " + tagS);
					 tagS= tagS.substring(0,tagS.indexOf(" " ));
					 
				 }
				 if (tagS.startsWith("ext-link ext-link-type")) {
					// System.out.println(tagS);
				 }
				 else {
			    matchList.add(tagS);//Fetching Group from String
				 }
			 }
			 
			 
			 regexMatcher = regex2.matcher(xmlContents);

			 while (regexMatcher.find()) {//Finds Matching Pattern in String
				 String str =regexMatcher.group(1);
				// System.out.println("regex2 " + str);
				 if (str.startsWith("/")) {
					 set.add(str.substring(1,str.length()));
					 //System.out.println(str.substring(1,str.length()));
					 
				 }
				 else if (str.endsWith("/")) {
					 set.add(str.substring(0,str.length()-1));
				 }
				 
				 else {
					 if (str.startsWith("book-part book-part-type=\"chapter\"")) {
						 continue;
					 }

					 else if (str.startsWith("book-part xmlns:xlink=\"http://www.w3.org/1999/xlink\"")) {
						 continue;
					 }
					 else {
					 set.add(str);
					 }
				 }
				 
			  //  matchList.add(regexMatcher.group(1));//Fetching Group from String
			 }


			 for(String str:matchList) {
				 set.add(str);
			 }
			 
			 
			 
		return set;
	}




	public static void mainXml(String[] args) throws ParserConfigurationException, SAXException, IOException {
		String tagName = "ref";
		Map<String,Integer>map = new HashMap<String,Integer>();

		   Set<String>xmlElements = new HashSet<String>();
		   String rootDir = PATH_TO_XML_FILES_DIR;
		   
		   List<String>filePaths = getFilePaths(rootDir);
		   int count=0;
		   for (String fileP : filePaths) {
			 //  System.out.println(fileP);
		   System.out.println(count++);
		   File file= new File(fileP);

		  // System.out.println(fileP);
		   Document doc= null;
	
		    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		    DocumentBuilder db = dbf.newDocumentBuilder();
		    doc = db.parse(file);
		    doc.getDocumentElement().normalize();
		    

		   // System.out.println("Root element " + doc.getDocumentElement().getNodeName());
		    xmlElements.add( doc.getDocumentElement().getNodeName());
		    
		    NodeList nodeList=doc.getElementsByTagName(tagName);
		    for (int i=0; i<nodeList.getLength(); i++) 
		    {
		        // Get element
		        Element element = (Element)nodeList.item(i);
		        xmlElements.add(element.getNodeName());
		        //System.out.println(element.getNodeName());
		        Integer times = map.get(element.getNodeName());
				if(times==null) {
					times=0;
				}
				times++;
				map.put(element.getNodeName(),times);
		    }
		    
		    
		    
		   }
		   
		   for (String el : xmlElements) {
			   
			   System.out.println(el);
		   }
			for (String key: map.keySet()) {
				System.out.println("key: " + key + " " + map.get(key));
			}
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
	            filePaths.add(rootDir + "/" + pathname);
	        }
	        return filePaths;
	}
	}

