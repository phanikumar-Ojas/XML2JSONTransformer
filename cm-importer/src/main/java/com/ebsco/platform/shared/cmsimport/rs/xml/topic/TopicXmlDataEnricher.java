package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.Topic;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBiography;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBusiness;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicHealth;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicHistory;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicLiterature;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicScience;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicSocialScience;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.TopicXmlParser.Input;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.TopicXmlParser.Output;
import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TopicXmlDataEnricher {
	 
	 	private static final Map<Class<? extends Topic>, TopicXmlParser<? extends Topic>> TOPIC_PARSERS = Map.of(
			 TopicBusiness.class, new TopicBusinessXmlParser(),
			 TopicHealth.class, new TopicHealthXmlParser(),
			 TopicScience.class, new TopicScienceXmlParser(),
			 TopicSocialScience.class, new TopicSocialScienceXmlParser(),
			 TopicHistory.class, new TopicHistoryXmlParser(),
			 TopicBiography.class, new TopicBiographyXmlParser(),
			 TopicLiterature.class, new TopicLiteratureXmlParser()
	 	);
	 
	 	private XmlDocumentReader xmlDocumentReader = XmlDocumentReader.DEFAULT;
	 	
	 	private List<PostProcessor> postProcessors = new ArrayList<>();
	    
	    public Collection<TopicXmlParser.Output<?>> enrich(Map<String, Topic> articleId2Topic) {
	    	Collection<TopicXmlParser.Output<?>> outputs = new ArrayList<>();
	    	int count = 1;
            Map<String, Exception> errors = new HashMap<>();
            for (Map.Entry<String, Topic> entry : articleId2Topic.entrySet()) {
            	String articleId = entry.getKey();
            	String filename = articleId + ".xml";
            	Topic topic = entry.getValue();
            	log.info("({} of {}) : [{} : {}]", count++, articleId2Topic.size(), topic.getContentTypeUid(), filename);
            	
            	Document document = null;
            	try {
            		document = xmlDocumentReader.read(articleId);
    	        } catch (Exception e) {
    	        	errors.put(filename, e);
    	        }
            	
            	if (Objects.nonNull(document)) {
            		TopicXmlParser parser = TOPIC_PARSERS.get(topic.getClass());
                    if (Objects.nonNull(parser)) {
                    	Input input = TopicXmlParser.Input.builder()
                    			.document(document)
                    			.fileName(filename).build();
                    	outputs.add(parser.parse(input, topic));
                    }
            	}
                
            }
            TOPIC_PARSERS.values().forEach(parser -> parser.getStatistic().print());
            log.warn("Found:{} frong files", errors.size());
            for (Map.Entry<String, Exception> entry : errors.entrySet()) {
            	log.error("Wrong xml file:{}, {}", entry.getKey(), entry.getValue());
			}
            if (CollectionUtils.isNotEmpty(postProcessors)) {
            	log.info("Configured:{} post processors", postProcessors.size());
            	for (PostProcessor postProcessor : postProcessors) {
            		log.info("Start post processor:{}", postProcessor.getClass().getName());
            		postProcessor.process(outputs, articleId2Topic);
            		log.info("Post processor complete:{}", postProcessor.getClass().getName());
				}
            }
            return outputs;
	    }
	    
	    public void setXmlDocumentReader(XmlDocumentReader xmlDocumentReader) {
			this.xmlDocumentReader = xmlDocumentReader;
		}

		public TopicXmlDataEnricher then(PostProcessor postProcessor) {
			this.postProcessors.add(postProcessor);
			return this;
		}

		public static interface XmlDocumentReader {
	    	
		    static final String ROOT_ARTICLE_XML_FOLDER_PATH = AppPropertiesUtil.getProperty("ARTICLE_XML_FOLDER");
		    
	    	public static XmlDocumentReader DEFAULT = new XmlDocumentReader() {};
	    	
	    	private static String getFilePath(String fileName) {
	             return ROOT_ARTICLE_XML_FOLDER_PATH + FileSystems.getDefault().getSeparator() + fileName;
	        }
	    	
	    	default Document read(String articleId) throws IOException {
	    		String path = getFilePath(articleId + ".xml");
            	Document document = null;
            	File xml = new File(path);
            	document = Jsoup.parse(xml);
	            document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
	            document.outputSettings().prettyPrint(false);
	            return document;
	    	}
	    }
		
		public static interface PostProcessor {
			
			default void process(Collection<Output<?>> outputs, Map<String, Topic> articleId2Topic) {}
		}
}
