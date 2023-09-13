package com.ebsco.platform.shared.cmsimport.rs.service.topic;

import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicEducation;
import com.ebsco.platform.shared.cmsimport.rs.repository.DatabaseClient;
import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicEducationRepository;
import com.ebsco.platform.shared.cmsimport.rs.service.TestJsonFileWriter;
import com.ebsco.platform.shared.cmsimport.rs.service.api.ContentTypeApi;

@Ignore
public class TopicEducationIntegrationTest  extends TopicServiceIntegrationTest {
	
    private static final String CONTENT_TYPE_UID = TopicEducation.CONTENT_TYPE_UID;
    
	public TopicEducationIntegrationTest() {
		super(new TopicService(
				new TopicEducationRepository(new DatabaseClient()))
				.jsonWriter(new TestJsonFileWriter()));
	}

	@Test
	public void all() throws JSONException {
		String folder = "/topic_education_resource";
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
