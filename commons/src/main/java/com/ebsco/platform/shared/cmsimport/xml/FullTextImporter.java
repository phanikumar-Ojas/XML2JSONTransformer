package com.ebsco.platform.shared.cmsimport.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil;
import com.ebsco.platform.shared.cmsimport.utilities.FileUtil;

public class FullTextImporter {

	private static final String ROOT_XML_DIR = "/Users/mpamuk/Desktop/cms_pov/xml_files/example";
	private static Pattern bodyRegex = Pattern.compile("<body>(.*?)</body>", Pattern.MULTILINE | Pattern.DOTALL);
	private static Pattern bookPartRegex = Pattern.compile("<book-part (.*?)</book-part>", Pattern.MULTILINE | Pattern.DOTALL);
	private static Pattern secRegex = Pattern.compile("<sec(.*?)>(.*?)</sec>", Pattern.MULTILINE | Pattern.DOTALL);


	
	

	public static void main (String [] args) throws IOException {

		int noOfAssets = ContentstackUtil.getCountOfAssets();		
		Map<String,String> fileUidMap = ContentstackUtil.getFilenameToUidUrlMap(noOfAssets);	
		
		String rootDir = ROOT_XML_DIR;

		List<String>filePaths = FileUtil.getFilePaths(rootDir);
		

		int count=0;
		for (String fileP : filePaths) {

			//System.out.println(fileP);

			System.out.println(count++);
			POVFullText povft = getPOVFullText(fileUidMap, rootDir, fileP);
			
			System.out.println(povft.getBodyTxt());
			
		   //importSingleArticle(fileP.substring(0, fileP.length()-4), povft.getBodyTxt(),povft.getBibliographyTxt(),povft.getAuthorNoteTxt());


		}
	}




	public static POVFullText getPOVFullText(Map<String, String> fileUidMap, String rootDir, String fileP) {
		String text  = getTextOfArticle(rootDir, fileP);

		String bookPart = getBookPart(text);
		String body = getBody(bookPart);
		//System.out.println(body);

		String citationSection= getCitationSection(body);
		//System.out.println("CITATIN SEC " + citationSection);
		String citationHtml = getCitationHtml(citationSection);
		//System.out.println("CITATION " + citationHtml);

		String bodyMinusCitation = getBodyWithoutCitation(body);
		//System.out.println("BODY WITHOUT CITATION " + bodyMinusCitation);

		
		
		String bodyHtml = getBodyHtml(bodyMinusCitation, fileUidMap);			
		
		//System.out.println("BODY HTML " + bodyHtml);
		

		String authorNote= getAuthorNoteSection(body);
		//System.out.println("CITATIN SEC " + citationSection);
		
		String authorNoteHtml  = null;
		if (authorNote!=null) {
			authorNoteHtml = getBodyHtml(authorNote,fileUidMap);
		}


		String id = fileP.substring(0,fileP.length()-4);
		return new POVFullText(bodyHtml, citationHtml, authorNoteHtml);
	}
	
	
	public static POVFullText getPOVFullText(Map<String, String> fileUidMap, String text) {

		String bookPart = getBookPart(text);
		String body = getBody(bookPart);
		//System.out.println(body);

		String citationSection= getCitationSection(body);
		//System.out.println("CITATIN SEC " + citationSection);
		String citationHtml = getCitationHtml(citationSection);
		//System.out.println("CITATION " + citationHtml);

		String bodyMinusCitation = getBodyWithoutCitation(body);

		
		
		String bodyHtml = getBodyHtml(bodyMinusCitation, fileUidMap);			
		
		//System.out.println("BODY HTML " + bodyHtml);
		

		String authorNote= getAuthorNoteSection(body);
		//System.out.println("CITATIN SEC " + citationSection);
		
		String authorNoteHtml  = null;
		if (authorNote!=null) {
			authorNoteHtml = getBodyHtml(authorNote,fileUidMap);
		}


		return new POVFullText(bodyHtml, citationHtml, authorNoteHtml);
	}



	public static String getBodyWithoutCitation(String body) {
		if (body==null) {
			return null;
		}
		List<String>sections = new ArrayList<String>();
		Matcher regexMatcher = secRegex.matcher(body);

		while (regexMatcher.find()) {

			String tagS= regexMatcher.group(0);

			//remove citations/bibliography as we will put it in a seperate field

			if (tagS.contains("<ref>")) {
				continue;
			}

			//remove author note as we will put it in a seperate field
			if (tagS.contains("sec-type=\"au_note\"")) {
				continue;
			}
			sections.add(tagS);

		}
		return convertSectionsToTxt(sections);
	}



