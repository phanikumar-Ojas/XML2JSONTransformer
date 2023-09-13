package com.ebsco.platform.shared.cmsimport.export;

import com.ebsco.platform.shared.cmsimport.collection.CollectionObj;
import com.ebsco.platform.shared.cmsimport.contributor.ContributorObj;
import com.ebsco.platform.shared.cmsimport.export.utils.RestClient;
import com.ebsco.platform.shared.cmsimport.export.utils.UrlUtil;
import com.ebsco.platform.shared.cmsimport.pdf.PDFHelper;
import com.ebsco.platform.shared.cmsimport.utilities.*;
import org.apache.commons.cli.*;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

public class CMLoaderExporter {

	private static final String PROJECT_ID_STR = "projectId";
	private static final String SKIP_PDF_OPTION_NAME = "nopdf";

	private static final Map<String, String> CONTENTSTACK_HEADERS = ContentstackHelper.getContentstackHeaders();
	
	public static class Project {
		
		private String title;
		private Type type;
		
		public enum Type {
			NEW, UPDATE
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public Type getType() {
			return type;
		}

		public void setType(Type type) {
			this.type = type;
		}
	}

    public static void main(String[] args) throws IOException, TransformerConfigurationException, ParserConfigurationException {

		Options options = new Options();

		options.addOption(Option.builder("p").longOpt(PROJECT_ID_STR).hasArg()
				.desc("project id")
				.build());
		
		options.addOption(Option.builder(SKIP_PDF_OPTION_NAME).required(false)
				.desc("Skip export PDF files")
				.build());

		CommandLineParser parser = new DefaultParser();

		String projectId = null;
		boolean nopdf = false;
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (!line.hasOption(PROJECT_ID_STR)) {

				System.out.println("You need to supply a project id with the p flag to run CM Loader Export");
				return;
			}

			else {
				projectId = line.getOptionValue(PROJECT_ID_STR);
			}
			
			if (line.hasOption(SKIP_PDF_OPTION_NAME)) {
				nopdf = true;
			}

		}
		catch (ParseException exp) {
			System.out.println("Unexpected exception:" + exp.getMessage());
			return;
		}

//		int noOfAssets = ContentstackUtil.getCountOfAssets();
//		Map<String,String> fileUidMap = ContentstackUtil.getFilenameToUidUrlMap(noOfAssets);
		
		System.out.println("projectId = " + projectId);
		System.out.println("nopdf = " + nopdf);
		
		Project project = getProjectByUid(projectId);
		if (project == null) {
			System.out.println("projectId = " + projectId + " was not found in contentstack");
			return;
		}

		String rootFolder = createOverarchingFolder(project.getTitle());

		//get all articles in project

		List<String>projectsProductUids = getProjectsProductCodes(projectId);

		int numberOfArticles = ContentstackUtil.getCountOfArticlesTiedToProject(projectId);
		Set<String>titleSourceUids  =  getTitleSourceUidsTiedToProject(projectId, numberOfArticles, projectsProductUids);

		System.out.println(titleSourceUids.size());
		Map<String,String> titleSourceUidPublisherMap = getTitleSourceUidPublisherMap(titleSourceUids);


		Map<String,List<ExportArticleObj>>midArticleMap = getMidToArticleUidMap(titleSourceUids);

		Date projectDate = getProjectDate(projectId);

		createProjectDirs (rootFolder, midArticleMap, projectDate, project.getType());


		if (Project.Type.UPDATE.equals(project.getType())) {
			createIssueSwitchesExcelSheet(rootFolder, midArticleMap, projectDate);
			createIssueDetailsExcelSheet(rootFolder, midArticleMap, projectDate);
		}

		int articleCount=0;
		for (String mid : midArticleMap.keySet()) {

			List<ExportArticleObj>articles = midArticleMap.get(mid);
			for (ExportArticleObj article : articles) {

				Document doc = createMetaXml(article, mid, projectDate, project.getType(), titleSourceUidPublisherMap, articles );
				String xmlStr = getStringFromDocument(doc);

				String folder = getFolder(rootFolder, mid, article, projectDate, project.getType());
			    writeXML(xmlStr, folder, article, project.getType());
                writeImage(article, folder);
                if (!nopdf) {
                	String articleUrl = UrlUtil.getArticleByIdUrl(article.getUrl());
    				String html = RestClient.sendGetRequest(articleUrl);
    				if (html != null) {
    					writePDF(article, html, folder, project.getType());
    				}
                }

                System.out.println("Exporting article " + ++articleCount + " : " + article.getArticleTitle());

            }

		}
		//get all mids in the articles
		//get all articles belonging to the mid

    }

	private static List<String> getProjectsProductCodes(String projectId) {
		List<String>productIdList = new ArrayList<>();

		String url = UrlUtil.getEntriesWithFilter("project","\"uid\":\""
						+ projectId + "\"");
		String responseBody = RestClient.sendGetRequest(url, CONTENTSTACK_HEADERS);

		if (Objects.isNull(responseBody)) {
			return Collections.emptyList();
		}

		JSONObject jo = new JSONObject(responseBody);
		JSONArray entriesArray = jo.getJSONArray("entries");
		for (int j=0; j<entriesArray.length(); j++) {
			JSONObject item = entriesArray.getJSONObject(j);
			if (item.has("product") && !item.isNull("product")) {
				JSONArray productsArr = item.getJSONArray("product");

				for (int i=0;i<productsArr.length();i++) {
					JSONObject productObj = productsArr.getJSONObject(i);
					String productUid = productObj.getString("uid");
					productIdList.add(productUid);

				}
			}

		}

		return productIdList;
	}

