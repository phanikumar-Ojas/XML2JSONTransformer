package com.ebsco.platform.shared.cmsimport.mfssync;

import static com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil.updateArticle;
import static com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil.getTitleSourceUid;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ebsco.platform.shared.cmsimport.article.ArticleObj;
import com.ebsco.platform.shared.cmsimport.article.productdefinition.ArticleProductDefinitionObj;
import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil;
import com.ebsco.platform.shared.cmsimport.utilities.DBUtil;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MFSSyncer {


	private static final String ALL_CONTENT_STR = "ALL";

	private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");

	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");

	private static final String PROJECT_ID_STR = "projectId";





	private static void sycnWithAllMFS(Connection conn) throws IOException, SQLException {
		Map<String,String>mfsAnToArticleUidMap = new HashMap<String,String>();
		Set<String>allNonDeletedArticlesFromCMS = new HashSet<String>();
		Map<String,String>allTitlesFromCMS = new HashMap<String,String>();
		Map<String,String>allDtFormatFromCMS = new HashMap<String,String>();
		Map<String, String> midTitleMap = new HashMap<String, String> ();
		//System.out.println("setArticleDtFormatTitlesFieldsFromCMS");
		setArticleDtFormatTitlesFieldsFromCMS(allNonDeletedArticlesFromCMS, allTitlesFromCMS,allDtFormatFromCMS, midTitleMap, mfsAnToArticleUidMap);

		
		Set<String>allDeletedArticlesFromMFS = new HashSet<String>();
		Map<String,String>allTitlesFromMFS = new HashMap<String,String>();
		Map<String,Date>allDtFormatFromMFS = new HashMap<String,Date>();
		Map<String,String>uidMissingAnMidMap = new HashMap<String,String>();
		


		//System.out.println("setArticleDtFormatTitlesFieldsFromMFS");

		setArticleDtFormatTitlesFieldsFromMFS(allDeletedArticlesFromMFS, allTitlesFromMFS,allDtFormatFromMFS,  midTitleMap, uidMissingAnMidMap, conn );
		
		updateMissingAns( uidMissingAnMidMap);

		
		//update deleted
		updateDeletedArticles(allNonDeletedArticlesFromCMS,allDeletedArticlesFromMFS , mfsAnToArticleUidMap );
		
		//update titles
		updateNonMatchingTitles(allTitlesFromCMS,allTitlesFromMFS,mfsAnToArticleUidMap );
		//update dtformat
		updateNonMatchingDtFormat(allDtFormatFromCMS,allDtFormatFromMFS, mfsAnToArticleUidMap);
	}






	private static Map<String,String> getNewTitleSources() throws IOException{
		Map<String,String> newMidsUidMap = new HashMap<String,String>();
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/book_source/entries?query={\"is_new\":true}")
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
			//System.out.println("NEW TITLE SOURCE " + item);

			if (item.has("mid") && !item.isNull("mid")) {
				String uid = item.getString("uid");
				String mid = item.getString("mid");
				newMidsUidMap.put(mid, uid);
			}

		}

		return newMidsUidMap;



	}






	private static void syncWithProjectOnly(String projectId, Connection conn) throws IOException, SQLException {
		List<ArticleObj> projectArticles;
		int numberOfArticles = ContentstackUtil.getCountOfArticlesTiedToProject(projectId);
		projectArticles =  getArticlesTiedToProject(projectId, numberOfArticles);
		
		
		System.out.println("Number of articles in the project " + projectArticles.size());

		updateMFSAns(projectArticles, conn);

		//update do_not_use,title, dtformat, lexile, word count, arttype

		updateArticleMetadata(projectArticles,conn);
		
		//update title sources to have products
		
		//get title source  uid to mid map
		Map<String,String>titleSourceUidMidMap = getTitleSourceUidMidMap(projectId, numberOfArticles);
		
		//create short product code product uid map
		Map<String,String>productCodeProductIdMap = getProductCodeProductUidMap();
		
		for (String titleSourceUid : titleSourceUidMidMap.keySet()) {
			
			String mid = titleSourceUidMidMap.get(titleSourceUid);
			List<String>productCodesPerMid = getProductCodesByMid(mid, conn);
			updateProductCodesForTitleSource(titleSourceUid, productCodesPerMid, productCodeProductIdMap);
		}
		
		//updateDeletedArticles(projectArticles, conn);
		//updateTitlesOfArticles(projectArticles, conn);
		//updateDTFormat(projectArticles,conn);
	}
	
	
	



	private static void updateProductCodesForTitleSource(String titleSourceUid, List<String> productCodesPerMid,
			Map<String, String> productCodeProductIdMap) {
		
		List<String>productUids = new ArrayList<String>();
		
		for (String prodCode : productCodesPerMid) {
			
			String existingProductUid  = productCodeProductIdMap.get(prodCode.toLowerCase());
			if (existingProductUid!=null) {
				productUids.add(existingProductUid);
			}
			else {
				//create new product
				String newProductUid = createNewProduct(prodCode.toLowerCase());
				productUids.add(newProductUid);
			}
		}
		
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();

				Request request = new Request.Builder()
				  .url("https://api.contentstack.io/v3/content_types/book_source/entries?query={\"uid\":\"" + titleSourceUid +"\"}")
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
				
			JSONObject jo = new JSONObject(jsonStr);
			JSONArray entriesArray = jo.getJSONArray("entries");
			for (int j=0; j<entriesArray.length(); j++) {
			    JSONObject item = entriesArray.getJSONObject(j);
			   
			    JSONArray productArr =new JSONArray();
			    
				for (String productUid : productUids) {
					JSONObject pro = new JSONObject();
					pro.put("uid", productUid);

					pro.put("_content_type_uid", "product");
					productArr.put(pro);
				}
				item.put("products",productArr);

				updateObject("book_source", titleSourceUid, item);
			}
		
	}






	private static String createNewProduct(String prodCode) {
		
		System.out.println("PROD CODE " + prodCode);
		JSONObject jo = new JSONObject();
		JSONObject entry = new JSONObject();


		String title =prodCode;


		entry.put("title", title);




		jo.put("entry", entry);

		String jsonStr= jo.toString();


		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, jsonStr);
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/product/entries?locale=en-us")
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		

		System.out.println("Response STR " + responseStr);
		JSONObject responseObj = new JSONObject(responseStr);
		if (responseObj.has("entry") && !responseObj.isNull("entry")) {
			JSONObject entryObj = responseObj.getJSONObject("entry");
			return entryObj.getString("uid");
		}
		
		return null;
	}






	private static List<String> getProductCodesByMid(String mid, Connection conn) {
		List<String>productCodes = new ArrayList<String>();
		String qry = "SELECT DISTINCT prodcode FROM  MAGPRODCOVRG  WHERE mid = ?";

		try {
			PreparedStatement st = conn.prepareStatement(qry);

			st.setString(1, mid);
			
			ResultSet rs = st.executeQuery();
			while ( rs.next() )
			{
				
				String prodCode = rs.getString(1);
				productCodes.add(prodCode);

			}
			rs.close();
			st.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		return productCodes;
	}






	private static Map<String, String> getProductCodeProductUidMap() throws IOException {
		Map<String,String>productCodeToUidMap = new HashMap<String,String>();
		int totalEntries = ContentstackUtil.getCountOfEntries("product");
		String contentTypeId= "product";
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
					String title = item.getString("title");
					productCodeToUidMap.put(title,uid);
				}
				//  System.out.println(contentTypeId.toUpperCase() + " MAP   " +title + " " + uid);

			}

		}
		return productCodeToUidMap;
	}






	private static Map<String, String> getTitleSourceUidMidMap(String projectId, int totalEntries) throws IOException {
		Map<String,String>titleSourceUidMidMap = new HashMap<String,String>();
		Set<String>allTitleSourceUids = new HashSet<String>();
		//String query="{\"current_project\": { \"$in_query\": { \"title\": \"" +  projectId +"\"}}}";
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
			Response response = client.newCall(request).execute();
			String jsonStr = response.body().string();

			//System.out.println(jsonStr);
			JSONObject jo = new JSONObject(jsonStr);
			JSONArray entriesArray = jo.getJSONArray("entries");
			for (int j=0; j<entriesArray.length(); j++) {
				JSONObject item = entriesArray.getJSONObject(j);
				//System.out.println(item);
				
				JSONArray artDefArr = item.getJSONArray("article_definitions");
				List<String>artDefUids = new ArrayList<String>();
				for (int k=0; k<artDefArr.length();k++) {
					JSONObject artDefObj = artDefArr.getJSONObject(k);
					String artDefUid = artDefObj.getString("uid");
					artDefUids.add(artDefUid);
				}
				List<String> titleSourcEUids = getTitleSourceUidFromArtDefUids(artDefUids);
				allTitleSourceUids.addAll(titleSourcEUids);

			}

		}
		
		


		for (String titleSourceUid : allTitleSourceUids) {
			String mid = getMidFromTitleSource(titleSourceUid);
			titleSourceUidMidMap.put(titleSourceUid, mid);
		}
		
		return titleSourceUidMidMap;
	}







	private static void updateArticleMetadata(List<ArticleObj> projectArticles, Connection conn) throws SQLException, IOException {
		
		Map<String,Date>anDtFormatMap = new HashMap<String,Date>();
		List<String>ans = getAnsFromArticles(projectArticles);
		if (ans.isEmpty()) {
			return;
		}
		Map<String,ArticleObj> map = extractAnArticleObjMap(projectArticles);
		String qry = "SELECT an, arttitle, dtformat,updatetype,wrdcnt,lexrank FROM mfs.article WHERE an in (";
		for (String an : ans) {
			qry += "?,";
		}

		qry= qry.substring(0,qry.length()-1) + ")";

		System.out.println(qry);
		PreparedStatement st = conn.prepareStatement(qry);

		int index=1;

		for (String an : ans) {
			st.setString(index++, an);
		}

		ResultSet rs = st.executeQuery();
		while ( rs.next() )
		{
			
			String an = rs.getString(1);
			String arttitle = rs.getString(2);
			if (arttitle!=null) {
				if (arttitle.endsWith(".")) {
					arttitle = arttitle.substring(0, arttitle.length()-1);
				}
			}
			Date dtformat = rs.getDate(3);
			
		
			System.out.println("Project articles AN & DTFORMAT" + an + " " + dtformat);
			String updateType = rs.getString(4);
			int wordCount = rs.getInt(5);
			int lexile = rs.getInt(6);
			ArticleObj ao = map.get(an);
			ao.setArticleTitle(arttitle);
			ao.setMfsAn(an);
			anDtFormatMap.put(an, dtformat);
			if (updateType.equals("D")) {
				ao.setDoNotUse(true);
			}
			ao.setWordCount(wordCount);
			ao.setLexile(lexile);
		}
		rs.close();
		st.close();



		for (ArticleObj ao  : projectArticles) {
			OkHttpClient client = new OkHttpClient().newBuilder()
					.build();

			Request request = new Request.Builder()
					.url("https://api.contentstack.io/v3/content_types/article/entries?query={\"uid\":\"" + ao.getArticleId() +"\"}")
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
				item.put("title", ao.getArticleTitle());
				
				item.put("word_count", ao.getWordCount());
				item.put("lexile", ao.getLexile());
				if (ao.isDoNotUse()) {
					item.put("do_not_use", true);
				}
				updateArticle(ao.getArticleId(), item);


				if (item.has("article_definitions") && !item.isNull("article_definitions")) {
					JSONArray artDefsArray = item.getJSONArray("article_definitions");
					for (int i=0; i<artDefsArray.length(); i++) {
						JSONObject artDefObj = artDefsArray.getJSONObject(i);
						String artDefUid = artDefObj.getString("uid");
						
						JSONObject fullArtDefObj = getArticleDefinitionObjectByUid(artDefUid);
						String an = fullArtDefObj.getString("an");
						if (an.equals(ao.getMfsAn())) {
							Date dtformat = anDtFormatMap.get(an);
							if (dtformat!=null) {
								String dateStr = extractDate(dtformat.toString());
								fullArtDefObj.put("dtformat", dateStr);
								updateArticleDefinition(artDefUid, fullArtDefObj);
							}
						}
					}
				}

			}
		}


	}






	private static JSONObject getArticleDefinitionObjectByUid(String artDefUid) {
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();

		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/article_product_definition/entries?query={\"uid\":\"" + artDefUid +"\"}")
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
		//System.out.println("https://api.contentstack.io/v3/content_types/article/entries?query={\"uid\":\"" + articleUid +"\"}");
		
		JSONObject apdObj = new JSONObject(jsonStr);
		JSONArray apdArray = apdObj.getJSONArray("entries");
		
		if (apdArray.length()>0) {
			return apdArray.getJSONObject(0);
		}
		else {
			return null;
		}
	}






	private static Map<String, ArticleObj> extractAnArticleObjMap(List<ArticleObj> projectArticles) {
		
		
		Map<String,ArticleObj> map = new HashMap<String,ArticleObj>();
		for (ArticleObj ao : projectArticles) {
			List<String> ans = ao.getMfsAns();
			for (String an : ans) {
				map.put(an, ao);
			}
		}
		return map;
	}






	private static void updateDeletedArticles(Set<String> allNonDeletedArticlesFromCMS,
			Set<String> allDeletedArticlesFromMFS, Map<String, String> mfsAnToArticleUidMap) throws IOException {
		for (String an : allNonDeletedArticlesFromCMS) {
			if (allDeletedArticlesFromMFS.contains(an)) {
				String artUid = mfsAnToArticleUidMap.get(an);
				updateDoNotUseForArticle(artUid);
			}
		}
		
	}





	private static void updateDoNotUseForArticle(String artUid) throws IOException {
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
	    		item.put("do_not_use", true);
	    		updateArticle(artUid, item);


	
			}
	}






	private static void updateMissingAns(Map<String, String> uidMissingAnMidMap) throws IOException {
		for (String uid : uidMissingAnMidMap.keySet()) {
			String anMid  = uidMissingAnMidMap.get(uid);
			String [] toks = anMid.split("_");
			String an = toks[0];
			String mid = toks[1];
			updateAnForArticle(uid, an, mid);
		}
		
	}






	private static void updateAnForArticle(String uid, String an, String mid) throws IOException {
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
		//System.out.println("uid an mid " + uid + " " +an + " " + mid);
		//System.out.println(jsonStr);
		JSONObject jo = new JSONObject(jsonStr);
		JSONArray entriesArray = jo.getJSONArray("entries");
		for (int j=0; j<entriesArray.length(); j++) {
			JSONObject item = entriesArray.getJSONObject(j);



			JSONArray artDefArr = item.getJSONArray("article_definitions");
			List<String>artDefUids = new ArrayList<String>();
			for (int k=0; k<artDefArr.length();k++) {
				JSONObject artDefObj = artDefArr.getJSONObject(k);
				String artDefUid = artDefObj.getString("uid");
				artDefUids.add(artDefUid);


			}

			for (String artDefUid : artDefUids) {
				client = new OkHttpClient().newBuilder()
						.build();

				request = new Request.Builder()
						.url("https://api.contentstack.io/v3/content_types/article_product_definition/entries?query={\"uid\":\"" + artDefUid +"\"}")
						.method("GET", null)
						.addHeader("api_key", API_KEY)
						.addHeader("authorization",MANAGEMENT_TOKEN)
						.addHeader("Content-Type", "application/json")
						.build();
				response = client.newCall(request).execute();
				//System.out.println("https://api.contentstack.io/v3/content_types/article/entries?query={\"uid\":\"" + articleUid +"\"}");
				jsonStr = response.body().string();
				//System.out.println(jsonStr);
				JSONObject apdObj = new JSONObject(jsonStr);
				JSONArray apdArray = apdObj.getJSONArray("entries");
				for (int index=0; index<apdArray.length(); index++) {
					JSONObject apdO = apdArray.getJSONObject(index);

					if (apdO.has("mid") && !apdO.isNull("mid") ){
						String midApd = apdO.getString("mid");
						if (midApd.equals(mid)) {
							apdO.put("an", an);
							String apdUid = apdO.getString("uid");
							//apdO.put("title", mid +"_" + mid + "_" +an);
							updateArticleDefinition(apdUid, apdO);
						}


					}


				}

			}


		}

		
		
	}






	private static void updateNonMatchingDtFormat(Map<String, String> allDtFormatFromCMS,
			Map<String, Date> allDtFormatFromMFS, Map<String, String> mfsAnToArticleUidMap) throws IOException {
		for (String an : allDtFormatFromCMS.keySet()) {
			//System.out.println("an " + an);
			String cmsDtformat = allDtFormatFromCMS.get(an);
			Date mfsDtformat = allDtFormatFromMFS.get(an);

			String mfsDtformatStr = null;
			if (mfsDtformat!=null) {
				mfsDtformatStr= mfsDtformat.toString();
			}
			String articleUid = mfsAnToArticleUidMap.get(an);				

			if (mfsDtformat!=null) {

				if (cmsDtformat==null) {
					System.out.println("cms dtformat " + cmsDtformat + " mfs dtformat " + mfsDtformatStr);

					setDtFormat(articleUid, an, mfsDtformat);

				}

				else if (!cmsDtformat.equals(mfsDtformatStr)) {
					System.out.println("cms dtformat " + cmsDtformat + " mfs dtformat " + mfsDtformatStr);

					setDtFormat(articleUid, an, mfsDtformat);
				}
			}
		}
	}






	private static void updateNonMatchingTitles(Map<String, String> allTitlesFromCMS,
			Map<String, String> allTitlesFromMFS, Map<String, String> mfsAnToArticleUidMap) throws IOException {

		for (String an : allTitlesFromCMS.keySet()) {
			String cmsTitle = allTitlesFromCMS.get(an);
			String mfsTitle = allTitlesFromMFS.get(an);
			mfsTitle = cleanMFSTitle(mfsTitle);
			
			if (mfsTitle!=null && !cmsTitle.equals(mfsTitle)) {
				String cmsTitleWithPeriod = cmsTitle + ".";
		
				if (!cmsTitleWithPeriod.equals(mfsTitle)) {
					String articleUid = mfsAnToArticleUidMap.get(an);		
					System.out.println("cms title " + cmsTitle  + " mfs title" + mfsTitle);
					if (mfsTitle.endsWith(".")) {
						mfsTitle = mfsTitle.substring(0,mfsTitle.length()-1);
					}
					setTitle(articleUid, mfsTitle);

				}
			}
		}

	}






	private static String cleanMFSTitle(String mfsTitle) {
		if (mfsTitle==null) {
			return null;
		}
		String mfsTitleCleaned = mfsTitle;
		mfsTitleCleaned = mfsTitleCleaned.replaceAll("&#8217;", "'");
		mfsTitleCleaned = mfsTitleCleaned.replaceAll("&#8220;", "\"");
		mfsTitleCleaned = mfsTitleCleaned.replaceAll("&#8221;", "\"");
		mfsTitleCleaned = mfsTitleCleaned.replaceAll("&eacute;", "é");

		return mfsTitleCleaned;
		
	}






	private static void setArticleDtFormatTitlesFieldsFromCMS(Set<String> allNonDeletedArticlesFromCMS,
			Map<String, String> allTitlesFromCMS, Map<String, String> allDtFormatFromCMS, Map<String, String> midTitleMap, Map<String, String> mfsAnToArticleUidMap) throws IOException {
		Map<String,String>artDefUidToMidMap = new HashMap<String,String>();
		Map<String,String>artDefUidToAnMap = new HashMap<String,String>();
		Map<String,String>artDefUidToDtformatMap = new HashMap<String,String>();

		createArticleDefinitionUidToAnMap(artDefUidToAnMap, artDefUidToMidMap,artDefUidToDtformatMap );

		String contentTypeId = "article";
		
		long start = java.lang.System.currentTimeMillis() ;
		int totalEntries = ContentstackUtil.getCountOfEntries("article");

		int index = (totalEntries/100)+1;

		for (int i=0;i<index;i++) {
			int skip=i *100;
			String urlStr= "https://api.contentstack.io/v3/content_types/" + contentTypeId + "/entries?&skip=" +skip;
			//System.out.println(urlStr);
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
				List<String>mfsAns = new ArrayList<String>();
				List<String>mids = new ArrayList<String>();

				JSONObject item = entriesArray.getJSONObject(j);
				String title = null;
				boolean doNotUse = false;

				String uid = item.getString("uid");
				if (item.has("title")) {
					title = item.getString("title");
				}

				if (item.has("do_not_use")) {
					doNotUse = item.getBoolean("do_not_use");
				}
				if (item.has("article_definitions")) {
					JSONArray artDefArr = item.getJSONArray("article_definitions");
					for (int k=0; k<artDefArr.length();k++) {
						JSONObject artDefObj = artDefArr.getJSONObject(k);
						String artDefUid = artDefObj.getString("uid");
						String an = artDefUidToAnMap.get(artDefUid);
						String mid = artDefUidToMidMap.get(artDefUid);
						String dtformat = artDefUidToDtformatMap.get(artDefUid);
						if (mid!=null) {
						 mids.add(mid);
						}
						
						if (an!=null && !an.isEmpty()) {
							mfsAns.add(an);
							if (dtformat!=null) {
								allDtFormatFromCMS.put(an, dtformat);
							}
						}
						
						mfsAnToArticleUidMap.put(an, uid);
					}
				}
				for (String an : mfsAns) {
					if ( doNotUse==false) {
						allNonDeletedArticlesFromCMS.add(an);
					}
					allTitlesFromCMS.put(an, title);
					
				}
				
				if (mfsAns.size()==0) {
					System.out.println("MFS AN SIZE 0 - " + uid);
					if (mids.size()>0) {
						//System.out.println("mids size greater than 0");
						for (String mid : mids) {
							midTitleMap.put(mid + "_" + title, uid);
							
						}
					}
				}
			}

		}
		
		long end = java.lang.System.currentTimeMillis() ;
		//System.out.println("TIME ELAPSED SCANNING ARTICLES " + (end-start));

	}






	private static void createArticleDefinitionUidToAnMap(Map<String, String> artDefUidToAnMap, Map<String, String> artDefUidToMidMap,  Map<String, String> artDefUidToDtformatMap) throws IOException {
		String contentTypeId = "article_product_definition";

		int totalEntries = ContentstackUtil.getCountOfEntries(contentTypeId);
		int index = (totalEntries/100)+1;

		for (int i=0;i<index;i++) {
			int skip=i *100;
			String urlStr= "https://api.contentstack.io/v3/content_types/" + contentTypeId + "/entries?&skip=" +skip;
			//System.out.println(urlStr);
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

				if (item.has("an")) {
					String an = item.getString("an");
					artDefUidToAnMap.put(uid, an);
				}
				
				if (item.has("mid")) {
					String mid = item.getString("mid");
					artDefUidToMidMap.put(uid, mid);
				}
				
				if (item.has("dtformat")) {
					String dtformatStr = extractDate(item.getString("dtformat"));
					artDefUidToDtformatMap.put(uid, dtformatStr);
				}
				
			}

		}

	}






	private static void setArticleDtFormatTitlesFieldsFromMFS(Set<String> allDeletedArticlesFromMFS,
			Map<String, String> allTitlesFromMFS, Map<String, Date> allDtFormatFromMFS, Map<String, String> midTitleMap, Map<String, String> uidMissingAnMap, Connection conn) throws SQLException {
		String qry = "SELECT an, arttitle, dtformat,UPDATETYPE, ma.mid FROM mfs.article ma JOIN mfs.magtitle  mt ON ma.mid = mt.mid  WHERE  mt.pid IN (345,8275,11528,87894)";


		PreparedStatement st = conn.prepareStatement(qry);


		int count=0;


		ResultSet rs = st.executeQuery();
		while ( rs.next() )
		{

			String an = rs.getString(1);
			String title = rs.getString(2);
			Date dtformat  = rs.getDate(3);
			//System.out.println("mfs  " + count++);
			String updateType = rs.getString(4);
			String mid = rs.getString(5);
			if (updateType!=null) {
				if (updateType.equals("D")) {
					allDeletedArticlesFromMFS.add(an);
				}
			}
			allTitlesFromMFS.put(an, title);
			allDtFormatFromMFS.put(an,dtformat);
			String articleUid = midTitleMap.get(mid + "_" +title)	;
			
			
			
			
			if (articleUid==null) {
				if (title.endsWith(".")) {
					articleUid = midTitleMap.get(mid + "_" +title.substring(0,title.length()-1));	
					//System.out.println(articleUid + " " + mid + " " +title.substring(0,title.length()-1) );
				}
			}
			if (articleUid!=null) {
				uidMissingAnMap.put(articleUid,an + "_" + mid);
			}
		}
		rs.close();
		st.close();

	}












	private static void updateLexileForDerivedArticles(List<ArticleObj> articles) throws IOException {
		for (ArticleObj ao : articles) {
			String mfsAn = ao.getMfsAn();
			int lexile  = ao.getLexile();
			String articleId = getArticleIdByMfsAn(mfsAn);
			if (articleId!=null) {
				OkHttpClient client = new OkHttpClient().newBuilder()
						.build();
				Request request = new Request.Builder()
						.url("https://api.contentstack.io/v3/content_types/article/entries?query={\"derived_from_id\":\"" + articleId +"\"}")
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
					String uid = item.getString("uid");
					item.put("lexile", lexile);
					updateArticle(uid, item);


				}
			}

		}
	}






	private static String getArticleIdByMfsAn(String mfsAn) throws IOException {
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/article/entries?query={\"article_definitions\":{\"$in_query\":{\"an\":\""  + mfsAn +"\"}}}")
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
			return item.getString("article_id");
			

		}
		return null;
	}






	private static void updateMFSAns(List<ArticleObj> projectArticles, Connection conn) throws IOException, SQLException {
		// TODO Auto-generated method stub
		
		List<ArticleObj>articlesWithoutMfsAn = new ArrayList<ArticleObj>();
		for (ArticleObj ao : projectArticles) {


			if (ao.getMfsAns().size()==0) {
				articlesWithoutMfsAn.add(ao);
				System.out.println("articles without Mfs an " + ao.getArticleTitle());
			}
		}
		
		for (ArticleObj ao : articlesWithoutMfsAn) {
			
			String articleTitle = ao.getArticleTitle();
			Map<String,ArticleProductDefinitionObj>  apdos = getArticleDefinitionObjects(ao.getArticleId());
			
			updateAnFromMFs(ao,apdos,articleTitle,conn);
			//getAnFromMFS()
			//set an
		}
		
	}






	private static void updateAnFromMFs(ArticleObj ao, Map<String,ArticleProductDefinitionObj>  apdos, String articleTitle, Connection conn) throws IOException, SQLException {

		for (String apdoUId :apdos.keySet()) {
			//System.out.println("UPDATE AN FROM MFS");
			ArticleProductDefinitionObj apdo = apdos.get(apdoUId);
			String tsource = apdo.getTitleSource();
			String mid = getMidFromTitleSource(tsource);

	
			
			if (mid!=null) {
				
				String anDtFormat = getMFSAnDtformatFromTitleMid(articleTitle, mid, conn);
				if (anDtFormat!=null) {
					String [] toks = anDtFormat.split("_");
					String an = toks[0];
					String dtformat  = toks[1];
					updateAnAndDtformatOfArticleDefinitionObj(apdoUId,an,dtformat,mid);
					List<String>ans  = new ArrayList<String>();
					ans.add(an);
					ao.setMfsAns(ans);
				}
				//query mfs for  
			}
		}
	}



	private static void updateAnAndDtformatOfArticleDefinitionObj(String uid, String an,String dtformat,String mid) throws IOException {
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();

		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/article_product_definition/entries?query={\"uid\":\"" + uid +"\"}")
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

			String title = mid;
			item.put("an", an);
			item.put("title", title);
			item.put("dtformat", dtformat);

			updateArticleDefinition(uid, item);

		}

	}






	private static void updateArticleDefinition(String uid, JSONObject item) {
		// TODO Auto-generated method stub

		JSONObject jo = new JSONObject();
		jo.put("entry", item);
		String jsonStr = jo.toString();
		//System.out.println("JSON STR " + jsonStr);
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, jsonStr);
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/article_product_definition/entries/" + uid)
				.method("PUT", body)
				.addHeader("api_key", API_KEY)
				.addHeader("authorization", MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		try {
			Response response = client.newCall(request).execute();
			String responseStr = response.body().string();
			//System.out.println("UPDATED ARTICLE DEF " + responseStr);
			//System.out.println(responseStr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}






	private static String getMFSAnDtformatFromTitleMid(String articleTitle, String mid, Connection conn) throws SQLException {
		String qry= "SELECT an, dtformat from mfs.article WHERE  mid = ? AND arttitle = ?";
		PreparedStatement st = conn.prepareStatement(qry);
		

			st.setString(1, mid);
			st.setString(2, articleTitle);
			
			   ResultSet rs = st.executeQuery();
			    while ( rs.next() )
			    {
			    	String an = rs.getString(1);
			    	Date dtformat = rs.getDate(2);
			    	String anDt = an + "_" + dateToFormattedStr(dtformat);
			    	return anDt;
			    	
			    }
			    rs.close();
			    st.close();
			    
			    return null;
	}




	private static String dateToFormattedStr(Date dtFormat) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf = new SimpleDateFormat(
			    "MM/dd/yyyy");
		return sdf.format(dtFormat).toString();
	}


	//update updo obj


	private static String getMidFromTitleSource(String tsource) throws IOException {
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/book_source/entries?query={\"uid\":\"" + tsource +"\"}")
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
			if (item.has("mid") && !item.isNull("mid"))
				return item.getString("mid");
		}

		return null;
		
	}






	private static Map<String,ArticleProductDefinitionObj>  getArticleDefinitionObjects(String uid) throws IOException {

		Map<String,ArticleProductDefinitionObj> apdos=  new HashMap<String,ArticleProductDefinitionObj>();
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
		for (int j=0; j<entriesArray.length(); j++) {
			JSONObject item = entriesArray.getJSONObject(j);



			JSONArray artDefArr = item.getJSONArray("article_definitions");
			List<String>artDefUids = new ArrayList<String>();
			for (int k=0; k<artDefArr.length();k++) {
				JSONObject artDefObj = artDefArr.getJSONObject(k);
				String artDefUid = artDefObj.getString("uid");
				artDefUids.add(artDefUid);
			}

			for (String artDefUid : artDefUids) {
				client = new OkHttpClient().newBuilder()
						.build();

				request = new Request.Builder()
						.url("https://api.contentstack.io/v3/content_types/article_product_definition/entries?query={\"uid\":\"" + artDefUid +"\"}")
						.method("GET", null)
						.addHeader("api_key", API_KEY)
						.addHeader("authorization",MANAGEMENT_TOKEN)
						.addHeader("Content-Type", "application/json")
						.build();
				response = client.newCall(request).execute();
				//System.out.println("https://api.contentstack.io/v3/content_types/article/entries?query={\"uid\":\"" + articleUid +"\"}");
				jsonStr = response.body().string();
				JSONObject apdObj = new JSONObject(jsonStr);
				JSONArray apdArray = apdObj.getJSONArray("entries");
				for (int index=0; index<apdArray.length(); index++) {
					JSONObject apdO = apdArray.getJSONObject(index);
					String apduid = apdO.getString("uid");

					String title= apdO.getString("title");
					String an = null;
					String titleSource = null;
					Date dtformat = null;
					if (apdO.has("an")  && !apdO.isNull("an")) {
						an =apdO.getString("an");
					}
					if (apdO.has("title_source") && !apdO.isNull("title_source")) {
						JSONArray titleSourceArr = apdO.getJSONArray("title_source"); 

						JSONObject titleSourceObj = (JSONObject) titleSourceArr.get(0);
						titleSource = titleSourceObj.getString("uid");

					}
					if (apdO.has("dtformat")  && !apdO.isNull("dtformat")) {
						String dateStr = extractDate(apdO.getString("dtformat"));
						dtformat = Date.valueOf(dateStr);
					}
					ArticleProductDefinitionObj apdo = new ArticleProductDefinitionObj(title, an,titleSource, dtformat);

					apdos.put(apduid,apdo);


				}

			}


		}

		return apdos;
			

			
			
			
	}




	private static String extractDate(String dateStr) {
		// TODO Auto-generated method stub
		String date = dateStr.substring(0,10);
		return date;
	}


	private static void updateDeletedArticles(List<ArticleObj> projectArticles, Connection conn) throws SQLException, IOException {
		
		List<String>ans = getAnsFromArticles(projectArticles);
		List<String>deletedAns = new ArrayList<String>();
		String qry = "SELECT an FROM mfs.article WHERE updatetype = 'D' AND an in (";
		for (String an : ans) {
			qry += "?,";
		}
		
		qry= qry.substring(0,qry.length()-1) + ")";
		
		PreparedStatement st = conn.prepareStatement(qry);
		
		int index=1;
		
		for (String an : ans) {
			st.setString(index++, an);
		}
		
		   ResultSet rs = st.executeQuery();
		    while ( rs.next() )
		    {
		    	String delAn = rs.getString(1);
		    	deletedAns.add(delAn);
		    	
		    }
		    rs.close();
		    st.close();
		
	
	  Map<String,String>mfsAnUidMap = getMfsAnToUidMap(projectArticles);
		    
	  for (String delAn : deletedAns) {
		  
		  String deletedArticleUid = mfsAnUidMap.get(delAn);
		  setToDoNotUse(deletedArticleUid);
	  }

		
	}





	private static void setDtFormat(String uid, String matchingAn, Date dtformat) throws IOException {


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
		for (int j=0; j<entriesArray.length(); j++) {
			JSONObject item = entriesArray.getJSONObject(j);


			if (item.has("article_definitions") && !item.isNull("article_definitions")) {
				JSONArray artDefsArray = item.getJSONArray("article_definitions");
				for (int i=0; i<artDefsArray.length(); i++) {
					JSONObject artDefObj = artDefsArray.getJSONObject(i);
					String artDefUid = artDefObj.getString("uid");
					String an = artDefObj.getString("an");
					if (an.equals(matchingAn)) {

						String dateStr = extractDate(dtformat.toString());
						artDefObj.put("dtformat", dateStr);
						updateArticleDefinition(artDefUid, artDefObj);

					}
				}
			}

		}


	}
	

	private static void updateTitlesOfArticles(List<ArticleObj> projectArticles, Connection conn) throws SQLException, IOException {
		
		List<String>ans = getAnsFromArticles(projectArticles);
		Map<String,String>anTitleMap = new HashMap<String,String>();
		String qry = "SELECT an, arttitle FROM mfs.article WHERE an in (";
		for (String an : ans) {
			qry += "?,";
		}
		
		qry= qry.substring(0,qry.length()-1) + ")";
		
		PreparedStatement st = conn.prepareStatement(qry);
		
		int index=1;
		
		for (String an : ans) {
			st.setString(index++, an);
		}
		
		   ResultSet rs = st.executeQuery();
		    while ( rs.next() )
		    {
		    	String an = rs.getString(1);
		    	String artTitle = rs.getString(2);
		    	anTitleMap.put(an,artTitle);
		    	
		    }
		    rs.close();
		    st.close();
		
	
	  Map<String,String>mfsAnUidMap = getMfsAnToUidMap(projectArticles);
		    
	  for (String an : mfsAnUidMap.keySet()) {
		  
		  String uid = mfsAnUidMap.get(an);
		  String title = anTitleMap.get(an);
		  setTitle(uid, title);
	  }
		
	}



	private static void setTitle(String uid,String title) throws IOException {

		
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
			for (int j=0; j<entriesArray.length(); j++) {
			    JSONObject item = entriesArray.getJSONObject(j);
			   
			    if (item.has("title")){
			    	String origTitle = item.getString("title");
			    	if (!origTitle.equals(title)) {
			    		item.put("title", title);
			    		updateArticle(uid, item);
			    	}
			    }
			    else {
		    		item.put("title", title);
			    	updateArticle(uid, item);
			    }

	
			}
}



	private static void setToDoNotUse(String deletedArticleUid) throws IOException {


		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();

		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/article/entries?query={\"uid\":\"" + deletedArticleUid +"\"}")
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

			if (item.has("do_not_use")){
				boolean doNotUse = item.getBoolean("do_not_use");
				if (!doNotUse) {
					item.put("do_not_use", true);
					updateArticle(deletedArticleUid, item);
				}
			}
			else {
				item.put("do_not_use", true);
				updateArticle(deletedArticleUid, item);
			}


		}
	}

		
	

	public static void updateTitleSource(String uid, JSONObject item) {

		JSONObject jo = new JSONObject();
		jo.put("entry", item);
		String jsonStr = jo.toString();
		// TODO Auto-generated method stub
		//System.out.println("JSON STR " + jsonStr);
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, jsonStr);
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/book_source/entries/" + uid)
				.method("PUT", body)
				.addHeader("api_key", API_KEY)
				.addHeader("authorization", MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		try {
			Response response = client.newCall(request).execute();
			//String responseStr = response.body().string();
			//System.out.println(responseStr);
			//System.out.println(responseStr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	public static void updateObject(String contentType, String uid, JSONObject item) {

		JSONObject jo = new JSONObject();
		jo.put("entry", item);
		String jsonStr = jo.toString();
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
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		
		
	}



	private static Map<String, String> getMfsAnToUidMap(List<ArticleObj> projectArticles) {
		 Map<String, String> map = new HashMap<String,String>();
		for (ArticleObj ao : projectArticles) {
			String uid  = ao.getArticleId();
			List<String>mfsAns = ao.getMfsAns();
			if (mfsAns.size()>0) {
				map.put(mfsAns.get(1), uid);
			}
		}
		return map;
	}





	private static List<String> getAnsFromArticles(List<ArticleObj> projectArticles) {

		List<String> ans = new ArrayList<String>();
		for  (ArticleObj ao : projectArticles) {
			ans.addAll(ao.getMfsAns());
		}
		return ans;
	}


	




	public static void main(String []args) throws  IOException, SQLException {
		
		
		
		Options options = new Options();
		
		options.addOption(Option.builder("p").longOpt(PROJECT_ID_STR).hasArg()
				.desc("project id")
				.build());
		
		
		
		CommandLineParser parser = new DefaultParser();


		String projectId = null;
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (!line.hasOption(PROJECT_ID_STR)) {

				System.out.println("You need to supply a project id with the p flag to run MFS Sync, to run on a particular project, set projectId to the particular project id ,or to run MFS Sync on all articles within the CMS, set project id to ALL");
				return;
			}
			
			else {
				projectId = line.getOptionValue(PROJECT_ID_STR);
			}

		}
		catch (ParseException exp) {
			System.out.println("Unexpected exception:" + exp.getMessage());
		}
		
	

		Connection conn = DBUtil.getMFSConn();


		
		if (projectId.equals(ALL_CONTENT_STR)) {
        	sycnWithAllMFS(conn);
    		findAndImportNewMidsAndUpdateAssociatedArticles(conn);
			
		}
		else {
			syncWithProjectOnly( projectId, conn);
			findAndImportNewMidsAndUpdateAssociatedArticles(conn);
			
		}
		conn.close();
		
		
		/*
		Connection conn = DBUtil.getMFSConn();

    	Map<String,String>mfsAnToArticleUidMap = new HashMap<String,String>();
    	Set<String>allNonDeletedArticlesFromCMS = new HashSet<String>();
    	Map<String,String>allTitlesFromCMS = new HashMap<String,String>();
    	Map<String,String>allDtFormatFromCMS = new HashMap<String,String>();
    	Map<String, String> midTitleMap = new HashMap<String, String> ();
    	System.out.println("setArticleDtFormatTitlesFieldsFromCMS");
    	
    	
    	setArticleDtFormatTitlesFieldsFromCMS(allNonDeletedArticlesFromCMS, allTitlesFromCMS,allDtFormatFromCMS, midTitleMap, mfsAnToArticleUidMap);

    	System.out.println("DT Format size from CMS " + allDtFormatFromCMS.keySet().size());
    	System.out.println(mfsAnToArticleUidMap.keySet().size() + " SIZE OF MFS ANS");
    	Set<String>allDeletedArticlesFromMFS = new HashSet<String>();
    	Map<String,String>allTitlesFromMFS = new HashMap<String,String>();
    	Map<String,Date>allDtFormatFromMFS = new HashMap<String,Date>();
    	Map<String,String>uidMissingAnMidMap = new HashMap<String,String>();
    	
    	
    	
    	System.out.println("setArticleDtFormatTitlesFieldsFromMFS");

    	setArticleDtFormatTitlesFieldsFromMFS(allDeletedArticlesFromMFS, allTitlesFromMFS,allDtFormatFromMFS,  midTitleMap, uidMissingAnMidMap, conn );
    	
    	
    	
    	System.out.println("updateDeletedArticles");

    	
    	//update deleted
    	updateDeletedArticles(allNonDeletedArticlesFromCMS,allDeletedArticlesFromMFS , mfsAnToArticleUidMap );
    	System.out.println("updateNonMatchingTitles");

    	
    	//update titles
    	updateNonMatchingTitles(allTitlesFromCMS,allTitlesFromMFS,mfsAnToArticleUidMap );
    	//update dtformat
    	
    	System.out.println("updateNonMatchingDtFormat");

    	updateNonMatchingDtFormat(allDtFormatFromCMS,allDtFormatFromMFS, mfsAnToArticleUidMap); 
    	
    	//update missing ans
    	System.out.println("updateMissingAns");

    	updateMissingAns( uidMissingAnMidMap);
    	conn.close();
		
		
    	syncWithProjectOnly(projectName,conn)
		

		*/
    	


	}






	private static void findAndImportNewMidsAndUpdateAssociatedArticles(Connection conn) throws IOException {
		Map<String,String> newTitleSourceMidUidMap = getNewTitleSources();
		


		//Select mid metadata from MFS for the mid difference set
		importNewMidsAndUpdateAssociatedArticles(conn, newTitleSourceMidUidMap);
	}






	private static void importNewMidsAndUpdateAssociatedArticles(Connection conn,
			Map<String, String> newTitleSourceMidUidMap) throws IOException {
		if (newTitleSourceMidUidMap.keySet().size()>0) {
			
			

			
			System.out.println("Insert new title sources");
			
			List<TitleSourceMetadataObj> titleSources = getNewTitleSourcesFromMFS(conn,newTitleSourceMidUidMap);
			//insert to title source via api
			updateTitleSourcesInContentstack(titleSources,newTitleSourceMidUidMap );		

			//select all articles in the diff mid from mfs
			Map<String,Date>anDtFormatMap = new HashMap<String,Date>();
			List<ArticleObj>articles = getNewArticlesFromMFS(conn,newTitleSourceMidUidMap.keySet(), anDtFormatMap);
			System.out.println(articles.size() + " articles size");
			//for these articles, update the article object with the metadata from MFS
			updateArticlesInContentstack(articles, anDtFormatMap);
			updateLexileForDerivedArticles(articles);
		}
	}


	private static List<ArticleObj> getArticlesTiedToProject(String projectId, int totalEntries) throws IOException {

		List<ArticleObj>articles = new ArrayList<ArticleObj>();

		//String query="{\"current_project\": { \"$in_query\": { \"title\": \"" +  projectId +"\"}}}";
		int index = (totalEntries/100)+1;

		Set<String>titleSourceUids = new HashSet<String>();
		
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
			Response response = client.newCall(request).execute();
			String jsonStr = response.body().string();

			//System.out.println(jsonStr);
			JSONObject jo = new JSONObject(jsonStr);
			JSONArray entriesArray = jo.getJSONArray("entries");
			for (int j=0; j<entriesArray.length(); j++) {
				JSONObject item = entriesArray.getJSONObject(j);
				//System.out.println(item);
				
				JSONArray artDefArr = item.getJSONArray("article_definitions");
				List<String>artDefUids = new ArrayList<String>();
				for (int k=0; k<artDefArr.length();k++) {
					JSONObject artDefObj = artDefArr.getJSONObject(k);
					String artDefUid = artDefObj.getString("uid");
					artDefUids.add(artDefUid);
				}
				
				List<String>titleSourceList = getTitleSourceUidFromArtDefUids(artDefUids);
				titleSourceUids.addAll(titleSourceList);
			}

		}
		
		for (String titleSourceUid : titleSourceUids) {
			
			int numberOfArticles = getCountOfArticlesTiedToTitleSourceUid(titleSourceUid);
			List<ArticleObj> articleList = getArticlesByTitleSource(titleSourceUid, numberOfArticles);		
			articles.addAll(articleList);

		}
		
		
		
		/*
		 * 
		 * 
		 * 		for (int i=0;i<index;i++) {
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
			Response response = client.newCall(request).execute();
			String jsonStr = response.body().string();

			//System.out.println(jsonStr);
			JSONObject jo = new JSONObject(jsonStr);
			JSONArray entriesArray = jo.getJSONArray("entries");
			for (int j=0; j<entriesArray.length(); j++) {
				JSONObject item = entriesArray.getJSONObject(j);
				//System.out.println(item);
				
				JSONArray artDefArr = item.getJSONArray("article_definitions");
				List<String>artDefUids = new ArrayList<String>();
				for (int k=0; k<artDefArr.length();k++) {
					JSONObject artDefObj = artDefArr.getJSONObject(k);
					String artDefUid = artDefObj.getString("uid");
					artDefUids.add(artDefUid);
				}
				
				List<String> mfsAns = getMFSAnsFromArtDefUids(artDefUids);
				//System.out.println("MFS ANS " + mfsAns);
				ArticleObj ao = new ArticleObj();
				ao.setArticleId(item.getString("uid"));
				ao.setArticleTitle(item.getString("title"))	;
				ao.setMfsAns(mfsAns);
				articles.add(ao);
			}

		}
		 */
		

		return articles;


	}
	
	

	private static List<ArticleObj> getArticlesByTitleSource(String titleSourceUid, int totalEntries) throws IOException {
		List<ArticleObj>articles = new ArrayList<ArticleObj>();

		int index = (totalEntries/100)+1;

		for (int i=0;i<index;i++) {
			int skip=i *100;


			OkHttpClient client = new OkHttpClient().newBuilder()
					.build();
			Request request = new Request.Builder()
					.url("https://api.contentstack.io/v3/content_types/article/entries?skip=" +skip+ "&query={\"article_definitions\": {\"$in_query\": {\"title_source\": { \"$in_query\": {\"uid\": \"" + titleSourceUid +"\"}}}}}")
					.method("GET", null)
					.addHeader("api_key", API_KEY)
					.addHeader("authorization", MANAGEMENT_TOKEN)
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

			JSONObject jo = new JSONObject(jsonStr);

			

			JSONArray entriesArray = jo.getJSONArray("entries");
			for (int j=0; j<entriesArray.length(); j++) {
				JSONObject item = entriesArray.getJSONObject(j);
				//System.out.println(item);
				
				JSONArray artDefArr = item.getJSONArray("article_definitions");
				List<String>artDefUids = new ArrayList<String>();
				for (int k=0; k<artDefArr.length();k++) {
					JSONObject artDefObj = artDefArr.getJSONObject(k);
					String artDefUid = artDefObj.getString("uid");
					artDefUids.add(artDefUid);
				}
				
				List<String> mfsAns = getMFSAnsFromArtDefUids(artDefUids);
				//System.out.println("MFS ANS " + mfsAns);
				ArticleObj ao = new ArticleObj();
				ao.setArticleId(item.getString("uid"));
				ao.setArticleTitle(item.getString("title"))	;
				ao.setMfsAns(mfsAns);
				articles.add(ao);
			}

		}

		return articles;
		
	}






	private static int getCountOfArticlesTiedToTitleSourceUid(String titleSourceUid) throws IOException {
		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
				Request request = new Request.Builder()
				  .url("https://api.contentstack.io/v3/content_types/article/entries?count=true&query={\"article_definitions\": {\"$in_query\": {\"title_source\": { \"$in_query\": {\"uid\": \"" + titleSourceUid +"\"}}}}}")
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
	
	
	private static List<String> getTitleSourceUidFromArtDefUids(List<String> artDefUids) throws IOException {

		List<String> titleSourceUids = new ArrayList<String>();
		
		for (String artDefUid : artDefUids) {
			OkHttpClient client = new OkHttpClient().newBuilder()
					  .build();

					Request request = new Request.Builder()
					  .url("https://api.contentstack.io/v3/content_types/article_product_definition/entries?query={\"uid\":\"" + artDefUid +"\"}")
					  .method("GET", null)
					  .addHeader("api_key", API_KEY)
					  .addHeader("authorization",MANAGEMENT_TOKEN)
					  .addHeader("Content-Type", "application/json")
					  .build();
					Response response = client.newCall(request).execute();
					//System.out.println("https://api.contentstack.io/v3/content_types/article/entries?query={\"uid\":\"" + articleUid +"\"}");
					String jsonStr = response.body().string();
				//System.out.println(jsonStr);
				JSONObject jo = new JSONObject(jsonStr);
				JSONArray entriesArray = jo.getJSONArray("entries");
				for (int i=0; i<entriesArray.length(); i++) {
				    JSONObject item = entriesArray.getJSONObject(i);
				    if (item.has("title_source")) {
				    	JSONArray titleSourceArr = item.getJSONArray("title_source");
				    	for (int j=0; j<titleSourceArr.length(); j++) {
						    JSONObject tsObj = titleSourceArr.getJSONObject(j);
						    String titleSourceUid = tsObj.getString("uid");

						    	titleSourceUids.add(titleSourceUid);
						    

				    	}
				    }
				}
			
		}
		
		return titleSourceUids;
	}


	public static List<String> getMFSAnsFromArtDefUids(List<String> artDefUids) throws IOException {

		List<String> mfsAns = new ArrayList<String>();

		for (String artDefUid : artDefUids) {
			OkHttpClient client = new OkHttpClient().newBuilder()
					.build();

			Request request = new Request.Builder()
					.url("https://api.contentstack.io/v3/content_types/article_product_definition/entries?query={\"uid\":\"" + artDefUid +"\"}")
					.method("GET", null)
					.addHeader("api_key", API_KEY)
					.addHeader("authorization",MANAGEMENT_TOKEN)
					.addHeader("Content-Type", "application/json")
					.build();
			Response response = client.newCall(request).execute();
			//System.out.println("https://api.contentstack.io/v3/content_types/article/entries?query={\"uid\":\"" + articleUid +"\"}");
			String jsonStr = response.body().string();
			JSONObject jo = new JSONObject(jsonStr);
			JSONArray entriesArray = jo.getJSONArray("entries");
			for (int j=0; j<entriesArray.length(); j++) {
				JSONObject item = entriesArray.getJSONObject(j);
				if (item.has("an")) {
					String an = item.getString("an");
					if (an!=null && !an.isEmpty()) {
						if (isNumber(an)) {
							mfsAns.add(an);
						}
					}
				}

			}

		}

		return mfsAns;
	}


	private static boolean isNumber(String strNum) {
	    if (strNum == null) {
	        return false;
	    }
	    try {
	        double d = Double.parseDouble(strNum);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}






	private static void updateArticlesInContentstack(List<ArticleObj> articles, Map<String, Date> anDtFormatMap) throws IOException {

		Map<String, String>anArtDef = new HashMap<String,String>();
		//create article definition

		for (ArticleObj ao : articles) {

			String mfsAn = ao.getMfsAn();
			String mid = ao.getArticleMid();
			String titleSourceUid = getTitleSourceUid(mid);
			String title = mid;
			Date dtformat = anDtFormatMap.get(mfsAn);

			String apdUid = getArticleProductDefinitionUid(mfsAn);
			if (apdUid==null) {
				ArticleProductDefinitionObj apd = new ArticleProductDefinitionObj(title, mfsAn, titleSourceUid, dtformat);
				ContentstackUtil.importArticleProductDefinition(apd);
				apdUid = getArticleProductDefinitionUid(title);

			}
			anArtDef.put(mfsAn,apdUid);


		}


		for (ArticleObj upio : articles) {



			//query={"article_definitions":{"$in_query":{"an":"44716523"}}}

			OkHttpClient client = new OkHttpClient().newBuilder()
					.build();
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, "");
			Request request = new Request.Builder()
					.url("https://api.contentstack.io/v3/content_types/article/entries?query={\"article_definitions\":{\"$in_query\":{\"an\":\""  + upio.getMfsAn() +"\"}}}")
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
				String uid = item.getString("uid");

				JSONArray artDefArr = item.getJSONArray("article_definitions");

				String artDefUid = anArtDef.get(upio.getMfsAn());

				if ( artDefArr ==null) {
					artDefArr= new JSONArray();



				}
				else {
					artDefArr = removeIncorrectArtDefs(artDefArr, upio.getArticleMid(), upio.getArticleAn(), artDefUid);

				}
				JSONObject cont = new JSONObject();
				cont.put("uid", artDefUid);
				cont.put("_content_type_uid", "article_product_definition");
				artDefArr.put(cont);


				item.put("article_definitions",artDefArr);


				item.put("search_title",upio.getSearchTitle());
				item.put("word_count", upio.getWordCount());
				item.put("review_date", upio.getReviewDate());
				item.put("last_updated_date", upio.getLastUpdatedDate());
				item.put("title",upio.getArticleTitle());
				item.put("art_type", upio.getArtType());
				item.put("lexile", upio.getLexile());


				//ao.setMfsAn(an);
				//ao.setArticleMid(mid);





				JSONObject entry= new JSONObject();
				entry.put("entry", item);

				mediaType = MediaType.parse("application/json");
				body = RequestBody.create(mediaType, entry.toString());
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






	private static JSONArray removeIncorrectArtDefs(JSONArray artDefArr, String mid, String an, String artDefUid) throws IOException {
		
		JSONArray newArtDefArr = new JSONArray();

		List<JSONObject>newList = new ArrayList<JSONObject>();


       //System.out.println("ART DEF UID " + artDefUid);

		for (int i=0; i<artDefArr.length(); i++)  {
			JSONObject artDefObj = artDefArr.getJSONObject(i);

			//if uid equals the correct article definition, skip (the article def will be inserted after this method is done
			String uid = artDefObj.getString("uid");
			if (uid.equals(artDefUid)) {
				//newList.add(artDefObj);
			}

			else {
				OkHttpClient client = new OkHttpClient().newBuilder()
						.build();
				Request request = new Request.Builder()
						.url("https://api.contentstack.io/v3/content_types/article_product_definition/entries?query={\"uid\":\"" + uid +"\"}")
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
					String title = item.getString("title");
					
					if (!title.startsWith(mid )) {
						newList.add(artDefObj);
					}
					else {
						ContentstackUtil.deleteEntry(uid, "article_product_definition");
					}


				}
			}


		}

		for (JSONObject artDefObj : newList) {
			newArtDefArr.put(artDefObj);
		}

		return newArtDefArr;
		
	}






	private static String getArticleProductDefinitionUid(String an) throws IOException {
		String uid = null;
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/article_product_definition/entries?query={\"an\":\"" + an +"\"}")
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

	private static List<ArticleObj> getNewArticlesFromMFS(Connection conn, Set<String> midDifferenceSet, Map<String, Date> anDtFormatMap)  {
		List<ArticleObj> articles = new ArrayList<ArticleObj>();

		String qry = "SELECT an, arttitle, dtformat, arttype, wrdcnt,mid, lexrank FROM mfs.article m WHERE mid IN (";

		for (String mid : midDifferenceSet) {
			qry += "?,";
		}

		qry = qry.substring(0, qry.length()-1) + ")";
		qry += " AND UPDATETYPE <>'D'";

		
		
		
		
		try {
			PreparedStatement st = conn.prepareStatement(qry);
			
			int index=1;
			
			for (String mid : midDifferenceSet) {
				st.setString(index++, mid);
			}
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				String an = rs.getString(1);
				String arttitle = rs.getString(2);
				Date dtFormat =rs.getDate(3);
				String arttype = rs.getString(4);
				int wordCount = rs.getInt(5);
				String mid = rs.getString(6);
				int lexrank = rs.getInt(7);

				anDtFormatMap.put("an", dtFormat);
				ArticleObj ao  = new ArticleObj();
				ao.setMfsAn(an);
				ao.setArticleTitle(arttitle);
				
				ao.setArtType(arttype);
				ao.setWordCount(wordCount);
				ao.setArticleMid(mid);
				ao.setLexile(lexrank);
				//date_orig, update_date, review_date

				ao.setReviewDate(dtFormat);
				ao.setLastUpdatedDate(dtFormat);
				//search title

				if (arttitle!=null) {
					String artTitleStr = arttitle;
					if (artTitleStr.endsWith(".")) {
						ao.setSearchTitle(artTitleStr.substring(0,artTitleStr.length()-1));
					}															
				}

				articles.add(ao);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return articles;
	}






	private static void updateTitleSourcesInContentstack(List<TitleSourceMetadataObj> titleSources, Map<String, String> newTitleSourceMidUidMap) throws IOException {
		System.out.println("importing title sources");

		for (TitleSourceMetadataObj tso : titleSources) {
			OkHttpClient client = new OkHttpClient().newBuilder()
					.build();

			Request request = new Request.Builder()
					.url("https://api.contentstack.io/v3/content_types/book_source/entries?query={\"mid\":\"" + tso.getMid() +"\"}")
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
				String uid = item.getString("uid");
			    item.put("book_title", tso.getTitle());
			    item.put("pub_date", tso.getPubDate());
			    item.put("publisher", tso.getPublisher());
			    item.put("series_id", tso.getSeriesId());
			    item.put("is_new", false);

			    updateTitleSource(uid, item);
				}
			
			


		}
	}






	private static List<TitleSourceMetadataObj> getNewTitleSourcesFromMFS(Connection conn, Map<String,String>midUidMap) {
		List<TitleSourceMetadataObj> tsList = new ArrayList<TitleSourceMetadataObj>();

		Map<Integer, String>pubMap = getPublisherMap(); 
		Set<String>mids = midUidMap.keySet();
		
		String qry = "SELECT magname, mid,  dtfirst, pid, bookseriesid FROM mfs.magtitle m WHERE mid IN (";

		for (String mid : mids) {
			qry += "?,";
		}

		qry = qry.substring(0, qry.length()-1) + ")";

		try {
			PreparedStatement st = conn.prepareStatement(qry);
			
			int index=1;
			
			for (String mid : mids) {
				st.setString(index++, mid);
			}
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				String magName = rs.getString(1);
				String mid = rs.getString(2);
				java.sql.Date dtFirst =rs.getDate(3);
				int pid = rs.getInt(4);
				int bookSeriesId = rs.getInt(5);

				String publisherName = pubMap.get(pid);
				TitleSourceMetadataObj tmo = new TitleSourceMetadataObj(magName, mid, mid, dtFirst, publisherName, bookSeriesId);
				tsList.add(tmo);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return tsList;
	}






	private static Map<Integer, String> getPublisherMap() {

		Map<Integer, String> pMap = new HashMap<Integer, String> ();

		pMap.put(345, "Salem Press");
		pMap.put(8275, "Great Neck Publishing");
		pMap.put(11528, "Lakeside Publishing Group, LLC");
		pMap.put(87894, "Grey House Publishing");

		return pMap;
	}






	public static Set<String> getSetOfPropPubMidsFromMFS (Connection conn){

		Set<String> set = new HashSet<String>();
		String qry = "SELECT mid FROM mfs.MAGTITLE m WHERE (m.pid='345' OR m.pid='8275' OR m.pid='11528' OR m.pid='87894') " +
				" AND (m.BOOKSERIESID IS NULL OR m.BOOKSERIESID<>4121) " ;





		try {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(qry);
			while (rs.next()) {
				set.add(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return set;

	}


	public static Set<String> getMidsFromContentstack() throws IOException{
		Set<String> set = new HashSet<String>();
		String contentTypeId = "book_source";
		int totalEntries = ContentstackUtil.getCountOfEntries(contentTypeId);
		int index = (totalEntries/100)+1;



		for (int i=0;i<index;i++) {
			int skip=i *100;
			String urlStr= "https://api.contentstack.io/v3/content_types/" + contentTypeId + "/entries?&skip=" +skip;
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
				if (item.has("mid")) {
					String mid = item.getString("mid");
					set.add(mid);
				}
				//  System.out.println(contentTypeId.toUpperCase() + " MAP   " +title + " " + uid);

			}

		}


		return set;
	}



}
