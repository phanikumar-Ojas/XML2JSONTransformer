package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicScience;
import com.ebsco.platform.shared.cmsimport.rs.util.TestUtils;

public class TopicScienceXmlParserTest {
	
	private static final String FOLDER = "/topic_science/";
	
	@Test
	public void envis_rs_35321() throws JSONException {
		check("envis_rs_35321.xml");
	}
	
	@Test
	public void forensic_rs_46661() throws JSONException {
		check("forensic_rs_46661.xml");
	}
	
	@Test
	public void forensic_rs_46866() throws JSONException {
		check("forensic_rs_46866.xml");
	}
	
	@Test
	public void glor_rs_45963() throws JSONException {
		check("glor_rs_45963.xml");
	}
	
	@Test
	public void glow_rs_46451() throws JSONException {
		check("glow_rs_46451.xml");
	}
	
	@Test
	public void math_rs_92449() throws JSONException {
		check("math_rs_92449.xml");
	}
	
	@Test
	public void solar_sp_ency_sci_285359() throws JSONException {
		check("solar_sp_ency_sci_285359.xml");
	}
	
	@Test
	public void amazonRiver() throws JSONException {
		check(FOLDER + "amazon-river/", "rsbiomes_92590.xml");
		check(FOLDER + "amazon-river/", "rssalemprimaryencyc_136419.xml");
	}
	
	private void check(String fileName) throws JSONException {
		check(FOLDER, fileName);
	}
	
	private void check(String folder, String fileName) throws JSONException {
		TopicScienceXmlParser parser = new TopicScienceXmlParser();
		
		Document document = TestUtils.readDocument(folder + fileName);
		
		TopicScience topic = create();
		
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
	
	private static TopicScience create() {
		TopicScience result = new TopicScience();
		result.setTitle("Mock Title Topic Science");
		Collection<String> tags = new LinkedHashSet<>();
		tags.add("mock brstCategory");
		result.setTags(tags);
		return result;
	}
}
