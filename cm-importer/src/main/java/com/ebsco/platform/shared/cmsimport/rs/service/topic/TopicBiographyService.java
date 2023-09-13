package com.ebsco.platform.shared.cmsimport.rs.service.topic;

import java.util.function.Function;

import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicBiographyRepository;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.DuplicatesResolverPostProcessor;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.MovementBioRefPostProcessor;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.SalemNamesPostProcessor;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.TopicBiographyReferenceBinder;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.TopicXmlDataEnricher;

public class TopicBiographyService extends TopicService {
	
    private TopicBiographyReferenceBinder referenceBinder;
    
    public TopicBiographyService(TopicBiographyRepository repository) {
		super(repository);
		referenceBinder = new TopicBiographyReferenceBinder();
        xmlDataEnricher(new TopicXmlDataEnricher()
    			.then(referenceBinder)
    			.then(new MovementBioRefPostProcessor(repository))
    			.then(new SalemNamesPostProcessor(repository))
    			.then(new DuplicatesResolverPostProcessor()));
	}

    @Override
    public void setTitleValueResolver(Function<String, String> titleValueResolver) {
        super.setTitleValueResolver(titleValueResolver);
        this.referenceBinder.setTitleValueResolver(titleValueResolver);
    }
}
