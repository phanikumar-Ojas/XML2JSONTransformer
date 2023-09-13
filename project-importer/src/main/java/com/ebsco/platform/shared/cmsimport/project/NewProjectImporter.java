package com.ebsco.platform.shared.cmsimport.project;

import static com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil.getWorkflowStageUid;
import static com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil.setWorkflowState;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Name;
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

import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil;
import com.ebsco.platform.shared.cmsimport.utilities.FileUtil;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NewProjectImporter {

	
	private static final String ARTICLE_WORKFLOW = "Article Workflow";

	private static final String OUTPUT_DIR = "outputDir";

	private static final String PROJECT_SHEET_PATH = "projectSheetPath";

	private static final String WORKFLOW_STAGE_AVAILABLE = "Available";

	private static final String PROJECT_ID_STR = "projectId";

	private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");

	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");

	private static final String AUTH_TOKEN = AppPropertiesUtil.getProperty("AUTH_TOKEN");

	private static final String UPDATE = "update";

	private static final String WORKFLOW_STAGE_APPROVED_FOR_PRODUCT = "Approval for product";

	private static final String WORKFLOW_STAGE_ASSIGNED = "Assigned";
	
	
	public static void main (String [] args) throws IOException {
		// create Options object
		Options options = new Options();

		// add t option


		options.addOption(Option.builder("gm").longOpt("generate-mid-worksheet-for-new-project")
				.desc("generate worksheet for mid")
				.build());

		options.addOption(Option.builder("i").longOpt("import-new-project")
				.desc("import new project worksheet")
				.build());


		options.addOption(Option.builder("im").longOpt("import-mid-worksheet-for-new-project")
				.desc("import new project worksheet for mids")
				.build());
		
		options.addOption(Option.builder("gw").longOpt("generate-writer-worksheet-for-new-project")
				.desc("generate new project worksheet for assigning writers")
				.build());
		
		options.addOption(Option.builder("iw").longOpt("import-writer-worksheet-for-new-project")
				.desc("import new project worksheet for assigning writers")
				.build());

		
		options.addOption(Option.builder("p").longOpt(PROJECT_ID_STR).hasArg()
				.desc("project id")
				.build());

		options.addOption(Option.builder("o").longOpt(OUTPUT_DIR).hasArg()
				.desc("output directory for the worksheet for mid")
				.build());




		options.addOption(Option.builder("s").longOpt(PROJECT_SHEET_PATH).hasArg()
				.desc("path to sheet for new project")
				.build());



		//HelpFormatter formatter = new HelpFormatter();
		//formatter.printHelp("java -jar image.jar", options);

		CommandLineParser parser = new DefaultParser();

		//String[] args2 = new String[]{ "-g" };

		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("import-new-project")) {

				if (!line.hasOption(PROJECT_ID_STR)) {
					System.out.println("You need to supply a project id with the p flag");
				}


				if (!line.hasOption(PROJECT_SHEET_PATH)) {
					System.out.println("You need to supply a path to the project sheet with the s flag");
				}
				


				if (line.hasOption(PROJECT_ID_STR)&&line.hasOption(PROJECT_SHEET_PATH)) {
					String projectIdStr = line.getOptionValue(PROJECT_ID_STR);
					String projectSheetPath = line.getOptionValue(PROJECT_SHEET_PATH);
					importNewProject(projectIdStr,projectSheetPath);

				}
			}


			else if (line.hasOption("import-mid-worksheet-for-new-project")) {

				if (!line.hasOption(PROJECT_ID_STR)) {
					System.out.println("You need to supply a project id with the p flag");
				}

				if (!line.hasOption(PROJECT_SHEET_PATH)) {
					System.out.println("You need to supply a path to the project sheet with the s flag");
				}

				if (line.hasOption(PROJECT_ID_STR)&&line.hasOption(PROJECT_SHEET_PATH)) {
					String projectIdStr = line.getOptionValue(PROJECT_ID_STR);
					String excelSheetPath = line.getOptionValue(PROJECT_SHEET_PATH);
	                importWorksheetForMid(projectIdStr, excelSheetPath);
				}  
			}
			

			else if (line.hasOption("generate-mid-worksheet-for-new-project")) {

				if (!line.hasOption(PROJECT_ID_STR)) {
					System.out.println("You need to supply a project id with the p flag");
				}

				if (!line.hasOption(OUTPUT_DIR)) {
					System.out.println("You need to supply an output directory with the o flag");
				}

				if (line.hasOption(PROJECT_ID_STR)&&line.hasOption(OUTPUT_DIR)) {
					String projectIdStr = line.getOptionValue(PROJECT_ID_STR);
					String outputDirStr = line.getOptionValue(OUTPUT_DIR);
					generateWorksheetForMidImport(projectIdStr, outputDirStr);
					
					/*
					String excelSheetPath = ImageUpdateProjectShellGenerator.generateShellExcel(projectIdStr,outputDirStr);
					if (new File(excelSheetPath).exists()) {
						System.out.println("Output Excel sheet written to " + excelSheetPath);
						System.out.println("Program completed successfully");
					}*/
				}
			}
			
			else if (line.hasOption("generate-writer-worksheet-for-new-project")) {

				if (!line.hasOption(PROJECT_ID_STR)) {
					System.out.println("You need to supply a project id with the p flag");
				}

				if (!line.hasOption(OUTPUT_DIR)) {
					System.out.println("You need to supply an output directory with the o flag");
				}

				if (line.hasOption(PROJECT_ID_STR)&&line.hasOption(OUTPUT_DIR)) {
					String projectIdStr = line.getOptionValue(PROJECT_ID_STR);
					String outputDirStr = line.getOptionValue(OUTPUT_DIR);
					List<String>userList = getAllUsers();
					generateWorksheetForAssigningWriters(projectIdStr, outputDirStr, userList);
					

				}
			}
			
			else if (line.hasOption("import-writer-worksheet-for-new-project")) {

				if (!line.hasOption(PROJECT_ID_STR)) {
					System.out.println("You need to supply a project id with the p flag");
				}

				if (!line.hasOption(PROJECT_SHEET_PATH)) {
					System.out.println("You need to supply a path to the project sheet with the s flag");
				}

				if (line.hasOption(PROJECT_ID_STR)&&line.hasOption(PROJECT_SHEET_PATH)) {
					String projectIdStr = line.getOptionValue(PROJECT_ID_STR);
					String excelSheetPath = line.getOptionValue(PROJECT_SHEET_PATH);
	                importWorksheetForAssigningWriters(projectIdStr, excelSheetPath);
				}  
			}

			else {
				
				String warning = "You need to either specify -i to import worksheet for new project or  -gw generate writer assignment worksheet for new project\n" +
				" or -iw to import writer assignment worksheet or -gm to to generate mid worksheet for new project or -im to import mid worksheet";
				
				System.out.println(warning);
			}
		}
		catch (ParseException exp) {
			System.out.println("Unexpected exception:" + exp.getMessage());
		}
		
		
		
	}
	
	
	

	
	private static void importWorksheetForAssigningWriters(String projectIdStr, String excelSheetPath) throws IOException {
		// TODO Auto-generated method stub
		List<String>usersFromAssignedToField = getUsersFromAssignedToField();
		Map<String,String>writerNameUidMap = getWriterNameUidMap();
		List<NewProjectArticleObj> list = getNewArticleObjListForAssignment(excelSheetPath);
		for (NewProjectArticleObj npao : list ) {
			String writerUid = writerNameUidMap.get(npao.getAssignedTo());		
			updateArticleWithAssignedUserAndDueDate(npao.getArticleUid(), npao.getAssignedTo(),usersFromAssignedToField, npao.getWriterDueDate());
			assignToUser(npao.getArticleUid(), writerUid, npao.getWriterDueDate());
		}
	}





	private static void updateArticleWithAssignedUserAndDueDate(String articleUid, String assignedTo,
			List<String> usersFromAssignedToField, Date writerDueDate) {

		if (assignedTo==null || assignedTo.isEmpty()) {
			return;
		}
		String assignedToStr = "unassigned";
		for (String user : usersFromAssignedToField) {
			if (user.equalsIgnoreCase(assignedTo)) {
				assignedToStr = user;
				break;			

			}
		}

		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();

		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/article/entries?query={\"uid\":\"" + articleUid +"\"}")
				.method("GET", null)
				.addHeader("api_key", API_KEY)
				.addHeader("authorization",MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = null;
		String jsonStr = null;
		try {
			response = client.newCall(request).execute();
			jsonStr = response.body().string();
		} catch (IOException e) {
			e.printStackTrace();
		}
		JSONObject jo = new JSONObject(jsonStr);
		JSONArray entriesArray = jo.getJSONArray("entries");
		for (int j=0; j<entriesArray.length(); j++) {
			JSONObject item = entriesArray.getJSONObject(j);
		    java.sql.Date sqlVersionOfDueDate = convertUtilDateToSqlDate(writerDueDate);
			item.put("assigned_to", assignedToStr);			
			item.put("writer_due_date",sqlVersionOfDueDate);
		
			ContentstackUtil.updateArticle(articleUid, item);

		}
		
	}





	private static java.sql.Date convertUtilDateToSqlDate(Date date) {
		if (date==null) {
			return null;
		}
		return new java.sql.Date(date.getTime());
	}





	private static List<String> getUsersFromAssignedToField() {
		List<String>assignedToList = new ArrayList<String>();
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();

		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/article" )
				.method("GET", null)
				.addHeader("api_key", API_KEY)
				.addHeader("Content-Type", "application/json")
				.addHeader("authtoken", AUTH_TOKEN)

				.build();
		Response response = null;
		String jsonStr = null;
		try {
			response = client.newCall(request).execute();
			jsonStr = response.body().string();
			JSONObject jo = new JSONObject(jsonStr);
			JSONObject contentTypeObj = jo.getJSONObject("content_type");
			JSONArray schemaArray = contentTypeObj.getJSONArray("schema");
			for (int i=0;i<schemaArray.length();i++) {
				JSONObject schemaObj = schemaArray.getJSONObject(i);
				String uid = schemaObj.getString("uid");
				if (uid.equals("assigned_to")) {
					JSONObject enumObj = schemaObj.getJSONObject("enum");
					JSONArray choicesArray = enumObj.getJSONArray("choices");
					for (int j=0;j<choicesArray.length();j++) {
						JSONObject choiceObj = choicesArray.getJSONObject(j);
						assignedToList.add(choiceObj.getString("value"));
					}
				}
				
			}
		}

		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return assignedToList;
	}





	private static void assignToUser(String articleUid, String writerUid, Date writerDueDate)  throws IOException {
		if (writerUid==null) {
			return;
		}
		
		
		String dateText= null;
		if (writerDueDate!=null) {
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			dateText = df2.format(writerDueDate);
		}
		
		JSONObject workflowObj = new JSONObject();
		JSONObject wfStageObj = new JSONObject();
		
		
		JSONArray assignedToArr = new JSONArray();
		JSONObject assignee = new JSONObject();
		assignedToArr.put(assignee);
		assignee.put("uid", writerUid);
		wfStageObj.put("uid", getWorkflowStageUid(ARTICLE_WORKFLOW, WORKFLOW_STAGE_ASSIGNED));
		wfStageObj.put("notify", true);
		wfStageObj.put("assigned_to", assignedToArr);
		wfStageObj.put("due_date", dateText);
		workflowObj.put("workflow_stage", wfStageObj);
		JSONObject overarchingObj = new JSONObject();
		overarchingObj.put("workflow", workflowObj);
		
		


		String jsonStr = overarchingObj.toString();
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, jsonStr);
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/article/entries/" +articleUid + "/workflow")
				.method("POST", body)
				.addHeader("api_key",  API_KEY)
				.addHeader("authorization", MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = client.newCall(request).execute();
		//System.out.println(articleUid);
		System.out.println(response.body().string());
		response.body().close() ;
	
	}




	private static Map<String, String> getWriterNameUidMap() {
		Map<String,String>writerNameUidMap = new HashMap<String,String>();
		String jsonStr = callForAllUsers();
		
		JSONObject jo = new JSONObject(jsonStr);
		JSONObject stackObj = jo.getJSONObject("stack");
		//System.out.println(stackObj);
		JSONArray collaboratorArr = stackObj.getJSONArray("collaborators");
		for (int i=0; i<collaboratorArr.length(); i++) {
			JSONObject collaboratorObj = collaboratorArr.getJSONObject(i);
			String firstName = null;
			String lastName = null;
			String uid  = collaboratorObj.getString("uid");
			if (collaboratorObj.has("first_name")) {
				firstName = collaboratorObj.getString("first_name");
			}
			if (collaboratorObj.has("last_name")) {
				lastName = collaboratorObj.getString("last_name");
			}
			String fullName = firstName + " " + lastName;
			writerNameUidMap.put(fullName, uid);
		}
		
		return writerNameUidMap;

	}





	private static List<String> getAllUsers () {
		
		List<String>list = new ArrayList<String>();
		String jsonStr = callForAllUsers();
		System.out.println(jsonStr);
		JSONObject jo = new JSONObject(jsonStr);
		JSONObject stackObj = jo.getJSONObject("stack");
		//System.out.println(stackObj);
		JSONArray collaboratorArr = stackObj.getJSONArray("collaborators");
		for (int i=0; i<collaboratorArr.length(); i++) {
			JSONObject collaboratorObj = collaboratorArr.getJSONObject(i);
			String firstName = null;
			String lastName = null;
			
			if (collaboratorObj.has("first_name")) {
				firstName = collaboratorObj.getString("first_name");
			}
			if (collaboratorObj.has("last_name")) {
				lastName = collaboratorObj.getString("last_name");
			}
			String fullName = firstName + " " + lastName;
			list.add(fullName);
		}

		return list;
	}





	private static String callForAllUsers() {
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();

		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/stacks?include_collaborators=true" )
				.method("GET", null)
				.addHeader("api_key", API_KEY)
				.addHeader("Content-Type", "application/json")
				.addHeader("authtoken", AUTH_TOKEN)

				.build();
		Response response = null;
		String jsonStr = null;
		try {
			response = client.newCall(request).execute();
			jsonStr = response.body().string();
		
			}
		
		 catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonStr;
	}
	
	private static void generateWorksheetForAssigningWriters(String projectIdStr, String outputDirStr, List<String> userList) throws IOException {
	String workbookName = "new_project_writer_assignment_" + projectIdStr;
		
		String rootDir = outputDirStr;
		//int count = getCountOfArticlesTiedToProject(projectIdStr);
		//System.out.println("Number of articles tied to the project " + count);
		//List<ArticleObj>articles = getArticlesTiedToProject(projectIdStr,count);
		//System.out.println(articles.size());

		Workbook wb = new XSSFWorkbook();
		Sheet sheet1 = wb.createSheet("q_new_project_writer_assignment");
		CreationHelper createHelper = wb.getCreationHelper();

		//create header row
		Row row = sheet1.createRow(0);

		//UI	project	original title	title	status
		row.createCell(0).setCellValue(
			     createHelper.createRichTextString("Entry Id"));
		
		row.createCell(1).setCellValue(
			     createHelper.createRichTextString("Article Title"));


		row.createCell(2).setCellValue(
			     createHelper.createRichTextString("Subject"));
		row.createCell(3).setCellValue(
			     createHelper.createRichTextString("Region"));

		row.createCell(4).setCellValue(
			     createHelper.createRichTextString("Assigned to"));
		
		row.createCell(5).setCellValue(
			     createHelper.createRichTextString("Writer due date"));
		
		int totalEntries = getCountOfArticlesTiedToProject(projectIdStr);
		List<NewProjectArticleObj> articles = getNewProjectArticleObjsByProjectId(projectIdStr, totalEntries, false);
		
		for (int i=0;i<articles.size();i++) {
			
			int rowNumber =i+1;
			Row rowC = sheet1.createRow(rowNumber);
			NewProjectArticleObj ao = articles.get(i);
			rowC.createCell(0).setCellValue(
				     createHelper.createRichTextString(ao.getArticleUid()));
			rowC.createCell(1).setCellValue(
				     createHelper.createRichTextString(ao.getArticleTitle()));
			
			rowC.createCell(2).setCellValue(
				     createHelper.createRichTextString(String.valueOf(ao.getSubject())));
			

			rowC.createCell(3).setCellValue(
				     createHelper.createRichTextString(ao.getRegion()));
			
		}
		
		
		String[] userArray = userList.toArray(new String[0]);
		XSSFDataValidationHelper validationHelper =  new XSSFDataValidationHelper((XSSFSheet) sheet1);;
		XSSFDataValidation validation = null;

		Sheet hidden = wb.createSheet("hidden");
		for (int i = 0, length= userArray.length; i < length; i++) {
		   String name =userArray[i];
		   Row rowH = hidden.createRow(i);
		   Cell cell = rowH.createCell(0);
		   cell.setCellValue(name);
		 }
		 Name namedCell = wb.createName();
		 namedCell.setNameName("hidden");
		 namedCell.setRefersToFormula("hidden!$A$1:$A$" + userArray.length);
		 XSSFDataValidationConstraint  constraint = (XSSFDataValidationConstraint) validationHelper.createFormulaListConstraint("hidden");
		 CellRangeAddressList addressList = new CellRangeAddressList(1, articles.size(), 4, 4);

		 validation =  (XSSFDataValidation) validationHelper.createValidation(constraint,
	                addressList);
		 wb.setSheetHidden(1, true);
		 sheet1.addValidationData(validation);
        
		String filePath = rootDir + FileUtil.getFileSeperator() + workbookName + ".xlsx";
		try (OutputStream fileOut = new FileOutputStream(filePath)) {
		    wb.write(fileOut);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void importWorksheetForMid(String projectIdStr, String excelSheetPath) {
		//get all lines
		List<NewProjectArticleObj> list = getNewArticleObjFromSheet(excelSheetPath);
		
		
		
         //get all mids		
		 //create mid if it doesn't exist
		//create a map of mid str/uid
		Map<String,String>midUidMap = createMidMap(list);

		//for each line, take title, find the article id within project
		for (NewProjectArticleObj npao : list) {
			String articleUid = npao.getArticleUid();
			String midStr =npao.getMid();
			String midUid = midUidMap.get(midStr);
			String collectionName=  npao.getCollectionName();
			//associate collection
			 String collUid = getCollectionUidByName (collectionName);
			 String articleDefinitionUid = createArticleDefinition(midStr,midUid);
			//associate article definition
			 
			 updateArticleWithArticleDefinitionAndCollection(articleUid, collUid, articleDefinitionUid);
		}
		//associate collection with article
		//associate mid with article
	}



	private static void updateArticleWithArticleDefinitionAndCollection(String articleUid, String collUid, String articleDefinitionUid) {

		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();

		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/article/entries?query={\"uid\":\"" + articleUid +"\"}")
				.method("GET", null)
				.addHeader("api_key", API_KEY)
				.addHeader("authorization",MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = null;
		String jsonStr = null;
		try {
			response = client.newCall(request).execute();
			jsonStr = response.body().string();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(jsonStr);
		JSONObject jo = new JSONObject(jsonStr);
		JSONArray entriesArray = jo.getJSONArray("entries");
		for (int j=0; j<entriesArray.length(); j++) {
			JSONObject item = entriesArray.getJSONObject(j);
			if  (collUid!=null) {
				
				
				JSONArray collArr = new JSONArray();
				
				if (item.has("collections") && !item.isNull("collections")) {
					collArr = item.getJSONArray("collections");
				}


				JSONObject cont = new JSONObject();
				cont.put("uid", collUid);
				cont.put("_content_type_uid", "collection");
				collArr.put(cont);

				item.put("collections",collArr);

			}

			if (articleDefinitionUid!=null) {
				
				//if the article has previous article definitions , then append them
				JSONArray apdArr = new JSONArray();

				if (item.has("article_definitions") && !item.isNull("article_definitions")) {
					apdArr = item.getJSONArray("article_definitions");
				}

				JSONObject cont = new JSONObject();
				cont.put("uid", articleDefinitionUid);
				cont.put("_content_type_uid", "article_product_definition");
				apdArr.put(cont);

				item.put("article_definitions",apdArr);
			}

			//item.put("do_not_use", true);
			ContentstackUtil.updateArticle(articleUid , item);

		}

	}

	private static String createArticleDefinition(String mid, String midUid) {
		JSONObject jo = new JSONObject();
		JSONObject entry = new JSONObject();

		JSONArray bookArr = new JSONArray();

		JSONObject bookObj = new JSONObject();

		bookObj.put("uid", midUid);
		bookObj.put("_content_type_uid", "book_source");
		bookArr.put(bookObj);
		entry.put("title_source", bookArr);

		//create placeholder an for now until we receive the an
		String an = UUID.randomUUID().toString();
	
		entry.put("an", an);


		String title = mid;

		entry.put("title", title);

		jo.put("entry", entry);

		String jsonStr= jo.toString();


		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, jsonStr);
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/article_product_definition/entries?locale=en-us")
				.method("POST", body)
				.addHeader("api_key",  API_KEY)
				.addHeader("authorization", MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = null;
		String responseStr = null;

		try {
			response = client.newCall(request).execute();
			responseStr = response.body().string();

		} catch (IOException e) {
			e.printStackTrace();
		}

		JSONObject responseObj = new JSONObject(responseStr);
		JSONObject entryObj = responseObj.getJSONObject("entry");
		String uid = entryObj.getString("uid");
		response.body().close() ;
		return uid;
	}

	private static String getCollectionUidByName(String collectionName) {
		if (collectionName==null) {
			return null;
		}
		else if (collectionName.isEmpty()) {
			return null;
		}
		
		String collectionUid = null;
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/collection/entries?query={\"title\":\"" + collectionName +"\"}")
				.method("GET", null)
				.addHeader("api_key", API_KEY)
				.addHeader("authorization",MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = null;
		String jsonStr = null;
		try {
			response = client.newCall(request).execute();
			jsonStr = response.body().string();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("json str  " + jsonStr);
		JSONObject jo = new JSONObject(jsonStr);
		JSONArray entriesArray = jo.getJSONArray("entries");
		for (int j=0; j<entriesArray.length(); j++) {
			JSONObject item = entriesArray.getJSONObject(j);
			collectionUid = item.getString("uid");

		}
		response.body().close();
		return collectionUid;
	}

	private static Map<String, String> createMidMap(List<NewProjectArticleObj> list) {

		Map<String,String>map = new HashMap<String,String>();
		for (NewProjectArticleObj npao : list) {
			String midUid = null;
			try {
				midUid = selectOrCreateMid(npao.getMid());
			} catch (IOException e) {
				e.printStackTrace();
			}
			map.put(npao.getMid(), midUid);
		
		}
		return map;
	}

	private static String selectOrCreateMid(String mid) throws IOException {

		String midUid = retrieveMid(mid);
		if (midUid!=null) {
			return midUid;
		}
		else {
			return createMid(mid);
		}
	}

	private static String createMid(String mid) {

		JSONObject jo = new JSONObject();
		JSONObject entry = new JSONObject();


		String title = mid;
		entry.put("title", title);
		entry.put("is_new",true);
		entry.put("mid",mid);

		jo.put("entry", entry);

		String jsonStr= jo.toString();


		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, jsonStr);
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/book_source/entries?locale=en-us")
				.method("POST", body)
				.addHeader("api_key",  API_KEY)
				.addHeader("authorization", MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = null;
		String responseStr = null;

		try {
			response = client.newCall(request).execute();
			responseStr = response.body().string();

		} catch (IOException e) {
			e.printStackTrace();
		}

		JSONObject responseObj = new JSONObject(responseStr);
		JSONObject entryObj = responseObj.getJSONObject("entry");
		String uid = entryObj.getString("uid");
		response.body().close() ;
		return uid;
	}

	private static String retrieveMid(String mid) throws IOException {

		return ContentstackUtil.getTitleSourceUid(mid);
	}

	private static List<NewProjectArticleObj> getNewArticleObjFromSheet(String excelSheetPath) {
		List<NewProjectArticleObj> list = new ArrayList<NewProjectArticleObj> ();

		XSSFWorkbook wb  = null;

		try {
			wb = new XSSFWorkbook(new File(excelSheetPath));
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

			//UI	project	original title	title	status	web_source	webpage_url	filename
			String mid = getStringContent(row,0);
			String articleUid = getStringContent(row,1);
			String articleTitle = getStringContent(row,3);
			String collectionName= getStringContent(row,4);
			String rsAN =  getStringContent(row,5);


			NewProjectArticleObj npao = new NewProjectArticleObj();
			npao.setMid(mid);
			npao.setArticleUid(articleUid);
			npao.setArticleTitle(articleTitle);
			npao.setRsAn(rsAN);
			npao.setCollectionName(collectionName);
			list.add(npao);		
		}

		return list;
	
	}
	
	
	private static List<NewProjectArticleObj> getNewArticleObjListForAssignment(String excelSheetPath) {
		List<NewProjectArticleObj> list = new ArrayList<NewProjectArticleObj> ();

		XSSFWorkbook wb  = null;

		try {
			wb = new XSSFWorkbook(new File(excelSheetPath));
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
			

			String articleUid = getStringContent(row,0);
			//String articleTitle = getStringContent(row,1);
			//String subject = getStringContent(row,2);
			//String region= getStringContent(row,3);
			String assignedTo =  getStringContent(row,4);

		    Date writerDueDate = getDateContent(row, 5);
			
		    /*SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			String dateText = df2.format(date);
			String newDate= dateText;
	*/

		    

			NewProjectArticleObj npao = new NewProjectArticleObj();
			npao.setArticleUid(articleUid);

			npao.setAssignedTo(assignedTo);
			npao.setWriterDueDate(writerDueDate);
			list.add(npao);		
		}

		return list;
	
	}

	private static void generateWorksheetForMidImport(String projectIdStr, String outputDirStr) throws IOException {
	String workbookName = "new_project_mid_import_" + projectIdStr;
		
		String rootDir = outputDirStr;
		//int count = getCountOfArticlesTiedToProject(projectIdStr);
		//System.out.println("Number of articles tied to the project " + count);
		//List<ArticleObj>articles = getArticlesTiedToProject(projectIdStr,count);
		//System.out.println(articles.size());

		Workbook wb = new XSSFWorkbook();
		Sheet sheet1 = wb.createSheet("q_new_project_mid_import");
		CreationHelper createHelper = wb.getCreationHelper();

		//create header row
		Row row = sheet1.createRow(0);

		//UI	project	original title	title	status
		row.createCell(0).setCellValue(
			     createHelper.createRichTextString("MID"));
		
		row.createCell(1).setCellValue(
			     createHelper.createRichTextString("Entry Id"));
		row.createCell(2).setCellValue(
			     createHelper.createRichTextString("Article Title"));

		row.createCell(3).setCellValue(
			     createHelper.createRichTextString("Collection Name"));
		row.createCell(4).setCellValue(
			     createHelper.createRichTextString("RS AN"));
		
		
		int totalEntries = getCountOfArticlesTiedToProject(projectIdStr);
		List<NewProjectArticleObj> articles = getNewProjectArticleObjsByProjectId(projectIdStr, totalEntries, true);
		
		for (int i=0;i<articles.size();i++) {
			
			int rowNumber =i+1;
			Row rowC = sheet1.createRow(rowNumber);
			NewProjectArticleObj ao = articles.get(i);
			rowC.createCell(1).setCellValue(
				     createHelper.createRichTextString(ao.getArticleUid()));
			rowC.createCell(2).setCellValue(
				     createHelper.createRichTextString(ao.getArticleTitle()));
			

			
		}
		String filePath = rootDir + FileUtil.getFileSeperator() + workbookName + ".xlsx";
		try (OutputStream fileOut = new FileOutputStream(filePath)) {
		    wb.write(fileOut);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static List<NewProjectArticleObj> getNewProjectArticleObjsByProjectId(String projectId, int totalEntries, boolean includeReused) {
		List<NewProjectArticleObj>articles = new ArrayList<NewProjectArticleObj>();

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
			String jsonStr = null;
			try {
				Response response = client.newCall(request).execute();

				jsonStr = response.body().string();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//System.out.println(jsonStr);
			JSONObject jo = new JSONObject(jsonStr);
			JSONArray entriesArray = jo.getJSONArray("entries");
			for (int j=0; j<entriesArray.length(); j++) {
				JSONObject item = entriesArray.getJSONObject(j);
				//System.out.println(item);
				NewProjectArticleObj ao = new NewProjectArticleObj();
				ao.setArticleUid(item.getString("uid"));
				ao.setArticleTitle(item.getString("title"))	;
				System.out.println(ao.getArticleTitle());
				//target word count
				if (item.has("original_requested_word_count")) {
					if (!item.isNull("original_requested_word_count")) {
						int wordCount = item.getInt("original_requested_word_count");
						ao.setTargetWordCount(wordCount);
					}
				}
				//subject
				if (item.has("collection_debate_category") && !item.isNull("collection_debate_category")) {
					String subject = item.getString("collection_debate_category");
					ao.setSubject(subject);

				}
				if (item.has("region")&& !item.isNull("region")) {
					String region = item.getString("region");
					ao.setRegion(region);
				}

				if (includeReused) {
					articles.add(ao);
				}

				else {
					boolean reused = false;
					if (item.has("reused") && !item.isNull("reused") ) {
						reused=  item.getBoolean("reused");
					}
					if (!reused) {
						articles.add(ao);
					}
				}
			}

		}

		return articles;
	}

	public static void main2 (String [] args) throws IOException {
		String projectIdStr = "blt2e6c8899030ea279";
		String projectSheetPath = "/Users/mpamuk/Desktop/CutlistForNewContentProject.xlsx";
		//importNewProject(projectIdStr,projectSheetPath);
		
		String outputDirStr = "/Users/mpamuk/Desktop/cms_pov/newproject";
		//generateWorksheetForMidImport(projectIdStr, outputDirStr);
		
		
		String midExcelSheet = "/Users/mpamuk/Desktop/cms_pov/newproject/new_project_mid_import_blt2e6c8899030ea279.xlsx";
		//importWorksheetForMid(projectIdStr, midExcelSheet);
		List<String> userList = getAllUsers();		
		//generateWorksheetForAssigningWriters(projectIdStr, outputDirStr, userList);
		
		String excelSheetPath = "/Users/mpamuk/Desktop/cms_pov/newproject/new_project_writer_assignment_blt0a8235603387da8e.xlsx";
		
       //importWorksheetForAssigningWriters(projectIdStr, excelSheetPath);

		
		
	}
	
	
	public static void mainStage (String [] args) throws IOException{
		String articleUid = "bltcf67693205a06b80";
		String availableWfStageUid = null;
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		//MediaType mediaType = MediaType.parse("application/json");
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/workflows")
				.method("GET", null)
				.addHeader("api_key", API_KEY)
				.addHeader("authorization",MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = client.newCall(request).execute();
		String jsonStr = response.body().string();
		//System.out.println(jsonStr);
		JSONObject jo = new JSONObject(jsonStr);
		JSONArray entriesArray = jo.getJSONArray("workflows");
		for (int i=0; i<entriesArray.length(); i++) {
			JSONObject item = entriesArray.getJSONObject(i);
			String wfName = item.getString("name");
			if (wfName.equals(ARTICLE_WORKFLOW)) {
				JSONArray wfStagesArray = item.getJSONArray("workflow_stages");
				for (int j=0; j<wfStagesArray.length(); j++) {
					JSONObject wfStageObj = wfStagesArray.getJSONObject(j);
					String stageName = wfStageObj.getString("name");
					if (stageName.equals(WORKFLOW_STAGE_AVAILABLE)) {
						availableWfStageUid = wfStageObj.getString("uid");
					}
				}
			}

		}
		
		JSONObject workflowObj = new JSONObject();
		JSONObject wfStageObj = new JSONObject();
		wfStageObj.put("uid", availableWfStageUid);
		workflowObj.put("workflow_stage", wfStageObj);
		JSONObject overarchingObj = new JSONObject();
		overarchingObj.put("workflow", workflowObj);
		
		jsonStr = overarchingObj.toString();
		client = new OkHttpClient().newBuilder()
				  .build();
				MediaType mediaType = MediaType.parse("application/json");
				RequestBody body = RequestBody.create(mediaType, jsonStr);
				request = new Request.Builder()
				  .url("https://api.contentstack.io/v3/content_types/article/entries/" +articleUid + "/workflow")
				  .method("POST", body)
				  .addHeader("api_key",  API_KEY)
				  .addHeader("authorization", MANAGEMENT_TOKEN)
				  .addHeader("Content-Type", "application/json")
				  .build();
				response = client.newCall(request).execute();
				System.out.println(response.body().string());
				response.body().close() ;
		
	}
	
	

	private static void importNewProject(String projectIdStr, String newProjectSheetPath) throws IOException {
		// TODO Auto-generated method stub
		List<NewProjectArticleObj> articles = readArticleListFromWorkbook(newProjectSheetPath);


		//parsers++
		//create topic if pertinent++
		//create article++
		//set workflow
		//set project++
		//set product++
		//set topic++

		String articleContentTypeUid = getArticleContentUid();
		String availableWorkflowStageUid = getWorkflowStageUid(ARTICLE_WORKFLOW, WORKFLOW_STAGE_AVAILABLE); 
		String approvalForProductWorkflowStageUid = getWorkflowStageUid(ARTICLE_WORKFLOW, WORKFLOW_STAGE_APPROVED_FOR_PRODUCT); 


		for (NewProjectArticleObj article : articles) {
			//create new topic if pertinent
			String topicId = createTopicIfNew(article.getTopicType(), article.getExistingTopicId(), article.getNewTopicTitle());
			String productId = getProductUid(article.getProductCode());
			String topicTypeUidStr = getTopicTypeUid(article.getTopicType());

			boolean isReuseArticle = article.getReuseEntryId()!=null && !article.getReuseEntryId().isEmpty();
			if (isReuseArticle) {
				article.setArticleUid(article.getReuseEntryId());
				updateExistingArticleWithProjectandProduct(article, productId, projectIdStr);
			}
			else {
				String articleUid = createArticle(article, topicId, topicTypeUidStr, productId, projectIdStr);
				article.setArticleUid(articleUid);

			}
			if (isReuseArticle) {
				setWorkflowState(article.getArticleUid(), articleContentTypeUid, approvalForProductWorkflowStageUid);
			}
			else  {
				setWorkflowState(article.getArticleUid(), articleContentTypeUid, availableWorkflowStageUid);
			}
		}
	}

	private static void updateExistingArticleWithProjectandProduct(NewProjectArticleObj article, String productId, String projectIdStr) {

		String articleUid = article.getArticleUid();
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();

		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/article/entries?query={\"uid\":\"" + articleUid +"\"}")
				.method("GET", null)
				.addHeader("api_key", API_KEY)
				.addHeader("authorization",MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = null;
		String jsonStr = null;
		try {
			response = client.newCall(request).execute();
			jsonStr = response.body().string();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(jsonStr);
		JSONObject jo = new JSONObject(jsonStr);
		JSONArray entriesArray = jo.getJSONArray("entries");
		for (int j=0; j<entriesArray.length(); j++) {
			JSONObject item = entriesArray.getJSONObject(j);

			item.put("reused",true);
			JSONArray projectArr = new JSONArray();

			JSONObject projectObj = new JSONObject();

			projectObj.put("uid", projectIdStr);
			projectObj.put("_content_type_uid", "project");
			projectArr.put(projectObj);
			item.put("current_project", projectArr);
			
			JSONArray productArr = new JSONArray();

			if (item.has("products") && !item.isNull("products")) {
				productArr =item.getJSONArray("products");
				
			}
			JSONObject productObj = new JSONObject();

			productObj.put("uid", productId);
			productObj.put("_content_type_uid", "product");
			productArr.put(productObj);
			item.put("products", productArr);

			//item.put("do_not_use", true);
			ContentstackUtil.updateArticle(articleUid , item);

		}
		
		

		

	
	}

	private static String getArticleContentUid() {
		return "article";
	}

	private static String createArticle(NewProjectArticleObj article, String topicId, String topicTypeUid, String productId, String projectIdStr) throws IOException {
		JSONObject jo = new JSONObject();
		JSONObject entry = new JSONObject();
		
		entry.put("title",article.getArticleTitle());
		
		entry.put("original_requested_word_count", article.getTargetWordCount());
		//TO DO : this may have to become more general
		
		if (article.getSubject()!=null) {
			if (!article.getSubject().isEmpty()) {
				entry.put("collection_debate_category", article.getSubject());
			}
		}
		
		JSONArray projectArr = new JSONArray();

		JSONObject projectObj = new JSONObject();

		projectObj.put("uid", projectIdStr);
		projectObj.put("_content_type_uid", "project");
		projectArr.put(projectObj);
		entry.put("current_project", projectArr);
		
		
		
		JSONArray productArr = new JSONArray();

		JSONObject productObj = new JSONObject();

		productObj.put("uid", productId);
		productObj.put("_content_type_uid", "product");
		productArr.put(productObj);
		entry.put("products", productArr);
		
		
		entry.put("reused",false);

		
		
		JSONArray topicArr = new JSONArray();

		JSONObject topicObj = new JSONObject();

		topicObj.put("uid", topicId);
		topicObj.put("_content_type_uid", topicTypeUid);
		topicArr.put(topicObj);
		entry.put("topic", topicArr);
		
		entry.put("po_notes", article.getProjectOwnerNotes());

		
		
		entry.put("region",parseRegion(article.getRegion()));
		entry.put("article_work_type" , parseWorkType(article.getWorkType()));

		
		entry.put("urgent", article.isUrgent());

		entry.put("update_cycle", parseUpdateCycle(article.getUpdateCycle()));
		entry.put("usage_note", article.getUsageNote());
		entry.put("audience", parseAudience(article.getAudience()));

		//workflowStage;  
		// reuseEntryId;
	
		//project id
		
		entry.put("image_status", parseImageStatus(article.getImageStatus()));
		entry.put("bulk_project_image_placement", article.getBulkImagePlacement());
		entry.put("art_spec_summary", article.getArtSpec());

        
        
		jo.put("entry", entry);
		
		String jsonStr= jo.toString();
		System.out.println( " JSON " +jsonStr);
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
				MediaType mediaType = MediaType.parse("application/json");
				RequestBody body = RequestBody.create(mediaType, jsonStr);
				Request request = new Request.Builder()
				  .url("https://api.contentstack.io/v3/content_types/article/entries?locale=en-us")
				  .method("POST", body)
				  .addHeader("api_key",  API_KEY)
				  .addHeader("authorization", MANAGEMENT_TOKEN)
				  .addHeader("Content-Type", "application/json")
				  .build();
				Response response = client.newCall(request).execute();
				String responseStr = response.body().string();
				JSONObject responseObj = new JSONObject(responseStr);
				System.out.println(responseStr);
				JSONObject entryObj = responseObj.getJSONObject("entry");
				String uid = entryObj.getString("uid");
				response.body().close() ;
				return uid;
				

	}

	private static String parseImageStatus(String imageStatus) {
		if (imageStatus==null) {
			return null;
		}
		else if (imageStatus.isEmpty()) {
			return null;
		}
		else if (imageStatus.equalsIgnoreCase("Needs Image")) {
			return "Needs image";
		}
		else if (imageStatus.equalsIgnoreCase("Needs update to existing")) {
			return "Needs update to existing";
		}
		else if (imageStatus.equalsIgnoreCase("Needs RTE image placed")) {
			return "Needs RTE image placed";
		}
		else if (imageStatus.equalsIgnoreCase("Complete inside RTE")) {
			return "Complete inside RTE";
		}
		else if (imageStatus.equalsIgnoreCase("No image needed")) {
			return "No image needed";
		}
		return null;
	}

	private static String parseAudience(String audience) {

		if (audience==null) {
			return null;
		}
		else if (audience.isEmpty()) {
			return null;
		}
		else if (audience.equalsIgnoreCase("K-5")) {
			return "K-5";
		}
		else if (audience.equalsIgnoreCase("Public Schools")) {
			return "Public Schools";
		}
		else if (audience.equalsIgnoreCase("Consumer Preferred")) {
			return "Consumer Preferred";
		}
		else if (audience.equalsIgnoreCase("Corporate Preferred")) {
			return "Corporate Preferred";
		}
		else if (audience.equalsIgnoreCase("Academic Preferred")) {
			return "Academic Preferred";
		}
		return null;
	}

	private static String parseUpdateCycle(String updateCycle) {
		
		//2-year, 3-year,5-year,6-month,annually,monthly, weekly,eod
		
		if (updateCycle==null) {
			return null;
		}
		else if (updateCycle.isEmpty()) {
			return null;
		}
		else if (updateCycle.equalsIgnoreCase("2-year")) {
			return "2-year";
		}
		else if (updateCycle.equalsIgnoreCase("3-year")) {
			return "3-year";
		}
		else if (updateCycle.equalsIgnoreCase("5-year")) {
			return "5-year";
		}
		else if (updateCycle.equalsIgnoreCase("6-month")) {
			return "6-month";
		}
		else if (updateCycle.equalsIgnoreCase("annually")) {
			return "Annually";
		}
		else if (updateCycle.equalsIgnoreCase("monthly")) {
			return "Monthly";
		}
		else if (updateCycle.equalsIgnoreCase("weekly")) {
			return "Weekly";
		}
		else if (updateCycle.equalsIgnoreCase("EOD")) {
			return "EOD";
		}
		else if (updateCycle.equalsIgnoreCase("Never")) {
			return "Never";
		}
		return null;
	}

	private static String parseWorkType(String workType) {
		if (workType==null) {
			return null;
		}
		else if (workType.isEmpty()) {
			return null;
		}
		else if (workType.equalsIgnoreCase("update")) {
			return "update";
		}
		else if (workType.equalsIgnoreCase("new")) {
			return "new";
		}
		return null;
	}

	private static String parseRegion(String region) {
		// TODO Auto-generated method stub
		
		if (region==null) {
			return null;
		}
		else if (region.isEmpty()) {
			return null;
		}
		else if (region.equalsIgnoreCase("All")) {
			return region;
		}
		else if (region.equalsIgnoreCase("US")) {
			return  region;
		}
		else if (region.equalsIgnoreCase("UK")) {
			return region;
		}
		else if (region.equalsIgnoreCase("AUS")) {
			return "Australia/New Zealand";
		}
		else if (region.equalsIgnoreCase("CAN")) {
			return "Canada";
		}
		return null;
	}

	private static String getProductUid(String productCode) throws IOException {
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/product/entries?query={\"title\":\"" + productCode +"\"}")
				.method("GET", null)
				.addHeader("api_key", API_KEY)
				.addHeader("authorization",MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = client.newCall(request).execute();
		String jsonStr = response.body().string();

		JSONObject jo = new JSONObject(jsonStr);
		JSONArray entriesArray = jo.getJSONArray("entries");
		for (int j=0; j<entriesArray.length(); j++) {
			JSONObject item = entriesArray.getJSONObject(j);

			String uid = item.getString("uid");
			response.body().close();
			return uid;

		}
		return null;
	}

	private static String createTopicIfNew(String topicType, String existingTopicId, String newTopicTitle ) throws IOException {
         if (existingTopicId!=null) {
        	 return existingTopicId;
         }
         else if (newTopicTitle!=null) {
        	 String newTopicId = createNewTopic(topicType, newTopicTitle);
        	 return newTopicId;
         }
         return null;
		
	}

	private static String createNewTopic(String topicType, String newTopicTitle) throws IOException {

		String topicTypeUidStr = getTopicTypeUid(topicType);
		
		JSONObject jo = new JSONObject();
		JSONObject entry = new JSONObject();
		
		entry.put("title",newTopicTitle);
		
		entry.put("topic_title", newTopicTitle);
		jo.put("entry", entry);
		String jsonStr= jo.toString();

		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
				MediaType mediaType = MediaType.parse("application/json");
				RequestBody body = RequestBody.create(mediaType, jsonStr);
				Request request = new Request.Builder()
				  .url("https://api.contentstack.io/v3/content_types/" + topicTypeUidStr + "/entries?locale=en-us")
				  .method("POST", body)
				  .addHeader("api_key",  API_KEY)
				  .addHeader("authorization", MANAGEMENT_TOKEN)
				  .addHeader("Content-Type", "application/json")
				  .build();
				Response response = client.newCall(request).execute();
				//System.out.println(response.body().string());
				response.body().close() ;
				
		return getTopicUidByTitle(topicTypeUidStr, newTopicTitle );
	}

	private static String getTopicTypeUid(String topicType) {
		String topicTypeUidStr= null;
		if (topicType==null) {
			return null;
		}
		if (topicType.equalsIgnoreCase("Debate")) {
			topicTypeUidStr = "topic_debate";
		}
		else if (topicType.equalsIgnoreCase("Biography")) {
			topicTypeUidStr = "topic_biography";
		}
		else if(topicType.equalsIgnoreCase("Business")) {
			topicTypeUidStr = "topic_business";
		}
		
		else if(topicType.equalsIgnoreCase("Education")) {
			topicTypeUidStr = "topic_education_resource";
		}
		
		else if(topicType.equalsIgnoreCase("Health")) {
			topicTypeUidStr = "topic_health";
		}
		
		else if(topicType.equalsIgnoreCase("History")) {
			topicTypeUidStr = "topic_history";
		}
		
		else if(topicType.equalsIgnoreCase("Literature")) {
			topicTypeUidStr = "topic_literature";
		}
		
		else if(topicType.equalsIgnoreCase("Science")) {
			topicTypeUidStr = "topic_science";
		}
		else if(topicType.equalsIgnoreCase("Social Sciences")) {
			topicTypeUidStr = "topic_social_sciences";
		}
		return topicTypeUidStr;
	}

	private static String getTopicUidByTitle(String topicTypeUidStr, String newTopicTitle) throws IOException {
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/" + topicTypeUidStr + "/entries?query={\"title\":\"" + newTopicTitle +"\"}")
				.method("GET", null)
				.addHeader("api_key", API_KEY)
				.addHeader("authorization",MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = client.newCall(request).execute();
		String jsonStr = response.body().string();

		JSONObject jo = new JSONObject(jsonStr);
		JSONArray entriesArray = jo.getJSONArray("entries");
		for (int j=0; j<entriesArray.length(); j++) {
			JSONObject item = entriesArray.getJSONObject(j);

			String uid = item.getString("uid");
			response.body().close();
			return uid;

		}
		return null;
	}

	private static List<NewProjectArticleObj> readArticleListFromWorkbook(String newProjectSheetPath) {
		List<NewProjectArticleObj> list = new ArrayList<NewProjectArticleObj> ();

		XSSFWorkbook wb  = null;

		try {
			wb = new XSSFWorkbook(new File(newProjectSheetPath));
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
			
			
			// mid articleTitle  targetWordCount  subject projectId
			
			
			String articleTitle = getStringContent(row,0);
			int targetWordCount = parseInt(row, 1);
			String subject = getStringContent(row,2);
			
			// productCode topicType existingTopicId newTopicTitle projectOwnerNotes
			

			String productCode = getStringContent(row,3);
			String topicType = getStringContent(row,4);
			String existingTopicId = getStringContent(row,5);
			String newTopicTitle = getStringContent(row,6);
			String projectOwnerNotes = getStringContent(row,7);
			String region = getStringContent(row,8);
	

			String urgentStr = getStringContent(row,9);
			Boolean urgent = parseUrgent(urgentStr);
			

			//updateCycle usageNote audience imageStatus bulkImagePlacement
			
			String updateCycle = getStringContent(row,10);
			String usageNote  = getStringContent(row,11);
			String audience  = getStringContent(row,12);
			String imageStatus  = getStringContent(row,13);
			String bulkImagePlacement  = getStringContent(row,14);

			//artSpec workflowStage reuseEntryId rsAn
			
			
			String artSpec = getStringContent(row,15);
			String reuseEntryId  = getStringContent(row,16);
			
			
			NewProjectArticleObj npao = new NewProjectArticleObj( articleTitle, targetWordCount, subject,  productCode, topicType, existingTopicId, newTopicTitle, projectOwnerNotes,  region, UPDATE, urgent, updateCycle, usageNote, audience, imageStatus, bulkImagePlacement, artSpec, reuseEntryId);
			list.add(npao);


		}

		return list;			
		
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

	/*
	private static Date parseDate(Row row, int index) {
        Date date = row.getCell(index).getDateCellValue();
        return date;
	}
 	*/
	
	private static Boolean parseUrgent(String str) {
		if (str==null) {
			return false;
		}


		if (str.equalsIgnoreCase("y") ) {
			return true;
		}
		else if (str.equalsIgnoreCase("n") ) {
			return false;
		}
		else {
			return false;
		}

	}

	private static int parseInt(Row row, int index) {
		// TODO Auto-generated method stub
    	Cell numericCell = row.getCell(index);
    	if (numericCell!=null) {
    		double num = numericCell.getNumericCellValue();
    		int value = (int)num;
    		return value;
    	}
    	
    	return 0;

	}

	private static String getStringContent(Row row, int cellIndex) {
		// TODO Auto-generated method stub
		Cell cell = row.getCell(cellIndex);
		if (cell!=null){
			return cell.getRichStringCellValue().getString();
		}
		return null;
	}
	
	private static Date getDateContent(Row row, int cellIndex) {
		// TODO Auto-generated method stub
		Cell cell = row.getCell(cellIndex);
		if (cell!=null){
			return row.getCell(cellIndex).getDateCellValue();
		}
		return null;
	}

}
