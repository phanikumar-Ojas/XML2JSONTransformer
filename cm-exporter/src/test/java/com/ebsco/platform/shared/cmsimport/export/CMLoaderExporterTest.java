package com.ebsco.platform.shared.cmsimport.export;

import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.sql.Date;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ebsco.platform.shared.cmsimport.export.utils.TestUtils;

public class CMLoaderExporterTest {
	
	@Test
	public void insertFullTextContent_JsonRTE_to_XML_ul_li_p_text_SPAN_text() throws Exception {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setCoalescing(true);
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();
        doc.setXmlStandalone(true);
        
        Element textBodyElement = doc.createElement("body");
        
        ExportArticleObj article = new ExportArticleObj();
        String json = TestUtils.readResourceFile("/cm-exporter/article-bltee464b8bc545f82b.json");
        
        JSONObject jo = new JSONObject(json);
        article.setMainBody(jo.getJSONObject("main_body").toString());
    	
        CMLoaderExporter.insertFullTextContent(doc, textBodyElement, article, Collections.emptyList(), true);
    	
    	doc.appendChild(textBodyElement);
    	
    	String xml = toXML(doc);
    	
    	//FileUtil.writeStringToFileWithUTF8("D:\\ebsco\\explore-source-data-rs-products\\bug\\blocker-us1078890\\out\\5.xml", xml);
    	
    	assertTrue(xml, xml.contains("<list-item><p>In a single sentence, state what you currently think is/are the argument(s) in the abstinence education debate.</p></list-item>") );
    	assertTrue(xml, xml.contains("<list-item><p>On the Points of View home page, under") );
    	assertTrue(xml, xml.contains("<title>Before You Read</title>") );
	}
	
	
	@Test
	public void missedTitlesInH1withSpan() throws Exception {
		String json = TestUtils.readResourceFile("/cm-exporter/h1-span-missed-titles/article-blt228515c473b13332.json");
		ExportArticleObj article = from(json);
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setCoalescing(true);
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        doc.setXmlStandalone(true);
        
        Element textBodyElement = doc.createElement("body");
		CMLoaderExporter.insertFullTextContent(doc, textBodyElement, article, Collections.emptyList(), true);
    	
    	doc.appendChild(textBodyElement);
    	
    	String xml = toXML(doc);
    	assertTrue(xml, xml.contains("<title>Before You Read</title>") );
	}
	
	private static String toXML(Document doc) {
		try {
	       DOMSource domSource = new DOMSource(doc);
	       StringWriter writer = new StringWriter();
	       StreamResult result = new StreamResult(writer);
	       TransformerFactory tf = TransformerFactory.newInstance();
	       Transformer transformer = tf.newTransformer();
		   transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "sec abstract citation");
		   transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	       transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		   transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		   
	       transformer.transform(domSource, result);
	       return cleanCData(writer.toString());
	    } catch(TransformerException e) {
	       throw new RuntimeException(e);
	    }
    }
	
	private static String cleanCData(String xmlStr) {
		String str = xmlStr;
		str = str.replaceAll("<!\\[CDATA\\[", "");
		str = str.replaceAll("\\]\\]>", "");
		return str;
	}
	
	private static ExportArticleObj from(String json) {
		JSONObject item = new JSONObject(json);
		ExportArticleObj ao = new ExportArticleObj();
		ao.setArticleId(item.getString("uid"));
		ao.setArticleTitle(item.getString("title"))	;
		
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
		
		return ao;
	}
	
	private static String extractDate(String dateStr) {
		String date = dateStr.substring(0,10);
		return date;
	}
}
