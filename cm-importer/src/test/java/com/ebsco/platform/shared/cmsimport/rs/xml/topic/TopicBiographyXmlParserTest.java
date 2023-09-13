package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBiography;
import com.ebsco.platform.shared.cmsimport.rs.util.TestUtils;

public class TopicBiographyXmlParserTest {
	
	private static final String FOLDER = "/topic_biography/";
	
	@Test
	public void _1920_sp_ency_bio_263261() throws JSONException {
		check("1920_sp_ency_bio_263261.xml");
	}
	
	@Test
	public void _1930_sp_ency_bio_263263() throws JSONException {
		check("1930_sp_ency_bio_263263.xml");
	}
	
	@Test
	public void _1980_sp_ency_bio_283473() throws JSONException {
		check("1980_sp_ency_bio_283473.xml");
	}
	
	@Test
	public void ancw_rs_113772() throws JSONException {
		check("ancw_rs_113772.xml");
	}
	
	@Test
	public void athletes_rs_222606() throws JSONException {
		check("athletes_rs_222606.xml");
	}
	
	//Also known as, Principal Works JSON RTE
	@Test
	public void brb_2014_rs_210025() throws JSONException {
		check("brb_2014_rs_210025.xml");
	}
	
	@Test
	public void gll_rs_222933() throws JSONException {
		check("gll_rs_222933.xml");
	}
	
	//Principal Works JSON RTE
	@Test
	public void rsspencyclopedia_236553() throws JSONException {
		check("rsspencyclopedia_236553.xml");
	}
	
	//gender afl_rs
	@Test
	public void afl_rs_157581() throws JSONException {
		check("afl_rs_157581.xml");
	}
	
	//with race value
	@Test
	public void rrai_rs_137934() throws JSONException {
		check("rrai_rs_137934.xml");
	}
	
	//other_occupation_related_fields
	@Test
	public void supc_rs_97463() throws JSONException {
		check("supc_rs_97463.xml");
	}
	
	//associated_figures ext-link-type="xrefmolp" 
	@Test
	public void phi_rs_84492() throws JSONException {
		check("phi_rs_84492.xml");
	}
	
	//gender [GEN] + associated_figures ext-link-type="xrefmolp" 
	@Test
	public void csd_rs_161974() throws JSONException {
		check("csd_rs_161974.xml");
	}
	
	//Exception in thread "main" java.lang.NullPointerException: Cannot invoke "org.jsoup.nodes.Element.ownText()" 
	//because the return value of "org.jsoup.select.Elements.first()" is null
	//reason: strange bookId 10.3331/rsbioencyc it placed in <book-id pub-id-type="doi">10.3331/rsbioencyc</book-id> 
	@Test
	public void rsbioencyc_91697() throws JSONException {
		check("rsbioencyc_91697.xml");
	}
	
	@Test
	public void michaelJackson() throws JSONException {
		check(FOLDER + "michael-jackson/", "glaa_rs_64186.xml");
		check(FOLDER + "michael-jackson/", "rssalemprimaryencyc_20160901_123.xml");
	}
	
	
	@Test
	public void josephStalin() throws JSONException {
		check(FOLDER + "joseph-stalin/" ,"ell_2341_sp_ency_pri_591141.xml");
		check(FOLDER + "joseph-stalin/" ,"gln_rs_48180.xml");
		
		//Stalin issue with birth date
		check(FOLDER + "joseph-stalin/" ,"rssalemprimaryencyc_20160901_93.xml");
	}
	
	@Test
	public void maryCassatt() throws JSONException {
		check(FOLDER + "mary-cassatt/", "gl19_rs_138754.xml");
		check(FOLDER + "mary-cassatt/", "rssalemprimaryencyc_20190214_46.xml");
		check(FOLDER + "mary-cassatt/", "rssalemprimaryencyc_20190926_39.xml");
	}
	
	@Test
	public void thomasCole() throws JSONException {
		check(FOLDER + "thomas-cole/", "rsbioencyc_20170720_338.xml");
		check(FOLDER + "thomas-cole/", "rsbioencyc_sp_ency_bio_596951.xml");
	}
	
	@Test
    public void charlesTaylor() throws JSONException {
        check(FOLDER + "charles-taylor/", "gln_sp_ency_bio_309701.xml");
        check(FOLDER + "charles-taylor/", "rsbioencyc_20170720_56.xml");
    }
	
	private void check(String fileName) throws JSONException {
		check(FOLDER, fileName);
	}
	
	private void check(String folder, String fileName) throws JSONException {
		TopicBiographyXmlParser parser = new TopicBiographyXmlParser();
		
		Document document = TestUtils.readDocument(folder + fileName);
		
		TopicBiography topic = create();
		
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
	
	private static TopicBiography create() {
		TopicBiography result = new TopicBiography();
		result.setTitle("Mock Title Topic Biography");
		Collection<String> tags = new LinkedHashSet<>();
		tags.add("mock brstCategory");
		result.setTags(tags);
		return result;
	}
}
