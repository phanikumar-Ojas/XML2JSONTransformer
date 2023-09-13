package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBusiness;
import com.ebsco.platform.shared.cmsimport.rs.util.TestUtils;

public class TopicBusinessXmlParserTest {
	
	private static final String FOLDER = "/topic_business/";
	
	private TopicXmlParser<TopicBusiness> parser = new TopicBusinessXmlParser();
	
	@Test
	public void rsspencyclopedia_236590() throws JSONException {
		check("rsspencyclopedia_236590.xml");
	}
	
	@Test
	public void rsspencyclopedia_210318() throws JSONException {
		check("rsspencyclopedia_210318.xml");
	}
	
	private void check(String fileName) throws JSONException {
		
		
		Document document = TestUtils.readDocument(FOLDER + fileName);
		
		TopicBusiness topic = create();
		
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
	
	private static TopicBusiness create() {
		TopicBusiness result = new TopicBusiness();
		result.setTitle("Mock Title Topic Business");
		Collection<String> tags = new LinkedHashSet<>();
		tags.add("mock brstCategory");
		result.setTags(tags);
		return result;
	}
}
