package com.ebsco.platform.shared.cmsimport.rs.service.topic;

import java.util.Map;
import java.util.function.Function;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.Topic;
import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicRepository;
import com.ebsco.platform.shared.cmsimport.rs.service.JsonWriter;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.DuplicatesResolverPostProcessor;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.TopicXmlDataEnricher;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.TopicXmlDataEnricher.XmlDocumentReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository repository;
    
    private TopicXmlDataEnricher xmlDataEnricher = new TopicXmlDataEnricher()
            .then(new DuplicatesResolverPostProcessor());
    
    protected JsonWriter jsonWriter = JsonWriter.DEFAULT;

    public Map<String, Topic> entries(String... articleId) {
        try {
            log.info("Reading from {} from db ...", repository.getClass().getSimpleName());
            Map<String, Topic> articleId2Topic = repository.find(articleId);
            log.info("Created {} articleId2Topic items", articleId2Topic.size());
            log.info("Enriching info from xml files...");
            xmlDataEnricher.enrich(articleId2Topic);

            jsonWriter.write(articleId2Topic);

            log.info("done");
            return articleId2Topic;
        } catch (Exception e) {
            log.error("Topics import failed: ", e);
            throw e;
        }
    }

    public TopicService jsonWriter(JsonWriter jsonWriter) {
        this.jsonWriter = jsonWriter;
        return this;
    }

    public TopicService xmlDataEnricher(TopicXmlDataEnricher xmlDataEnricher) {
        this.xmlDataEnricher = xmlDataEnricher;
        return this;
    }
    
    public void setXmlDocumentReader(XmlDocumentReader xmlDocumentReader) {
        this.xmlDataEnricher.setXmlDocumentReader(xmlDocumentReader);
    }

    public void setTitleValueResolver(Function<String, String> titleValueResolver) {
        this.repository.setTitleValueResolver(titleValueResolver);
    }
}
