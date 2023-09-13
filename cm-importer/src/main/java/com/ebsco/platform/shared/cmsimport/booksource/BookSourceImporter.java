package com.ebsco.platform.shared.cmsimport.booksource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.ebsco.platform.shared.cmsimport.asset.ImageMetadataImporter;
import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil;
import com.ebsco.platform.shared.cmsimport.utilities.DBUtil;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BookSourceImporter {

	
	
	private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");
	
	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");

	
	//mainImport
	public static void main(String [] args) throws SQLException, IOException {
		
		
		
		//select all the pov books
		//create pov book source objects
		
		List<BookSourceObj> bookSources = getBookSources();
		int count = 0;
		for (BookSourceObj bso : bookSources) {
			System.out.println(count++);
			//import to Contentstack
			importBookSourceToContentstack(bso);
		}
		
		
	}
	
	
	//mainDelete
	public static void mainDelete(String [] args) throws IOException {
		String dataType = "book_source";
		int noOfEntries = ContentstackUtil.getCountOfEntries(dataType);
		List<String> uids= ContentstackUtil.getListOfEntryUids(noOfEntries, dataType);
		int count = 0;
		for (String uid : uids) {
			System.out.println(count++);
			ContentstackUtil.deleteEntry(uid,dataType);
		}
	}

	public static void importBookSourceToContentstack(BookSourceObj bso) throws IOException {
		

		JSONObject jo = new JSONObject();
		JSONObject entry = new JSONObject();
		
		
		String title = bso.getBookTitle();
		entry.put("book_title",title);
		
		entry.put("title", bso.getBookId());
		
		entry.put("book_series_id",bso.getBookSeriesId());
		entry.put("publisher",bso.getPublisher());
		
		entry.put("isbn",bso.getIsbn());
		entry.put("series_id_xml",bso.getSeriesIdXML());
		
		entry.put("publication_date",bso.getPublicationDate());		
		
		entry.put("source", bso.getSource());
		
		entry.put("doc_type", bso.getDocType());
		entry.put("book_note", bso.getBookNote());

		
		entry.put("publication_year",bso.getPubYear());
		entry.put("publisher_location",bso.getPublisherLocation());
		entry.put("copyright_statement",bso.getCopyrightStatement());

		entry.put("copyright_holder",bso.getCopyrightHolder());

		entry.put("date_added",bso.getDateAdded());
		entry.put("data_format",bso.getDataFormat());

		entry.put("rights",bso.getRights());

		entry.put("mid",bso.getMid());


		
		jo.put("entry", entry);
	
			String jsonStr= jo.toString();
			
	
			
			OkHttpClient client = new OkHttpClient().newBuilder()
					  .build();
					MediaType mediaType = MediaType.parse("application/json");
					RequestBody body = RequestBody.create(mediaType, jsonStr);
					Request request = new Request.Builder()
					  .url("https://api.contentstack.io/v3/content_types/book_source/entries?locale=en-us")
					  .method("POST", body)
					  .addHeader("api_key",  API_KEY)
					  .addHeader("authorization", MANAGEMENT_TOKEN)
					  .addHeader("Content-Type", "application/json")
					  .build();
					Response response = client.newCall(request).execute();
					System.out.println(response.body().string());
					response.body().close() ;
	}

	private static List<BookSourceObj> getBookSources() throws SQLException {
		List<BookSourceObj>povBookSources = new ArrayList<BookSourceObj>(); 
		/*String qry = "select book_title, book_id, series_id, publisher, isbn ,series_id_xml , pub_date ,source, doctype , book_note , pub_year , publisher_loc ,copyright_statement "
				+ ", copyright_holder , date_added , data_format, rights, mid  from cms.t_Books ";
			//	+ "where book_id in "
			//	+ "(select distinct tb.book_id from cms.t_articles ta join cms.t_books tb on ta.book_id  = tb.book_id ";
			//	+ "where  "
			//	+ "ta.article_id like '%pov_%' and tb.book_id!='rspov' and "
			//	+ "brst_category is not null)";*/
		
		String qry = "select book_title, book_id, series_id, publisher, isbn ,series_id_xml , pub_date ,source, doctype , book_note , pub_year , publisher_loc ,copyright_statement "
							+ ", copyright_holder , date_added , data_format, rights, mid  from cms.t_Books WHERE mid IN "
							+ "(select article_mid from cms.t_articles 	ta	 join cms.t_books tb on ta.book_id  = tb.book_id "
							+ "where do_not_use = false AND ta.book_id like 'pov_%' AND research_starter is null)";
				
				

		   Connection conn = DBUtil.getPostgresConn();

            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(qry);
            
            
            while (rs.next()) {
            	
            	String bookTitle = rs.getString(1);
            	String  bookId =rs.getString(2);
            	String seriesId = rs.getString(3);
            	String publisher= rs.getString(4);
            	String isbn = rs.getString(5);
            	String seriesIdXml = rs.getString(6);
            	java.sql.Date pubDate = rs.getDate(7);
            	String source = rs.getString(8);
            	
            	
            	String docType = rs.getString(9);
            	String bookNote = rs.getString(10);
            	int pubyear = rs.getInt(11);
            	String publisherLoc = rs.getString(12);
            	String copyrightStatement = rs.getString(13);
            	String copyrightHolder = rs.getString(14);
            	java.sql.Date dateAdded = rs.getDate(15);
            	String dataFormat = rs.getString(16);

            	String rights = rs.getString(17);
            	String mid = rs.getString(18);
            	BookSourceObj bso = new BookSourceObj(bookTitle, bookId, seriesId, publisher, isbn, seriesIdXml, pubDate, source, docType, bookNote, pubyear, publisherLoc, copyrightStatement, copyrightHolder, dateAdded, dataFormat, rights, mid);

            	povBookSources.add(bso);
            }
	             
	     conn.close();        
		return povBookSources;
	}
	
	
	
}
