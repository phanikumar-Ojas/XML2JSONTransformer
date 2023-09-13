package com.ebsco.platform.shared.cmsimport.collection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil;
import com.ebsco.platform.shared.cmsimport.utilities.DBUtil;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CollectionImporter {

	
private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");
	
	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");

	
	//mainImport
	public static void main(String [] args) throws SQLException, IOException {
					
		//select all the pov collections
		//create pov collection objects
		
		List<CollectionObj> collections = getCollections();
		int count = 0;
		for (CollectionObj co : collections) {
			System.out.println(count++);
			//import to Contentstack
			importCollectionToContentstack(co);
		}
		
		
	}
	
	
	//mainDelete
	public static void mainDelete(String [] args) throws IOException {
		String dataType = "collection";
		int noOfEntries = ContentstackUtil.getCountOfEntries(dataType);
		List<String> uids= ContentstackUtil.getListOfEntryUids(noOfEntries, dataType);
		int count = 0;
		for (String uid : uids) {
			System.out.println(count++);
			ContentstackUtil.deleteEntry(uid,dataType);
		}
	}

	public static void importCollectionToContentstack(CollectionObj co) throws IOException {		

		JSONObject jo = new JSONObject();
		JSONObject entry = new JSONObject();


		String title = co.getCollectionTitle();
		entry.put("collection_title",title);

		entry.put("title", co.getCollectionId());

		entry.put("publisher",co.getPublisher());

		entry.put("series_id_xml",co.getSeriesIdXML());

		entry.put("publication_date",co.getPublicationDate());		

		entry.put("publication_year",co.getPubYear());
		entry.put("publisher_location",co.getPublisherLocation());
		entry.put("copyright_statement",co.getCopyrightStatement());
		entry.put("copyright_holder",co.getCopyrightHolder());


		jo.put("entry", entry);

		String jsonStr= jo.toString();


		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, jsonStr);
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/collection/entries?locale=en-us")
				.method("POST", body)
				.addHeader("api_key",  API_KEY)
				.addHeader("authorization", MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = client.newCall(request).execute();
		System.out.println(response.body().string());
		response.body().close() ;
	}
	
	

	private static List<CollectionObj> getCollections() throws SQLException {
		List<CollectionObj>povCollections = new ArrayList<CollectionObj>(); 
		String qry = "SELECT book_id, book_title, publisher, series_id_xml, pub_date, pub_year, publisher_loc, copyright_statement, "
				+ "copyright_holder, rights FROM cms.t_books where mid is null and book_id like 'pov_%' ";


		Connection conn = DBUtil.getPostgresConn();

		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery(qry);


		while (rs.next()) {

			String  bookId =rs.getString(1);
			String bookTitle = rs.getString(2);
			String publisher= rs.getString(3);
			String seriesIdXml = rs.getString(4);
			java.sql.Date pubDate = rs.getDate(5);
			int pubyear = rs.getInt(6);
			String publisherLoc = rs.getString(7);
			String copyrightStatement = rs.getString(8);
			String copyrightHolder = rs.getString(9);
			String rights = rs.getString(10);

			CollectionObj coll = new CollectionObj(bookId, bookTitle, publisher, seriesIdXml, pubDate, pubyear, publisherLoc, copyrightStatement, copyrightHolder, rights);

			povCollections.add(coll);
		}

		conn.close();        
		return povCollections;
	}
}
