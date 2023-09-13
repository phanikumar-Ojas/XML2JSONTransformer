package com.ebsco.platform.shared.cmsimport.lexile;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ebsco.platform.shared.cmsimport.asset.ImageMetadataImporter;
import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil;
import com.ebsco.platform.shared.cmsimport.utilities.DBUtil;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LexileImporter {

	private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");

	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");


	

	public static void main (String [] args) throws SQLException, IOException {
		
		Map<String,Integer>lexileMap = createLexileMap();
		Map<String,String>articleIdUidMap = createArticleIdUidMap();
		
		int count=0;
		
		for (String articleId : articleIdUidMap.keySet()) {
			System.out.println(count++);
			Integer lex = lexileMap.get(articleId);
			if (lex!=null) {
				String uid = articleIdUidMap.get(articleId);
				updateArticleWithLexile (uid,lex);
				System.out.println(uid);
				
			}
		}
		
		
	
	}
	
	
	
	private static void updateArticleWithLexile(String uid, Integer lex) throws IOException {
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
			System.out.println(jsonStr);
			JSONObject jo = new JSONObject(jsonStr);
			JSONArray entriesArray = jo.getJSONArray("entries");
			for (int j=0; j<entriesArray.length(); j++) {
			    JSONObject item = entriesArray.getJSONObject(j);
			   

		    		item.put("lexile", lex);

		    		ContentstackUtil.updateArticle(uid, item);
			    }

	
			}
		
	



	public static Map<String, String> createArticleIdUidMap() throws IOException {
		// TODO Auto-generated method stub
		Map<String, String>uids = new HashMap<String, String>();
		int totalEntries = ContentstackUtil.getCountOfEntries("article");
		String contentTypeId = "article";

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
				if (item.has("title")) {
				String uid = item.getString("uid");
				String articleId = item.getString("article_id");
				uids.put(articleId,uid);
				}
				//  System.out.println(contentTypeId.toUpperCase() + " MAP   " +title + " " + uid);

			}

		}

		return uids;
	}



	public static Map<String,Integer> createLexileMap() throws SQLException {
		Map<String,Integer>map = new HashMap<String,Integer>();
		String qry = "select ta.article_id, lexile "

				

			+ "	from cms.t_articles ta JOIN cms.t_lexiles tl ON  ta.article_id =tl.article_id where book_id  like '%pov_%'   and research_starter is null and  do_not_use ='false'  ";
		//	+ " order by article_title limit 1 ";

    // +  "  and article_id = 'pov_can_2019_20190102_17'";


		Connection conn = DBUtil.getPostgresConn();

		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery(qry);

		while (rs.next()) {
			String articleId = rs.getString(1);
			int lexile = rs.getInt(2);
			map.put(articleId, lexile);
		}
		
		conn.close();
		return map;
	}
}
