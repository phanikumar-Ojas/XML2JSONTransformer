package com.ebsco.platform.shared.cmsimport.rs.service.topic;

import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicLiterature;
import com.ebsco.platform.shared.cmsimport.rs.repository.DatabaseClient;
import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicLiteratureRepository;
import com.ebsco.platform.shared.cmsimport.rs.service.TestJsonFileWriter;

@Ignore
public class TopicLiteratureServiceIntegrationTest extends TopicServiceIntegrationTest {
	
    private static final String CONTENT_TYPE_UID = TopicLiterature.CONTENT_TYPE_UID;
    
	public TopicLiteratureServiceIntegrationTest() {
		super(new TopicLiteratureService(
				new TopicLiteratureRepository(new DatabaseClient()))
				.jsonWriter(new TestJsonFileWriter()));
	}
	
	@Test
	public void all() throws JSONException {
		String folder = "/topic_literature";
		
		test(folder);
	}
	
	@Test
	public void exodus() throws JSONException {
		String folder = "/topic_literature/exodus";
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
