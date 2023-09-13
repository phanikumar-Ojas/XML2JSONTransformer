package com.ebsco.platform.shared.cmsimport.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil;
import com.ebsco.platform.shared.cmsimport.utilities.FileUtil;

public class FullTextImportTest {

	private static final String PATH_TO_XML_DIR = "/Users/mpamuk/Desktop/cms_pov/reimport/XML/";
	private static Pattern bodyRegex = Pattern.compile("<body>(.*?)</body>", Pattern.MULTILINE | Pattern.DOTALL);
	private static Pattern bookPartRegex = Pattern.compile("<book-part (.*?)</book-part>", Pattern.MULTILINE | Pattern.DOTALL);

	private static Pattern secRegex = Pattern.compile("<sec(.*?)>(.*?)</sec>", Pattern.MULTILINE | Pattern.DOTALL);







	public static void main (String [] args) throws IOException, SAXException, ParserConfigurationException {
		int noOfAssets = ContentstackUtil.getCountOfAssets();

		Map<String,String> fileUidMap = ContentstackUtil.getFilenameToUidUrlMap(noOfAssets);

		String text  =getTextOfArticle("pov_aus_2015_229682.xml");
		//text= replaceFigures(text);

		String bookPart = getBookPart(text);
		//System.out.println(bookPart);
		String body = getBody(bookPart);

		//System.out.println(body);

		List<String>sections = getSections(body, fileUidMap);
		String htmlTxt = convertSectionsToTxt(sections);
		System.out.println(htmlTxt);
		//importSingleArticle("pov_aus_2015_229682",  htmlTxt);


		//	String html = convertToHtml(sections);

		//	importSingleArticle("pov_aus_2015_229303",  html);
		//getbody
		//split by section
		//convert to html

		//convert p++
		//convert italic++
		//convert bold++
		//convert title+

		//convert ordered li++
		//convert unordered li++


		//convert extlink
		//convert figure
		//convert ref and citation
		//sub ++
		//subp++

		//import single article
	}



	private static String replaceTags(String html,Map<String,String>fileUidMap) {
		Document doc = Jsoup.parse(html);

		//replace figures

		Elements elements = doc.select("fig");


		// rename all 'font'-tags to 'span'-tags, will also keep attributs etc.
		elements.tagName("figure");

		for (Element e: elements)
			e.select("permissions").remove();

		String caption=null;
		//System.out.println("HTTTTML " + doc.html());

		for (Element e: elements) {
			if (e.select("caption")!=null){
				caption=e.select("caption").text();
				e.select("caption").tagName("figurecaption");
			}

		}


		Elements elements2 = doc.select("ext-link[ext-link-type$=db-image]");
		elements2.tagName("img");

		for (Element element : elements2) {

			System.out.println("ELI " + element.text());

			String imagean = element.attr("xlink:href");

			element.attr("href", element.attr("xlink:href"));
			String assetUidUrl = lookupAssetUid(imagean,fileUidMap );

			if (assetUidUrl!=null) {
				//set asset uid

				String [] toks = assetUidUrl.split("_");
				element.attr("asset_uid",toks[0]);
				element.attr("src",toks[1]);


			}

		}


		elements2.removeAttr("xlink:href");



		//replace external uri

		elements = doc.select("ext-link[ext-link-type$=uri]");
		System.out.println("ELEMENTS " + elements.size());
		// rename all 'font'-tags to 'span'-tags, will also keep attributs etc.
		elements.tagName("a");
		List<String>urls = new ArrayList<String>();
		for (Element element : elements) {

			System.out.println("ELI " + element.text());
			urls.add(element.text());
			element.attr("href", element.text());
		}

		//replace li items

		elements = doc.select("list-item");


		// rename all 'font'-tags to 'span'-tags, will also keep attributs etc.
		elements.tagName("li");

		//replace ordered

		elements = doc.select("list[list-type$=order]");


		// rename all 'font'-tags to 'span'-tags, will also keep attributs etc.
		elements.tagName("ol");
		elements.removeAttr("list-type");


		//replace unordered
		elements = doc.select("list[list-type$=bullet]");


		// rename all 'font'-tags to 'span'-tags, will also keep attributs etc.
		elements.tagName("ul");
		elements.removeAttr("list-type");



		//replace citation

		elements = doc.select("citation");


		// rename all 'font'-tags to 'span'-tags, will also keep attributs etc.
		elements.tagName("p");


		elements = doc.select("preformat");


		// rename all 'font'-tags to 'span'-tags, will also keep attributs etc.
		elements.tagName("pre");


		return doc.html();
	}



	private static String lookupAssetUid(String imagean, Map<String, String> fileUidMap) {
		String filen = imagean.substring(0,imagean.length()-4);

		return fileUidMap.get(filen);
	}

	

	private static String convertSectionsToTxt(List<String> sections) {
		// TODO Auto-generated method stub

		String txt= "";
		for (String section : sections) {
			txt+=section;
		}
		return txt;
	}

	private static List<String> getSections(String body, Map<String,String> fileUidMap) {
		List<String>sections = new ArrayList<String>();
		Matcher regexMatcher = secRegex.matcher(body);

		while (regexMatcher.find()) {

			String tagS= regexMatcher.group(2);

			if (tagS.contains("<ref>")) {
				continue;
			}
			tagS = tagS.replaceAll("<title/>", "");

			tagS= replaceTags(tagS);

			tagS=replaceTags(tagS, fileUidMap);
			System.out.println("SECTIONS" + tagS);
			sections.add(tagS);

		}
		return sections;
	}








	private static String replaceTags(String tagS) {
		String txt =tagS.replaceAll("<title>", "<h1>");
		txt =txt.replaceAll("</title>", "</h1>");

		txt =txt.replaceAll("<bold>", "<strong>");
		txt =txt.replaceAll("</bold>", "</strong>");


		txt =txt.replaceAll("<italic>", "<i>");
		txt =txt.replaceAll("</italic>", "</i>");

		//txt = txt.replaceAll("<list list-type=\"bullet\">(.*?)</list>", "<ol>(.*?)</ol>");
		return txt;


	}

	private static String getBody(String text) {
		Matcher regexMatcher = bodyRegex.matcher(text);

		while (regexMatcher.find()) {//Finds Matching Pattern in String

			String tagS= regexMatcher.group(1);

			// System.out.println("sssssSSSSSSS " + tagS);
			return tagS;

		}
		return null;
	}



	private static String getBookPart(String text) {
		Matcher regexMatcher = bookPartRegex.matcher(text);

		while (regexMatcher.find()) {//Finds Matching Pattern in String

			String tagS= regexMatcher.group(1);
			return tagS;

		}
		return null;
	}
	private static String getTextOfArticle(String filename) {
		// TODO Auto-generated method stub
		String filePath = PATH_TO_XML_DIR +filename;
		return FileUtil.readStringFromFile(filePath);
	}








}
