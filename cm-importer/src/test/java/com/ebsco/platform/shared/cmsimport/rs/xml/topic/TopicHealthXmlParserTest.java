package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicHealth;
import com.ebsco.platform.shared.cmsimport.rs.util.TestUtils;

public class TopicHealthXmlParserTest {
	
	private static final String FOLDER = "/topic_health/";
	
	@Test
	public void asa_rs_139645() throws JSONException {
		check("asa_rs_139645.xml");
	}
	
	//RTE with links - definition field
	@Test
	public void asa_rs_87650() throws JSONException {
		check("asa_rs_87650.xml");
	}
	
	//"Treatment & Therapy" not found category in json schema, by default ignore it
	@Test
	public void cam_rs_66152() throws JSONException {
		check("cam_rs_66152.xml");
	}
	
	@Test
	public void rsdiseases_119513() throws JSONException {
		check("rsdiseases_119513.xml");
	}
	
	//"Treatment & Therapy" not found category in json schema, by default ignore it 
	@Test
	public void rsdiseases_19785() throws JSONException {
		check("rsdiseases_19785.xml");
	}
	
	/*
	 * TopicXmlDataEnricher (1 of 5472) Processing : [topic_health : cancer_rs_89303.xml]
	Exception in thread "main" java.lang.NullPointerException: Cannot invoke "java.util.Collection.iterator()" because "valuesToAdd" is null
	at com.ebsco.platform.shared.cmsimport.rs.util.ContentTypeUtil.addTags(ContentTypeUtil.java:118)
	at com.ebsco.platform.shared.cmsimport.rs.xml.topic.BoundedAndOrderedTagsExtractor.extract(BoundedAndOrderedTagsExtractor.java:25)
	
	The reason was tags list was not created on reading from DB
	 */
	@Test
	public void cancer_rs_89303() throws JSONException {
		check("cancer_rs_89303.xml");
	}
	
	private void check(String fileName) throws JSONException {
		TopicHealthXmlParser parser = new TopicHealthXmlParser();
		
		Document document = TestUtils.readDocument(FOLDER + fileName);
		
		TopicHealth topic = create();
		
		parser.parse(TopicXmlParser.Input.builder()
    			.document(document)
    			.fileName(fileName).build(),
    			topic);
		
		String actualJson = TestUtils.toJSON(topic);
		
		String jsonFileName = fileName.replace(".xml", ".json");
		
		//TestUtils.writeStringToFile(Paths.get("D:\\workspaces\\0\\platform.shared.cms-import\\cm-importer\\src\\test\\resources\\", FOLDER, jsonFileName), actualJson);
		
		String expectedJson = TestUtils.readResourceFile(FOLDER + jsonFileName);
		
		TestUtils.assertJsonEquals(expectedJson, actualJson);
	}
	
	private static TopicHealth create() {
		TopicHealth result = new TopicHealth();
		result.setTitle("Mock Title Topic Health");
		return result;
	}
}
