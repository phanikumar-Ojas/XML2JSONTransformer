package com.ebsco.platform.shared.cmsimport.contributor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import com.ebsco.platform.shared.cmsimport.utilities.ContentstackUtil;
import com.ebsco.platform.shared.cmsimport.utilities.FileUtil;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ContributorImporter {


	private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");

	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");

	private static final String ARTICLE_XML_FOLDER_PATH =  "/Users/mpamuk/Desktop/cms_pov/reimport/XML";

	//mainDelete
	public static void mainDelete(String [] args) throws IOException {

		int noOfEntries = ContentstackUtil.getCountOfEntries("contributor");
		List<String> uids= ContentstackUtil.getListOfEntryUids(noOfEntries,"contributor");
		int count = 0;
		for (String uid : uids) {
			System.out.println(count++);
			ContentstackUtil.deleteEntry(uid,"contributor");
		}
	}

	//mainImport
	public static void main (String [] args) throws ParserConfigurationException, SAXException, IOException {

		// Set<String>allNames = new HashSet<String>();
		String rootDir = ARTICLE_XML_FOLDER_PATH;


		Map<String , ContributorObj> fullNameOnly = new HashMap<String,ContributorObj>();
		Map<String , ContributorObj> givenNameFullName = new HashMap<String,ContributorObj>();


		List<String>filePaths =FileUtil.getFilePaths(rootDir);
		int count=0;
		for (String fileP : filePaths) {
			// System.out.println(fileP);
			// System.out.println(extractFileName(fileP));


			System.out.println(count++);
			File file= new File(fileP);

			// System.out.println(fileP);
			Document doc= null;

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(file);
			doc.getDocumentElement().normalize();



			// System.out.println("Root element " + doc.getDocumentElement().getNodeName());

			NodeList nodeList=doc.getElementsByTagName("contrib");


			for (int i=0; i<nodeList.getLength(); i++) 
			{
				// Get element
				Node n = nodeList.item(i);
				NodeList contribCh = n.getChildNodes();

				for (int j=0;j<contribCh.getLength();j++) {
					Node innerN = contribCh.item(j);
					//System.out.println("inner node " + innerN.getNodeName());
					if (innerN.getNodeName().equals("string-name")) {

						String fullName = innerN.getTextContent();
						//System.out.println(" full Name " + fullName);

						//insertContribToContentstack(fullName,allNames);
						fullNameOnly.put(fullName, new ContributorObj(fullName));

					}
					else if (innerN.getNodeName().equals("name")) {
						String givenNames = "";
						String surname = "";
						String degrees = "";

						//iterate over parent node to see if there is a degrees node
						for (int k=0;k<contribCh.getLength();k++) {

							if (contribCh.item(k).getNodeName().equals("degrees")){
								degrees =contribCh.item(k).getTextContent();
							}
						}


						NodeList authorFieldList = innerN.getChildNodes();

						for (int k=0;k<authorFieldList.getLength();k++) {
							Node authN = authorFieldList.item(k);
							if (authN.getNodeName().equals("given-names")) {
								givenNames = authN.getTextContent();
							}
							else if (authN.getNodeName().equals("surname")) {
								surname = authN.getTextContent();
							}
							//else if (authN.getNodeName().equals("degrees")) {
							//	degrees = authN.getTextContent();
							//}
							// System.out.println(authN.getNodeName() + " " +authN.getTextContent());


						}

						String fullName = givenNames + " " + surname;

						ContributorObj cont = givenNameFullName.get(fullName);

						if (cont!=null) {
							if (!degrees.isEmpty()) {
								givenNameFullName.put(fullName, new ContributorObj(fullName,givenNames,surname, degrees));
							}
						}

						else {
							givenNameFullName.put(fullName, new ContributorObj(fullName,givenNames,surname, degrees));
						}

						//insertContribToContentstack(givenNames,surname,degrees, allNames);
					}
				}


			}




		}


		for (String fullName : fullNameOnly.keySet()) {
			if (givenNameFullName.keySet().contains(fullName)) {

				// System.out.println("contains " + fullName);
				continue;
			}
			else {
				insertContribToContentstack(fullName);
			}
		}

		for (String fullName : givenNameFullName.keySet()) {

			ContributorObj co = givenNameFullName.get(fullName);
			insertContribToContentstack(co.getFirstName(), co.getLastName(), co.getDegrees());
		}

	}


	private static void insertContribToContentstack(String givenNames, String surname, String degrees) throws IOException {
		// TODO Auto-generated method stub

		if (givenNames==null || surname==null) {
			return;
		}
		else if (givenNames.isEmpty() || surname.isEmpty()) {
			return;
		}


		JSONObject jo = new JSONObject();
		JSONObject entry = new JSONObject();
		String fullName = givenNames + " " + surname;
		entry.put("title",fullName);

		entry.put("first_name", givenNames);
		entry.put("last_name", surname);
		entry.put("degrees", degrees);

		entry.put("full_name", fullName);

		/*if (allNames.contains(fullName)) {
			return;
		}
		allNames.add(fullName);*/

		jo.put("entry", entry);

		String jsonStr= jo.toString();
		//System.out.println(entry.getString("title"));


		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, jsonStr);
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/contributor/entries?locale=en-us")
				.method("POST", body)
				.addHeader("api_key",  API_KEY)
				.addHeader("authorization", MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = client.newCall(request).execute();
		System.out.println(response.body().string());
		response.body().close() ;

	}

	private static void insertContribToContentstack(String fullName) throws IOException {
		// TODO Auto-generated method stub
		if (fullName==null) {
			return;
		}
		else if (fullName.isEmpty()) {
			return;
		}

		/*	if (allNames.contains(fullName)) {
			return;
		}


		allNames.add(fullName);*/
		JSONObject jo = new JSONObject();
		JSONObject entry = new JSONObject();
		entry.put("title",fullName);
		entry.put("contributor_line", fullName);
		jo.put("entry", entry);

		String jsonStr= jo.toString();
		insertAuthor(jsonStr);



	}



	private static void insertAuthor(String jsonStr) throws IOException {
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, jsonStr);
		Request request = new Request.Builder()
				.url("https://api.contentstack.io/v3/content_types/contributor/entries?locale=en-us")
				.method("POST", body)
				.addHeader("api_key",  API_KEY)
				.addHeader("authorization", MANAGEMENT_TOKEN)
				.addHeader("Content-Type", "application/json")
				.build();
		Response response = client.newCall(request).execute();
		System.out.println(response.body().string());
		response.body().close() ;
	}





	public static Map<String,List<String>> getFileUniqueTitle() throws ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		Map<String,List<String>>map = new HashMap<String,List<String>>();

		Set<String>elNames=  new HashSet<String>();
		Set<String>allNames = new HashSet<String>();
		String rootDir = ARTICLE_XML_FOLDER_PATH;

		List<String>filePaths =FileUtil.getFilePaths(rootDir);
		int count=0;
		for (String fileP : filePaths) {

			String filename = extractFileName(fileP);
			//  System.out.println(fileP);
			//System.out.println(count++);
			File file= new File(fileP);

			// System.out.println(fileP);
			Document doc= null;

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(file);
			doc.getDocumentElement().normalize();



			// System.out.println("Root element " + doc.getDocumentElement().getNodeName());

			NodeList nodeList=doc.getElementsByTagName("contrib");


			for (int i=0; i<nodeList.getLength(); i++) 
			{
				// Get element
				Node n = nodeList.item(i);
				NodeList contribCh = n.getChildNodes();

				for (int j=0;j<contribCh.getLength();j++) {
					Node innerN = contribCh.item(j);
					//System.out.println("inner node " + innerN.getNodeName());
					if (innerN.getNodeName().equals("string-name")) {

						String fullName = innerN.getTextContent();
						//System.out.println(" full Name " + fullName);
						List<String>list = map.get(filename);
						if (list == null) {
							list= new ArrayList<String>();

						}
						list.add(fullName);
						map.put(filename, list);


					}
					else if (innerN.getNodeName().equals("name")) {
						String givenNames = "";
						String surname = "";
						String degrees = "";
						NodeList authorFieldList = innerN.getChildNodes();

						for (int k=0;k<authorFieldList.getLength();k++) {
							Node authN = authorFieldList.item(k);
							if (authN.getNodeName().equals("given-names")) {
								givenNames = authN.getTextContent();
							}
							else if (authN.getNodeName().equals("surname")) {
								surname = authN.getTextContent();
							}
							else if (authN.getNodeName().equals("degrees")) {
								degrees = authN.getTextContent();
							}
							//System.out.println(authN.getNodeName() + " " +authN.getTextContent());
						}

						if (notEmpty(givenNames,surname)) {
							String fullName = givenNames + " " + surname;
							List<String>list = map.get(filename);
							if (list == null) {
								list= new ArrayList<String>();

							}
							list.add(fullName);
							map.put(filename, list);
						}

					}
				}


			}




		}
		return map;
	}

	private static boolean notEmpty(String givenNames, String surname) {
		if (givenNames==null || surname ==null) {
			return false;
		}
		if (givenNames.isEmpty()) {
			return false;
		}
		if (surname.isEmpty()) {
			return false;
		}
		return true;
	}

	private static String extractFileName(String fileP) {
		// TODO Auto-generated method stub

		int filePLast = fileP.lastIndexOf("/");
		String txt = fileP.substring(filePLast+1, fileP.length()-4);
		return txt;
	}
}
