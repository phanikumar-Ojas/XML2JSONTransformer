package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.Topic;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBiography;
import com.ebsco.platform.shared.cmsimport.rs.util.PojoUtil;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.TopicXmlParser.Output;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DuplicatesResolverPostProcessor implements TopicXmlDataEnricher.PostProcessor {
	
	@Override
	public void process(Collection<Output<?>> outputs, Map<String, Topic> articleId2Topic) {
		log.info("RESOLVE DUPLICATES START:");
		postProcessing(outputs, articleId2Topic);
		log.info("RESOLVE DUPLICATES COMPLETE:");
	}
	
	private void postProcessing(Collection<Output<?>> outputs, Map<String, Topic> articleId2Topic) {
		Map<String, List<Topic>> title2Topic = groupBy(topic -> topic.getTitle(), articleId2Topic.values());
		log.info("Found: {} titles having duplicates", title2Topic.size());
		
		for (Map.Entry<String, List<Topic>> grouppedByTitle : title2Topic.entrySet()) {
			String title = grouppedByTitle.getKey();
			List<Topic> titleDuplicates = grouppedByTitle.getValue();
			
			log.info("\n {} - {} duplicates:\n  {}\n", title, titleDuplicates.size(), String.join("\n  ", 
					titleDuplicates.stream().map(topic -> String.format("%s:%s:%s", topic.getContentTypeUid(), topic.getTitle(), topic.getArticleIds())).toList()));
			
			if (titleDuplicates.iterator().next() instanceof TopicBiography) { 
				Map<String, List<Topic>> biographyTitle2Topic = titleDuplicates.stream().collect(Collectors.groupingBy(
						topic -> biographyTitle((TopicBiography) topic)));
				log.info("Found: {} biography groups", biographyTitle2Topic.size());
				
				for (Map.Entry<String, List<Topic>> grouppedByBiographyTitle : biographyTitle2Topic.entrySet()) {
					String biographyTitle = grouppedByBiographyTitle.getKey();
					List<Topic> biographyTitleDuplicates = grouppedByBiographyTitle.getValue();
					
					log.info("\n     {} - {} splited:\n       {}\n", biographyTitle, titleDuplicates.size(), String.join("\n       ", 
							biographyTitleDuplicates.stream().map(topic -> String.format("%s:%s:%s", topic.getContentTypeUid(), topic.getTitle(), topic.getArticleIds())).toList()));
					
					Set<String> articleIds = biographyTitleDuplicates.stream().map(topic -> topic.getArticleIds().iterator().next()).collect(Collectors.toSet());
					
					Topic merged = PojoUtil.merge(biographyTitleDuplicates);
					
					merged.setArticleIds(articleIds);
					log.info("Merged {}", PojoUtil.describe(merged));
					for (String articleId : articleIds) {
						Topic old = articleId2Topic.put(articleId, merged);
						log.info(" Replaced {}", PojoUtil.describe(old));
					}
					
				}
			} else {
				Set<String> articleIds = titleDuplicates.stream().map(topic -> topic.getArticleIds().iterator().next()).collect(Collectors.toSet());
				
				Topic merged = PojoUtil.merge(titleDuplicates);
				merged.setTitle(title);
				merged.setArticleIds(articleIds);
				log.info("Merged {}", PojoUtil.describe(merged));
				
				for (String articleId : articleIds) {
					Topic old = articleId2Topic.put(articleId, merged);
					log.info(" Replaced {}", PojoUtil.describe(old));
				}
			}
		}
		
	}
	
	public static String biographyTitle(TopicBiography biography) {
	    String title = biography.getTitle();
	    if (StringUtils.isNoneBlank(biography.getNonNumericBirthDate())) {
		    title = String.format("%s (%s)", title, biography.getNonNumericBirthDate());
		}
		return title;
	}
	
	private static Map<String, List<Topic>> groupBy(Function<Topic, String> classifier, Collection<Topic> topics) {
		Map<String, List<Topic>> result = topics.stream().collect(Collectors.groupingBy(
				classifier)).entrySet().stream().filter(e -> e.getValue().size() > 1)
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		return result;
	}
	
	
}
