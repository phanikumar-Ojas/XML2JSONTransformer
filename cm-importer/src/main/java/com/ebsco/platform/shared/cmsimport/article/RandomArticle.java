package com.ebsco.platform.shared.cmsimport.article;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.json.JSONObject;

import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import com.ebsco.platform.shared.cmsimport.utilities.DBUtil;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RandomArticle {

	
	private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");

	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");
	
	
	public static void mainRandom (String [] args) throws SQLException {
		int sizeOfArticles= 30;
		
		List<ArticleObj> ukArticles = getArticles ("us");
		
		int ukSize = ukArticles.size();
		List<Integer> ukNos =  randomNumbersRange(sizeOfArticles, ukSize);
		
		for (Integer index : ukNos) {
			ArticleObj ao = ukArticles.get(index);
			System.out.println(  ao.getArticleId() + "\t" +ao.getMfsAn()+ "\t" + ao.getArticleTitle()  );
		}
 		
		
		//List<ArticleObj> usArticles = getArticles ("us");
		
		//List<ArticleObj> ausArticles = getArticles ("aus");
		
		//List<ArticleObj> canArticles = getArticles ("can");

		
	}
	
	
	public static void main (String [] args) throws IOException {
		
		JSONObject jo = new JSONObject();
		JSONObject entry = new JSONObject();



		entry.put("title","random");
		entry.put("article_id", "1234");
		entry.put("current_update_type", "random");
		
		jo.put("entry", entry);
		String jsonStr= jo.toString();

		System.out.println(jo.toString());

		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, jsonStr);
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/blog_post/entries?locale=en-us")
				.method("POST", body)
				.addHeader("api_key",  API_KEY)
				.addHeader("authorization", MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = client.newCall(request).execute();
		System.out.println(response.body().string());
		response.body().close() ;
	}
	
	private static List<Integer>randomNumbersRange(int sizeOfArticles, int totalSizeOfList) {
		List<Integer>ids =new ArrayList<Integer>();
		Random r = new Random();
		Set<Integer> uniqueNumbers = new HashSet<>();
		while (uniqueNumbers.size()<sizeOfArticles){
		    uniqueNumbers.add(r.nextInt(totalSizeOfList));
		}
		for (Integer i : uniqueNumbers){
		    ids.add(i);
		}
		return ids;
	}
	
	

	private static List<ArticleObj> getArticles(String region) throws SQLException {
		// TODO Auto-generated method stub
		List<ArticleObj>povArticles = new ArrayList<ArticleObj>(); 
		String qry = "select article_title, article_id,  mfs_an "+

			"	from cms.t_articles ta where  brst_category is not null  and research_starter is null and mfs_an is not null ";


		if (region.equals("uk")) {
			qry+="and article_id like 'pov_uk_%'";
		}
		else if (region.equals("us")) {
			qry+="and (article_id like 'pov_usa_%' or article_id like 'pov_us_%')";
			
		}
		
		else if (region.equals("aus")) {
			qry+="and article_id like 'pov_aus_%'";
		}
		
		else if (region.equals("can")) {
			qry+="and article_id like 'pov_can_%'";
		}
		

		Connection conn = DBUtil.getPostgresConn();

		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery(qry);


		while (rs.next()) {

			String articleTitle = rs.getString(1);
			String  articleId =rs.getString(2);

			String mfsAn = rs.getString(3);
			ArticleObj pa = new ArticleObj();
			pa.setArticleId(articleId);
			pa.setArticleTitle(articleTitle);
			pa.setMfsAn(mfsAn);
			povArticles.add(pa);
		}
		conn.close();
		return povArticles;
	}
}