	private static Map<String, String> getTitleSourceUidPublisherMap(Set<String> titleSourceUids) {
		// TODO Auto-generated method stub

		Map<String,String>map = new HashMap<String,String>();
		for (String titleSourceUid : titleSourceUids) {
			String publisher = null;
			try {
				publisher = getPublisherFromTitleSourceUid(titleSourceUid);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			map.put(titleSourceUid, publisher);
		}
		return map;
	}


	private static Project getProjectByUid(String projectId) {
		String responseBody = RestClient.sendGetRequest(
				"https://api.contentstack.io/v3/content_types/project/entries?query={\"uid\":\""
				+ projectId +"\"}", CONTENTSTACK_HEADERS);

		JSONObject jo = new JSONObject(responseBody);
		JSONArray entriesArray = jo.getJSONArray("entries");
		for (int j=0; j<entriesArray.length(); j++) {
			JSONObject item = entriesArray.getJSONObject(j);
			Project result = new Project();
			if (item.has("project_type")) {
				String projectType = item.getString("project_type");
				if (projectType.equals("New")) {
					result.setType(Project.Type.NEW);
				}
				else if (projectType.equals("Update")) {
					result.setType(Project.Type.UPDATE);
				}
			}
			if (item.has("title")) {
				result.setTitle(item.getString("title"));
			}
			return result;
		}

		return null;
	}

	private static void writePDF(ExportArticleObj article, String html, String folder, Project.Type projectType) throws IOException {

		String xhtml = PDFHelper.htmlToXhtml(html);

		String articleId = null;

		if (Project.Type.UPDATE.equals(projectType)) {
			articleId = article.getProjectAn();
		}
		else if (Project.Type.NEW.equals(projectType)) {
			articleId = article.getArticleId();
		}

		String pathToFile = folder + FileUtil.getFileSeperator() + articleId + ".pdf";
		PDFHelper.xhtmlToPdf(xhtml,  pathToFile);


	}


	private static Date getProjectDate(String projectId) throws IOException {
		String responseBody = RestClient.sendGetRequest(
				"https://api.contentstack.io/v3/content_types/project/entries?query={\"uid\":\""
						+ projectId +"\"}", CONTENTSTACK_HEADERS);

		JSONObject jo = new JSONObject(responseBody);
		JSONArray entriesArray = jo.getJSONArray("entries");
		for (int j=0; j<entriesArray.length(); j++) {
			JSONObject item = entriesArray.getJSONObject(j);
			if (item.has("start_date")) {
				Date projectDate = Date.valueOf(item.getString("start_date"));
				return projectDate;
			}
		}
		return null;
	}

	private static void createIssueDetailsExcelSheet(String rootFolder,
			Map<String, List<ExportArticleObj>> midArticleMap, Date projectDate) {


		String dirDivider =FileUtil.getFileSeperator();

		String rootFolderName = rootFolder.substring(rootFolder.lastIndexOf(dirDivider)+1);
		Workbook wb = new XSSFWorkbook();
		String workbookName = "issue_details_" + rootFolderName;
		Sheet sheet1 = wb.createSheet("q_export_issue_switches_details");
		CreationHelper createHelper = wb.getCreationHelper();

		//create header row
		Row row = sheet1.createRow(0);

//title	images	Old MID	Old DTFORMAT	New MID	New DTFORMAT	mfs_an	article_id
		row.createCell(0).setCellValue(
			     createHelper.createRichTextString("title"));

		row.createCell(1).setCellValue(
			     createHelper.createRichTextString("images"));
		row.createCell(2).setCellValue(
			     createHelper.createRichTextString("Old MID"));
		row.createCell(3).setCellValue(
			     createHelper.createRichTextString("Old DTFORMATT"));
		row.createCell(4).setCellValue(
			     createHelper.createRichTextString("New MID"));
		row.createCell(5).setCellValue(
			     createHelper.createRichTextString("New DTFORMAT"));
		row.createCell(6).setCellValue(
			     createHelper.createRichTextString("mfs_an"));
		row.createCell(7).setCellValue(
			     createHelper.createRichTextString("article_id"));


		int rowNumber= 0;
		for (String mid : midArticleMap.keySet()) {
			List<ExportArticleObj> articles = midArticleMap.get(mid);

			for (int i=0;i<articles.size();i++) {
				rowNumber++;
				Row rowC = sheet1.createRow(rowNumber);
				ExportArticleObj ao = articles.get(i);
				//System.out.println("project an " + ao.getProjectAn());
				int images = getNumberOfImagesInFT(ao);

				rowC.createCell(0).setCellValue(
					     createHelper.createRichTextString(ao.getArticleTitle()));
				rowC.createCell(1).setCellValue(
					     createHelper.createRichTextString(String.valueOf(images)));
				rowC.createCell(2).setCellValue(
					     createHelper.createRichTextString(mid));
				rowC.createCell(3).setCellValue(
					     createHelper.createRichTextString(getDtFormatStr(ao.getProjectDtFormat())));
				rowC.createCell(4).setCellValue(
					     createHelper.createRichTextString(mid));
				rowC.createCell(5).setCellValue(
					     createHelper.createRichTextString(getDtFormatStr(projectDate)));
				rowC.createCell(6).setCellValue(
					     createHelper.createRichTextString(ao.getProjectAn()));
				rowC.createCell(7).setCellValue(
					     createHelper.createRichTextString(ao.getArticleId()));
			}
		}


		String filePath = rootFolder + FileUtil.getFileSeperator() + workbookName + ".xlsx";
		try (OutputStream fileOut = new FileOutputStream(filePath)) {
		    wb.write(fileOut);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static int getNumberOfImagesInFT(ExportArticleObj ao) {
		// TODO Auto-generated method stub
		int noOfImages = 0;
		String mainBodyStr = ao.getMainBody();
		JSONObject jo = new JSONObject(mainBodyStr);
		JSONArray children = jo.getJSONArray("children");
		for (int i=0;i<children.length();i++) {
			JSONObject child = children.getJSONObject(i);
			String type = child.getString("type");
			if (type.equals("p")) {
				JSONArray grandchildren = child.getJSONArray("children");
				for (int j=0;j<grandchildren.length();j++) {
					JSONObject grandchild = grandchildren.getJSONObject(j);
					if (grandchild.has("type")) {
						String grandchildType = grandchild.getString("type");
						if (grandchildType.equals("reference")) {
							noOfImages = addToImageCount(noOfImages, grandchild);
						}
					}
				}

			}
			else if (type.equals("reference")) {
				noOfImages = addToImageCount(noOfImages, child);
			}
		}
		return noOfImages;
	}

	private static int addToImageCount(int noOfImages, JSONObject child) {
		int tot = noOfImages;

		if (child.has("attrs")) {
			JSONObject attrsObj = child.getJSONObject("attrs");
			//if (attrsObj.has("asset-type")) {
			String assetType = attrsObj.getString("asset-type");
			if (assetType.equals("image/jpeg")) {
				tot++;
			}
			else if (assetType.equals("image/png")) {
				tot++;
			}
			//}
		}
		return tot;
	}

	private static void createIssueSwitchesExcelSheet(String rootFolder,
			Map<String, List<ExportArticleObj>> midArticleMap, Date projectDate) {

		String dirDivider =FileUtil.getFileSeperator();

		String rootFolderName = rootFolder.substring(rootFolder.lastIndexOf(dirDivider)+1);
		Workbook wb = new XSSFWorkbook();
		String workbookName = "issue_switches_" + rootFolderName;
		Sheet sheet1 = wb.createSheet("q_export_issue_switches_report");
		CreationHelper createHelper = wb.getCreationHelper();

		//create header row
		Row row = sheet1.createRow(0);

		//UI	project	original title	title	status
		row.createCell(0).setCellValue(
			     createHelper.createRichTextString("Old MID"));

		row.createCell(1).setCellValue(
			     createHelper.createRichTextString("Old DTFORMAT"));
		row.createCell(2).setCellValue(
			     createHelper.createRichTextString("New MID"));
		row.createCell(3).setCellValue(
			     createHelper.createRichTextString("New DTFORMAT"));
		row.createCell(4).setCellValue(
			     createHelper.createRichTextString("Volume"));
		row.createCell(5).setCellValue(
			     createHelper.createRichTextString("Issue"));
		row.createCell(6).setCellValue(
			     createHelper.createRichTextString("MAGDTCOVER"));


		int rowNumber = 0;

		for (String mid : midArticleMap.keySet()) {
			List<ExportArticleObj> articles = midArticleMap.get(mid);

			String oldDtFormatStr = getDtFormatStr(articles.get(0).getProjectDtFormat());
			String newDtFormatStr = getDtFormatStr(projectDate);
				rowNumber++;
				Row rowC = sheet1.createRow(rowNumber);
				rowC.createCell(0).setCellValue(
					     createHelper.createRichTextString(mid));
				rowC.createCell(1).setCellValue(
					     createHelper.createRichTextString(oldDtFormatStr));
				rowC.createCell(2).setCellValue(
					     createHelper.createRichTextString(mid));
				rowC.createCell(3).setCellValue(
					     createHelper.createRichTextString(newDtFormatStr));
				rowC.createCell(6).setCellValue(
					     createHelper.createRichTextString(newDtFormatStr));

		}


		String filePath = rootFolder + FileUtil.getFileSeperator() + workbookName + ".xlsx";
		try (OutputStream fileOut = new FileOutputStream(filePath)) {
		    wb.write(fileOut);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private static void writeImage(ExportArticleObj article, String folder) {
		// TODO Auto-generated method stub

		String mainBodyStr = article.getMainBody();
		JSONObject jo = new JSONObject(mainBodyStr);
		JSONArray children = jo.getJSONArray("children");
		for (int i=0;i<children.length();i++) {
			JSONObject child = children.getJSONObject(i);
			String type = child.getString("type");
			if (type.equals("p")) {
				JSONArray grandchildren = child.getJSONArray("children");
				for (int j=0;j<grandchildren.length();j++) {
					JSONObject grandchild = grandchildren.getJSONObject(j);

					if (grandchild.has("type")) {

						String grandchildType = grandchild.getString("type");
						if (grandchildType.equals("reference")) {
							downloadImage(grandchild, folder);
						}
					}
				}

			}
			else if (type.equals("reference")) {
				downloadImage(child,folder);
			}
		}
	}

	private static void downloadImage(JSONObject child, String folder) {
		// TODO Auto-generated method stub
		String dirDivider =FileUtil.getFileSeperator();

		if (child.has("attrs")) {
			JSONObject attrsObj = child.getJSONObject("attrs");
			String assetName = attrsObj.getString("asset-name");
			String assetType = attrsObj.getString("asset-type");
			String assetLink = attrsObj.getString("asset-link");


			if (assetType.equals("image/jpeg")) {
				if (!assetName.endsWith(".jpg") && !assetName.endsWith(".jpeg")) {
					assetName += ".jpg";
				}
			}
			else if (assetType.equals("image/png")) {
				if (!assetName.endsWith(".png")) {
					assetName += ".png";
				}
			}

			if (assetType.equals("image/jpeg")) {
				String filePath = folder + dirDivider + assetName ;
				try(InputStream in = new URL(assetLink).openStream()){
					Files.copy(in, Paths.get(filePath));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if (assetType.equals("image/png")) {
				String filePath = folder + dirDivider + assetName;
				try(InputStream in = new URL(assetLink).openStream()){
					Files.copy(in, Paths.get(filePath));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private static String getFolder(String rootFolder, String mid, ExportArticleObj article, Date projectDate, Project.Type projectType) {
		String dirDivider =FileUtil.getFileSeperator();
		String folderStart = rootFolder + dirDivider + mid;

		Date dtFormat = article.getProjectDtFormat();

		if (dtFormat==null && projectType.equals(Project.Type.NEW)) {
			dtFormat = projectDate;
		}

		if (dtFormat == null) {
			dtFormat = Date.valueOf(LocalDate.now());
		}
		String dtFormatStr = createDirStr(dtFormat);


		String fullFolderPath = folderStart + dirDivider + dtFormatStr;
		return fullFolderPath;
	}

	private static void  writeXML(String xmlStr, String rootFolder, ExportArticleObj article, Project.Type projectType) {
		String dirDivider =FileUtil.getFileSeperator();

		String cleanedXmlStr = cleanCData(xmlStr);

		byte[] bytes = cleanedXmlStr.getBytes(StandardCharsets.UTF_8);

		cleanedXmlStr = new String(bytes, StandardCharsets.UTF_8);

		String fileName = null;

		if (Project.Type.UPDATE.equals(projectType)) {
			fileName = article.getProjectAn() + ".xml";

		}
		else if (Project.Type.NEW.equals(projectType)) {
			fileName = article.getArticleId() + ".xml";
		}
		String fullPath = rootFolder + dirDivider + fileName;
		FileUtil.writeStringToFileWithUTF8(fullPath, cleanedXmlStr);
	}

	private static String cleanCData(String xmlStr) {
		String str = xmlStr;
		str = str.replaceAll("<!\\[CDATA\\[", "");
		str = str.replaceAll("\\]\\]>", "");
		return str;
	}

	private static Document createMetaXml(ExportArticleObj article, String mid, Date projectDate, Project.Type projectType, Map<String,String>tsUidPublisherMap, List<ExportArticleObj> siblingArticles) throws ParserConfigurationException, TransformerConfigurationException, IOException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setCoalescing(true);
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();
        doc.setXmlStandalone(true);

        Element bookElement = doc.createElement("book");
        bookElement.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
        bookElement.setAttribute("xmlns:mml", "http://www.w3.org/1998/Math/MathML");
        bookElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        bookElement.setAttribute("dtd-version", "2.3");
        bookElement.setAttribute("xml:lang", "EN");

        Element bookSeriesMetaElement = doc.createElement("book-series-meta");
        Element bookIdElement = doc.createElement("book-id");

        String collName = getColSeriesName(article);
        bookIdElement.setTextContent(collName);
        bookIdElement.setAttribute("pub-id-type", "publisher-id");

        bookSeriesMetaElement.appendChild(bookIdElement);
        bookElement.appendChild(bookSeriesMetaElement);

        Element bookMetaElement = createBookMetaElement(doc,article,mid, projectDate,tsUidPublisherMap);
        bookElement.appendChild(bookMetaElement);

        Element bodyElement = doc.createElement("body");
        bookElement.appendChild(bodyElement);

       // <book-part book-part-type="chapter" id="pov_uk.773649" indexed="true" xml:lang="EN" xlink:type="simple">

        Element bookPartElement = doc.createElement("book-part");
        bookPartElement.setAttribute("book-part-type", "chapter");
        String bookPartId = getColSeriesName(article) + UUID.randomUUID();
        bookPartElement.setAttribute("id", bookPartId);
        bookPartElement.setAttribute("indexed", "true");
        bookPartElement.setAttribute("xml:lang", "EN");
        bookPartElement.setAttribute("xlink:type", "simple");

        bodyElement.appendChild(bookPartElement);

        Element bookPartMetaElement = createBookPartMetaElement(doc,article,mid, projectType);
        bookPartElement.appendChild(bookPartMetaElement);
        doc.appendChild(bookElement);


        Element textBodyElement = doc.createElement("body");
        boolean needsTocSection = needTOCSection(article);
        insertFullTextContent(doc, textBodyElement,article, siblingArticles, needsTocSection);
        bookPartElement.appendChild(textBodyElement);



        if (!isEmptyJSONRTEField(article.getAuthorNote())) {
        	Element authorNoteElement = createAuthorNoteElement(doc, article.getAuthorNote(), article);
        	if (authorNoteElement!=null) {
        		textBodyElement.appendChild(authorNoteElement);
        	}
        }


        if (!isEmptyJSONRTEField(article.getCitations())) {

        	Element citationsElement = createCitationsElement(doc, article.getCitations(), article);
        	if (citationsElement!=null) {
        		textBodyElement.appendChild(citationsElement);
        	}
        }

        Document doc1 = null;
        try {
        	doc1 = addingStylesheet(doc);
        } catch (ParserConfigurationException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }
        return doc1;
	}




	private static Element createAuthorNoteElement(Document doc, String authorNote, ExportArticleObj article) {
		if (authorNote == null) {
			return null;

		}

		else {
			Element authorNoteSecElement = doc.createElement("sec");
			authorNoteSecElement.setAttribute("sec-type","bodytext" );
			authorNoteSecElement.setAttribute("indexed", "true");
			addEmptyTitleIfTheFirstElementIsNotH1(doc, authorNoteSecElement, authorNote);
			JSONObject jo = new JSONObject(authorNote);
			JSONArray entriesArray = jo.getJSONArray("children");

			for (int i=0;i<entriesArray.length();i++) {

				JSONObject item = entriesArray.getJSONObject(i);

				if (i==0&& item.getString("type").equals("h1")) {
					continue;
				}
				else {



					String cdataTxt = handleP(item, false);
					authorNoteSecElement.appendChild(doc.createTextNode(cdataTxt));


				}

			}
			return authorNoteSecElement;
		}
	}

	private static Element createCitationsElement(Document doc, String citations, ExportArticleObj article) {
		// TODO Auto-generated method stub
		//<sec sec-type="hangindent" indexed="true">

		if (citations == null) {
			return null;

		}

		else {
			Element citationSecElement = doc.createElement("sec");
			citationSecElement.setAttribute("sec-type","hangindent" );
			citationSecElement.setAttribute("indexed", "true");

			addEmptyTitleIfTheFirstElementIsNotH1(doc, citationSecElement, citations);



			JSONObject jo = new JSONObject(citations);
			JSONArray entriesArray = jo.getJSONArray("children");


			List<List<JSONObject>> allSections = new ArrayList<List<JSONObject>>();

			List<JSONObject>sectionList = new ArrayList<JSONObject>();
			for (int j=0; j<entriesArray.length(); j++) {

				JSONObject item = entriesArray.getJSONObject(j);
				String itemType  =item.getString("type");
				if (itemType.equals("h1") && j==0) {

					continue;
				}
				else if (itemType.equals("h1") && j!=0) {

					if (sectionList.isEmpty()) {
						sectionList.add(item);
					}

					else {
					List<JSONObject>newList = new ArrayList<JSONObject>();
					newList.addAll(sectionList);
					sectionList.clear();
					sectionList.add(item);
					allSections.add(newList);
					}
				}
				else {
					sectionList.add(item);
				}

			}

			if (sectionList.size()>0) {
				allSections.add(sectionList);
			}



			for (List<JSONObject> secList : allSections) {

				Element refListElement = doc.createElement("ref-list");
				citationSecElement.appendChild(refListElement);


				for (int i=0;i<secList.size();i++) {
					JSONObject item = secList.get(i);
					if ( item.getString("type").equals("h1")) {


					     Element titleElement = doc.createElement("title");
					     titleElement.setTextContent(getTextContent(item));
						 refListElement.appendChild(titleElement);


					}
					else {
						Element refElement = doc.createElement("ref");
						Element citationElement = doc.createElement("citation");
						handleCitationAttributes(item, citationElement);
						String cdataTxt = handleP(item, true);

						if (!cdataTxt.isEmpty()) {
							citationElement.appendChild(doc.createTextNode(cdataTxt));

							refElement.appendChild(citationElement);
							refListElement.appendChild(refElement);
						}
					}



				}
			}

			return citationSecElement;
		}
	}




	private static void handleCitationAttributes(JSONObject item, Element citationElement) {
		if (item.has("attrs")) {
			JSONObject attrs = item.getJSONObject("attrs");
			if (attrs.has("redactor-attributes")) {
				//System.out.println("ITEM " + item);
				JSONObject redactorAttrs = attrs.getJSONObject("redactor-attributes");
				if (redactorAttrs.has("xmlns:xlink")) {
					citationElement.setAttribute("xmlns:xlink", redactorAttrs.getString("xmlns:xlink"));

				}
				if (redactorAttrs.has("xlink:type")) {
					citationElement.setAttribute("xlink:type", redactorAttrs.getString("xlink:type"));

				}
				if (redactorAttrs.has("citation-type")) {
					citationElement.setAttribute("citation-type", redactorAttrs.getString("citation-type"));

				}

			}

		}
	}

	private static void addEmptyTitleIfTheFirstElementIsNotH1(Document doc, Element citationSecElement,
			String citations) {

		JSONObject jo = new JSONObject(citations);
		JSONArray entriesArray = jo.getJSONArray("children");
		if (entriesArray.length()>0) {
			JSONObject item = entriesArray.getJSONObject(0);
			String itemType  =item.getString("type");
			if (itemType.equals("h1")) {
		        String cdataTxt = handleH1(item);
		        citationSecElement.appendChild(doc.createTextNode(cdataTxt));
			}
			else {
		        Element emptyTitleElement = doc.createElement("title");
		        citationSecElement.appendChild(emptyTitleElement);
			}
		}

	}

	static void insertFullTextContent(Document doc, Element textBodyElement, ExportArticleObj article, List<ExportArticleObj> siblingArticles, boolean needsTocSection) throws IOException {

		//System.out.println("NEEDS TOC SECTION " + needsTocSection);
		JSONObject jo = new JSONObject(article.getMainBody());
		JSONArray entriesArray = jo.getJSONArray("children");

		List<List<JSONObject>> allSections = new ArrayList<List<JSONObject>>();

		List<JSONObject>sectionList = new ArrayList<JSONObject>();
		for (int j=0; j<entriesArray.length(); j++) {

			JSONObject item = entriesArray.getJSONObject(j);
			String itemType  =item.getString("type");
			if (itemType.equals("h1") && j!=0) {

				List<JSONObject>newList = new ArrayList<JSONObject>();
				newList.addAll(sectionList);
				sectionList.clear();
				sectionList.add(item);
				allSections.add(newList);
			}
			else {
				sectionList.add(item);
			}

		}

		if (sectionList.size()>0) {
			allSections.add(sectionList);
		}

		if (needsTocSection) {
			boolean hasTocSection = hasTocSection(allSections);
			//System.out.println("HAS TOC SECTION " + hasTocSection);


			if (!hasTocSection) {
				addTocSection(doc,textBodyElement,article, siblingArticles);
			}
		}

		for (List<JSONObject> secList : allSections) {


			Element secElement = doc.createElement("sec");
			secElement.setAttribute("sec-type", "bodytext");
			secElement.setAttribute("indexed", "true");


			addEmptyTitleIfTheFirstElementIsNotH1(doc, secElement, secList);

			String cdataTxt ="";

			for (JSONObject subsec : secList) {

				//create section obj
				//treat p, h1, reference,ol, ul, fragment
				String itemType  = subsec.getString("type");
				switch (itemType) {
				case "fragment":
					cdataTxt+=handleFragment(subsec);
					break;
				case "h1":
					cdataTxt+=handleH1(subsec);
					break;
				case "p":
					cdataTxt+=handleP(subsec, false);
					break;
				case "reference":
					cdataTxt+=handleReference(subsec);
					break;
				case "ol":
					cdataTxt+=handleOl(subsec);
					break;
				case "ul":
					cdataTxt+=handleUl(subsec);
					break;
				case "code":
					cdataTxt+=handleCode(subsec);
					break;

				case "blockquote":
					cdataTxt+=handleBlockQuote(subsec);
					break;
				}


			}
			//secElement.appendChild(doc.createCDATASection(cdataTxt));
			secElement.appendChild(doc.createTextNode(cdataTxt));
			textBodyElement.appendChild(secElement);
		}

	}




	private static void addTocSection(Document doc, Element textBodyElement, ExportArticleObj article, List<ExportArticleObj> siblingArticles) throws IOException {
		String articlesOwnAn = article.getProjectAn();
		Element secElement = doc.createElement("sec");
		secElement.setAttribute("sec-type", "toclink");
		secElement.setAttribute("indexed", "true");

		List<ExportArticleObj>relatedArticles = siblingArticles;
		String cdataTxt ="";

		//insert title
		cdataTxt = "<title>Related Articles</title>\n";

		//insert ps with ext link
		for (ExportArticleObj eao : relatedArticles) {
			String an = eao.getProjectAn();
			if (an.equals(articlesOwnAn)) {
				continue;
			}
			else {
				String title = eao.getArticleTitle();
				cdataTxt +=  " <p> <ext-link ext-link-type=\"AN\" xlink:href=\"" + an + "\" xlink:title=\"" + title + "\" xlink:type=\"simple\"/></p>\n";

			}
		}
		secElement.appendChild(doc.createTextNode(cdataTxt));

		textBodyElement.appendChild(secElement);
	}




	private static List<ExportArticleObj> getRelatedArticlesOld(ExportArticleObj article) throws IOException {

		List<ExportArticleObj>  relatedArticles = new ArrayList<ExportArticleObj>();
		String topic = article.getArtTopic();

		String responseBody = RestClient.sendGetRequest(
				"https://api.contentstack.io/v3/content_types/article/entries?&query={\"topic\":{\"$in_query\":{\"uid\":\""
						+ topic + "\"}}}", CONTENTSTACK_HEADERS);

		JSONObject jo = new JSONObject(responseBody);
		JSONArray entriesArray = jo.getJSONArray("entries");
		for (int j=0; j<entriesArray.length(); j++) {
			JSONObject item = entriesArray.getJSONObject(j);
			ExportArticleObj eao = new ExportArticleObj();
			if (item.has("title")) {
				String titleStr  =item.getString("title");
				//System.out.println("RELATED ART" + titleStr);

				eao.setArticleTitle(titleStr);
			}

			addProjectMFSAnAndDtFormat(eao,item, article.getTitleSourceUid());
			relatedArticles.add(eao);

		}

		return relatedArticles;

	}



	private static boolean hasTocSection(List<List<JSONObject>> allSections) {
		List<JSONObject>firstSection = new ArrayList<JSONObject>();
		if (allSections.size()>0) {
			firstSection = allSections.get(0);
		}
		for (JSONObject subsec : firstSection) {

			//create section obj
			//treat p, h1, reference,ol, ul, fragment
			String itemType  = subsec.getString("type");
			if (itemType.equals("h1")) {
				JSONArray childrenArray = subsec.getJSONArray("children");
				String allText= "";
				for (int i=0; i<childrenArray.length(); i++) {
					JSONObject item = childrenArray.getJSONObject(i);
					String pText="";
					pText = handleFTStyles(item, pText);
					allText+=pText;
				}
				if (allText.equals("Related Articles")) {
					return true;
				}
				else {
					return false;
				}
			}
			else {
				return false;
			}

		}

		return false;
	}




	private static boolean needTOCSection(ExportArticleObj article) throws IOException {
		List<String>productUids = article.getProduct();

		for (String productUid : productUids) {


			String responseBody = RestClient.sendGetRequest(
					"https://api.contentstack.io/v3/content_types/product/entries?query={\"uid\":\""
							+productUid +"\"}", CONTENTSTACK_HEADERS);

			JSONObject jo = new JSONObject(responseBody);
			JSONArray entriesArray = jo.getJSONArray("entries");
			for (int i=0; i<entriesArray.length(); i++) {
				JSONObject prod = entriesArray.getJSONObject(i);
				if (prod.has("toc_on_export")) {
					boolean tocOnExport = prod.getBoolean("toc_on_export");
					if (tocOnExport) {
						return true;
					}
				}

			}
		}
		return false;
	}




	private static String handleBlockQuote(JSONObject subsec) {
		JSONArray childrenArray = subsec.getJSONArray("children");
		String allText= "<p content-type=\"extract\">";
		for (int i=0; i<childrenArray.length(); i++) {
			JSONObject item = childrenArray.getJSONObject(i);
			String pText="";
			pText = handleFTStyles(item, pText);
			allText+=pText;
		}
		allText +="</p>\n";

		return allText;
	}

	private static String handleFTStyles(JSONObject item, String pText) {
		if (item.has("text")) {
			pText = item.getString("text");
			pText = escapeSpecialCharacters(pText);
		}
		if (item.has("bold")) {
			pText = "<bold>" + pText + "</bold>";
		}

		if (item.has("italic")) {
			pText = "<italic>" + pText + "</italic>";

		}

		if (item.has("superscript")) {
			pText = "<sup>" + pText + "</sup>";

		}

		if (item.has("subscript")) {
			pText = "<sub>" + pText + "</sub>";

		}
		return pText;
	}

	private static String escapeSpecialCharacters(String pText) {
		// TODO Auto-generated method stub
		String txt = pText;
		txt = txt.replaceAll("&" , "&amp;");
		return txt;
	}

	private static String handleCode(JSONObject subsec) {
		JSONArray childrenArray = subsec.getJSONArray("children");
		String allText= "<preformat position=\"float\" xml:space=\"preserve\">";
		for (int i=0; i<childrenArray.length(); i++) {
			JSONObject item = childrenArray.getJSONObject(i);
			String pText="";
			pText = handleFTStyles(item, pText);
			allText+=pText;
		}
		allText +="</preformat>\n";

		return allText;
	}

	private static void addEmptyTitleIfTheFirstElementIsNotH1(Document doc, Element secElement, List<JSONObject> secList) {
		if (secList!=null) {
			if (secList.size()>0) {

				JSONObject firstItem = secList.get(0);
				String itemType  = firstItem.getString("type");
				if (!itemType.equals("h1")) {
			        Element emptyTitleElement = doc.createElement("title");
			        secElement.appendChild(emptyTitleElement);

				}

			}

		}

	}



	private static String handleUl(JSONObject subsec) {



		JSONArray childrenArray = subsec.getJSONArray("children");
		String allText= "<list list-type=\"bullet\">";
		for (int i=0; i<childrenArray.length(); i++) {
			JSONObject item = childrenArray.getJSONObject(i);


			String pText="<list-item>";
			JSONArray liItems = item.getJSONArray("children");
			String liText="";
			boolean isPara = false;

			for (int j=0; j<liItems.length();j++) {
				JSONObject liItem = liItems.getJSONObject(j);

				if (liItem.has("type") && !liItem.isNull("type")) {
					if (liItem.getString("type").equals("p")) {
						isPara=true;
						liText="<p>";
					}


				}

				else {
					isPara = true;
					if (j==0) {
						liText+="<p>";
					}
					liText+=   handleFTStyles(liItem, liText);

				}

				if (liItem.has("children") && !liItem.isNull("children")) {
					JSONArray items = liItem.getJSONArray("children");
					for (int k=0; k<items.length(); k++) {
						JSONObject ulLiP = items.getJSONObject(k);
						String individualTxt = "";
						if (ulLiP.has("type") && ulLiP.getString("type").equals("span")) {
							if (ulLiP.has("children")) {
								JSONArray spanChildren = ulLiP.getJSONArray("children");
								for (Object object : spanChildren) {
									JSONObject spanChild = (JSONObject) object;
									individualTxt += handleFTStyles(spanChild, individualTxt);
								}
							}
						} else {
							individualTxt = handleFTStyles(ulLiP, individualTxt);
						}
						liText += individualTxt;
					}
				}
				//System.out.println("litext" + liText);
			}
			if (isPara) {
				liText+="</p>";
			}
			pText+=liText;

			pText+="</list-item>";
			allText+=pText;
		}
		allText +="</list>\n";

		//System.out.println("ALL OL " + allText );
		return allText;

	}


	private static String handleReference(JSONObject subsec) {
		JSONObject attrsObj = subsec.getJSONObject("attrs");
		String caption = null;
		String fileName = null;

		String allText= " <fig fig-type=\"elec_use\" position=\"margin\">";
		allText+="<caption></caption>";
		if (attrsObj.has("asset-caption")) {
			caption = attrsObj.getString("asset-caption");

		}

		if (attrsObj.has("asset-name") && attrsObj.has("asset-type")) {
			String assetName = attrsObj.getString("asset-name");
			String assetType = attrsObj.getString("asset-type");
			fileName = assetName ;


			if (assetType.equals("image/jpeg")) {
				if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
					fileName += ".jpg";
				}
			}
			else if (assetType.equals("image/png")) {
				if (!fileName.endsWith(".png")) {
					fileName += ".png";
				}
			}
		}
		allText+="<ext-link ext-link-type=\"db-image\" xlink:href=\"" +fileName+"\" xlink:type=\"simple\"/>";
		allText+="<permissions><copyright-statement>" +caption + "</copyright-statement> </permissions>";
		allText+="</fig>";


		return allText;
	}

	private static String handleHtmlReference(JSONObject subsec) {
		JSONObject attrsObj = subsec.getJSONObject("attrs");
		String caption = null;
		String fileName = null;
		String url = null;
		String assetLink = null;
		String allText= "<div class=\"imagediv\"><figure>";
		if (attrsObj.has("asset-caption")) {
			caption = attrsObj.getString("asset-caption");

		}

		if (attrsObj.has("asset-name") && attrsObj.has("asset-type")) {
			String assetName = attrsObj.getString("asset-name");
			String assetType = attrsObj.getString("asset-type");
			fileName = assetName ;


			if (assetType.equals("image/jpeg")) {
				if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
					fileName += ".jpg";
				}
			}
			else if (assetType.equals("image/png")) {
				if (!fileName.endsWith(".png")) {
					fileName += ".png";
				}
			}

		}

		if (attrsObj.has("asset-link")) {
			assetLink = attrsObj.getString("asset-link");
		}

		if (attrsObj.has("redactor-attributes")) {
			JSONObject redactorAttrs = attrsObj.getJSONObject("redactor-attributes");


			if (redactorAttrs.has("src")) {
				url = redactorAttrs.getString("src");

			}
		}


		if (caption==null) {
			caption = "";
		}

		if (url==null) {
			url = assetLink;
		}

		allText+="  <img src=\"" + url + "\"  style=\"width:400px; height:auto; display: block; margin: 0 auto;\">";
		allText+="<figcaption>" +caption + "</figcaption>";
		allText+="</figure></div>";


		return allText;
	}

	private static String handleOl(JSONObject subsec) {
		JSONArray childrenArray = subsec.getJSONArray("children");
		String allText= "<list list-type=\"order\">";
		for (int i=0; i<childrenArray.length(); i++) {
			JSONObject item = childrenArray.getJSONObject(i);

			//System.out.println("json item "  +item);

			String pText="<list-item>";
			JSONArray liItems = item.getJSONArray("children");
			String liText="";
			boolean isPara = false;

			for (int j=0; j<liItems.length();j++) {


				JSONObject liItem = liItems.getJSONObject(j);

				if (liItem.has("type") && !liItem.isNull("type"))  {

					if (liItem.getString("type").equals("p")) {
						isPara=true;
						liText="<p>";
					}

				}
				else {
					isPara = true;
					if (j==0) {
						liText+="<p>";
					}
					liText+=   handleFTStyles(liItem, liText);

				}

				if (liItem.has("children") && !liItem.isNull("children")) {

					JSONArray indTextItemArray = liItem.getJSONArray("children");
					for (int k=0; k<indTextItemArray.length();k++) {
						JSONObject indTextItem = indTextItemArray.getJSONObject(k);
						String individualTxt = "";
						individualTxt = handleFTStyles(indTextItem, individualTxt);
						liText+=individualTxt;
					}
				}
				//System.out.println("litext" + liText);
			}
			if (isPara) {
				liText+="</p>";
			}
			pText+=liText;

			pText+="</list-item>";
			allText+=pText;
		}
		allText +="</list>\n";

		return allText;

	}



	private static String handleP(JSONObject subsec, boolean isCitation) {
		// TODO Auto-generated method stub
		JSONArray childrenArray = subsec.getJSONArray("children");
		String allText= "";
		if (!isCitation) {
			allText="<p>";
		}
		for (int i=0; i<childrenArray.length(); i++) {
			JSONObject item = childrenArray.getJSONObject(i);

			String pText="";

			if (item.has("type")) {
				if (item.getString("type").equals("a")) {
					pText = handleExtLink(item);
				}
				if (item.getString("type").equals("reference")) {
					return handleReference(item);
				}
				if (item.getString("type").equals("ul")) {
					return handleUl(item);
				}
				if (item.getString("type").equals("span")) {

					if (item.has("children"))
					{

						JSONArray spanChildrenArray = item.getJSONArray("children");
						for (int j=0;j<spanChildrenArray.length(); j++)	{
							JSONObject spanChild = spanChildrenArray.getJSONObject(j);

							pText += handleFTStyles(spanChild, pText);
						}

					}
				}
			}

			else {
				pText = handleFTStyles(item, pText);
			}
			allText+=pText;
		}
		if (!isCitation)
			allText +="</p>\n";

		return allText;
	}



	private static String handleExtLink(JSONObject item) {
		String txt= "<ext-link";

		boolean isANLink = false;
		if (item.has("attrs")) {
			JSONObject attrs = item.getJSONObject("attrs");
			if (attrs.has("redactor-attributes")) {
				//System.out.println("ITEM " + item);
				JSONObject redactorAttrs = attrs.getJSONObject("redactor-attributes");


				if (redactorAttrs.has("xmlns:xlink")) {
					String val = redactorAttrs.getString("xmlns:xlink");
					txt += " xmlns:xlink=\""  + val +"\"";

				}
				if (redactorAttrs.has("xlink:type")) {
					String val = redactorAttrs.getString("xlink:type");
					txt += " xlink:type=\""  + val +"\"";


				}
				if (redactorAttrs.has("ext-link-type")) {
					String val = redactorAttrs.getString("ext-link-type");
					txt += " ext-link-type=\""  + val +"\"";

					if (val.equals("AN")) {
						isANLink = true;
					}
				}

				if (redactorAttrs.has("xlink:href")) {
					String val = redactorAttrs.getString("xlink:href");
					txt += " xlink:href=\""  + val +"\"";


				}

				if (redactorAttrs.has("xlink:title")) {
					String val = redactorAttrs.getString("xlink:title");
					txt += " xlink:title=\""  + val +"\"";


				}

			}
		}
		txt +=">";

		if (!isANLink) {
			JSONArray childrenArray = item.getJSONArray("children");
			for (int i=0; i<childrenArray.length(); i++) {
				JSONObject child = childrenArray.getJSONObject(i);

				String pText="";

				pText = handleFTStyles(child, pText);

				txt+=pText;
			}

		}
		txt+= "</ext-link>";
		return txt;
	}

	private static String handleH1(JSONObject subsec) {
		JSONArray childrenArray = subsec.getJSONArray("children");
		String allText= "<title>";
		for (int i=0; i<childrenArray.length(); i++) {
			JSONObject item = childrenArray.getJSONObject(i);
			String pText="";
			if (item.has("type") && item.getString("type").equals("span")) {
				if (item.has("children")) {
					JSONArray spanChildren = item.getJSONArray("children");
					for (Object object : spanChildren) {
						JSONObject spanChild = (JSONObject) object;
						pText += handleFTStyles(spanChild, pText);
					}
				}
			} else {
				pText = handleFTStyles(item, pText);
			}
			allText+=pText;
		}
		allText +="</title>\n";

		return allText;
	}



	private static String handleFragment(JSONObject subsec) {
		//do nothing
		return "";

	}


	private static String getTextContent(JSONObject subsec) {
		JSONArray childrenArray = subsec.getJSONArray("children");
		String allText="";
		for (int i=0; i<childrenArray.length(); i++) {
			JSONObject item = childrenArray.getJSONObject(i);
			String pText="";
			pText = handleFTStyles(item, pText);
			allText+=pText;
		}
		return allText;
	}

	private static Element createBookPartMetaElement(Document doc, ExportArticleObj article, String mid, Project.Type projectType) {
		// TODO Auto-generated method stub
        Element bookPartMetaElement = doc.createElement("book-part-meta");


       // <book-part-id pub-id-type="publisher-id">pov_aus_2015_229422</book-part-id>
        Element bookPartIdElement = doc.createElement("book-part-id");
        bookPartIdElement.setAttribute("pub-id-type", "publisher-id");
        bookPartIdElement .setTextContent(article.getArticleId());

		bookPartMetaElement.appendChild(bookPartIdElement);

		/*<title-group>
           <title>Global Recession: An Overview.</title>
        </title-group>*/

        Element titleGroupElement = doc.createElement("title-group");
        Element titleElement = doc.createElement("title");
        titleElement.setTextContent(article.getArticleTitle());
		titleGroupElement.appendChild(titleElement);

		bookPartMetaElement.appendChild(titleGroupElement);


		/*
		 *             <contrib-group>
               <contrib xmlns:xlink="http://www.w3.org/1999/xlink"
                        contrib-type="author"
                        xlink:type="simple">
                  <name name-style="western">
                     <given-names>Anastasia</given-names>
                     <x xml:space="preserve"> </x>
                     <surname>Holt</surname>
                  </name>
               </contrib>
            </contrib-group>
		 */
        Element contribGroupElement = doc.createElement("contrib-group");

        for (ContributorObj co : article.getExpContributors()) {
        	Element contribElement = doc.createElement("contrib");
            contribElement.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
            contribElement.setAttribute("contrib-type", "author");
            contribElement.setAttribute("xlink:type", "simple");



            if (co.getFirstName()!=null && co.getLastName()!=null) {
            	  Element nameElement = doc.createElement("name");
                  nameElement.setAttribute("name-style", "western");
                  contribElement.appendChild(nameElement);
            	Element givenNamesElement = doc.createElement("given-names");
            	givenNamesElement.setTextContent(co.getFirstName());
            	Element xSpaceElement = doc.createElement("x");
            	xSpaceElement.setAttribute("xml:space", "preserve");
            	xSpaceElement.setTextContent(" ");
            	Element surnameElement = doc.createElement("surname");
            	surnameElement.setTextContent(co.getLastName());
            	nameElement.appendChild(givenNamesElement);
            	nameElement.appendChild(xSpaceElement);
            	nameElement.appendChild(surnameElement);

            }
            else {
            	String fullName = co.getFullName();
            	Element stringNameElement = doc.createElement("string-name");
            	stringNameElement.setTextContent(fullName);

            	contribElement.appendChild(stringNameElement);
            }

        	contribGroupElement.appendChild(contribElement);
        }

        addEmptyContributorIfNoContributors( article.getExpContributors(), contribGroupElement, doc);

		bookPartMetaElement.appendChild(contribGroupElement);


		/*
		 *
		 *             <abstract abstract-type="placard">
               <p>The global recession of 2008–09 is considered the worst financial crisis since the Great Depression of the 1930s. Highlighting the increased interdependence of the global economy, it affected almost every nation, with the impact of the financial downturn being followed by reductions in trade and consumption worldwide. Key markers of the global recession were the collapse of several key international financial institutions, significant reductions in housing values in the United States, and declining growth and rising unemployment throughout the developed world. Though Australia was affected less severely than other nations, such as the United States and the United Kingdom, there were significant changes to the national economy and federal government policy as a result of the global recession. Australia was also involved in changes to international finance regulations as a result of the crisis. The effects of the global recession are ongoing.</p>
            </abstract>
		 */

		if (!isEmptyJSONRTEField(article.getFtAbstract())) {
			//System.out.println("ABSTRACT " + article.getFtAbstract());
			Element abstractElement = doc.createElement("abstract");
			abstractElement.setAttribute("abstract-type", "placard");
			String abstractTxt= processAbstractTxt(article.getFtAbstract());
			abstractElement.appendChild(doc.createTextNode(abstractTxt));

			bookPartMetaElement.appendChild(abstractElement);
		}

		/*
		 *             <custom-meta-wrap>
               <custom-meta xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple">
                  <meta-name>mfs AN</meta-name>
                  <meta-value>47962545</meta-value>
               </custom-meta>
               <custom-meta xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple">
                  <meta-name>doctype</meta-name>
                  <meta-value>ENCYC</meta-value>
               </custom-meta>
               <custom-meta xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple">
                  <meta-name>arttype</meta-name>
                  <meta-value>Article</meta-value>
               </custom-meta>
            </custom-meta-wrap>
		 */

		Element customMetaWrapElement = doc.createElement("custom-meta-wrap");

		if (projectType.equals(Project.Type.UPDATE)) {
			addCustomMeta(doc, customMetaWrapElement,"mfs AN", article.getProjectAn());
		}
		addCustomMeta(doc, customMetaWrapElement,"arttype", article.getArtType());
		//addCustomMeta(doc, customMetaWrapElement,"derived_from_ud", article.getArtType());


		bookPartMetaElement.appendChild(customMetaWrapElement);

		return bookPartMetaElement;
	}



	private static void addEmptyContributorIfNoContributors(List<ContributorObj> expContributors,
			Element contribGroupElement, Document doc) {

		if (expContributors==null || expContributors.size()==0) {

			Element contribElement =  doc.createElement("contrib");
			contribElement.setAttribute("contrib-type", "author");
			Element nameElement =  doc.createElement("name");
			Element givenNamesElement =  doc.createElement("given-names");
			Element surnameElement =  doc.createElement("surname");
			nameElement.appendChild(givenNamesElement);
			nameElement.appendChild(surnameElement);
			contribElement.appendChild(nameElement);
			contribGroupElement.appendChild(contribElement);

		}

	}

	private static boolean isEmptyJSONRTEField(String jsonRTETxt) {
		if (jsonRTETxt==null) {
			return true;
		}

		String txt = "";
		JSONObject absObj = new JSONObject(jsonRTETxt);
		if (absObj.has("children")) {
			JSONArray children = absObj.getJSONArray("children");
			for (int i=0; i<children.length();i++) {
				JSONObject child = children.getJSONObject(i);
				if (child.has("children")) {
					JSONArray grandChildren = child.getJSONArray("children");
					for (int j=0; j<grandChildren.length();j++) {
						JSONObject grandChild = grandChildren.getJSONObject(j);
						if (grandChild.has("text")) {
							String grandChildTxt = grandChild.getString("text");
							if (!grandChildTxt.isEmpty()) {
								return false;
							}
							txt+=  grandChildTxt ;
						}
					}

				}
			}
		}
		return txt.isEmpty();

	}

	private static String processAbstractTxt(String ftAbstract) {
		if (ftAbstract==null || ftAbstract.isEmpty()) {
			return "";
		}
		JSONObject jo = new JSONObject(ftAbstract);
		JSONArray children = jo.getJSONArray("children");
		String cdataTxt="";
		for (int i=0;i<children.length();i++) {
			JSONObject item = children.getJSONObject(i);
			String itemType  = item.getString("type");
			switch (itemType) {
			case "fragment":
				cdataTxt+=handleFragment(item);
				break;
			case "h1":
				cdataTxt+=handleH1(item);
				break;
			case "p":
				cdataTxt+=handleP(item, false);
				break;
			case "reference":
				cdataTxt+=handleReference(item);
				break;
			case "ol":
				cdataTxt+=handleOl(item);
				break;
			case "ul":
				cdataTxt+=handleUl(item);
				break;
			}


		}
		return cdataTxt;
	}

    private static void addCustomMeta(Document doc, Element customMetaWrapElement, String metaName, String metaValue) {


		/*
		 * <custom-meta xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple">
                  <meta-name>mfs AN</meta-name>
                  <meta-value>47962545</meta-value>
               </custom-meta>
		 */
		Element customMetaElement = doc.createElement("custom-meta");
        customMetaElement.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
        customMetaElement.setAttribute("xlink:type", "simple");
        Element metaNameElement = doc.createElement("meta-name");
        metaNameElement.setTextContent(metaName);

        Element metaValueElement = doc.createElement("meta-value");
        metaValueElement.setTextContent(metaValue);

        customMetaElement.appendChild(metaNameElement);
        customMetaElement.appendChild(metaValueElement);

        customMetaWrapElement.appendChild(customMetaElement);
	}



	private static Element createBookMetaElement(Document doc, ExportArticleObj article, String mid, Date projectDate, Map<String,String> tsUidPublisherMap) {
        Element bookMetaElement = doc.createElement("book-meta");
        //      <book-id pub-id-type="publisher-id">pov_aus_2018</book-id>
        Element bookIdElement = doc.createElement("book-id");
        bookIdElement.setAttribute("pub-id-type", "publisher-id");
        bookIdElement.setTextContent(getColName(article));
        bookMetaElement.appendChild(bookIdElement);

        //      <book-id pub-id-type="eis-mid">L6L7</book-id>

        Element bookIdElement2 = doc.createElement("book-id");
        bookIdElement2.setAttribute("pub-id-type", "eis-mid");
        bookIdElement2.setTextContent(mid);
        bookMetaElement.appendChild(bookIdElement2);


        /*
        <book-title-group>
         <book-title>Australian Points of View</book-title>
         <alt-title alt-title-type="mid">B29E</alt-title>
      </book-title-group>

         */

        Element bookTitleGroupElement = doc.createElement("book-title-group");
        Element bookTitleElement = doc.createElement("book-title");
        bookTitleElement.setTextContent(getColTitle(article));

        bookTitleGroupElement.appendChild( bookTitleElement);

        Element altTitleElement = doc.createElement("alt-title");
        altTitleElement.setAttribute("alt-title-type", "mid");
        altTitleElement.setTextContent(mid);
        bookTitleGroupElement.appendChild( altTitleElement);

        bookMetaElement.appendChild(bookTitleGroupElement);


        /*
         *       <publisher>
         <publisher-name>Great Neck Publishing</publisher-name>
      </publisher>
         *
         */

        Element publisherElement = doc.createElement("publisher");
        Element publisherNameElement = doc.createElement("publisher-name");
        publisherNameElement.setTextContent(getColPublisher(article, tsUidPublisherMap));
        publisherElement.appendChild(publisherNameElement);
        bookMetaElement.appendChild(publisherElement);

        Element pubDateReviewedElement = createPubDateElement (doc, projectDate,"reviewed");
        if (pubDateReviewedElement!=null) {
        	bookMetaElement.appendChild(pubDateReviewedElement);
        }

        Element pubDateOriginalElement = createPubDateElement (doc, article.getDateCreated(),"original");
        if (pubDateOriginalElement!=null) {
        	bookMetaElement.appendChild(pubDateOriginalElement);
        }

        /*
        Element pubDateRevisedElement = createPubDateElement (doc, article.getLastUpdatedDate(),"revised");
        if (pubDateRevisedElement!=null) {
        	bookMetaElement.appendChild(pubDateRevisedElement);
        }*/

        String copyrightStr = getColCopyright(article);
        if (copyrightStr!=null) {
        	if (!copyrightStr.isEmpty()) {
                Element copyrightStatementElement = doc.createElement("copyright-statement");
                copyrightStatementElement.setTextContent(copyrightStr);
                bookMetaElement.appendChild(copyrightStatementElement);

        	}
        }
        return bookMetaElement;

	}



	private static Element createPubDateElement(Document doc, Date date, String attVal) {

		if (date!=null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);

			int month = cal.get(Calendar.MONTH);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			int year = cal.get(Calendar.YEAR);

			Element pubDateElement = doc.createElement("pub-date");
        	pubDateElement.setAttribute("pub-type", attVal);

			Element dayElement = doc.createElement("day");
			Element monthElement = doc.createElement("month");
			Element yearElement = doc.createElement("year");

			dayElement.setTextContent(String.valueOf(day));
			monthElement.setTextContent(getMonthName(month));
			yearElement.setTextContent(String.valueOf(year));
			pubDateElement.appendChild(dayElement);
			pubDateElement.appendChild(monthElement);
			pubDateElement.appendChild(yearElement);

        	return pubDateElement;

		}
		else {
			return null;
		}
	}



	private static String getMonthName(int month) {
		//System.out.println("month int " + month);
		 String months[] = {"January", "February", "March", "April",
                 "May", "June", "July", "August", "September",
                 "October", "November", "December"};
		 return months[month];
	}

	private static String getColCopyright(ExportArticleObj article) {
		// TODO Auto-generated method stub
		List<CollectionObj> colls = article.getExpCollections();
		for (CollectionObj coll : colls) {
			return coll.getCopyrightStatement();
		}
		return null;
	}

	private static String getColSeriesName(ExportArticleObj article) {
		// TODO Auto-generated method stub
		List<CollectionObj> colls = article.getExpCollections();
		for (CollectionObj coll : colls) {
			return coll.getSeriesIdXML();
		}
		return null;
	}


	private static String getColPublisher(ExportArticleObj article, Map<String, String> tsUidPublisherMap) {
		String titleSourceUid =article.getTitleSourceUid();
		return tsUidPublisherMap.get(titleSourceUid);
	}

	private static String getColName(ExportArticleObj article) {
		// TODO Auto-generated method stub
		List<CollectionObj> colls = article.getExpCollections();
		for (CollectionObj coll : colls) {
			return coll.getCollectionId();
		}
		return null;
	}

	private static String getColTitle(ExportArticleObj article) {
		// TODO Auto-generated method stub
		List<CollectionObj> colls = article.getExpCollections();
		for (CollectionObj coll : colls) {
			return coll.getCollectionTitle();
		}
		return null;
	}

	public static String getStringFromDocument(Document doc)
	{
	    try
	    {
	       DOMSource domSource = new DOMSource(doc);
	       StringWriter writer = new StringWriter();
	       StreamResult result = new StreamResult(writer);
	       TransformerFactory tf = TransformerFactory.newInstance();
	       Transformer transformer = tf.newTransformer();
		   transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "sec abstract citation");
		   transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	       transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		   transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		   DOMImplementation domImpl = doc.getImplementation();

		   DocumentType doctype = domImpl.createDocumentType("book",
				    "-//Atypon//DTD Atypon Systems NCBI Book DTD Suite v2.3.0 20090807//EN",
				    "atypon-book.dtd");
				transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
				transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
	       transformer.transform(domSource, result);
	       return writer.toString();
	    }
	    catch(TransformerException ex)
	    {
	       ex.printStackTrace();
	       return null;
	    }
	}

	private static <ProcessingInstructionImpl> Document addingStylesheet(
	        Document doc) throws TransformerConfigurationException,
	        ParserConfigurationException {
	    ProcessingInstructionImpl pi = (ProcessingInstructionImpl) doc
	            .createProcessingInstruction("xml-stylesheet",
	                    "href=\"file:////edc-filer1/busdev/PropPubProjects/Research Starters/Common Documents/CSS/CSS_Salem.css\"");
	    Element root = doc.getDocumentElement();
	    doc.insertBefore((Node) pi, root);



	    //trans.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(bout, "utf-8")));
	    return doc;

	}

	private static void createProjectDirs(String rootFolder, Map<String, List<ExportArticleObj>> midArticlesMap, Date projectDate, Project.Type projectType) {


		for (String mid : midArticlesMap.keySet()) {
			List<ExportArticleObj> articles = midArticlesMap.get(mid);
			Set<Date>dtFormats = getDtFormatsFromArticles(articles, projectDate, projectType);
			String dirDivider =FileUtil.getFileSeperator();
			FileUtil.createDir(rootFolder + dirDivider + mid);
			for (Date dtFormat: dtFormats) {
				if (dtFormat!=null) {
					String dtFormatStr = createDirStr(dtFormat);
					FileUtil.createDir(rootFolder + dirDivider + mid + dirDivider + dtFormatStr);
				}
			}

		}
		//get dirs
	}



	private static String createDirStr(Date dtFormat) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf = new SimpleDateFormat(
			    "yyyyMMdd");
		return sdf.format(dtFormat);
	}


	private static String getDtFormatStr(Date dtFormat) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf = new SimpleDateFormat(
			    "MM/dd/yyyy");
		if (dtFormat == null) {
			dtFormat = Date.valueOf(LocalDate.now());
		}
		return sdf.format(dtFormat);
	}


	private static Set<Date> getDtFormatsFromArticles(List<ExportArticleObj> articles, Date projectDate, Project.Type pt) {
		Set<Date>dateSet = new HashSet<Date>();
		for (ExportArticleObj ao : articles) {
			if (ao.getProjectDtFormat()!=null) {
				dateSet.add(ao.getProjectDtFormat());
			} else {
				dateSet.add(Date.valueOf(LocalDate.now()));
			}
		}
		if (dateSet.isEmpty() && pt.equals(Project.Type.NEW)) {
			dateSet.add(projectDate);
		}
		return dateSet;
	}


    private static String createOverarchingFolder(String projectName) {

		String defaultDir = getDefaultDirectory();

		if (!new File(defaultDir).exists()) {
			FileUtil.createDir(defaultDir);
		}

		String folderPath = getDefaultDirectory() + FileUtil.getFileSeperator() + projectName;
		FileUtil.createDir(folderPath);
		return folderPath;
	}



	private static String getDefaultDirectory() {
		boolean isWindows = SystemUtil.isWindows();
		String defaultDir = null;

		if (!isWindows) {
			//defaultDir = "/usr/local/exports";
			defaultDir = "/Users/mpamuk/Desktop/cms_pov/export";
		}

		else  {
			defaultDir = "C:/exports";
		}

		return defaultDir;
	}

	private static Map<String, List<ExportArticleObj>> getMidToArticleUidMap(Set<String> titleSourceUids) throws IOException {

		Map<String, List<ExportArticleObj>>map = new HashMap<>();

		for (String titleSourceUid : titleSourceUids) {
			String mid = getMidFromTitleSourceUid(titleSourceUid);
			int numberOfArticles = getCountOfArticlesTiedToTitleSourceUid(titleSourceUid);

			List<ExportArticleObj> articles = getArticlesByTitleSource(titleSourceUid, numberOfArticles);
			map.put(mid, articles);

		}
		return map;
	}


	private static int getCountOfArticlesTiedToTitleSourceUid(String titleSourceUid) throws IOException {
		String responseBody = RestClient.sendGetRequest("https://api.contentstack.io/v3/content_types/article/entries?count=true&query={\"article_definitions\": {\"$in_query\": {\"title_source\": { \"$in_query\": {\"uid\": \""
						+ titleSourceUid +"\"}}}}}", CONTENTSTACK_HEADERS);

				JSONObject jo = new JSONObject(responseBody);
				return jo.getInt("entries");
	}



	private static List<ExportArticleObj> getArticlesByTitleSource(String titleSourceUid, int totalEntries) throws IOException {
		List<ExportArticleObj>articles = new ArrayList<>();

		int index = (totalEntries/100)+1;

		for (int i=0;i<index;i++) {
			int skip=i *100;

			String responseBody = RestClient.sendGetRequest("https://api.contentstack.io/v3/content_types/article/entries?skip="
							+skip+ "&query={\"article_definitions\": {\"$in_query\": {\"title_source\": { \"$in_query\": {\"uid\": \""
							+ titleSourceUid +"\"}}}}}", CONTENTSTACK_HEADERS);

			JSONObject jo = new JSONObject(responseBody);
			JSONArray entriesArray = jo.getJSONArray("entries");
			for (int j=0; j<entriesArray.length(); j++) {
				JSONObject item = entriesArray.getJSONObject(j);


				ExportArticleObj ao = new ExportArticleObj();
				ao.setArticleId(item.getString("uid"));
				ao.setArticleTitle(item.getString("title"))	;
				ao.setTitleSourceUid(titleSourceUid);
				ao.setUrl(item.getString("url"));

				//System.out.println(ao.getArticleTitle());
				/*if (item.has("dtformat") && !item.isNull("dtformat")) {
					String dateStr = extractDate(item.getString("dtformat"));

					Date dtformat = Date.valueOf(dateStr);
					ao.setDtformat(dtformat);
				}*/
				/*
				if (item.has("abstract")) {
					ao.setFtAbstract(item.getString("abstract"));
				}*/
				/*
				
				if (item.has("article_id") && !item.isNull("article_id")) {
					ao.setArticleId(item.getString("article_id"));
				}*/


				if (item.has("derived_from_id")) {
					ao.setDerivedFromId(item.getString("derived_from_id"));
				}
				if (item.has("art_type")) {
					ao.setArtType(item.getString("art_type"));
				}

				if (item.has("date_created") && !item.isNull("date_created")) {
					String dateStr = extractDate(item.getString("date_created"));
					Date dateCreated = Date.valueOf(dateStr);
					ao.setDateCreated(dateCreated);
				}

				if (item.has("review_date")&&!item.isNull("review_date")) {
					String dateStr = extractDate(item.getString("review_date"));
					Date reviewDate = Date.valueOf(dateStr);
					ao.setReviewDate(reviewDate);
				}

				if (item.has("last_updated_date")&&!item.isNull("last_updated_date")) {
					String dateStr = extractDate(item.getString("last_updated_date"));
					Date lastUpdatedDate = Date.valueOf(dateStr);
					ao.setLastUpdatedDate(lastUpdatedDate);
				}

				if (item.has("abstract") && !item.isNull("abstract")) {
					ao.setFtAbstract(item.getJSONObject("abstract").toString());
				}

				if (item.has("main_body")) {
					ao.setMainBody(item.getJSONObject("main_body").toString());
				}

				if (item.has("citations")&&!item.isNull("citations")) {
					JSONObject citObj = item.getJSONObject("citations");
					ao.setCitations(citObj.toString());

				}

				if (item.has("author_note")) {
					ao.setAuthorNote(item.getJSONObject("author_note").toString());
				}


				if (item.has("topic")) {
					JSONArray topicArray = item.getJSONArray("topic");
					if (topicArray.length()>0) {
						JSONObject topicObj= (JSONObject) topicArray.get(0);
						ao.setArtTopic(topicObj.getString("uid"));
					}
				}

				addContributors(ao, item);
				addCollections(ao,item);
				addProjectMFSAnAndDtFormat(ao,item, titleSourceUid);
				addProducts(ao,item);


				//System.out.println(ao);

				articles.add(ao);
			}

		}

		return articles;


	}



	private static String extractDate(String dateStr) {
		// TODO Auto-generated method stub
		String date = dateStr.substring(0,10);
		return date;
	}







	private static void addProducts(ExportArticleObj ao, JSONObject item) {
		List<String>productUids = new ArrayList<String>();
		if (item.has("products")) {
			JSONArray entriesArray = item.getJSONArray("products");
			for (int i=0; i<entriesArray.length(); i++) {
				JSONObject colObj = entriesArray.getJSONObject(i);
				String productUid = colObj.getString("uid");
				productUids.add(productUid);
			}
		}

		ao.setProduct(productUids);
	}



//ADD
	private static void addProjectMFSAnAndDtFormat(ExportArticleObj ao, JSONObject item, String titleSourceUid) throws IOException {

		List<String>artDefUids = new ArrayList<String>();
		if (item.has("article_definitions")) {
			JSONArray entriesArray = item.getJSONArray("article_definitions");
			for (int i=0; i<entriesArray.length(); i++) {
				JSONObject colObj = entriesArray.getJSONObject(i);
				String artDefUid = colObj.getString("uid");
				artDefUids.add(artDefUid);
			}
		}



		for (String artDefUid : artDefUids) {
			String responseBody = RestClient.sendGetRequest("https://api.contentstack.io/v3/content_types/article_product_definition/entries?query={\"uid\":\""
							+ artDefUid +"\"}", CONTENTSTACK_HEADERS);
					;
			JSONObject jo = new JSONObject(responseBody);
			JSONArray entriesArray = jo.getJSONArray("entries");
			for (int j=0; j<entriesArray.length(); j++) {
				JSONObject artDefItem = entriesArray.getJSONObject(j);
				if (artDefItem.has("title_source")  && !artDefItem.isNull("title_source")) {


					JSONArray titleSourceArr = artDefItem.getJSONArray("title_source");
					if (titleSourceArr.length()>0) {
						JSONObject titleSourceObj = titleSourceArr.getJSONObject(0);

						if (titleSourceUid.equals(titleSourceObj.getString("uid"))) {

							if (artDefItem.has("an") && !artDefItem.isNull("an")) {
								ao.setProjectAn(artDefItem.getString("an"));
							}
							if (artDefItem.has("dtformat") && !artDefItem.isNull("dtformat")) {

								String dateStr = extractDate(artDefItem.getString("dtformat"));
								Date projectDtformat = Date.valueOf(dateStr);
								ao.setProjectDtFormat(projectDtformat);

							}
						}
					}


				}

			}

		}
		
		
	
		/*
		
		List<String> ans = MFSSyncer.getMFSAnsFromArtDefUids(artDefUids);
		for (String an : ans) {
			if (an!=null && !an.isEmpty()) {
				ao.setArticleAn(an);
			}
		}
		
		*/

	}

	private static void addCollections(ExportArticleObj ao, JSONObject item) throws IOException {
		List<CollectionObj>collList = new ArrayList<CollectionObj>();
		String colUid =null;
		if (item.has("collections")) {
			JSONArray entriesArray = item.getJSONArray("collections");
			for (int i=0; i<entriesArray.length(); i++) {
				JSONObject colObj = entriesArray.getJSONObject(i);
				colUid = colObj.getString("uid");
			}
		}

		if (colUid!=null) {
			String responseBody = RestClient.sendGetRequest("https://api.contentstack.io/v3/content_types/collection/entries?query={\"uid\":\""
							+colUid +"\"}", CONTENTSTACK_HEADERS);

			JSONObject jo = new JSONObject(responseBody);
			JSONArray entriesArray = jo.getJSONArray("entries");
			for (int i=0; i<entriesArray.length(); i++) {
				JSONObject col = entriesArray.getJSONObject(i);


				String collId =  null;
				String collTitle = null;
				String publisher = null;
				String seriesIdXml = null;
				Integer pubYear  = null;
				Date pubDate = null;
				String publisherLoc = null;
				String copyrightStatement = null;
				String copyrightHolder = null;
				String rights = null;

				if (col.has("title")) {
					collId = col.getString("title");
				}
				if (col.has("collection_title")) {
					collTitle = col.getString("collection_title");
				}
				if (col.has("publisher")) {
					publisher = col.getString("publisher");
				}
				if (col.has("series_id_xml")) {
					seriesIdXml = col.getString("series_id_xml");
				}
				if (col.has("publication_date")) {
					String pubDateStr = col.getString("publication_date");
					pubDate = Date.valueOf(pubDateStr);
				}
				if (col.has("publication_year")) {
					pubYear = col.getInt("publication_year");
				}
				if (col.has("publication_location")) {
					publisherLoc = col.getString("publication_location");
				}
				if (col.has("copyright_statement")) {
					copyrightStatement = col.getString("copyright_statement");
				}
				if (col.has("copyright_holder")) {
					copyrightHolder = col.getString("copyright_holder");
				}
				if (col.has("rights")) {
					rights = col.getString("rights");
				}
				CollectionObj coll = new CollectionObj(collId, collTitle, publisher, seriesIdXml, pubDate, pubYear, publisherLoc, copyrightStatement, copyrightHolder, rights);
				collList.add(coll);
			}



		}

		ao.setExpCollections(collList);
	}






	private static void addContributors(ExportArticleObj ao, JSONObject item) throws IOException {
		List<ContributorObj>contributorList = new ArrayList<ContributorObj>();
		List<String> contUids = new ArrayList<String>();
		if (item.has("contributors")) {
			JSONArray entriesArray = item.getJSONArray("contributors");
			for (int i=0; i<entriesArray.length(); i++) {
				JSONObject colObj = entriesArray.getJSONObject(i);
				String contUid = colObj.getString("uid");
				contUids.add(contUid);
			}
		}

		if (contUids.size()>0) {

			for (String contUid : contUids) {
				ContributorObj co = getContributorObjByUid(contUid);
				contributorList.add(co);
			}


		}
		ao.setExpContributors(contributorList);

	}



	private static ContributorObj getContributorObjByUid(String contUid) throws IOException {
		ContributorObj co = null;

		String responseBody = RestClient.sendGetRequest("https://api.contentstack.io/v3/content_types/contributor/entries?query={\"uid\":\""
						+ contUid +"\"}", CONTENTSTACK_HEADERS);

		JSONObject jo = new JSONObject(responseBody);
		JSONArray entriesArray = jo.getJSONArray("entries");
		for (int i=0; i<entriesArray.length(); i++) {
			JSONObject item = entriesArray.getJSONObject(i);

			String firstName = null;
			String lastName = null;
			String fullName = null;
			String degrees = null;
			if (item.has("first_name")) {
				firstName = item.getString("first_name");
			}
			if (item.has("last_name")) {
				lastName = item.getString("last_name");
			}
			if (item.has("full_name")) {
				fullName = item.getString("full_name");
			}
			if (item.has("degrees")) {
				degrees = item.getString("degrees");
			}
			if (fullName==null) {
				fullName = item.getString("title");
			}
			co = new ContributorObj(fullName, firstName, lastName, degrees);
		}

		return co;
	}



	private static String getMidFromTitleSourceUid(String titleSourceUid) throws IOException {
		String responseBody = RestClient.sendGetRequest("https://api.contentstack.io/v3/content_types/book_source/entries?query={\"uid\":\""
						+ titleSourceUid +"\"}", CONTENTSTACK_HEADERS);

		JSONObject jo = new JSONObject(responseBody);
		JSONArray entriesArray = jo.getJSONArray("entries");
		for (int i=0; i<entriesArray.length(); i++) {
			JSONObject item = entriesArray.getJSONObject(i);
			if (item.has("title") && !item.isNull("title")) {
				return item.getString("title");
			}
		}

		return null;
	}


	private static String getPublisherFromTitleSourceUid(String titleSourceUid) throws IOException {
		String responseBody = RestClient.sendGetRequest("https://api.contentstack.io/v3/content_types/book_source/entries?query={\"uid\":\""
						+ titleSourceUid +"\"}", CONTENTSTACK_HEADERS);

		JSONObject jo = new JSONObject(responseBody);
		JSONArray entriesArray = jo.getJSONArray("entries");
		for (int i=0; i<entriesArray.length(); i++) {
			JSONObject item = entriesArray.getJSONObject(i);
			if (item.has("publisher") && !item.isNull("publisher")) {
				return item.getString("publisher");
			}
		}

		return null;
	}

	public static Set<String> getTitleSourceUidsTiedToProject(String projectId, int totalEntries, List<String> projectsProductUids) throws IOException {

		Set<String>allTitleSourceUids = new HashSet<>();

		int index = (totalEntries/100)+1;

		for (int i=0;i<index;i++) {
			int skip=i *100;

			String responseBody = RestClient.sendGetRequest(
					"https://api.contentstack.io/v3/content_types/article/entries?skip=" + skip
							+ "&query={\"current_project\":{\"$in_query\":{\"uid\":\"" + projectId + "\"}}}", CONTENTSTACK_HEADERS);

            JSONObject jo = new JSONObject(responseBody);
            JSONArray entriesArray = jo.getJSONArray("entries");
            for (int j = 0; j < entriesArray.length(); j++) {
                JSONObject item = entriesArray.getJSONObject(j);
                //System.out.println(item);

				JSONArray artDefArr = item.getJSONArray("article_definitions");
				List<String>artDefUids = new ArrayList<String>();
				for (int k=0; k<artDefArr.length();k++) {
					JSONObject artDefObj = artDefArr.getJSONObject(k);
					String artDefUid = artDefObj.getString("uid");
					artDefUids.add(artDefUid);
				}
				List<String> titleSourcEUids = getTitleSourceUidFromArtDefUids(artDefUids, projectsProductUids);
				allTitleSourceUids.addAll(titleSourcEUids);
			}
		}
		return allTitleSourceUids;
	}


	private static List<String> getTitleSourceUidFromArtDefUids(List<String> artDefUids, List<String> projectsProductUids) throws IOException {

		List<String> mfsMids = new ArrayList<String>();

		for (String artDefUid : artDefUids) {

			String responseBody = RestClient.sendGetRequest(
					"https://api.contentstack.io/v3/content_types/article_product_definition/entries?query={\"uid\":\""
							+ artDefUid +"\"}", CONTENTSTACK_HEADERS);

				JSONObject jo = new JSONObject(responseBody);
				JSONArray entriesArray = jo.getJSONArray("entries");
				for (int i=0; i<entriesArray.length(); i++) {
				    JSONObject item = entriesArray.getJSONObject(i);
				    if (item.has("title_source")) {
				    	JSONArray titleSourceArr = item.getJSONArray("title_source");
				    	for (int j=0; j<titleSourceArr.length(); j++) {
						    JSONObject tsObj = titleSourceArr.getJSONObject(j);
						    String titleSourceUid = tsObj.getString("uid");
						    boolean include = doIncludeTitleSource(titleSourceUid, projectsProductUids);
						    if (include) {
						    	mfsMids.add(titleSourceUid);
						    }

				    	}
				    }
				}

		}

		return mfsMids;
	}







	private static boolean doIncludeTitleSource(String titleSourceUid, List<String> projectProductUids) {

		String responseBody = RestClient.sendGetRequest(
				"https://api.contentstack.io/v3/content_types/book_source/entries?query={\"uid\":\""
						+ titleSourceUid +"\"}", CONTENTSTACK_HEADERS);

			JSONObject jo = new JSONObject(responseBody);
			JSONArray entriesArray = jo.getJSONArray("entries");
			for (int j=0; j<entriesArray.length(); j++) {
				JSONObject item = entriesArray.getJSONObject(j);
				if (item.has("products") && !item.isNull("products")) {
					JSONArray productsArr = item.getJSONArray("products");
					for (int i=0;i<productsArr.length();i++) {
						JSONObject productObj = productsArr.getJSONObject(i);
						String productUid = productObj.getString("uid");
						if (projectProductUids.contains(productUid)) {
							return true;
						}
					}
				}

			}


		return false;
	}
}
