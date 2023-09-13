package com.ebsco.platform.shared.cmsimport.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import com.ebsco.platform.shared.cmsimport.utilities.FileUtil;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class XML2JSONFTConverter {
	
	private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");
	
	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");

	private static Pattern secRegex = Pattern.compile("<sec(.*?)>(.*?)</sec>", Pattern.MULTILINE | Pattern.DOTALL);

	
	public static void main (String [] args) throws IOException {
		
		
		
		//int noOfAssets = ImageMetadataImporter.getCountOfAssets();		
		//Map<String,String> fileUidMap = ImageMetadataImporter.getFilenameToUidUrlMap(noOfAssets);	
		
			//	String rootDir = "/Users/mpamuk/Desktop/cms_pov/xml_files/example";

		String rootDir = "/Users/mpamuk/Desktop/cms_pov/reimport/XML";
		List<String>filePaths = FileUtil.getFilePaths(rootDir);
		

		int count=0;
		for (String fileP : filePaths) {
			String text  = FullTextImporter.getTextOfArticle(rootDir, fileP);

			String bookPart = FullTextImporter.getBookPart(text);
			String body = FullTextImporter.getBody(bookPart);
			//System.out.println(body);

			String citationSection= FullTextImporter.getCitationSection(body);
			//System.out.println("CITATIN SEC " + citationSection);
			//System.out.println("CITATION " + citationHtml);

			String bodyMinusCitation = FullTextImporter.getBodyWithoutCitation(body);
			System.out.println("BODY WITHOUT CITATION " + bodyMinusCitation);

			JSONArray ftChildren = fullTextToJSONArray(bodyMinusCitation);
			
			
			//System.out.println("BODY HTML " + bodyHtml);
			

			String authorNote= FullTextImporter.getAuthorNoteSection(body);
			//System.out.println("CITATIN SEC " + citationSection);
			

			break;



		}
	}
	
	private static JSONArray fullTextToJSONArray(String body) {

		//split into sections
		//for each section
		List<String>sections = new ArrayList<String>();
		 Matcher regexMatcher = secRegex.matcher(body);
		 //System.out.println("WTF");
		 while (regexMatcher.find()) {
			
			 String tagS= regexMatcher.group(2);
				tagS = tagS.replaceAll("<title/>", "");

			//System.out.println("section " + tagS);
			sections.add( tagS);
			 
		 }
		
		 for (String section : sections) {
			 parseSection(section);
			 
		 }
	 
		 
		return null;
	}
	

	private static void parseSection(String sectionTxt) {
		// TODO Auto-generated method stub
		int index = 0;
		String section  =sectionTxt.trim();
		int txtLength = section.length();
		System.out.println("SECTION LENGTH " + section.length());
		while (index<txtLength) {
			System.out.println("FIND NEXT TAG");
			FullTextTag tag = findNextTag(section,index);
			index= tag.getEndIndex();
			System.out.println("INDEX " + index );
			System.out.println("REMAINING " + section.substring(index));
		}		
	}

	private static FullTextTag findNextTag(String section, int index) {
		int openBracketIndex = section.indexOf( "<",index);
		int closeBracketIndex = section.indexOf(">",openBracketIndex);
		
		String tagStr = section.substring(openBracketIndex,closeBracketIndex+1);
		
		FullTextTag ftt = extractFullTextTag (tagStr, section, openBracketIndex, closeBracketIndex+1);
		System.out.println("TAG " + tagStr);
		return ftt;
	}

	private static FullTextTag extractFullTextTag(String tagStr, String section, int openBracketIndex, int closeBracketIndex) {
		FullTextTag ft = null;
		if (tagStr.equals("<title>")) {
			String ftTagType = "title";
			int closingTagIndex = section.indexOf("</title>", closeBracketIndex);
			String value = section.substring(closeBracketIndex, closingTagIndex);
			int lengthOfEndTag= "</title>".length();
			System.out.println("REMAINING SECTION " + section.substring(closingTagIndex+lengthOfEndTag));
			ft = new FullTextTag(ftTagType, value, "<title>", openBracketIndex, closingTagIndex+lengthOfEndTag);
			System.out.println(ft);
		}
		
		else if (tagStr.equals("<p>")) {
			String ftTagType = "paragraph";
			int closingTagIndex = section.indexOf("</p>", closeBracketIndex);
			String value = section.substring(closeBracketIndex, closingTagIndex);
			int lengthOfEndTag= "</p>".length();
			System.out.println("REMAINING SECTION " + section.substring(closingTagIndex+lengthOfEndTag));
			ft = new FullTextTag(ftTagType, value, "<p>", openBracketIndex, closingTagIndex+lengthOfEndTag);
			System.out.println(ft);

			
		}
		else if (tagStr.startsWith("<fig")) {
			String ftTagType = "figure";
			int closingTagIndex = section.indexOf("</fig>", closeBracketIndex);
			String value = section.substring(closeBracketIndex, closingTagIndex);
			int lengthOfEndTag = "</fig>".length();
			String startTagStr = tagStr;
			ft = new FullTextTag(ftTagType, value, startTagStr, openBracketIndex, closingTagIndex+lengthOfEndTag);
			System.out.println(ft);

		}
		
		
		
		return ft;
	}

	public static void mainSingleFullTxt (String [] args) throws IOException {
		
		JSONObject jo = new JSONObject();
		jo.put("title", "Example One");
		
		
		JSONObject entryObj = new JSONObject();
		entryObj.put("entry", jo);
		
		JSONObject jsonRte = new JSONObject();
		jsonRte.put("type", "doc");
		jsonRte.put("uid", "lkjwbhdjdnff77632346123");
		
		
		JSONObject jsonRteAttrs = new JSONObject();
		jsonRteAttrs.put("dirty", true);
		
		jsonRte.put("attrs", jsonRteAttrs);

		
		jo.put("body_json_rte", jsonRte);
		
		
		
		/*
		 *   "children":[
        {
          "children":[
            {
              "text":"Hello world! This is paragraph 1."
            }
          ],
          "type":"p",
          "uid":"hjsbhys1234",
          "attrs":{
            "dirty":true
          }
        },
		 */
		JSONArray childrenArr = new JSONArray();
		
		
		JSONObject paraObj  = new JSONObject();
		paraObj.put("type", "p");
		paraObj.put("uid", "hjsbhys1234");

		JSONObject paraAttrs = new JSONObject();
		paraAttrs.put("dirty", true);
		paraObj.put("attrs", paraAttrs);

		JSONArray paraChildrenArr = new JSONArray();
		
		JSONObject textObj  = new JSONObject();
		textObj.put("text", "Hello world!");
		paraChildrenArr.put(textObj);
		
		paraObj.put("children", paraChildrenArr);
		
		
		childrenArr.put(paraObj);
		jsonRte.put("children", childrenArr);

        
		System.out.println(entryObj.toString());
		
		
		String jsonStr = entryObj.toString();

		
		
		
		
		
		
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
				MediaType mediaType = MediaType.parse("application/json");
				RequestBody body = RequestBody.create(mediaType, jsonStr);
				Request request = new Request.Builder()
				  .url("https://api.contentstack.io/v3/content_types/fulltextexample/entries")
				  .method("POST", body)
				  .addHeader("api_key", API_KEY)
				  .addHeader("authorization",MANAGEMENT_TOKEN)
				  .addHeader("Content-Type", "application/json")
				  .build();
				Response response = client.newCall(request).execute();
				String responseStr = response.body().string();
				System.out.println("json str  " + responseStr);
		
		
	}
	
	
	
	
	private String uuidGenerator() {
	    final String uuid = UUID.randomUUID().toString().replace("-", "");
	    return uuid;

	}

}
