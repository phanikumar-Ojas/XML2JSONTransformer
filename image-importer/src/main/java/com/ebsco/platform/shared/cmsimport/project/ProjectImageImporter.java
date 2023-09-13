package com.ebsco.platform.shared.cmsimport.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import com.ebsco.platform.shared.cmsimport.utilities.FileUtil;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class ProjectImageImporter {


	private static final String OUTPUT_DIR = "outputDir";

	private static final String ASSET_FOLDER = "assetFolder";

	private static final String IMAGE_SHEET_PATH = "imageSheetPath";

	private static final String IMAGE_DIR = "imageDir";

	private static final String PROJECT_ID = "projectId";

	private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");

	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");




	public static void main (String [] args) throws IOException {
		// create Options object
		Options options = new Options();

		// add t option


		options.addOption(Option.builder("g").longOpt("generate-worksheet")
				.desc("generate image worksheet")
				.build());

		options.addOption(Option.builder("i").longOpt("import-worksheet")
				.desc("import image worksheet")
				.build());



		options.addOption(Option.builder("p").longOpt(PROJECT_ID).hasArg()
				.desc("project id")
				.build());

		options.addOption(Option.builder("o").longOpt(OUTPUT_DIR).hasArg()
				.desc("output directory for the shell image worksheet")
				.build());


		options.addOption(Option.builder("d").longOpt(IMAGE_DIR).hasArg()
				.desc("image directory for the new images")
				.build());

		options.addOption(Option.builder("s").longOpt(IMAGE_SHEET_PATH).hasArg()
				.desc("path to image sheet")
				.build());

		options.addOption(Option.builder("a").longOpt(ASSET_FOLDER).hasArg()
				.desc("asset folder name")
				.build());



		//HelpFormatter formatter = new HelpFormatter();
		//formatter.printHelp("java -jar image.jar", options);

		CommandLineParser parser = new DefaultParser();

		//String[] args2 = new String[]{ "-g" };

		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("import-worksheet")) {

				if (!line.hasOption(PROJECT_ID)) {
					System.out.println("You need to supply a project id with the p flag");
				}

				if (!line.hasOption(IMAGE_DIR)) {
					System.out.println("You need to supply an image directory with the d flag");
				}

				if (!line.hasOption(IMAGE_SHEET_PATH)) {
					System.out.println("You need to supply a path to the image sheet with the s flag");
				}
				

				if (!line.hasOption(ASSET_FOLDER)) {
					System.out.println("You need to supply an asset folder");
				}

				if (line.hasOption(PROJECT_ID)&&line.hasOption(IMAGE_DIR)&&line.hasOption(IMAGE_SHEET_PATH)&&line.hasOption(ASSET_FOLDER)) {
					String projectIdStr = line.getOptionValue(PROJECT_ID);
					String imageDirStr = line.getOptionValue(IMAGE_DIR);
					String imageSheetPath = line.getOptionValue(IMAGE_SHEET_PATH);
					String assetFolderName = line.getOptionValue(ASSET_FOLDER);
					importUpdateImageProject(assetFolderName, imageDirStr, projectIdStr, imageSheetPath);

				}
			}


			else if (line.hasOption("generate-worksheet")) {

				if (!line.hasOption(PROJECT_ID)) {
					System.out.println("You need to supply a project name with the p flag");
				}

				if (!line.hasOption(OUTPUT_DIR)) {
					System.out.println("You need to supply an output directory with the o flag");
				}

				if (line.hasOption(PROJECT_ID)&&line.hasOption(OUTPUT_DIR)) {
					String projectIdStr = line.getOptionValue(PROJECT_ID);
					String outputDirStr = line.getOptionValue(OUTPUT_DIR);
					String excelSheetPath = ImageProjectShellGenerator.generateShellExcel(projectIdStr,outputDirStr);
					if (new File(excelSheetPath).exists()) {
						System.out.println("Output Excel sheet written to " + excelSheetPath);
						System.out.println("Program completed successfully");
					}
				}
			}

			else {
				System.out.println("You need to either specify -g for generating worksheet or -i to import worksheet");
			}
		}
		catch (ParseException exp) {
			System.out.println("Unexpected exception:" + exp.getMessage());
		}
	}




	public static void importUpdateImageProject (String assetFolderName , String imageDir, String projectId, String pathToImageSheet) throws IOException {
		List<ProjectImageObj>list = readImageListFromWb(pathToImageSheet);


		String projectAssetFolderUid = getAssetFolderUid(assetFolderName);

		//System.out.println("Folder uid " + projectAssetFolderUid);

		//upload assets

		uploadAssets(list,imageDir,projectAssetFolderUid);

		//to do getproject
		//String projectUid = getProjectUid(projectId);

		//to do add metadata to image obj
		addMetadataOfImages(list);

		//to do associate with article

		updateArticleWithImageReferences(list);

	}


	private static String getAssetFolderUid(String assetFolderName) throws IOException {
		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
				Request request = new Request.Builder()
				  .url("https://api.contentstack.io/v3/assets?query={\"is_dir\": true, \"name\": \"" + assetFolderName + "\"}")
				  .method("GET", null)
				  .addHeader("api_key", API_KEY)
				  .addHeader("authorization",MANAGEMENT_TOKEN )
				  .addHeader("Content-Type", "application/json")
				  .build();
				Response response = client.newCall(request).execute();
				String responseStr = response.body().string();

				
				JSONObject jo = new JSONObject(responseStr);
				JSONArray entriesArray = jo.getJSONArray("assets");
				for (int j=0; j<entriesArray.length(); j++) {
				    JSONObject item = entriesArray.getJSONObject(j);
				    String uid = item.getString("uid");
				    return uid;
				   // System.out.println(filename  + " " + uid);
		
				}
				return null;
	}

	private static void updateArticleWithImageReferences(List<ProjectImageObj> list) throws IOException {
		// TODO Auto-generated method stub


		for (ProjectImageObj upio : list) {

			//get article uid

			//get article json

			//update article json with image ids




			OkHttpClient client = new OkHttpClient().newBuilder()
					.build();
			MediaType mediaType = MediaType.parse("application/json");
			Request request = new Request.Builder()
					.url("https://api.contentstack.io/v3/content_types/article/entries?query={\"uid\":\"" + upio.getArticleId() +"\"}")
					.method("GET", null)
					.addHeader("api_key", API_KEY)
					.addHeader("authorization",MANAGEMENT_TOKEN)
					.addHeader("Content-Type", "application/json")
					.build();
			Response response = client.newCall(request).execute();
			String jsonStr = response.body().string();
			//System.out.println("json str  " + jsonStr);
			JSONObject jo = new JSONObject(jsonStr);
			JSONArray entriesArray = jo.getJSONArray("entries");
			for (int j=0; j<entriesArray.length(); j++) {
				JSONObject item = entriesArray.getJSONObject(j);
				JSONArray ja = new JSONArray();

				if (item.has("associated_images") && !item.isNull("associated_images")){
					JSONArray associatedImages = item.getJSONArray("associated_images");
					
					for (int i=0;i<associatedImages.length();i++) {
						JSONObject img = associatedImages.getJSONObject(i);
						ja.put(img.getString("uid"));
					}
				}


				ja.put(upio.getAssetUid());
				item.put("associated_images",ja);
				String uid = item.getString("uid");

				JSONObject entry= new JSONObject();
				entry.put("entry", item);
				//System.out.println("ENTRY " + entry.toString());
				mediaType = MediaType.parse("application/json");
				RequestBody body = RequestBody.create(mediaType, entry.toString());
				request = new Request.Builder()
						.url("https://api.contentstack.io/v3/content_types/article/entries/" + uid)
						.method("PUT", body)
						.addHeader("api_key", API_KEY)
						.addHeader("authorization", MANAGEMENT_TOKEN)
						.addHeader("Content-Type", "application/json")
						.build();
				try {
					response = client.newCall(request).execute();
					String responseStr = response.body().string();

					//System.out.println(responseStr);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}



		}
	}




	private static void addMetadataOfImages(List<ProjectImageObj> list) throws IOException {

		for (ProjectImageObj imo : list) {
		
			if(imo.getFileName()==null) {
				continue;
			}
			JSONObject jo = new JSONObject();
			JSONObject entry = new JSONObject();
			String fileExt = extractFileExtension(imo.getFileName());
			String title = imo.getAssetUid() +  "." + fileExt;


			entry.put("title",title);

			entry.put("image_file",imo.getAssetUid());
			
			entry.put("vendor_source",imo.getVendorSource());
			entry.put("rights",imo.getRights());
			
			
			putJSONRteFieldIfNotEmpty(entry,"copyright_notes", imo.getCopyrightNotes());
			
			putJSONRteFieldIfNotEmpty(entry,"license", imo.getLicense());
			
			putJSONRteFieldIfNotEmpty(entry,"caption", imo.getCaption());
			putJSONRteFieldIfNotEmpty(entry,"credit", imo.getCredit());


			
			entry.put("content_type",imo.getContentType());
			
			putJSONRteFieldIfNotEmpty(entry,"alt_text", imo.getAltText());

			
			String[] webpageUrlArr = imo.getWebpageUrl().toArray(new String[0]);
			entry.put("webpage_url",webpageUrlArr);

			entry.put("position_inline",imo.getPositionInline());
			
			putJSONRteFieldIfNotEmpty(entry,"image_notes", imo.getImageNotes());

			
			String[] tagArr = imo.getTags().toArray(new String[0]);
			entry.put("tags", tagArr);



			jo.put("entry", entry);

			String jsonStr= jo.toString();

			//System.out.println("JO " + jo);

			OkHttpClient client = new OkHttpClient().newBuilder()
					.build();
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, jsonStr);
			Request request = new Request.Builder()
					.url("https://api.contentstack.io/v3/content_types/image/entries?locale=en-us")
					.method("POST", body)
					.addHeader("api_key", API_KEY)
					.addHeader("authorization", MANAGEMENT_TOKEN)
					.addHeader("Content-Type", "application/json")
					.build();
			Response response = client.newCall(request).execute();
			String responseStr = response.body().string();

			//System.out.println(responseStr);
			JSONObject newIm = new JSONObject(responseStr);
			JSONObject img = (JSONObject) newIm.get("entry");
			//System.out.println("IMAGE " + responseStr);
			response.body().close() ;

		}
	}







	private static void putJSONRteFieldIfNotEmpty(JSONObject entry, String fieldName, String fieldValue) {
		// TODO Auto-generated method stub
		if (fieldValue==null) {
			return;
		}
		if (fieldValue.isEmpty()) {
			return;
		}
		entry.put(fieldName, createJSONRteField(fieldValue));

	}

	private static String extractFileExtension(String fileName) {
		int indexOfDot = fileName.indexOf('.');
		if (indexOfDot>0) {
			String fileExt = fileName.substring(indexOfDot+1);
			return fileExt;
		}
		return null;
	}

	private static void uploadAssets(List<ProjectImageObj> list, String imageDir, String projectAssetFolderUid) throws IOException {
		for (ProjectImageObj upio : list) {

			if (upio.getFileName()!=null) {



				String imagePathStart = imageDir + FileUtil.getFileSeperator();
				String filePath = imagePathStart + upio.getFileName();
				String shortFileNameWoutExt = extractBaseFileName(upio.getFileName());
				
				String mediaTypeStr = getMediaTypeStr(upio.getFileName());

				String captionStr = upio.getCaption();
				//pass empty caption if there's none, addFormDataPart gives a NPE otherwise
				if (captionStr == null) {
					captionStr = "";
				}
				//System.out.println(mediaTypeStr);
				OkHttpClient client = new OkHttpClient().newBuilder()
						.build();
				MediaType mediaType = MediaType.parse("multipart/form-data");
				RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
						.addFormDataPart("asset[upload]",shortFileNameWoutExt,
								RequestBody.create(MediaType.parse(mediaTypeStr),
										new File(filePath)))
						.addFormDataPart("asset[parent_uid]",projectAssetFolderUid)
						.addFormDataPart("asset[description]",captionStr)
						.build();

				
		

				Request request = new Request.Builder()
						.url("https://api.contentstack.io/v3/assets")
						.method("POST", body)
						.addHeader("api_key", API_KEY)
						.addHeader("authorization", MANAGEMENT_TOKEN)
						.addHeader("Content-Type", "multipart/form-data")
						.build();

				Response response = client.newCall(request).execute();
				String jsonStr = response.body().string();
				//System.out.println("UPLOAD Assets response " + jsonStr);
				JSONObject jo = new JSONObject(jsonStr);
				JSONObject assetObj = (JSONObject) jo.get("asset");
				String assetUid=  assetObj.getString("uid");
				upio.setAssetUid(assetUid);
				response.body().close() ;


			}
			
		}

	}


	


	private static String getMediaTypeStr(String fileName) {
		int indexOfDot = fileName.indexOf('.');
		String fileExt = fileName.substring(indexOfDot+1);
		if (fileExt.equals("jpeg") || fileExt.equals("jpg")) {
			return "image/jpeg";
		}
		else if (fileExt.equals("png")) {
			return "image/png";
		}
		return null;
	}

	private static String extractBaseFileName(String filename) {
		// TODO Auto-generated method stub

		int indexOfDot = filename.indexOf('.');
		if (indexOfDot>1) {
			return filename.substring(0,indexOfDot);
		}
		return null;
	}




	private static List<ProjectImageObj> readImageListFromWb(String pathToImageSheet) {
		// TODO Auto-generated method stub
		List<ProjectImageObj> list = new ArrayList<ProjectImageObj> ();

		XSSFWorkbook wb  = null;

		try {
			wb = new XSSFWorkbook(new File(pathToImageSheet));
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
			String articleId = getStringContent(row,0);

			String vendorSource = getStringContent(row,3);
			
		//	List<String>rights = new ArrayList<String>();
			String rights =  getStringContent(row,4);



			String copyrightsNote = getStringContent(row,5);
			String license = getStringContent(row,6);
			String caption = getStringContent(row,7);
			String credit = getStringContent(row,8);
			String contentType = getStringContent(row,9);
			String altText = getStringContent(row,10);
			List<String> webpageUrlList = new ArrayList <String>();

			String webpageUrlsStr = getStringContent(row,11);
			
			if (webpageUrlsStr!=null) {
				String [] tags = webpageUrlsStr.split(";");
				for (String tag : tags) {
					webpageUrlList.add(tag.trim());
				}
				
			}
			
			String positionInline = getStringContent(row,12);
			String imageNotes = getStringContent(row,13);
			String artSpec = getStringContent(row,14);
			String filename = getStringContent(row,15);
			List<String> tagList = new ArrayList <String>();
			String tagStr = getStringContent(row,16);
			if (tagStr!=null) {
				String [] tags = tagStr.split(";");
				for (String tag : tags) {
					tagList.add(tag.trim());
				}
				
			}
			

			String existingContentstackUid = getStringContent(row,17);




			ProjectImageObj upao = new ProjectImageObj(vendorSource, rights, copyrightsNote, license, caption, credit, contentType, altText, webpageUrlList, positionInline, imageNotes, artSpec, tagList, articleId, filename, existingContentstackUid);
			
		
			if (upao.getExistingContentStackId()!=null || upao.getFileName()!=null) {
				list.add(upao);

			}
		}

		return list;
	}






	private static String getStringContent(Row row, int cellIndex) {
		// TODO Auto-generated method stub
		Cell cell = row.getCell(cellIndex);
		if (cell!=null){
			return cell.getRichStringCellValue().getString();
		}
		return null;
	}



	
	
	private static JSONObject createJSONRteField( String txt) {
		

		
		UUID docUUID = UUID.randomUUID();

		JSONObject txtObj = new JSONObject();
		txtObj.put("type","doc");
		txtObj.put("uid", docUUID.toString());
		txtObj.put("attrs", new JSONObject());
		
		JSONArray childrenArr = new JSONArray();
		JSONObject childObj = new JSONObject();
		childObj.put("type", "p");
		childObj.put("attrs", new JSONObject());
		UUID pUUID = UUID.randomUUID();

		childObj.put("uid", pUUID.toString());
		
		JSONArray pChildrenArr = new JSONArray();
		JSONObject childTxtObj = new JSONObject();
		childTxtObj.put("text", txt);
		pChildrenArr.put(childTxtObj);
		childObj.put("children", pChildrenArr);
		childrenArr.put(childObj);
		txtObj.put("children", childrenArr);
		return txtObj;

	}












}
