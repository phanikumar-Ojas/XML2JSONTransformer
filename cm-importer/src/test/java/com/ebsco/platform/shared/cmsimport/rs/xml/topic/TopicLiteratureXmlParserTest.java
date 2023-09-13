package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicLiterature;
import com.ebsco.platform.shared.cmsimport.rs.util.TestUtils;

public class TopicLiteratureXmlParserTest {
	
	private static final String FOLDER = "/topic_literature/";
	
	@Test
	public void amf_sp_ency_lit_263678() throws JSONException {
		check("amf_sp_ency_lit_263678.xml");
	}
	
	@Test
	public void mp4_rs_6550() throws JSONException {
		check("mp4_rs_6550.xml");
	}
	
	@Test
	public void rsspliterature_20220331_11() throws JSONException {
		check(FOLDER + "exodus/", "rsspliterature_20220331_11.xml");
	}
	
	private void check(String fileName) throws JSONException {
		check(FOLDER, fileName);
	}
	
	private void check(String folder, String fileName) throws JSONException {
		TopicLiteratureXmlParser parser = new TopicLiteratureXmlParser();
		
		Document document = TestUtils.readDocument(folder + fileName);
		
		TopicLiterature topic = create();
		
		parser.parse(TopicXmlParser.Input.builder()
    			.document(document)
    			.fileName(fileName).build(),
    			topic);
		
		String actualJson = TestUtils.toJSON(topic);
		
		String jsonFileName = fileName.replace(".xml", ".json");
		
		//TestUtils.writeStringToFile(Paths.get("D:\\workspaces\\0\\platform.shared.cms-import\\cm-importer\\src\\test\\resources\\", folder, jsonFileName), actualJson);
		
		String expectedJson = TestUtils.readResourceFile(folder + jsonFileName);
		
		TestUtils.assertJsonEquals(expectedJson, actualJson);
	}
	
	private static TopicLiterature create() {
		TopicLiterature result = new TopicLiterature();
		result.setTitle("Mock Title Topic Literature");
		Collection<String> tags = new LinkedHashSet<>();
		tags.add("mock brstCategory");
		result.setTags(tags);
		return result;
	}
}
