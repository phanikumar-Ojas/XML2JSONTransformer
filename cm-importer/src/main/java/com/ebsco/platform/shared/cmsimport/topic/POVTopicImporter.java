package com.ebsco.platform.shared.cmsimport.topic;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ebsco.platform.shared.cmsimport.article.ArticleObj;
import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil;
import com.ebsco.platform.shared.cmsimport.utilities.DBUtil;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class POVTopicImporter {

	private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");

	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");


	
	//get list of all MIDs
	//fetch articles by each mid
	//if has an overview title create topic
	//otherwise keep list of the mids without topic
	//get topic title topic map
	//update article with topic
	
	

	
	//get articles for topic
	
	//create topic
	//update articles with topic data
	
	//mainDelete
	public static void mainDelete(String [] args) throws IOException {
		
		String contentType = "topic_debate";
		int noOfEntries = ContentstackUtil.getCountOfEntries(contentType);
		
		List<String> uids= ContentstackUtil.getListOfEntryUids(noOfEntries,contentType);
		int count = 0;
		for (String uid : uids) {
			System.out.println(count++);
			ContentstackUtil.deleteEntry(uid,contentType);
		}
	}
	
	//mainImport
	public static void main (String [] args) throws SQLException, IOException {
		
		Set<String>topics=  new HashSet<String>();
		List<String>mids = getAllMids();
		Map<String,List<String>>midArtTitles = getMidArtTitles();
		
		//Map<String,String>artAnUid = getArtAnUidMap();
		
		for (String mid : mids) {
			
			List<String>artTitles= midArtTitles.get(mid);
			String overview = getOverviewTopic(artTitles);
			if (overview==null) {
				System.out.println("MID " + mid  + " is missing topic");
			}
			else {
				System.out.println("Topic is " + overview);
				

				topics.add(overview);
				//createTopic(overview,artTitles,artTitleUid);
			}
		}
		for (String topic : topics) {
			insertTopicToContentstack(topic);
		}
		

		}
	
		
	private static void insertTopicToContentstack(String topic) throws IOException {
		// TODO Auto-generated method stub
	
		JSONObject jo = new JSONObject();
		JSONObject entry = new JSONObject();
		
		
		String title = topic;
		
		String topicTitle = topic;
		String debateType= "Points of View";
		
		entry.put("title",title);
		
		entry.put("topic_title", topicTitle);
		entry.put("debate_type", debateType);
		
		jo.put("entry", entry);
		
		String jsonStr= jo.toString();
		

		
		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
				MediaType mediaType = MediaType.parse("application/json");
				RequestBody body = RequestBody.create(mediaType, jsonStr);
				Request request = new Request.Builder()
				  .url("https://api.contentstack.io/v3/content_types/topic_debate/entries?locale=en-us")
				  .method("POST", body)
				  .addHeader("api_key",  API_KEY)
				  .addHeader("authorization", MANAGEMENT_TOKEN)
				  .addHeader("Content-Type", "application/json")
				  .build();
				Response response = client.newCall(request).execute();
				System.out.println(response.body().string());
				response.body().close() ;
	}





	




	private static List<ArticleObj> searchForTopic(String topic, Connection conn) throws SQLException {
		// TODO Auto-generated method stub
		List<ArticleObj>list = new ArrayList<ArticleObj>(); 

		String qry = "select distinct article_mid from cms.t_articles ta where "
				+ "((article_Title like '" + replaceSingleQuote(topic) + ": An Overview%')" +
				
				" OR (article_Title like '"   
				 + replaceSingleQuote(topic) + ": Overview%')) "
						+ "and do_not_use =false and book_id like 'pov_%' and research_starter is null";
		
		//System.out.println(qry);
		List<String>mids= new ArrayList<String>();


		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery(qry);


		while (rs.next()) {

			String mid = rs.getString(1);

			mids.add(mid);
		}

		
		statement = conn.createStatement();
		qry="SELECT article_id, article_mid, article_title FROM  cms.t_articles where  article_mid in " +toStr(mids) +
				" and  do_not_use =false and book_id like 'pov_%' and research_starter is null";
		rs = statement.executeQuery(qry);
		
		while (rs.next()) {
			
			ArticleObj ao = new ArticleObj();
			String articleId = rs.getString("article_id");
			String articleMid = rs.getString("article_mid");
			String articleTitle = rs.getString("article_title");
			ao.setArticleMid(articleMid);
			ao.setArticleId(articleId);
			ao.setArticleTitle(articleTitle);
			list.add(ao);
		}
		
		
		
		
		//conn.close(); 
		
		
		
		
		return list;
	}


	private static String replaceSingleQuote(String topic) {
		// TODO Auto-generated method stub
		String txt =topic.replace("'", "''");
		return txt;
	}


	private static String toStr(List<String> mids) {
		
		String pred= "(";
		for (String mid  :mids) {
			pred +="'" + mid + "',";
		}
		pred = pred.substring(0,pred.length()-1);
		pred += ")";
		// TODO Auto-generated method stub
		return pred;
	}



	private static Map<String, List<String>> getMidArtTitles() throws SQLException {
		Map<String,List<String>>map = new HashMap<String,List<String>>(); 
		String qry = "SELECT article_title, article_mid  " 

			+ "	from cms.t_articles ta WHERE do_not_use =false and book_id like 'pov_%' and research_starter is null"
			;


		Connection conn = DBUtil.getPostgresConn();

		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery(qry);


		while (rs.next()) {

			String artT = rs.getString(1);
			String mid = rs.getString(2);
			List<String>lis  = map.get(mid);
			if (lis==null) {
				lis = new ArrayList<String>();
			}
			lis.add(artT);
			map.put(mid, lis);
		}

		conn.close();        
		return map;
	}

	private static String getOverviewTopic(List<String> artTitles) {
		// TODO Auto-generated method stub
		
		List<String>overviews=  new ArrayList<String>();
		
		for (String artT : artTitles) {
			if (artT.contains(": An Overview" ) || artT.contains(": Overview" )) {
				String extractedOverview = extractOverview(artT);
				if (extractedOverview!=null)
					overviews.add(extractedOverview);
				
			}
		}
		
		
		if (overviews.size()==1) {
			return overviews.get(0);
		}
		return null;
	}

	private static String extractOverview(String artT) {
		// TODO Auto-generated method stub
		
		if(artT.contains(": An Overview")) {
			int index = artT.indexOf(": An Overview");
			return artT.substring(0,index);
		}
		
		else if (artT.contains(": Overview" )) {
			int index = artT.indexOf(": Overview");
			return artT.substring(0,index);
		}

		return null;
	}


	private static List<String> getAllMids() throws SQLException {
		List<String>mids = new ArrayList<String>(); 
		String qry = "SELECT distinct article_mid " 

			+ "	FROM cms.t_articles ta WHERE do_not_use = false AND book_id like 'pov_%' AND research_starter is null";


		Connection conn = DBUtil.getPostgresConn();

		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery(qry);


		while (rs.next()) {

			String mid = rs.getString(1);
			mids.add(mid);
		}

		conn.close();        
		return mids;
	}


	public static Map<String, String> createTopicMap() throws IOException, SQLException {

		
		Connection conn = DBUtil.getPostgresConn();

		String contentTypeId= "topic_debate"	;

		int noOfEntries = ContentstackUtil.getCountOfEntries("topic_debate");
		Map<String,String>topicTitleToUidMap = new HashMap<String,String>();
		Map<String,String>articleIdTopicIdMap = new HashMap<String,String>();


		int index = (noOfEntries/100)+1;

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
				String title = item.getString("title");
				topicTitleToUidMap.put(title, uid);

			}

		}
        int count=0;

        for (String topic : topicTitleToUidMap.keySet()) {
        	System.out.println(topic + " " + count++);
        	List<ArticleObj> articles = searchForTopic(topic, conn);
        	for (ArticleObj ao : articles ) {
        		String articleId = ao.getArticleId();
        		String topicUid = topicTitleToUidMap.get(topic);
        		articleIdTopicIdMap.put(articleId, topicUid);
        	}


        }

        conn.close();
		return articleIdTopicIdMap;






	}
}
