package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicSocialScience;
import com.ebsco.platform.shared.cmsimport.rs.util.TestUtils;

public class TopicSocialScienceXmlParserTest {
	
	private static final String FOLDER = "/topic_social_science/";
	
	@Test
	public void parseCategory() throws JSONException {
		check("2000_sp_ency_251135.xml");
	}
	
	@Test
	public void parseCurriculumAncientHistory() throws JSONException {
		check("race_rs_113055.xml");
	}
	
	@Test
	public void parseWesternCivilizationEuropeanHistory() throws JSONException {
		check("crmj_rs_98081.xml");
	}
	
	@Test
	public void parseWomensHistoryGeographicalCategorySubGeographicalCategory() throws JSONException {
		check("1990_sp_ency_269038.xml");
	}
	
	@Test
	public void parseGeoKeywordAndTags() throws JSONException {
		check("glbt_rs_116549.xml");
	}
	
	@Test
	public void parseState() throws JSONException {
		check("rsstates_17420.xml");
	}
	
	@Test
	public void parseCountry() throws JSONException {
		check("rscountries_4110.xml");
	}
	
	@Test
	public void rsspencyclopedia_210260() throws JSONException {
		check(FOLDER + "exodus/","rsspencyclopedia_210260.xml");
	}
	
	private void check(String fileName) throws JSONException {
		check(FOLDER, fileName);
	}
	
	private void check(String folder, String fileName) throws JSONException {
		TopicSocialScienceXmlParser parser = new TopicSocialScienceXmlParser();
		
		Document document = TestUtils.readDocument(folder + fileName);
		
		TopicSocialScience topic = create();
		
		parser.parse(TopicXmlParser.Input.builder()
    			.document(document)
    			.fileName(fileName).build(),
    			topic);
		
		String actualJson = TestUtils.toJSON(topic);
		
		String jsonFileName = fileName.replace(".xml", ".json");
		
		//TestUtils.writeStringToFile(Paths.get("D:\\workspaces\\0\\platform.shared.cms-import\\cm-importer\\src\\test\\resources\\", folder, jsonFileName), actualJson);
		
		String expectedJson = TestUtils.readResourceFile(folder + jsonFileName);
		
		JSONAssert.assertEquals(expectedJson, actualJson, true);
	}
	
	private static TopicSocialScience create() {
		TopicSocialScience result = new TopicSocialScience();
		result.setTitle("Mock Title Topic Social Science");
		Collection<String> tags = new LinkedHashSet<>();
		tags.add("mock brstCategory");
		result.setTags(tags);
		return result;
	}
}