	public static String getCitationSection(String body) {
		if (body==null) {
			return null;
		}
		Matcher regexMatcher = secRegex.matcher(body);

		while (regexMatcher.find()) {

			String tagS= regexMatcher.group(0);

			//System.out.println("tahs " + tagS);
			
			if (tagS.contains("<ref>")) {
				//System.out.println("citatoin section ");
				return tagS;
			}


		}
		return null;
	}


	public static String getAuthorNoteSection(String body) {
		if (body==null) {
			return null;
		}
		Matcher regexMatcher = secRegex.matcher(body);

		while (regexMatcher.find()) {

			String tagS= regexMatcher.group(0);

			//System.out.println("tahs " + tagS);
			
			if (tagS.contains("sec-type=\"au_note\"")) {
				//System.out.println("citatoin section ");
				return tagS;
			}


		}
		return null;
	}


	private static String getCitationHtml(String citationBody) {
		
		
		if (citationBody==null) {
			return null;
		}
		String tagS = citationBody.replaceAll("<title/>", "");
		tagS = citationBody.replaceAll("<p/>", "");
		tagS= replaceFormatTags(tagS);
		
		//replace ref-list

		
		

		Document doc = Jsoup.parse(tagS);

		//replace figures



		for (Element _div : doc.select("sec")) {

		    // get the unwanted outer-tag
		    Element outerTag = _div.select("ref-list").first();

		    // delete any TextNodes that are within outer-tag
		    for (Node child : outerTag.childNodes()) {
		        if (child instanceof TextNode) child.remove();
		    }

		    // unwrap to remove outer-tag and move inner-tag to child of parent div
		    outerTag.unwrap();

		    // print the result 
		    //System.out.println(_div);
		}
		
		
		
		for (Element _div : doc.select("sec")) {

		    // get the unwanted outer-tag
		    Elements outerTag = _div.select("ref");
		    for (Element el :outerTag) {
		    	el.unwrap();
		    }

		}
		
		//Change sec tp section
		
		Elements elements = doc.select("sec");
		elements.tagName("section");
		
		elements = doc.select("title");
		elements.tagName("h2");
		
		//Change citation to p
		
		elements = doc.select("citation");
		elements.tagName("p");
		
		
		//Change links
		
		elements = doc.select("ext-link[ext-link-type$=uri]");
		//System.out.println("ELEMENTS " + elements.size());
		// rename all 'font'-tags to 'span'-tags, will also keep attributs etc.
		elements.tagName("a");
		for (Element element : elements) {

			//System.out.println("ELI " + element.text());
			//element.text(element.text());
			element.attr("href", element.text());
		}
		
		
		elements = doc.select("ext-link[ext-link-type$=AN]");
		//System.out.println("ELEMENTS " + elements.size());
		// rename all 'font'-tags to 'span'-tags, will also keep attributs etc.
		elements.tagName("a");
		for (Element element : elements) {

			element.text(element.attr("xlink:title"));

		}
		
		
		
		return doc.html();
		//return null;
	}







	private static String getBodyHtml (String body,Map<String,String> fileUidMap) {

		if (body==null) {
			return null;
		}
		String tagS = body.replaceAll("<title/>", "");
		tagS = body.replaceAll("<p/>", "");
	
		
		//new
		tagS = body.replaceAll("<caption/>", "");

		
		//end new
		tagS= replaceFormatTags(tagS);

		tagS= replaceTags(tagS, fileUidMap);
		return tagS;
	}

	public static String getTextOfArticle(String rootDir, String filename) {
		// TODO Auto-generated method stub
		String filePath = rootDir + "/" +filename;
		return FileUtil.readStringFromFile(filePath);
	}


	public static String getBookPart(String text) {
		Matcher regexMatcher = bookPartRegex.matcher(text);

		while (regexMatcher.find()) {//Finds Matching Pattern in String

			String tagS= regexMatcher.group(1);
			return tagS;

		}
		return null;
	}

	static String getBody(String text) {
		Matcher regexMatcher = bodyRegex.matcher(text);

		while (regexMatcher.find()) {//Finds Matching Pattern in String

			String tagS= regexMatcher.group(1);

			// System.out.println("sssssSSSSSSS " + tagS);
			return tagS;

		}
		return null;
	}




