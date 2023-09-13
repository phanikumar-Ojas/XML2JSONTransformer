package com.ebsco.platform.shared.cmsimport.rs.service.topic;

import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicSocialScience;
import com.ebsco.platform.shared.cmsimport.rs.repository.DatabaseClient;
import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicSocialScienceRepository;
import com.ebsco.platform.shared.cmsimport.rs.service.TestJsonFileWriter;

@Ignore
public class TopicSocialScienceIntegrationTest extends TopicServiceIntegrationTest{
	
    private static final String CONTENT_TYPE_UID = TopicSocialScience.CONTENT_TYPE_UID;
    
	public TopicSocialScienceIntegrationTest() {
		super(new TopicService(
				new TopicSocialScienceRepository(new DatabaseClient()))
				.jsonWriter(new TestJsonFileWriter()));
	}

	@Test
	public void all() throws JSONException {
		String folder = "/topic_social_science";
		
		test(folder);
	}
	
	@Test
	public void exodus() throws JSONException {
		String folder = "/topic_social_science/exodus";
		test(folder);
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
