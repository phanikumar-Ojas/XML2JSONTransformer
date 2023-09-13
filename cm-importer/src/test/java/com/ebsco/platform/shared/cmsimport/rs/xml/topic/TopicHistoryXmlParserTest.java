package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicHistory;
import com.ebsco.platform.shared.cmsimport.rs.util.TestUtils;

public class TopicHistoryXmlParserTest {
	
	private static final String FOLDER = "/topic_history/";
	
	@Test
	public void parseDateLocaleAlsoKnownAs() throws JSONException {
		check("ge20_rs_54887.xml");
	}
	
	@Test
	public void ambf_rs_34443() throws JSONException {
		check("ambf_rs_34443.xml");
	}
	
	@Test
	public void ge20_rs_54845() throws JSONException {
		check("ge20_rs_54845.xml");
	}
	
	@Test
	public void mgmh_rs_118135() throws JSONException {
		check("mgmh_rs_118135.xml");
	}
	
	@Test
	public void ge20_rs_54882() throws JSONException {
		check("ge20_rs_54882.xml");
	}
	
	private void check(String fileName) throws JSONException {
		TopicHistoryXmlParser parser = new TopicHistoryXmlParser();
		
		Document document = TestUtils.readDocument(FOLDER + fileName);
		
		TopicHistory topic = create();
		
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
	
	private static TopicHistory create() {
		TopicHistory result = new TopicHistory();
		result.setTitle("Mock Title Topic History");
		Collection<String> tags = new LinkedHashSet<>();
		tags.add("mock brstCategory");
		result.setTags(tags);
		return result;
	}
}
