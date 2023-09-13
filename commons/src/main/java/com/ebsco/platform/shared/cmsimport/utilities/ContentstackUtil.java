package com.ebsco.platform.shared.cmsimport.utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ebsco.platform.shared.cmsimport.article.productdefinition.ArticleProductDefinitionObj;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ContentstackUtil {


	private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");

	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");
	
	public static final String POV_ASSET_FOLDER_UID =  "blt1cd840d059515d9c";




	public static void bulkDeleteEntries(String contentType) throws IOException, InterruptedException {

		//get number of entries
		int noOfEntries = getCountOfEntries(contentType);
		//get uids to be deleted
		List<String> uids = getListOfEntryUids(noOfEntries, contentType);

		//bulk delete
		bulkDelete(uids,noOfEntries, contentType);

	}


	private static void bulkDelete(List<String> uids, int noOfEntries,String contentType) throws IOException, InterruptedException {
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		int maxBatchNo = 10;
		int index = (uids.size()/maxBatchNo)+1;
		
		for (int i=0;i<index;i++) {
			
			System.out.println(i);
			int sublistEnd = i+maxBatchNo;
			if (sublistEnd>noOfEntries) {
				sublistEnd=noOfEntries;
			}
	
			List<String> reqUids = uids.subList(i, sublistEnd);
			
			
			JSONObject jo = new JSONObject();
			JSONArray deleteEntriesArr = new JSONArray();

			for (String reqUid  : reqUids) {
				JSONObject tbd = new JSONObject();
				tbd.put("content_type", contentType);
				tbd.put("uid", reqUid);
				tbd.put("locale", "en-us");
				deleteEntriesArr.put(tbd);

			}
			
			jo.put("entries", deleteEntriesArr);
			String jsonStr= jo.toString();

			System.out.println(jsonStr);
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, jsonStr);

			Request request = new Request.Builder()
			  .url("https://api.contentstack.io/v3/bulk/delete")
			  .method("POST", body)
			  .addHeader("api_key", API_KEY)
			  .addHeader("authorization", MANAGEMENT_TOKEN)
			  .addHeader("Content-Type", "application/json")
			  .build();
			Response response = client.newCall(request).execute();
			System.out.println(response.body().string());
			response.body().close() ;
			Thread.sleep(1000);
		
		}
		
	}


	public static int getCountOfEntries (String contentTypeId) throws IOException {


		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/" + contentTypeId +"/entries?count=true")
				.method("GET", null)
				.addHeader("api_key",  API_KEY)
				.addHeader("authorization",MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = client.newCall(request).execute();
		String jsonStr = response.body().string();
		System.out.println(jsonStr);
		JSONObject jo = new JSONObject(jsonStr);
		return jo.getInt("entries");
	}


	public static List<String> getListOfEntryUids(int totalEntries, String contentTypeId) throws IOException{

		List<String>uids = new ArrayList<String>();


		int index = (totalEntries/100)+1;

		for (int i=0;i<index;i++) {
			int skip=i *100;
			String urlStr= "https://api.contentstack.io/v3/content_types/" + contentTypeId + "/entries?&skip=" +skip;
			System.out.println(urlStr);
			OkHttpClient client = new OkHttpClient().newBuilder()
					.build();
			Request request = new Request.Builder()
					.url( "https://api.contentstack.io/v3/content_types/" + contentTypeId + "/entries?&skip=" +skip)
					.method("GET", null)
					.addHeader("api_key", API_KEY)
					.addHeader("authorization", MANAGEMENT_TOKEN)
					.addHeader("Content-Type", "application/json")
					.build();
			Response response = client.newCall(request).execute();
			String jsonStr = response.body().string();
			JSONObject jo = new JSONObject(jsonStr);
			JSONArray entriesArray = jo.getJSONArray("entries");
			for (int j=0; j<entriesArray.length(); j++) {
				JSONObject item = entriesArray.getJSONObject(j);
				String uid = item.getString("uid");
				uids.add(uid);

			}

		}

		return uids;


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
	
	public static void updateArticle(String uid, JSONObject item) {

		JSONObject jo = new JSONObject();
		item.put("associated_images", JSONObject.NULL);
		jo.put("entry", item);
		String jsonStr = jo.toString();
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
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		
		
	}
	
	public static String getTitleSourceUid(String mid) throws IOException {
		String uid = null;
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		MediaType mediaType = MediaType.parse("application/json");
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/book_source/entries?query={\"title\":\"" + mid +"\"}")
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

			uid = item.getString("uid");

		}
		response.body().close();
		return uid;

	}
	
	public static String getWorkflowStageUid(String workflowName, String stage) throws IOException {
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
			if (wfName.equals(workflowName)) {
				JSONArray wfStagesArray = item.getJSONArray("workflow_stages");
				for (int j=0; j<wfStagesArray.length(); j++) {
					JSONObject wfStageObj = wfStagesArray.getJSONObject(j);
					String stageName = wfStageObj.getString("name");
					if (stageName.equals(stage)) {
						availableWfStageUid = wfStageObj.getString("uid");
					}
				}
			}

		}
		return availableWfStageUid;
	}
	
	public static void setWorkflowState(String articleUid, String contentTypeUid,
			String availableWfStageUid) throws IOException {
		JSONObject workflowObj = new JSONObject();
		JSONObject wfStageObj = new JSONObject();
		wfStageObj.put("uid", availableWfStageUid);
		workflowObj.put("workflow_stage", wfStageObj);
		JSONObject overarchingObj = new JSONObject();
		overarchingObj.put("workflow", workflowObj);
		
		String jsonStr = overarchingObj.toString();
		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
				MediaType mediaType = MediaType.parse("application/json");
				RequestBody body = RequestBody.create(mediaType, jsonStr);
				Request request = new Request.Builder()
				  .url("https://api.contentstack.io/v3/content_types/" + contentTypeUid + "/entries/" +articleUid + "/workflow")
				  .method("POST", body)
				  .addHeader("api_key",  API_KEY)
				  .addHeader("authorization", MANAGEMENT_TOKEN)
				  .addHeader("Content-Type", "application/json")
				  .build();
				Response response = client.newCall(request).execute();
				//System.out.println(response.body().string());
				response.body().close() ;
		
	}
	
	public static void importArticleProductDefinition(ArticleProductDefinitionObj apdo) throws IOException {



		JSONObject jo = new JSONObject();
		JSONObject entry = new JSONObject();

		JSONArray bookArr = new JSONArray();

		JSONObject bookObj = new JSONObject();

		bookObj.put("uid", apdo.getTitleSource());
		bookObj.put("_content_type_uid", "book_source");
		bookArr.put(bookObj);
		entry.put("title_source", bookArr);


	
		entry.put("an", apdo.getAn());


		String title = apdo.getTitle();

		entry.put("title", title);
		
		entry.put("dtformat", apdo.getDtformat());

		jo.put("entry", entry);

		String jsonStr= jo.toString();

		System.out.println("JSON STR " + jsonStr);

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
		Response response = client.newCall(request).execute();
		String responseStr = response.body().string();
		System.out.println(responseStr);
		response.body().close() ;


	}
	
	public static void deleteEntry(String uid, String contentType) throws IOException {
		// TODO Auto-generated method stub

		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, "");
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/" + contentType + "/entries/" + uid)
				.method("DELETE", body)
				.addHeader("api_key", API_KEY)
				.addHeader("authorization", MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = client.newCall(request).execute();
		response.body().close() ;


	}
	
	public static int getCountOfAssets () throws IOException {


		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/assets?folder=" + POV_ASSET_FOLDER_UID +"&count=true")
				.method("GET", null)
				.addHeader("api_key",  API_KEY)
				.addHeader("authorization",MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = client.newCall(request).execute();
		String jsonStr = response.body().string();
		JSONObject jo = new JSONObject(jsonStr);
		return jo.getInt("assets");
	}
	
	public static Map<String,String>  getFilenameToUidUrlMap(int totalAssets) throws IOException{




		Map<String,String> map = new HashMap<String,String>();



		int index = (totalAssets/100)+1;

		for (int i=0;i<index;i++) {
			int skip=i *100;
			String urlStr= "https://api.contentstack.io/v3/assets?folder="+ POV_ASSET_FOLDER_UID + "&skip=" +skip;
			System.out.println(urlStr);
			OkHttpClient client = new OkHttpClient().newBuilder()
					.build();
			Request request = new Request.Builder()
					.url("https://api.contentstack.io/v3/assets?folder="  + POV_ASSET_FOLDER_UID + "&skip=" +skip)
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
				String url = item.getString("url");
				// System.out.println(filename  + " " + uid);
				map.put(filename, uid  + "_" + url);
			}

		}



		return map;


	}
	
	public static Map<String, String> getListOfTitleEntryUids(int totalEntries, String contentTypeId) throws IOException {
        // TODO Auto-generated method stub
        Map<String, String> uids = new HashMap<String, String>();


        int index = (totalEntries / 100) + 1;

        for (int i = 0; i < index; i++) {
            int skip = i * 100;
            String urlStr = "https://api.contentstack.io/v3/content_types/" + contentTypeId + "/entries?&skip=" + skip;
            System.out.println(urlStr);
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            
            Request request = new Request.Builder()
					.url("https://api.contentstack.io/v3/content_types/" + contentTypeId + "/entries?&skip=" + skip)
					.method("GET", null)
					.addHeader("api_key", API_KEY)
					.addHeader("authorization", MANAGEMENT_TOKEN)
					.addHeader("Content-Type", "application/json")
					.build();
			Response response = client.newCall(request).execute();
			ResponseBody body = response.body();
			String responseText = Objects.nonNull(body) ? body.string() : "";

            JSONObject jo = new JSONObject(responseText);
            JSONArray entriesArray = jo.getJSONArray("entries");
            for (int j = 0; j < entriesArray.length(); j++) {
                JSONObject item = entriesArray.getJSONObject(j);
                if (item.has("title")) {
                    String uid = item.getString("uid");
                    String title = item.getString("title");
                    uids.put(title, uid);
                }
                //  System.out.println(contentTypeId.toUpperCase() + " MAP   " +title + " " + uid);

            }

        }

        return uids;
    }

}
