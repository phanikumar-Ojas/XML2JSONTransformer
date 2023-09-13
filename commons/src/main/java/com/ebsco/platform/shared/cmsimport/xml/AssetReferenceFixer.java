package com.ebsco.platform.shared.cmsimport.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AssetReferenceFixer {

	
	private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");
	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");
	
	
	public static void main2  (String [] args)throws IOException {
		String filePath = "/Users/mpamuk/Desktop/uids.txt";
		String contentTypeId = "article";

	    BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath), true));

		int noOfEntries = ContentstackUtil.getCountOfEntries(contentTypeId);
		List<String> uids= ContentstackUtil.getListOfEntryUids(noOfEntries,contentTypeId);
		for (String uid : uids) {
			
			writer.write(uid + "\n");
		}
		writer.close();
		
	}
	
	

	public static void main (String [] args)throws IOException {
		String filePath = "/Users/mpamuk/Desktop/uids.txt";

		
		String writePath = "/Users/mpamuk/Desktop/exception_uids.txt";
	    BufferedWriter writer = new BufferedWriter(new FileWriter(new File(writePath), true));

		List<String> uids;
		try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
			uids = lines.collect(Collectors.toList());
		}
		
		
		//int noOfEntries = ImageMetadataImporter.getCountOfEntries(contentTypeId);
		//List<String> uids= ImageMetadataImporter.getListOfEntryUids(noOfEntries,contentTypeId);

		for (String uid : uids) {


			OkHttpClient client = new OkHttpClient().newBuilder()
					.build();

			Request request = new Request.Builder()
					.url("https://api.contentstack.io/v3/content_types/article/entries?query={\"uid\":\"" + uid +"\"}")
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
			for (int i=0; i<entriesArray.length(); i++) {
				JSONObject item = entriesArray.getJSONObject(i);
				String fuid = item.getString("uid");
				System.out.println(uid);
				JSONObject bodyRteObj = item.getJSONObject("main_body");
				//System.out.println(bodyRteObj);

				JSONArray bodyRteChildren = bodyRteObj.getJSONArray("children");

				boolean hasImage= false;
				
				
				try {
				for (int j=0; j<bodyRteChildren.length(); j++) {
					JSONObject rteChild = bodyRteChildren.getJSONObject(j);
					//System.out.println(rteChild.toString());
					String rteChildType = rteChild.getString("type");
					if (rteChildType.equals("reference")) {
						JSONObject attrsObj = rteChild.getJSONObject("attrs");
						String attrType = attrsObj.getString("type");
						if (attrType.equals("asset")) {



							if (attrsObj.has("redactor-attributes")) {
								String assetLink = attrsObj.getString("asset-link");

								attrsObj.put("content-type-uid", "sys_assets");
								hasImage=true;
								JSONObject redactorAttrsObj = attrsObj.getJSONObject("redactor-attributes");
								String assetUid= null;

								if (attrsObj.has("asset-uid")) {
									assetUid=attrsObj.getString("asset-uid");
								}
								else {
									if (redactorAttrsObj.has("asset_uid")) {
										assetUid= redactorAttrsObj.getString("asset_uid");
									}
								}

								String captionStr = null;
								if (redactorAttrsObj.has("asset-caption")) {
									captionStr = redactorAttrsObj.getString("asset-caption");
								}

								redactorAttrsObj.put("caption", captionStr);


								attrsObj.put("asset-uid", assetUid);
								attrsObj.put("asset-type","image/jpeg");
								attrsObj.put("class-name","embedded-asset");

								String assetName = assetLink.substring(assetLink.lastIndexOf("/")+1);

								attrsObj.put("asset-name", assetName);
								attrsObj.put("dirty", true);
								attrsObj.remove("default");
								attrsObj.remove("max-width");
								attrsObj.remove("width");
							}


						}
					}
				}
				
				if (hasImage) {
					JSONObject entryObj = new JSONObject();
					entryObj.put("entry", item);
					System.out.println(entryObj.toString());
					updateEntry( "article", fuid, entryObj);
				}
				
				}
				
				catch (Exception e) {
					System.out.println("Exception " + fuid);
					writer.write(uid + "\n");

				}



			}
		}
		
		writer.close();
	}
	
	
	
	public static void maindd (String [] args)throws IOException {
		String filePath = "/Users/mpamuk/Desktop/uids.txt";
		
		int noOfAssets = ContentstackUtil.getCountOfAssets();
		
		Map<String,String> fileUidMap = ContentstackUtil.getFilenameToUidUrlMap(noOfAssets);

		String contentTypeId = "article";
		
	
		List<String> uids;
		try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
			uids = lines.collect(Collectors.toList());
		}
		
		
		//int noOfEntries = ImageMetadataImporter.getCountOfEntries(contentTypeId);
		//List<String> uids= ImageMetadataImporter.getListOfEntryUids(noOfEntries,contentTypeId);

		for (String uid : uids) {


			OkHttpClient client = new OkHttpClient().newBuilder()
					.build();

			Request request = new Request.Builder()
					.url("https://api.contentstack.io/v3/content_types/article/entries?query={\"uid\":\"" + uid +"\"}")
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
			for (int i=0; i<entriesArray.length(); i++) {
				JSONObject item = entriesArray.getJSONObject(i);
				String fuid = item.getString("uid");
				System.out.println(uid);
				JSONObject bodyRteObj = item.getJSONObject("main_body");
				//System.out.println(bodyRteObj);

				JSONArray bodyRteChildren = bodyRteObj.getJSONArray("children");

				boolean hasImage= false;
				for (int j=0; j<bodyRteChildren.length(); j++) {
					JSONObject rteChild = bodyRteChildren.getJSONObject(j);
					//System.out.println(rteChild.toString());
					String rteChildType = rteChild.getString("type");
					if (rteChildType.equals("reference")) {
						JSONObject attrsObj = rteChild.getJSONObject("attrs");
						String attrType = attrsObj.getString("type");
						if (attrType.equals("asset")) {



							if (attrsObj.has("redactor-attributes")) {
								
								
								
								
								
								String assetLink = attrsObj.getString("asset-link");

								attrsObj.put("content-type-uid", "sys_assets");
								hasImage=true;
								JSONObject redactorAttrsObj = attrsObj.getJSONObject("redactor-attributes");
								String assetUid= null;

								if (attrsObj.has("asset-uid")) {
									assetUid=attrsObj.getString("asset-uid");
								}
								else {
									if (redactorAttrsObj.has("asset_uid")) {
										assetUid= redactorAttrsObj.getString("asset_uid");
									}
								}

								String captionStr = null;
								if (redactorAttrsObj.has("asset-caption")) {
									captionStr = redactorAttrsObj.getString("asset-caption");
								}

								redactorAttrsObj.put("caption", captionStr);


								attrsObj.put("asset-uid", assetUid);
								attrsObj.put("asset-type","image/jpeg");
								attrsObj.put("class-name","embedded-asset");

								String assetName = assetLink.substring(assetLink.lastIndexOf("/")+1);

								attrsObj.put("asset-name", assetName);
								attrsObj.put("dirty", true);
								attrsObj.remove("default");
								attrsObj.remove("max-width");
								attrsObj.remove("width");
							}


						}
					}
				}

				if (hasImage) {
					JSONObject entryObj = new JSONObject();
					entryObj.put("entry", item);
					System.out.println(entryObj.toString());
					updateEntry( "article", fuid, entryObj);
				}

			}
		}
	}
	
	

	public static void updateEntry(String contentType, String uid, JSONObject item) {


		String jsonStr = item.toString();
		// TODO Auto-generated method stub
			//System.out.println("JSON STR " + jsonStr);
			OkHttpClient client = new OkHttpClient().newBuilder()
					  .build();
					MediaType mediaType = MediaType.parse("application/json");
					RequestBody body = RequestBody.create(mediaType, jsonStr);
					Request request = new Request.Builder()
					  .url("https://api.contentstack.io/v3/content_types/" + contentType + "/entries/" + uid)
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
}
