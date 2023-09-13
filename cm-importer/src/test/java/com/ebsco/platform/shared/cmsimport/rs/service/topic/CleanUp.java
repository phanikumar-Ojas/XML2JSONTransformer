package com.ebsco.platform.shared.cmsimport.rs.service.topic;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBiography;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBusiness;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicEducation;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicHealth;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicHistory;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicLiterature;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicScience;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicSocialScience;
import com.ebsco.platform.shared.cmsimport.rs.util.TestUtils;

public class CleanUp {
    
    private static final String[] TOPIC_CONTENT_TYPES = {
            TopicBiography.CONTENT_TYPE_UID,
            TopicBusiness.CONTENT_TYPE_UID,
            TopicScience.CONTENT_TYPE_UID,
            TopicSocialScience.CONTENT_TYPE_UID,
            TopicEducation.CONTENT_TYPE_UID,
            TopicHealth.CONTENT_TYPE_UID,
            TopicHistory.CONTENT_TYPE_UID,
            TopicLiterature.CONTENT_TYPE_UID
    };
    
    public static void clean(String contentTypeUid) {
        TestUtils.removeFromContentstack(contentTypeUid, TopicServiceIntegrationTest.TEST_INTEG_TITLE_MARKER);
    }
    
    public static void cleanAll() throws Exception {
        for (String contentType : TOPIC_CONTENT_TYPES) {
            clean(contentType);
        }
    }
}