	private static String replaceTags(String html,Map<String,String>fileUidMap) {
		//System.out.println(html);
		Document doc = Jsoup.parse(html);

		//replace figures


		Elements elements = doc.select("sec");


		elements.tagName("section");


		elements = doc.select("fig");


		// rename all 'font'-tags to 'span'-tags, will also keep attributs etc.
		elements.tagName("figure");

		for (Element e : elements) {
			for (Element c : e.children()) {
				if (c.toString().equals("<p></p>")) {
					c.remove();
				}
		
			}
		}

		String caption=null;
		//System.out.println("HTTTTML " + doc.html());

		List<String>captions = new ArrayList<String>();
		for (Element e: elements) {
			if (e.select("copyright-statement")!=null){
				caption=e.select("copyright-statement").text();
				captions.add(caption);
				//e.appendElement("figcaption").text();
			}
			else {
				captions.add(null);
			}

		}
		
		for (int i=0;i<elements.size();i++) {
			if (captions.get(i)!=null)
				elements.get(i).appendElement("figcaption").text(captions.get(i));
		}

		for (Element e: elements) {
			
			e.select("permissions").remove();
		}

		Elements elements2 = doc.select("ext-link[ext-link-type$=db-image]");
		elements2.tagName("img");

		for (Element element : elements2) {

			//System.out.println("ELI " + element.text());

			String imagean = element.attr("xlink:href");

			element.attr("href", element.attr("xlink:href"));
			String assetUidUrl = lookupAssetUid(imagean,fileUidMap );
			if (assetUidUrl!=null) {
				//set asset uid

				String [] toks = assetUidUrl.split("_");
				element.attr("asset_uid",toks[0]);
				element.attr("src",toks[1]);
				element.attr("max-width","700");
				element.attr("height","auto");


				//element.attr("style","vertical-align:middle;margin:30px 30px");


				//REMOVE
				/*

				//element.attr("max-width","80%");
				//element.attr("max-height","80%");
			*/

			}
			
			else {
				System.out.println("ASSET UID IS NULL " +imagean);
			}

		}

		elements2.removeAttr("xlink:href");

		//start change
		elements2.removeAttr("xlink:type");
		elements2.removeAttr("ext-link-type");
		elements2.removeAttr("xmlns:xlink");
		elements2.removeAttr("href");
		

		
		//end change

		
		//replace p type extract

		elements = doc.select("p[content-type$=extract]");
		elements.tagName("blockquote");


		//replace external uri

		elements = doc.select("ext-link[ext-link-type$=uri]");
		//System.out.println("ELEMENTS " + elements.size());
		// rename all 'font'-tags to 'span'-tags, will also keep attributs etc.
		elements.tagName("a");
		List<String>urls = new ArrayList<String>();
		for (Element element : elements) {

			//System.out.println("ELI " + element.text());
			urls.add(element.text());
			element.attr("href", element.text());
		}

		
		//replace ans
		
		elements = doc.select("ext-link[ext-link-type$=AN]");
		//System.out.println("ELEMENTS " + elements.size());
		// rename all 'font'-tags to 'span'-tags, will also keep attributs etc.
		elements.tagName("a");
		for (Element element : elements) {

			element.attr("href", element.attr("xlink:href"));

			//System.out.println("ELI " + element.text());
			//urls.add(element.text());
			element.text(element.attr("xlink:title"));
			//element.attr("href", element.text());
			//element.text(element.text());
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

	private static String replaceFormatTags(String tagS) {
		String txt =tagS.replaceAll("<title>", "<h1>");
		txt =txt.replaceAll("</title>", "</h1>");

		txt =txt.replaceAll("<bold>", "<strong>");
		txt =txt.replaceAll("</bold>", "</strong>");


		txt =txt.replaceAll("<italic>", "<i>");
		txt =txt.replaceAll("</italic>", "</i>");
		return txt;
	}



	private static String convertSectionsToTxt(List<String> sections) {
		// TODO Auto-generated method stub

		String txt= "";
		for (String section : sections) {
			txt+=section;
		}
		return txt;
	}


	private static String lookupAssetUid(String imagean, Map<String, String> fileUidMap) {
		String filen = imagean.substring(0,imagean.length()-4);

		return fileUidMap.get(filen);
	}




	
	

}
