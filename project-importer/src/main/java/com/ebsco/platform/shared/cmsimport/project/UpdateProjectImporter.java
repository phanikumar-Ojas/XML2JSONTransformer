package com.ebsco.platform.shared.cmsimport.project;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpdateProjectImporter {

	private static final String PROJECT_ID = "projectId";
	private static final String PROJECT_SHEET = "projectSheet";
	private static String floatRegex="([0-9]+[.][0-9]+)";
	private static String intRegex="([0-9]+)";

	private static  Pattern floatPattern=Pattern.compile(floatRegex);
	private static  Pattern intPattern=Pattern.compile(intRegex);

	


	
	private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");
	
	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");


	
	public static void main (String [] args) throws Exception {



		Options options = new Options();


		options.addOption(Option.builder("i").longOpt("import-update-project")
				.desc("import project for update articles")
				.build());
		

		
		options.addOption(Option.builder("p").longOpt(PROJECT_ID).hasArg()
				.desc("project id")
				.build());
		
		options.addOption(Option.builder("s").longOpt(PROJECT_SHEET).hasArg()
				.desc("project excel sheet")
				.build());
		
		

		CommandLineParser parser = new DefaultParser();


		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			// validate that block-size has been set
			if (line.hasOption("import-update-project")) {

				if (!line.hasOption(PROJECT_ID)) {
					System.out.println("You need to supply a project id with the p flag");
				}

				if (!line.hasOption(PROJECT_SHEET)) {
					System.out.println("You need to supply a path to a project excel sheet with the s flag");
				}



				if (line.hasOption(PROJECT_ID)&&line.hasOption(PROJECT_SHEET)) {
					String projectIdStr = line.getOptionValue(PROJECT_ID);
					String projectSheetStr = line.getOptionValue(PROJECT_SHEET);
					
					importProject(projectIdStr, projectSheetStr);
					System.out.println("Program completed successfully");

				}
			}



			else {
				System.out.println("You need to specify -u for importing update project");
			}
		}
		catch (ParseException exp) {
			System.out.println("Unexpected exception:" + exp.getMessage());
		}
	}
	
	
	public static void importProject(String projectUid, String pathToExcelFile) throws Exception {
		Map<String,UpdateProjectArticleObj> map = readCSV(pathToExcelFile);
		

		boolean projectExists = false;
		try {
			projectExists = testIfProjectExists(projectUid);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

		if(projectExists) {
			
			Map<String,String>articleIdJsonMap = getArticleIdJsonMap(map);
			Map<String,String> articleUidJsonMap =updateJson(projectUid,articleIdJsonMap, map);
			updateArticles( articleUidJsonMap);
			
			
		}
		
		else {
			throw new Exception("Project " + projectUid + " does not exist");
		}
	}

	private static Map<String, String> getArticleIdJsonMap(Map<String, UpdateProjectArticleObj> map) throws IOException {
		// TODO Auto-generated method stub
		
		
		Map<String, String> articleIdJsonMap = new HashMap<String,String>();
		for (String articleUid : map.keySet()) {

			

				
				OkHttpClient client = new OkHttpClient().newBuilder()
						  .build();

						Request request = new Request.Builder()
						  .url("https://api.contentstack.io/v3/content_types/article/entries?query={\"uid\":\"" + articleUid +"\"}")
						  .method("GET", null)
						  .addHeader("api_key", API_KEY)
						  .addHeader("authorization",MANAGEMENT_TOKEN)
						  .addHeader("Content-Type", "application/json")
						  .build();
						Response response = client.newCall(request).execute();
						System.out.println("https://api.contentstack.io/v3/content_types/article/entries?query={\"uid\":\"" + articleUid +"\"}");
						String jsonStr = response.body().string();
					System.out.println(jsonStr);
					JSONObject jo = new JSONObject(jsonStr);
					JSONArray entriesArray = jo.getJSONArray("entries");
					for (int j=0; j<entriesArray.length(); j++) {
					    JSONObject item = entriesArray.getJSONObject(j);
					    articleIdJsonMap.put(articleUid, item.toString());

			
					}
		}
		return articleIdJsonMap;
	}

	private static void updateArticles(Map<String, String> articleIdJsonMap) {
		// TODO Auto-generated method stub
		for (String uid : articleIdJsonMap.keySet()) {
			
			try {
				updateArticle(uid, articleIdJsonMap.get(uid));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/*String uid = "blt16ab0e79c6f7e5a9";
		String json = articleIdJsonMap.get(uid);
		
		updateArticle(uid, articleIdJsonMap.get(uid));*/
		
	}

	private static void updateArticle(String uid, String jsonStr) {
		// TODO Auto-generated method stub
		//System.out.println("JSON STR " + jsonStr);
		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
				MediaType mediaType = MediaType.parse("application/json");
				RequestBody body = RequestBody.create(mediaType, jsonStr);
				Request request = new Request.Builder()
				  .url("https://api.contentstack.io/v3/content_types/article/entries/" + uid)
				  .method("PUT", body)
				  .addHeader("api_key", API_KEY)
				  .addHeader("authorization", MANAGEMENT_TOKEN)
				  .addHeader("Content-Type", "application/json")
				  .build();
				try {
					Response response = client.newCall(request).execute();
					String responseStr = response.body().string();
					System.out.println(responseStr);
				//System.out.println(responseStr);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}

	private static Map<String, String> updateJson(String projectId, Map<String, String> articleIdJsonMap,
			Map<String, UpdateProjectArticleObj> map) {
		
		Map<String, String> updatedJson = new HashMap<String,String>();
		for (String articleUid : articleIdJsonMap.keySet()) {
			
			
			UpdateProjectArticleObj upao = map.get(articleUid);
			String json = articleIdJsonMap.get(articleUid);
			
			JSONObject jo = new JSONObject(json);
			
			String uid = jo.getString("uid");
			//remove html fields -- not sure if this is needed?
			List<String>htmlFields= new ArrayList<String>();
			for (String key: jo.keySet()) {
				if (key.endsWith("html"))
					htmlFields.add(key);
			}
			
			for (String hf: htmlFields) {
				jo.remove(hf);
			}
						
			
			
			//add notes
			jo.put("po_notes", upao.getNotes());
			

			//due to poor design of  fields referencing out to assets/other objects,  need to modify arrays with assets prior to updating an entry
			JSONArray newAssociatedImagesArr = new JSONArray();

			
			if (jo.has("associated_images") && !jo.isNull("associated_images")){
				JSONArray associatedImages = jo.getJSONArray("associated_images");
				
				for (int i=0;i<associatedImages.length();i++) {
					JSONObject img = associatedImages.getJSONObject(i);
					newAssociatedImagesArr .put(img.getString("uid"));
				}
				jo.put("associated_images",newAssociatedImagesArr );
				
			}


			
			//associated images end
			
			
			//add task
			jo.put("task_hours", upao.getTaskHours());
			
			//add project
			//if (jo.has("current_project")) {
			//	jo.remove("current_project");
			//}
			JSONArray projectArr = new JSONArray();

			JSONObject projectObj = new JSONObject();

			projectObj.put("uid", projectId);
			projectObj.put("_content_type_uid", "project");
			projectArr.put(projectObj);
			jo.put("current_project", projectArr);
			//add to map
			
			JSONObject entry = new JSONObject();
			entry.put("entry", jo);
			updatedJson.put(uid, entry.toString());
			//System.out.println(entry.toString());
			
		}
		// TODO Auto-generated method stub
		return updatedJson;
	}

	private static boolean testIfProjectExists(String projectId) throws IOException {
		// TODO Auto-generated method stub
		
		//get all project ids
		
		int noOfEntries = ContentstackUtil.getCountOfEntries("project");
		List<String> uids= ContentstackUtil.getListOfEntryUids(noOfEntries,"project");
		if (uids.contains(projectId)) {
			return true;
		}
		
		return false;
	}

	private static Map<String, UpdateProjectArticleObj> readCSV(String pathToCsv) {
		Map<String,UpdateProjectArticleObj> map = new HashMap<String,UpdateProjectArticleObj> ();

		XSSFWorkbook wb  = null;

		try {
			wb = new XSSFWorkbook(new File(pathToCsv));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Sheet sheet = wb.getSheetAt(0);
		
		boolean first = true;

		    for (Row row : sheet) {
		    	if (first) {
		    		first = false;
		    		continue;
		    	}
		    	
		    	if (row.getCell(0)==null) {
		    		break;
		    	}
		    	String articleTitle = row.getCell(0).getRichStringCellValue().getString();
		    	String articleUid =  row.getCell(1).getRichStringCellValue().getString();
		    	double taskHours=0;
		    	Cell taskCell = row.getCell(2);
		    	if (taskCell.getCellType() ==CellType.NUMERIC) {
		    		taskHours = taskCell.getNumericCellValue();
		    	}
		    	else if (taskCell.getCellType() ==CellType.STRING) {
		    		taskHours = extractTaskHours(taskCell.getRichStringCellValue().getString());
		    	}
		    	String notes=  "";
		    	if (row.getCell(3)!=null) {
		    	 notes = row.getCell(3).getRichStringCellValue().getString();
		    	}
		        Date date = row.getCell(4).getDateCellValue();
		    	SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
		    	String dateText = df2.format(date);
		    	String newDate= dateText;
		    	

		    
		    	UpdateProjectArticleObj upao = new UpdateProjectArticleObj(articleTitle, articleUid, taskHours, notes, newDate);
		    	//System.out.println(upao);
		    	map.put(articleUid, upao);
		    }
		
		
		return map;
	}
	
	



	private static float extractTaskHours(String input) {

		Matcher matcher=floatPattern.matcher(input);

		if(matcher.find())
		{
		   String match = matcher.group();
		   return Float.parseFloat(match);
		}
		matcher=intPattern.matcher(input);
		if(matcher.find())
		{
		   String match = matcher.group();
		   return Float.parseFloat(match);
		}
		
		return 0;
	}
}
