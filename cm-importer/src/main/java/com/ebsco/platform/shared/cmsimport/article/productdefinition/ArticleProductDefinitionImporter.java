package com.ebsco.platform.shared.cmsimport.article.productdefinition;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ebsco.platform.shared.cmsimport.article.ArticleImporter;
import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil;
import com.ebsco.platform.shared.cmsimport.utilities.DBUtil;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ArticleProductDefinitionImporter {

	private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");

	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");

	
	//mainDelete
	public static void mainDelete(String [] args) throws IOException {

		String type = "article_product_definition";
		int noOfEntries = ContentstackUtil.getCountOfEntries(type);
		List<String> uids= ContentstackUtil.getListOfEntryUids(noOfEntries,type);
		int count = 0;
		for (String uid : uids) {
			System.out.println(count++);
			ContentstackUtil.deleteEntry(uid,type);
		}
	}

	
	
	//mainUpdateArticles
	public static void mainUpdateArticles(String [] args) throws IOException, SQLException {


		//select title to art prod def uid

		Map<String,String> artProdDefMap = ArticleImporter.createArticleProductDefinitionMap();

		//to do - get art uids
		Map<String,String> artUidArtIdMap =getArticleUidArticleIDMap();
		

		//to do - get corresponding mid ,an (booksource comes from selecting the article

		Map<String,String>artIdArtDefTitle = getArticleIdArtDefTitle();
		
		//Map<String,String>artIdCollectionMap = getArticleIdCollectionMap();

		//to do - add article product def to item
		//to do update article
		
		int count=0;

		for (String artUid : artUidArtIdMap.keySet()) {
			System.out.println(count++);
			System.out.println(artUid);

			OkHttpClient client = new OkHttpClient().newBuilder()
					.build();

			Request request = new Request.Builder()
					.url("https://api.contentstack.io/v3/content_types/article/entries?query={\"uid\":\"" + artUid +"\"}")
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
				String articleId = item.getString("article_id");
				String artDefTitle = artIdArtDefTitle.get(articleId);
				String artDefUid = artProdDefMap.get(artDefTitle);

				JSONArray apdArr = new JSONArray();

				JSONObject cont = new JSONObject();
				cont.put("uid",artDefUid);
				cont.put("_content_type_uid", "article_product_definition");
				apdArr.put(cont);
				item.put("article_definitions",apdArr);
				
				

				
				
                /*
            	
				String collUid = artIdCollectionMap.get(articleId);
				JSONArray collArr = new JSONArray();
				JSONObject collObj = new JSONObject();
				collObj.put("uid", collUid);
				collObj.put("_content_type_uid", "collection");
				collArr.put(collObj);
				item.put("collections",collArr);
							*/								
								
				ContentstackUtil.updateArticle(artUid,item);
				
				
				

			}
			
		}

		
	}

	private static Map<String, String> getArticleIdCollectionMap() throws IOException, SQLException {
		Map<String,String> collMap = ArticleImporter.createCollectionMap();
		Map<String,String>map = new HashMap<String,String>();
		String qry = "select article_id, book_id "


			+ "	from cms.t_articles ta where article_id like '%pov_%' and brst_category is not null  and research_starter is null and mfs_an is not null";




		Connection conn = DBUtil.getPostgresConn();

		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery(qry);


		while (rs.next()) {
	
			String articleId = rs.getString(1);

			String bookId =rs.getString(2);
			
			String collUid = collMap.get(bookId);
             map.put(articleId, collUid);


		}

		conn.close();        
		return map;
	}


	private static Map<String, String> getArticleIdArtDefTitle() throws SQLException {
		Map<String,String>map = new HashMap<String,String>();
		String qry = "select article_id,  mfs_an, article_mid "



			+ "	from cms.t_articles ta where "
	
			+ "  book_id  like '%pov_%'   and research_starter is null and  do_not_use ='false'";




		Connection conn = DBUtil.getPostgresConn();

		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery(qry);


		while (rs.next()) {
	
			String articleId = rs.getString(1);

			String mfsAn = rs.getString(2);

			String mid =rs.getString(3);
			
			
			String title = mid + "_" +mfsAn;

             map.put(articleId, title);


		}

		conn.close();        
		return map;
	}

	private static Map<String, String> getArticleUidArticleIDMap() throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		String contentTypeId = "article";
		int totalEntries = ContentstackUtil.getCountOfEntries("article");
		
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
				if (item.has("article_id") && !item.isNull("article_id")) {
					String articleId = item.getString("article_id");
					map.put(uid, articleId);
				}
				//  System.out.println(contentTypeId.toUpperCase() + " MAP   " +title + " " + uid);

			}

		}
		return map;

	}
//mainImport 
	public static void main (String [] args) throws IOException, SQLException {

		//load title source, book_id-uid
		Map<String, String>titleSourceMap = ArticleImporter.createBookMap();

		//load product , product short code- uid

		Map<String,String> productMap = ArticleImporter.createProductMap();

		//select all articles from pov
		List<ArticleProductDefinitionObj> articles = getArticles( productMap, titleSourceMap);

		//create art prod def key
		// select an, title source and product and create article product definition project


		//List<ArticleProductDefinitionObj> apdList = new ArrayList<ArticleProductDefinitionObj>() ;

		System.out.println("Articles " + articles.size());
		int count = 0;
		for (ArticleProductDefinitionObj ao : articles) {

			System.out.println(count++);
			String mfsAn = ao.getAn();
			
			String titleSourceBookId = ao.getTitleSource();
			
			String title = ao.getTitle();
			java.sql.Date dtformat = ao.getDtformat();

			ArticleProductDefinitionObj apdo = new ArticleProductDefinitionObj(title, mfsAn, titleSourceBookId, dtformat);


			ContentstackUtil.importArticleProductDefinition(apdo);
			

		}

		//import article prod def



	}


	public static List<ArticleProductDefinitionObj> getArticles( Map<String, String> productMap, Map<String, String> bookMap) throws SQLException {
		// TODO Auto-generated method stub
		List<ArticleProductDefinitionObj>apds = new ArrayList<ArticleProductDefinitionObj>(); 
		String qry = "select  mfs_an, article_mid, dtformat "

			+ "	from cms.t_articles ta where book_id  like '%pov_%'   and research_starter is null and  do_not_use ='false'";


		

		Connection conn = DBUtil.getPostgresConn();

		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery(qry);


		while (rs.next()) {



			String mfsAn = rs.getString(1);

			String mid = rs.getString(2);

			String bookSource =bookMap.get(mid);

			java.sql.Date dtFormat = rs.getDate(3);

			
			String title = mid ;
			ArticleProductDefinitionObj apd = new ArticleProductDefinitionObj(title, mfsAn, bookSource, dtFormat);
			apds.add(apd);



		}

		conn.close();        
		return apds;
	}


	



}
