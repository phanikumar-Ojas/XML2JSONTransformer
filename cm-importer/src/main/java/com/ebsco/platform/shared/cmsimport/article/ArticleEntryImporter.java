package com.ebsco.platform.shared.cmsimport.article;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.ebsco.platform.shared.cmsimport.article.ftabstract.FTAbstractImporter;
import com.ebsco.platform.shared.cmsimport.contributor.ContributorImporter;
import com.ebsco.platform.shared.cmsimport.lexile.LexileImporter;
import com.ebsco.platform.shared.cmsimport.topic.POVTopicImporter;
import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil;
import com.ebsco.platform.shared.cmsimport.utilities.DBUtil;
import com.ebsco.platform.shared.cmsimport.xml.FullTextImporter;
import com.ebsco.platform.shared.cmsimport.xml.POVFullText;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ArticleEntryImporter {

	private static final String COMPLETE_INSIDE_RTE_IMAGE_STATUS = "Complete inside RTE";

	private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");

	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");

	private static final String ARTICLE_XML_FOLDER_PATH =  "/Users/mpamuk/Desktop/cms_pov/reimport/XML";


	//mainDelete
	public static void mainDelete(String [] args) throws IOException, InterruptedException {
		
		String contentTypeId = "blog_post";
		int noOfEntries = ContentstackUtil.getCountOfEntries(contentTypeId);
		List<String> uids= ContentstackUtil.getListOfEntryUids(noOfEntries,contentTypeId);
		int count=0;
		for (String uid : uids) {
			System.out.println(count++);
			ContentstackUtil.deleteEntry(uid,contentTypeId);
		}
		
		//ContentstackUtil.bulkDeleteEntries("article");
	}
	


//mainImport
	public static void main(String [] args) throws ParserConfigurationException, SAXException, IOException, SQLException {

		System.out.println("STARTING ");
		String rootDir = ARTICLE_XML_FOLDER_PATH;
		//create article id to contrib uid map
		Map<String,List<String>>contribMap = createContribMap();

		//create product - productuid map

		Map<String,String>productMap = createProductMap();

		//create book - book uid map

		Map<String,String>bookMap = createBookMap();
		
		Map<String,String> artProdDefMap = createArticleProductDefinitionMap(); 
		
		//create image file - uid map
		int noOfAssets = ContentstackUtil.getCountOfAssets();		
		Map<String,String> fileUidMap = ContentstackUtil.getFilenameToUidUrlMap(noOfAssets);	


		Map<String,String>articleIdTopicIdMap = POVTopicImporter.createTopicMap();
		
		Map<String,Integer>lexileMap = LexileImporter.createLexileMap();
		
		Map<String, String>abstractMap = FTAbstractImporter.getMapOfArticleIdAbstract();

		
		Map<String,String>collectionsMap = createCollectionMap();
		List<ArticleObj> articles = getArticles(contribMap, productMap, bookMap, artProdDefMap, lexileMap, collectionsMap, abstractMap);
		
		//System.out.println("PRODUCT SIZE " + articles.get(0).getProduct().size());

		//importArticleToContentstack(articles.get(0), contribMap, productMap, bookMap,articleIdTopicIdMap);


		
		
		
		int count=0;
		for (ArticleObj bso : articles) {
			//import to Contentstack
			
			String fileP = bso.getArticleId() +".xml";
			
			POVFullText povFt = FullTextImporter.getPOVFullText(fileUidMap, rootDir, fileP);
			System.out.println(bso.getArticleId());
			//System.out.println(povFt.getBodyTxt());
			System.out.println(count++);
		
			importArticleToContentstack(bso, contribMap, productMap, bookMap,articleIdTopicIdMap , povFt);
			
			
		}
	}














	private static void importArticleToContentstack(ArticleObj bso, Map<String, List<String>> contribMap,
			Map<String, String> productMap, Map<String, String> bookMap, Map<String, String> articleIdTopicIdMap, POVFullText povFt) throws IOException {

		JSONObject jo = new JSONObject();
		JSONObject entry = new JSONObject();



		entry.put("title",bso.getArticleTitle());
		entry.put("article_id", bso.getArticleId());
		entry.put("article_mid", bso.getArticleMid());
		entry.put("mfs_an", bso.getMfsAn());
		entry.put("date_created", bso.getDateCreated());
		entry.put("review_date", bso.getReviewDate());
		entry.put("last_updated_date", bso.getLastUpdatedDate());
		entry.put("region",bso.getRegion());
		entry.put("word_count", bso.getWordCount());

		entry.put("derived_from_id", bso.getDerivedFromId());
		entry.put("parent_article_id", bso.getParentArticleId());
		entry.put("alt_mid", bso.getAltMid());
		entry.put("previous_date", bso.getPreviousDate());
		
		entry.put("search_title",bso.getSearchTitle());
		entry.put("xml_version_ui", bso.getXmlVersionUi());
		JSONArray lastUpdateTypeArr = new JSONArray();
		if (bso.getLastUpdateType()!=null) {
			lastUpdateTypeArr.put(bso.getLastUpdateType());
		}
		entry.put("last_update_type", lastUpdateTypeArr);
		entry.put("data_type", bso.getDataType());
		entry.put("research_starter", bso.isResearchStarter());		
		entry.put("primary_article", bso.getPrimaryArticle());
		entry.put("id_of_primary", bso.getIdOfPrimary());
		entry.put("sidebar", bso.getSidebar());
		entry.put("image", bso.getImage());
		entry.put("art_type", bso.getArtType());
		entry.put("date_in_repository", bso.getDateInRepository());
		entry.put("date_in_repository_updated", bso.getDateInRepositoryUpdated());
		entry.put("pdf", bso.getPdf());
		entry.put("art_topic", bso.getArtTopic());

		entry.put("rs_title", bso.getRsTitle());
		entry.put("date_added", bso.getDateAdded());
		entry.put("update_cycle", bso.getUpdateCycle());
		entry.put("status", bso.getStatus());
		entry.put("source_note", bso.getSourceNote());
		entry.put("copied_to_build", bso.getCopiedToBuild());
		entry.put("updated_build", bso.getUpdatedToBuild());
		entry.put("do_not_use", bso.isDoNotUse());
		entry.put("usage_note", bso.getUsageNote());

		entry.put("primary_preferred", bso.isPrimaryPreferred());
		entry.put("secondary_preferred", bso.isSecondaryPreferred());
		entry.put("consumer_preferred", bso.isConsumerPreferred());
		entry.put("corporate_preferred", bso.isCorporatePreffered());
		entry.put("academic_preferred", bso.isAcademicPreffered());

		// rights, contribUids, products, bookSource);
		entry.put("article_an", bso.getArticleAn());
		entry.put("root_an", bso.getRootAn());
		entry.put("rs_an", bso.getRsAn());
		entry.put("b_an", bso.getbAn());
		entry.put("collection_topic", bso.getBrstTopic());
		entry.put("collection_debate_category", bso.getBrstCategory());
		entry.put("rights", bso.getRights());

		
		entry.put("main_body_html", povFt.getBodyTxt());
		
		entry.put("author_note_html", povFt.getAuthorNoteTxt());
		entry.put("abstract_html", bso.getFtAbstract());
		entry.put("lexile", bso.getLexile());
		

		entry.put("citations_html", povFt.getBibliographyTxt());
		
		
		
		String imageStatus = COMPLETE_INSIDE_RTE_IMAGE_STATUS;
		boolean hasImage = hasImage(povFt.getBodyTxt());
		if (hasImage) {
			entry.put("image_status", imageStatus);
		}

		JSONArray bookArr = new JSONArray();

		JSONObject bookObj = new JSONObject();

		bookObj.put("uid", bso.getBookId());
		bookObj.put("_content_type_uid", "book_source");
		bookArr.put(bookObj);
		entry.put("title_source", bookArr);
		
		
		String topicId = articleIdTopicIdMap.get(bso.getArticleId());
		
		if (topicId!=null) {
			JSONArray topicArr = new JSONArray();

			JSONObject topicObj= new JSONObject();

			topicObj.put("uid", topicId);
			topicObj.put("_content_type_uid", "topic_debate");
			topicArr.put(topicObj);
			entry.put("topic", topicArr);
											
		}
		
		
		if  (bso.getContributors()!=null) {
			JSONArray contribArr = new JSONArray();
			for (String contUid : bso.getContributors()) {

				JSONObject cont = new JSONObject();
				cont.put("uid", contUid);
				cont.put("_content_type_uid", "contributor");
				contribArr.put(cont);
			}
			entry.put("contributors",contribArr);

		}

		
		
		if  (bso.getArticleDefinitions()!=null) {
			JSONArray apdArr = new JSONArray();
			for (String apdUid : bso.getArticleDefinitions()) {

				JSONObject cont = new JSONObject();
				cont.put("uid", apdUid);
				cont.put("_content_type_uid", "article_product_definition");
				apdArr.put(cont);
			}
			entry.put("article_definitions",apdArr);


		}
		
		if  (bso.getCollections()!=null) {
			JSONArray collArr = new JSONArray();
			for (String collUid : bso.getCollections()) {

				JSONObject cont = new JSONObject();
				cont.put("uid", collUid);
				cont.put("_content_type_uid", "collection");
				collArr.put(cont);
			}
			entry.put("collections",collArr);

		}
		
		if (bso.getProduct()!=null) {
			JSONArray productArr = new JSONArray();
			for (String productUid : bso.getProduct()) {
				//System.out.println(productUid);
				JSONObject pro = new JSONObject();
				pro.put("uid", productUid);

				pro.put("_content_type_uid", "product");
				productArr.put(pro);
			}
			entry.put("products",productArr);

		}


		
		
		jo.put("entry", entry);
		String jsonStr= jo.toString();

		//System.out.println(jo.toString());

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
		String responseStr = response.body().string();

		try {
			JSONObject responseObj = new JSONObject(responseStr);
			if (responseObj.has("error_message")) {
				System.out.println(responseObj.getString("error_message"));
				System.out.println(responseStr);
			}


			response.body().close() ;
		}
		catch (Exception e) {
			System.out.println("EXCEPTION " + e.getMessage());
		}

	}


	private static boolean hasImage(String bodyTxt) {
		if (bodyTxt==null) {
			return false;
		}

		if (bodyTxt.contains("</figure>")) {
			return true;
		}
		return false;
	}



	public static List<ArticleObj> getArticles(Map<String, List<String>> contribMap, Map<String, String> productMap, Map<String, String> bookMap,Map<String,String> artProdDefMap, Map<String, Integer> lexileMap, Map<String,String> collectionsMap, Map<String,String> abstractMap) throws SQLException {
		// TODO Auto-generated method stub
		List<ArticleObj>povArticles = new ArrayList<ArticleObj>(); 
		String qry = "select article_title, article_id, book_id, 	derived_from_id, parent_article_id , mfs_an, article_mid, "

				+ " alt_mid,date_orig,update_date, previous_date, review_Date, dtformat, search_title, xml_version_ui, "

				+ "	last_update_type , data_type ,research_starter ,primary_article ,id_of_primary ,word_count,sidebar ,"

				+ "image, art_type,date_in_repository ,date_repository_updated , pdf,topic,rs_title ,date_added ,"

				+ "update_cycle ,status, source_note , copied_to_build,updated_build,do_not_use,usage_note,"

				+ "primary_preferred,secondary_preferred,consumer_preferred,corporate_preferred,academic_preferred,"

				+ " article_an ,root_an, rs_an,b_an,brst_topic,brst_category,rights,product "

		+ " from cms.t_articles ta where book_id  like '%pov_%'   and research_starter is null and  do_not_use ='false' " ;




		Connection conn = DBUtil.getPostgresConn();

		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery(qry);


		while (rs.next()) {

			String articleTitle = rs.getString(1);
			String  articleId =rs.getString(2);
			String bookId = rs.getString(3);
			String derivedFromId= rs.getString(4);
			String parentArticleId = rs.getString(5);
			String mfsAn = rs.getString(6);
			String articleMid = rs.getString(7);

			String altMid = rs.getString(8);
			java.sql.Date dateOrig = rs.getDate(9);
			java.sql.Date updateDate = rs.getDate(10);
			java.sql.Date previousDate = rs.getDate(11);
			java.sql.Date reviewDate = rs.getDate(12);
			java.sql.Date dtFormat = rs.getDate(13);
			String searchTitle = rs.getString(14);
			String xmlVersionUi = rs.getString(15);


			String lastUpdateType = rs.getString(16);
			String dataType = rs.getString(17);
			String researchStarter = rs.getString(18);
			String primaryArticle = rs.getString(19);
			String idOfPrimary = rs.getString(20);
			int wordCount = rs.getInt(21);
			String sidebar = rs.getString(22);

			String image = rs.getString(23);
			String artType = rs.getString(24);
			java.sql.Date dateInRepository = rs.getDate(25);
			java.sql.Date dateInRepositoryUpdated = rs.getDate(26);

			String pdf = rs.getString(27);
			String artTopic = rs.getString(28);
			String rsTitle = rs.getString(29);
			java.sql.Date dateAdded = rs.getDate(30);

			// "update_cycle ,status, source_note , copied_to_build,updated_build,do_not_use,usage_note,"

			String updateCycle = rs.getString(31);
			String status = rs.getString(32);
			String sourceNote = rs.getString(33);
			java.sql.Date copiedToBuild = rs.getDate(34);
			java.sql.Date updatedBuild = rs.getDate(35);
			boolean doNotUse= rs.getBoolean(36);
			String usageNote = rs.getString(37);
			// "primary_preffered,secondary_preferred,consumer_preferred,corporate_preferred,academic_preferred,"
			boolean primaryPreferred= rs.getBoolean(38);
			boolean secondaryPreferred= rs.getBoolean(39);
			boolean consumerPreferred = rs.getBoolean(40);
			boolean corporatePreferred = rs.getBoolean(41);
			boolean academicPreferred = rs.getBoolean(42);
			//article_an ,root_an, rs_an,b_an,brst_topic,brst_category,rights,product
			String articleAn = rs.getString(43);
			String rootAn = rs.getString(44);
			String rsAn = rs.getString(45);
			String bAn = rs.getString(46);
			String brstTopic = rs.getString(47);
			String brstCategory = rs.getString(48);
			String rights= rs.getString(49);
			String product = rs.getString(50);
			
			List<String>artDefList = new ArrayList<String>();


			List<String>products =new ArrayList<String>();

			if (product!=null) {
				String[] productToks =product.split(",");

				for (String pt :productToks) {
					String puid = productMap.get(pt);
					if (puid!=null) {
						products.add(puid);
					}
				}
				
				
			}
			
            String articleDefTitle = articleMid + "_" +mfsAn;
            //System.out.println("ARTICLE DEF TITLE " + articleDefTitle);
            String artProdDefUid = artProdDefMap.get(articleDefTitle);
            if (mfsAn==null) {
            	artProdDefUid = artProdDefMap.get(articleMid);
            }
            artDefList.add(artProdDefUid);
            
			String bookSource =bookMap.get(articleMid);



			boolean isRs = researchStarter!=null;


			String region = extractRegion(articleId);
			if (region==null) {
				region = extractRegion(bookId);
			}
				
			
			
			
			int lexile = lexileMap.get(articleId);
			
			String ftAbstract = abstractMap.get(articleId);
			
			List<String>contribUids  =contribMap.get(articleId);
			
			String collection = collectionsMap.get(bookId);
			List<String>collections = new ArrayList<String>();
			collections.add(collection);

			//List<String> contributors, List<String> product, String bookId)

			ArticleObj ao= new ArticleObj(articleId, articleTitle, articleMid, mfsAn, dateOrig, reviewDate, updateDate, region, wordCount,
					derivedFromId, parentArticleId, altMid, previousDate, searchTitle, xmlVersionUi, lastUpdateType, dataType, isRs,
					primaryArticle, idOfPrimary, sidebar, image, artType, dateInRepository, dateInRepositoryUpdated, pdf, artTopic, 
					rsTitle, dateAdded, updateCycle, status, sourceNote, copiedToBuild, updatedBuild, doNotUse,
					usageNote, primaryPreferred, secondaryPreferred, consumerPreferred, corporatePreferred, academicPreferred,
					articleAn, rootAn, rsAn, bAn, brstTopic, brstCategory, rights, contribUids, products, bookSource, artDefList,lexile, collections, ftAbstract);

			povArticles.add(ao);
		}

		conn.close();        
		return povArticles;
	}



	private static String extractRegion(String articleId) {
		String region= null;

		if (articleId.startsWith("pov_aus_")) {
			region="Australia/New Zealand";
		}
		else if (articleId.startsWith("pov_uk_")) {
			region="UK";

		}

		else if (articleId.startsWith("pov_can_")) {
			region="Canada";

		}
		else if (articleId.startsWith("pov_us_") ||articleId.startsWith("pov_usa_")) {

			region="US";

		}
		return region;
	}


	public static Map<String, String> createBookMap() throws IOException {
		// TODO Auto-generated method stub
		int noOfEntries = ContentstackUtil.getCountOfEntries("book_source");

		return getListOfTitleEntryUids(noOfEntries, "book_source");
	}
	
	


	public static Map<String, String> createCollectionMap() throws IOException{
		int noOfEntries = ContentstackUtil.getCountOfEntries("collection");

		return getListOfTitleEntryUids(noOfEntries, "collection");
	}


	public static Map<String, String> createProductMap() throws IOException {
		// TODO Auto-generated method stub

		int noOfEntries = ContentstackUtil.getCountOfEntries("product");

		return getListOfTitleEntryUids(noOfEntries, "product");
	}
	
	

	public static Map<String, String> createArticleProductDefinitionMap() throws IOException {
		// TODO Auto-generated method stub
		int noOfEntries = ContentstackUtil.getCountOfEntries("article_product_definition");

		// TODO Auto-generated method stub
		Map<String, String>uids = new HashMap<String, String>();

		String contentTypeId = "article_product_definition";
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
				if (item.has("title")) {
					//System.out.println(item);
					String uid = item.getString("uid");
					String title = item.getString("title");
					if (item.has("an") &&!item.isNull("an")) {
						String an = item.getString("an");
						title = title + "_" + an;
					}


					uids.put(title,uid);
				}
				//  System.out.println(contentTypeId.toUpperCase() + " MAP   " +title + " " + uid);

			}

		}
		
		return uids;

	}



	private static Map<String, List<String>> createContribMap() throws ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub

		Map<String,List<String>>fileUniqueTitle = ContributorImporter.getFileUniqueTitle();

		int noOfEntries = ContentstackUtil.getCountOfEntries("contributor");
		Map<String,String> titleUids= getListOfTitleEntryUids(noOfEntries,"contributor");


		Map<String,List<String>>fileContribUidMap = new HashMap<String,List<String>>();

		for (String file : fileUniqueTitle.keySet()) {

			List<String>cuids= new ArrayList<String>();
			List<String>uniqTitles = fileUniqueTitle.get(file);
			for (String ut : uniqTitles) {
				String contuid = titleUids.get(ut);
				if (contuid!=null) {
					cuids.add(contuid);
				}
			}

			if (cuids.size()>0) {
				fileContribUidMap.put(file, cuids);
			}
		}
		//System.out.println("FILE CONTTT " + fileContribUidMap.size());
		return fileContribUidMap;
	}


	public static Map<String, String> getListOfTitleEntryUids(int totalEntries, String contentTypeId) throws IOException {
		// TODO Auto-generated method stub
		Map<String, String>uids = new HashMap<String, String>();


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
				uids.put(title,uid);
				}
				//  System.out.println(contentTypeId.toUpperCase() + " MAP   " +title + " " + uid);

			}

		}

		return uids;
	}



}
