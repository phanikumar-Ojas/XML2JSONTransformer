package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.ebsco.platform.shared.cmsimport.rs.domain.ContentTypeReference;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.Topic;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBiography;
import com.ebsco.platform.shared.cmsimport.rs.util.TestUtils;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.TopicXmlDataEnricher.XmlDocumentReader;

public class TopicBiographyReferenceBinderTest {
	
	private TopicXmlDataEnricher enricher = new TopicXmlDataEnricher().then(new TopicBiographyReferenceBinder());
	
	@Test
	public void theBeatles() throws JSONException {
		String folder = "/topic_biography/the-beatles/";
		enricher.setXmlDocumentReader(new TestXmlDocumentReader(folder));
		
		Map<String, Topic> articleId2Topic = new HashMap<>();
		theBeatles(articleId2Topic);
		paulMcCurtney(articleId2Topic);
		georgeHarrison(articleId2Topic);
		
		enricher.enrich(articleId2Topic);
		
		check(folder, articleId2Topic);
	}
	
	//wrong file name because of title <italic>Piero Pirelli</italic>.json
	@Test
	public void gl20c_rs_30689() throws JSONException {
		String folder = "/topic_biography/italian-industrialists/";
		enricher.setXmlDocumentReader(new TestXmlDocumentReader(folder));
		
		Map<String, Topic> articleId2Topic = new HashMap<>();
		articleId2Topic.put("gl20c_rs_30689", create("Italian Industrialists"));
		enricher.enrich(articleId2Topic);
		check(folder, articleId2Topic);
	}
	
	private static void theBeatles(Map<String, Topic> articleId2Topic) {
		articleId2Topic.put("gl20c_rs_31828", create("The Beatles"));
	}
	
	private static void paulMcCurtney(Map<String, Topic> articleId2Topic) {
		articleId2Topic.put("musc_sp_ency_bio_245124", create("Paul McCartney"));
	}
	
	private static void georgeHarrison(Map<String, Topic> articleId2Topic) {
		articleId2Topic.put("musc_rs_79605", create("George Harrison"));
	}
	
	private void check(String folder, Map<String, Topic> articleId2Topic) throws JSONException {
		for (Map.Entry<String, Topic> entry : articleId2Topic.entrySet()) {
			String articleId = entry.getKey();
        	Topic topic = entry.getValue();
        	String jsonFileName = articleId + ".json";
        	String actualJson = TestUtils.toJSON(topic);
        	
        	//TestUtils.writeStringToFile(Paths.get("D:\\workspaces\\0\\platform.shared.cms-import\\cm-importer\\src\\test\\resources\\", folder, jsonFileName), actualJson);
        	String expectedJson = TestUtils.readResourceFile(folder + jsonFileName);
        	TestUtils.assertJsonEquals(jsonFileName, expectedJson, actualJson);
		}
		
		Collection<Topic> withoutArticles = findThoseHaveNoArticles(articleId2Topic);
		
		for (Topic topic : withoutArticles) {
			String jsonFileName = topic.getTitle() + ".json";
        	String actualJson = TestUtils.toJSON(topic);
        	
        	//TestUtils.writeStringToFile(Paths.get("D:\\workspaces\\0\\platform.shared.cms-import\\cm-importer\\src\\test\\resources\\", folder, jsonFileName), actualJson);
        	String expectedJson = TestUtils.readResourceFile(folder + jsonFileName);
        	TestUtils.assertJsonEquals(jsonFileName, expectedJson, actualJson);
		}
		
		assertNoDuplicates(articleId2Topic);
	}
	
	private static void assertNoDuplicates(Map<String, Topic> articleId2Topic) {
		List<Topic> topics = articleId2Topic.values().stream().toList();
		Set<Topic> all = new HashSet<>();
    	for (Topic topic : topics) {
    		TopicBiography topicBiography = (TopicBiography) topic;
    		Collection<ContentTypeReference<TopicBiography>> refs = topicBiography.getMultiple_biography_topics();
    		if (CollectionUtils.isNotEmpty(refs)) {
    			List<TopicBiography> relatedTopics = refs.stream().map(ref -> ref.getReferable()).toList();
    			all.addAll(relatedTopics);
    		}
		}
    	all.addAll(topics);
    	
    	Set<String> errors = new HashSet<>();
    	
    	for (Topic topic : all) {
    		List<Topic> found = all.stream().filter(candidate -> candidate.getTitle().equals(topic.getTitle())).toList();
    		if (found.size() > 1) {
    			errors.add(String.format("Found %s duplicates of '%s'", found.size(), found.iterator().next().getTitle()));
    		}
		}
    	
    	if (!errors.isEmpty()) {
    		fail("\n" + String.join("\n", errors.toArray(new String[0])));
    	}
	}
	
	private static Collection<Topic> findThoseHaveNoArticles(Map<String, Topic> articleId2Topic){
		Collection<Topic> result = new ArrayList<>();
		for (Map.Entry<String, Topic> entry : articleId2Topic.entrySet()) {
			TopicBiography topic = (TopicBiography) entry.getValue();
			Collection<ContentTypeReference<TopicBiography>> refs = topic.getMultiple_biography_topics();
			if (Objects.nonNull(refs)) {
				for (ContentTypeReference<TopicBiography> ref : refs) {
					TopicBiography byExample = ref.getReferable();
					TopicBiography found = find(byExample, articleId2Topic.values());
					if (Objects.isNull(found)) {
						found = find(byExample, result);
						if (Objects.isNull(found)) {
							result.add(byExample);
						}
					}
				}
			}
		}
		return result;
	}
	
	private static TopicBiography find(TopicBiography byExample, Collection<Topic> in) {
		for (Topic topic : in) {
			TopicBiography candidate = (TopicBiography) topic;
			if (TopicBiographyReferenceBinder.areEqual(candidate, byExample)) {
				return candidate;
			}
		}
		return null;
	}
	
	private static TopicBiography create(String title) {
		TopicBiography result = new TopicBiography();
		result.setTitle(title);
		Collection<String> tags = new LinkedHashSet<>();
		tags.add("mock brstCategory");
		result.setTags(tags);
		return result;
	}
	
	public static class TestXmlDocumentReader implements XmlDocumentReader {
		
		private String folder;
		
		public TestXmlDocumentReader(String folder) {
			this.folder = folder;
		}

		@Override
		public Document read(String articleId) throws IOException {
			Document document = TestUtils.readDocument(folder + articleId + ".xml");
			return document;
		}
	}
}
