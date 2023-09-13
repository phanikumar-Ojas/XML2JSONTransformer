package com.ebsco.platform.shared.cmsimport.rs.service.topic;

import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicRepository;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.DuplicatesResolverPostProcessor;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.MovementBioRefPostProcessor;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.TopicXmlDataEnricher;

public class TopicLiteratureService extends TopicService {
	
	public TopicLiteratureService(TopicRepository repository) {
		super(repository);
		xmlDataEnricher(new TopicXmlDataEnricher()
    			.then(new MovementBioRefPostProcessor(repository))
    			.then(new DuplicatesResolverPostProcessor()));
	}

}
