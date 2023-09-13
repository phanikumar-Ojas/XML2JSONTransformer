package com.ebsco.platform.shared.cmsimport.rs.service.topic;

import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicHistory;
import com.ebsco.platform.shared.cmsimport.rs.repository.DatabaseClient;
import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicHistoryRepository;
import com.ebsco.platform.shared.cmsimport.rs.service.TestJsonFileWriter;
import com.ebsco.platform.shared.cmsimport.rs.service.api.ContentTypeApi;

@Ignore
public class TopicHistoryIntegrationTest extends TopicServiceIntegrationTest {
	
    private static final String CONTENT_TYPE_UID = TopicHistory.CONTENT_TYPE_UID;
    
	public TopicHistoryIntegrationTest() {
		super(new TopicService(
				new TopicHistoryRepository(new DatabaseClient()))
				.jsonWriter(new TestJsonFileWriter()));
	}

	@Test
	public void all() throws JSONException {
		String folder = "/topic_history";
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
