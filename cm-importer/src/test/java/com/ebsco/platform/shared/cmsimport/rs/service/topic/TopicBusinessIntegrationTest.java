package com.ebsco.platform.shared.cmsimport.rs.service.topic;

import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBusiness;
import com.ebsco.platform.shared.cmsimport.rs.repository.DatabaseClient;
import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicBusinessRepository;
import com.ebsco.platform.shared.cmsimport.rs.service.TestJsonFileWriter;

@Ignore
public class TopicBusinessIntegrationTest extends TopicServiceIntegrationTest {
	
    private static final String CONTENT_TYPE_UID = TopicBusiness.CONTENT_TYPE_UID;
    
	public TopicBusinessIntegrationTest() {
		super(new TopicService(
				new TopicBusinessRepository(new DatabaseClient()))
				.jsonWriter(new TestJsonFileWriter()));
	}

	@Test
	public void all() throws JSONException {
		String folder = "/topic_business";
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
