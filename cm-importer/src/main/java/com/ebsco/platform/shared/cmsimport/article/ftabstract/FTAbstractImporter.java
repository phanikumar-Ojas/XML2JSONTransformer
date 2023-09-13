package com.ebsco.platform.shared.cmsimport.article.ftabstract;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import com.ebsco.platform.shared.cmsimport.utilities.FileUtil;

public class FTAbstractImporter {

	private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");

	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");

	private static Pattern abstractRegex = Pattern.compile("<abstract abstract-type=\"placard\">(.*?)</abstract>", Pattern.MULTILINE| Pattern.DOTALL);

	private static Pattern articleIdRegex = Pattern.compile("<book-part-id pub-id-type=\"publisher-id\">(.*?)</book-part-id>", Pattern.MULTILINE| Pattern.DOTALL);

	private static Pattern articleIdRegex2 = Pattern.compile("<book-part-id pub-id-type=\"doi\">(.*?)</book-part-id>", Pattern.MULTILINE| Pattern.DOTALL);

	private static final String ARTICLE_XML_FOLDER_PATH =  "/Users/mpamuk/Desktop/cms_pov/reimport/XML";










	public static Map<String,String> getMapOfArticleIdAbstract() throws ParserConfigurationException, SAXException, IOException {

		String rootDir = ARTICLE_XML_FOLDER_PATH;

		Map<String,String>articleIdAbstractMap = new HashMap<String,String>();

		List<String>filePaths =FileUtil.getFilePaths(rootDir);
		int count=0;
		for (String fileP : filePaths) {
			//System.out.println(fileP);
			// System.out.println(extractFileName(fileP));


			//System.out.println(count++);
			String xmlTxt = FileUtil.readStringFromFile(fileP);
			String abstractTxt = extractAbstract(xmlTxt);
			String articleIdTxt =extractArticleId (xmlTxt);

			if (abstractTxt!=null && articleIdTxt ==null) {
				System.out.println("anomaly");
			}
			if (abstractTxt!=null && articleIdTxt!=null) {
				articleIdAbstractMap.put(articleIdTxt,abstractTxt);				
			}

		}
		return articleIdAbstractMap;
	}




	private static String extractArticleId(String xmlContents) {
		Matcher regexMatcher = articleIdRegex.matcher(xmlContents);

		while (regexMatcher.find()) {//Finds Matching Pattern in String

			String tagS= regexMatcher.group(1);
			//System.out.println("article id " + tagS);
			return tagS;
		}

		regexMatcher = articleIdRegex2.matcher(xmlContents);
		while (regexMatcher.find()) {//Finds Matching Pattern in String

			String tagS= regexMatcher.group(1);
			//System.out.println("article id " + tagS);
			return tagS;
		}

		return null;
	}




	private static String extractAbstract(String xmlContents) {
		Matcher regexMatcher = abstractRegex.matcher(xmlContents);

		while (regexMatcher.find()) {//Finds Matching Pattern in String

			String tagS= regexMatcher.group(1);
			//System.out.println("group " + tagS);
			return tagS;
		}
		return null;
	}
}
