package com.ebsco.platform.shared.cmsimport.project;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ebsco.platform.shared.cmsimport.article.ArticleObj;
import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import com.ebsco.platform.shared.cmsimport.utilities.FileUtil;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImageProjectShellGenerator {

	private static final String ART_SPEC = "art_spec_summary";

	private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");
	
	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");

	
	
	public static void main (String [] args) throws IOException {
		generateShellExcel("blt7d81ab5857e1ba2c","/Users/mpamuk/Desktop/cms_pov/image import/Input Images");
	}
	
	
	public static String generateShellExcel(String projectId,String rootDir)  throws IOException{
		
				

		String workbookName = "images_needed_" + projectId;
		
		//String rootDir = "/Users/mpamuk/Desktop/cms_pov/project";
		int count = getCountOfArticlesTiedToProject(projectId);
		//System.out.println("Number of articles tied to the project " + count);
		List<ArticleObj>articles = getArticlesTiedToProject(projectId,count);
		
		String projectName = getProjectNameByUid(projectId);
		//System.out.println(articles.size());

		Workbook wb = new XSSFWorkbook();
		Sheet sheet1 = wb.createSheet("q_export_images_needed_for_proj");
		CreationHelper createHelper = wb.getCreationHelper();

		//create header row
		Row row = sheet1.createRow(0);
		
		


		//UI	project	original title	title	status
		row.createCell(0).setCellValue(
			     createHelper.createRichTextString("Contentstack Article Uid"));
		
		row.createCell(1).setCellValue(
			     createHelper.createRichTextString("Project name"));
		
		row.createCell(2).setCellValue(
			     createHelper.createRichTextString("Title"));
		row.createCell(3).setCellValue(
			     createHelper.createRichTextString("Vendor Source"));
		row.createCell(4).setCellValue(
			     createHelper.createRichTextString("Rights"));
		

		
		
		row.createCell(5).setCellValue(
			     createHelper.createRichTextString("Copyright Notes"));
		row.createCell(6).setCellValue(
			     createHelper.createRichTextString("License"));
		row.createCell(7).setCellValue(
			     createHelper.createRichTextString("Caption"));
		row.createCell(8).setCellValue(
			     createHelper.createRichTextString("Credit"));
		
		row.createCell(9).setCellValue(
			     createHelper.createRichTextString("Content Type"));
		
		row.createCell(10).setCellValue(
			     createHelper.createRichTextString("Alt Text"));
		
		row.createCell(11).setCellValue(
			     createHelper.createRichTextString("Webpage URL"));
		
		row.createCell(12).setCellValue(
			     createHelper.createRichTextString("Position Inline"));
		
		row.createCell(13).setCellValue(
			     createHelper.createRichTextString("Image Notes"));
		row.createCell(14).setCellValue(
			     createHelper.createRichTextString("Art Spec"));
		
		
		
		row.createCell(15).setCellValue(
			     createHelper.createRichTextString("Filename"));
		
		row.createCell(16).setCellValue(
			     createHelper.createRichTextString("Tags (use semi colons to separate tags"));
		
		row.createCell(17).setCellValue(
			     createHelper.createRichTextString("Existing Contentstack Image Uid"));
		
		

		
		for (int i=0;i<articles.size();i++) {
			
			int rowNumber =i+1;
			Row rowC = sheet1.createRow(rowNumber);
			ArticleObj ao = articles.get(i);
			rowC.createCell(0).setCellValue(
				     createHelper.createRichTextString(ao.getArticleId()));
			
			rowC.createCell(1).setCellValue(
				     createHelper.createRichTextString(projectName));
			
			rowC.createCell(2).setCellValue(
				     createHelper.createRichTextString(ao.getArticleTitle()));
			
			
			rowC.createCell(14).setCellValue(
				     createHelper.createRichTextString(ao.getArtSpec()));

			
		}



		XSSFDataValidationHelper validationHelper =  new XSSFDataValidationHelper((XSSFSheet) sheet1);;

		
		
		CellRangeAddressList vendorSourceAddressList = new CellRangeAddressList(1, articles.size(), 3, 3);		
		XSSFDataValidationConstraint  vendorSourceConstraint = (XSSFDataValidationConstraint) validationHelper.createExplicitListConstraint(new String[] { "Pixabay", "Wikimedia Commons", "Pexels", "Unsplash", "Flickr", "Getty Images","NASA Images", "Library of Congress", "US Department of Defense", "Other - Government Publisher" , "Misc./Unknown", "Mixed"});		
		XSSFDataValidation vendorSourceValidation =  (XSSFDataValidation) validationHelper.createValidation(vendorSourceConstraint,
	                vendorSourceAddressList);
		sheet1.addValidationData(vendorSourceValidation);
		
		
		CellRangeAddressList rightsAddressList = new CellRangeAddressList(1, articles.size(), 4, 4);		
		XSSFDataValidationConstraint  rightsConstraint = (XSSFDataValidationConstraint) validationHelper.createExplicitListConstraint(new String[] { "Public Domain", "Fair Use", "Copyrighted", "Royalty-Free", "Royalty-Free (Extended)", "Rights-Managed","Rights-Managed (Editorial)", "Permission Obtained/Record on File", "Creative Commons", "Misc./Unknown", "Mixed" });		
		XSSFDataValidation rightsValidation =  (XSSFDataValidation) validationHelper.createValidation(rightsConstraint,
	                rightsAddressList);
		sheet1.addValidationData(rightsValidation);
		
		
		CellRangeAddressList contentTypeList = new CellRangeAddressList(1, articles.size(), 9, 9);		
		XSSFDataValidationConstraint  contentTypeConstraint = (XSSFDataValidationConstraint) validationHelper.createExplicitListConstraint(new String[] { "Color Photo", "B/W Photo", "Illustration", "Infographic", "Photo with Text", "Photo Collage","Illustration/Photo Collage", "Table", "Map"});		
		XSSFDataValidation contentTypeValidation =  (XSSFDataValidation) validationHelper.createValidation(contentTypeConstraint,
				contentTypeList);
		sheet1.addValidationData(contentTypeValidation);
		
		
		
		String filePath = rootDir + FileUtil.getFileSeperator() + workbookName + ".xlsx";
		try (OutputStream fileOut = new FileOutputStream(filePath)) {
		    wb.write(fileOut);
		}
		return filePath;
	}
	
	


	private static List<ArticleObj> getArticlesTiedToProject(String projectId, int totalEntries) throws IOException {
		// TODO Auto-generated method stub

		List<ArticleObj>articles = new ArrayList<ArticleObj>();

		String query="{\"current_project\": { \"$in_query\": { \"title\": \"" +  projectId +"\"}}}";
		int index = (totalEntries/100)+1;

		for (int i=0;i<index;i++) {
			int skip=i *100;


			OkHttpClient client = new OkHttpClient().newBuilder()
					.build();
			Request request = new Request.Builder()
					.url("https://api.contentstack.io/v3/content_types/article/entries?skip=" +skip+ "&query={\"current_project\":{\"$in_query\":{\"uid\":\"" + projectId + "\"}}}")
					.method("GET", null)
					.addHeader("api_key", API_KEY)
					.addHeader("authorization", MANAGEMENT_TOKEN)
					.addHeader("Content-Type", "application/json")
					.build();
			Response response = client.newCall(request).execute();
			String jsonStr = response.body().string();

			//System.out.println(jsonStr);
			JSONObject jo = new JSONObject(jsonStr);
			JSONArray entriesArray = jo.getJSONArray("entries");
			for (int j=0; j<entriesArray.length(); j++) {
				JSONObject item = entriesArray.getJSONObject(j);
				//System.out.println(item);
				ArticleObj ao = new ArticleObj();
				ao.setArticleId(item.getString("uid"));
				ao.setArticleTitle(item.getString("title"))	;
				
				if (item.has(ART_SPEC) && !item.isNull(ART_SPEC)) {
					ao.setArtSpec(item.getString(ART_SPEC));
			
				}
				articles.add(ao);
			}

		}

		return articles;


	}
	
	
	public static int getCountOfArticlesTiedToProject (String projectId) throws IOException {

		
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
				Request request = new Request.Builder()
				  .url("https://api.contentstack.io/v3/content_types/article/entries?count=true&query={\"current_project\":{\"$in_query\":{\"uid\":\"" + projectId + "\"}}}")
				  .method("GET", null)
				  .addHeader("api_key", API_KEY)
				  .addHeader("authorization", MANAGEMENT_TOKEN)
				  .addHeader("Content-Type", "application/json")
				  .build();
				Response response = client.newCall(request).execute();
		
				String jsonStr = response.body().string();
				JSONObject jo = new JSONObject(jsonStr);
				return jo.getInt("entries");
		
	}
	
	private static String getProjectNameByUid(String projectId) throws IOException {
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		//MediaType mediaType = MediaType.parse("application/json");
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/project/entries?query={\"uid\":\"" + projectId +"\"}")
				.method("GET", null)
				.addHeader("api_key", API_KEY)
				.addHeader("authorization",MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = client.newCall(request).execute();
		String jsonStr = response.body().string();
		//System.out.println(jsonStr);
		JSONObject jo = new JSONObject(jsonStr);
		JSONArray entriesArray = jo.getJSONArray("entries");
		for (int j=0; j<entriesArray.length(); j++) {
			JSONObject item = entriesArray.getJSONObject(j);
			if (item.has("title")) {
				String title = item.getString("title");
				return title;
			}

		}

		return null;
	}


		
			
}
