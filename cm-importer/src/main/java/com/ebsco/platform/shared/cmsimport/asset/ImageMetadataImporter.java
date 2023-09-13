package com.ebsco.platform.shared.cmsimport.asset;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil;
import com.ebsco.platform.shared.cmsimport.utilities.DBUtil;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageMetadataImporter {



	private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");

	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");

	private static final String ASSET_FOLDER_PATH=  "/Users/mpamuk/Desktop/cms_pov/reimport/Images/";




	//mainDelete
	public static void  mainDelete(String [] args) throws IOException {
		
		String contentType = "image";
		int noOfEntries = ContentstackUtil.getCountOfEntries(contentType);
		
		List<String> uids= ContentstackUtil.getListOfEntryUids(noOfEntries,contentType);
		int count = 0;
		for (String uid : uids) {
			System.out.println(count++);
			ContentstackUtil.deleteEntry(uid,contentType);
		}
	}

	
	//mainImport
	public static void  main (String [] args) throws SQLException, IOException {

		int count=0;


		int noOfAssets = ContentstackUtil.getCountOfAssets();
		Map<String,String> fileUidMap = getFilenameToUidMap(noOfAssets);
		List<ImageMetadataObj>povImageMetadataObjList = getPOVImageMetadata();

		
		/*for (String file : fileUidMap.keySet()) {
			System.out.println("map " + file + " " + fileUidMap.get(file));
		}*/
		
		
	
		for (ImageMetadataObj imo : povImageMetadataObjList) {

			System.out.println(count++ );
			String imagePathStart = ASSET_FOLDER_PATH;
			String filePath = imagePathStart + imo.getImageFile();

			File f= new File(filePath);
			if (f.exists()) {
				insertImageToContentStack(imo, fileUidMap);
			}
			else {
				System.out.println("DOESN'T EXIST" + imo.getImageFile());
			}


		}



	}





	private static void publishAsset(String assetUid) throws IOException {


		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, "{\n\t\"asset\": {\n\t\t\"locales\": [\n\t\t\t\"en-us\"\n\t\t],\n\t\t\"environments\": [\n\t\t\t\"production\"\n\t\t]\n\t},\n\t\"version\": 1,\n\t\"scheduled_at\": \"2019-02-08T18:30:00.000Z\"\n}");
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/assets/" + assetUid + "/publish")
				.method("POST", body)
				.addHeader("api_key", API_KEY)
				.addHeader("authorization", MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = client.newCall(request).execute();
	}




	private static void publishEntry(String entryUid, String contentType) throws IOException {


		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, "{\n\t\"entry\": {\n\t\t\"locales\": [\n\t\t\t\"en-us\"\n\t\t],\n\t\t\"environments\": [\n\t\t\t\"production\"\n\t\t]\n\t},\n\t\"version\": 1}");
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/" + contentType + "/entries/" +entryUid + "/publish")
				.method("POST", body)
				.addHeader("api_key", API_KEY)
				.addHeader("authorization", MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = client.newCall(request).execute();
		String jsonStr = response.body().string();
		System.out.println(jsonStr);
		response.body().close() ;
	}

	private static Map<String,String>  getFilenameToUidMap(int totalAssets) throws IOException{


		Map<String,String> map = new HashMap<String,String>();


		int index = (totalAssets/100)+1;

		for (int i=0;i<index;i++) {
			int skip=i *100;
			String urlStr= "https://api.contentstack.io/v3/assets?folder="  + ContentstackUtil.POV_ASSET_FOLDER_UID + "&skip=" +skip;
			System.out.println(urlStr);
			OkHttpClient client = new OkHttpClient().newBuilder()
					.build();
			Request request = new Request.Builder()
					.url("https://api.contentstack.io/v3/assets?folder="  + ContentstackUtil.POV_ASSET_FOLDER_UID + "&skip=" +skip)
					.method("GET", null)
					.addHeader("api_key", API_KEY)
					.addHeader("authorization", MANAGEMENT_TOKEN)
					.addHeader("Content-Type", "application/json")
					.build();
			Response response = client.newCall(request).execute();
			String jsonStr = response.body().string();
			JSONObject jo = new JSONObject(jsonStr);
			JSONArray assetsArray = jo.getJSONArray("assets");
			for (int j=0; j<assetsArray.length(); j++) {
				JSONObject item = assetsArray.getJSONObject(j);
				String filename = item.getString("filename");
				String uid = item.getString("uid");
				// System.out.println(filename  + " " + uid);
				map.put(filename, uid);
			}

		}



		return map;


	}


	

	private static List<ImageMetadataObj> getPOVImageMetadata() throws SQLException {
		// TODO Auto-generated method stub
		List<ImageMetadataObj>povImages = new ArrayList<ImageMetadataObj>(); 
		String qry = "select image_file, image_file,caption, tf.description,copyright,source,image_content_type,webpage_url,position,rights_note,use_end_date, height,width, tf.date_added, tf.date_updated, terms from cms.t_articles ta join cms.t_figures tf on ta.article_id = tf.article_id  where ta.book_id like '%pov_%' "
				+ "and  use_image =true  ";



		Connection conn = DBUtil.getPostgresConn();

		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery(qry);


		while (rs.next()) {

			String imageTitle = rs.getString(1);
			String imageFile =rs.getString(2);
			String caption = rs.getString(3);
			String description = rs.getString(4);
			String copyright = rs.getString(5);
			String source = rs.getString(6);
			String imageContentType= rs.getString(7);
			String webpageUrl= rs.getString(8);
			String position = rs.getString(9);
			String rightsNote = rs.getString(10);
			java.sql.Date useEndDate = rs.getDate(11);
			int height = rs.getInt(12);
			int width = rs.getInt(13);
			java.sql.Date dateAdded = rs.getDate(14);
			java.sql.Date dateUpdated = rs.getDate(15);
			String terms = rs.getString(16);
			ImageMetadataObj imo = new ImageMetadataObj(imageTitle, imageFile, caption, description, copyright, source, imageContentType, webpageUrl, position, rightsNote, useEndDate, height, width, dateAdded, dateUpdated, terms);
			povImages.add(imo);
		}

		conn.close();        
		return povImages;
	}


	/*	
	java.sql.Date useEndDate = rs.getDate(11);

	java.sql.Date dateAdded = rs.getDate(14);
	java.sql.Date dateUpdated = rs.getDate(15);
	 */

	private static void insertImageToContentStack(ImageMetadataObj imo, Map<String,String>map) throws IOException {




		JSONObject jo = new JSONObject();
		JSONObject entry = new JSONObject();
		String title = imo.getImageTitle();
		title = title.substring(0,title.length()-4);
		entry.put("title",title);
		entry.put("image_file", map.get(title));
		entry.put("vendor_source",imo.getSource());
		
		entry.put("rights",imo.getTerms());

		putJSONRteFieldIfNotEmpty(entry, "copyright_notes", imo.getCopyright());
		putJSONRteFieldIfNotEmpty(entry, "license", imo.getRightsNote());
		putJSONRteFieldIfNotEmpty(entry, "caption", imo.getCopyright());



		entry.put("content_type",imo.getContentType());
		
		JSONArray webUrlArr = new JSONArray();
		if (imo.getWebpageUrl()!=null) {
			webUrlArr.put(imo.getWebpageUrl());
		}
		entry.put("webpage_url",webUrlArr);
		entry.put("position_inline",imo.getPosition());





		jo.put("entry", entry);

		String jsonStr= jo.toString();



		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, jsonStr);
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/image/entries?locale=en-us")
				.method("POST", body)
				.addHeader("api_key",  API_KEY)
				.addHeader("authorization", MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = client.newCall(request).execute();

		//System.out.println( "IMAGE IMPORT " + response.body().string());
		response.body().close() ;
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
	
	private static JSONObject createJSONRteField( String txt) {
		

		
		UUID docUUID = UUID.randomUUID();

		JSONObject jo = new JSONObject();
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
