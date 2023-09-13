package com.ebsco.platform.shared.cmsimport.rs.service.topic;

import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBiography;
import com.ebsco.platform.shared.cmsimport.rs.repository.DatabaseClient;
import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicBiographyRepository;
import com.ebsco.platform.shared.cmsimport.rs.service.TestJsonFileWriter;

 @Ignore
public class TopicBiographyServiceIntegrationTest extends TopicServiceIntegrationTest<TopicBiographyService> {
	
    private static final String CONTENT_TYPE_UID = TopicBiography.CONTENT_TYPE_UID;
    
	public TopicBiographyServiceIntegrationTest() {
		super((TopicBiographyService) new TopicBiographyService(
				new TopicBiographyRepository(new DatabaseClient()))
				.jsonWriter(new TestJsonFileWriter()));
	}
	
	@Test
	public void all() throws JSONException {
		String folder = "/topic_biography";
		test(folder);
	}
	
	@Test
	public void theBeatles() throws JSONException {
		String folder = "/topic_biography/the-beatles";
		test(folder);
	}
	
	@Test
	public void italianIndustrialists() throws JSONException {
		String folder = "/topic_biography/italian-industrialists";
		test(folder);
	}
	
	@Test
	public void josephStalin() throws JSONException {
		String folder = "/topic_biography/joseph-stalin";
		test(folder);
	}
	
	@Test
	public void maryCassatt() throws JSONException {
		String folder = "/topic_biography/mary-cassatt";
		test(folder, true);
	}
	
	@Test
	public void thomasCole() throws JSONException {
		String folder = "/topic_biography/thomas-cole";
		test(folder);
	}
	
	@Test
	public void michaelJackson() throws JSONException {
		String folder = "/topic_biography/michael-jackson";
		test(folder);
	}
	
	@Test
	public void charlesTaylor() throws JSONException {
		String folder = "/topic_biography/charles-taylor";
		test(folder, true);
	}
	
	@BeforeClass
	public static void beforeClass() {
	    CleanUp.clean(CONTENT_TYPE_UID);
	}
	
	@AfterClass
    public static void afterClass() {
        CleanUp.clean(CONTENT_TYPE_UID);
    }
}
