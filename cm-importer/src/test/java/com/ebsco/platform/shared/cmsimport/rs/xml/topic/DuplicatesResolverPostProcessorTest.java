package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.Topic;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBiography;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicScience;
import com.ebsco.platform.shared.cmsimport.rs.util.TestUtils;

public class DuplicatesResolverPostProcessorTest {
	
	@Test
	public void checkBiographies() throws Exception {
		TopicXmlDataEnricher enricher = new TopicXmlDataEnricher().then(new DuplicatesResolverPostProcessor());
		String [] folder = {
				"/topic_biography/the-beatles/",
				"/topic_biography/joseph-stalin/",
				"/topic_biography/michael-jackson/",
				"/topic_biography/mary-cassatt/",
				"/topic_biography/thomas-cole/"
				};
		
		List<String> xmlFileNames = new ArrayList<>();
		for (String dir : folder) {
			xmlFileNames.addAll(TestUtils.xmlFileNames(dir));
		}
		
		TestUtils.TestXmlDocumentReader xmlDocumentReader = new TestUtils.TestXmlDocumentReader(folder);
		enricher.setXmlDocumentReader(xmlDocumentReader);
		
		Map<String, Topic> article2Topic = article2Topic(TopicBiography.class, xmlFileNames, xmlDocumentReader);
		
		enricher.enrich(article2Topic);
		
		Set<Topic> result = new HashSet<>(article2Topic.values());
		for (Topic topic2 : result) {
            System.out.println(topic2);
        }
		
		assertEquals(8, result.size());
		
		Set<Topic> found = result.stream().filter(topic -> topic.getTitle().equals("Mary Cassatt") && Objects.isNull(((TopicBiography)topic).getNonNumericBirthDate())).collect(Collectors.toSet());
		assertEquals(1, found.size());
		Topic actual = found.iterator().next();
		Set<String> articleIds = actual.getArticleIds();
		assertEquals(1, articleIds.size());
		assertTrue(articleIds.contains("rssalemprimaryencyc_20190926_39"));
		
		found = result.stream().filter(topic -> topic.getTitle().equals("Mary Cassatt") && Objects.nonNull(((TopicBiography)topic).getNonNumericBirthDate())).collect(Collectors.toSet());
		assertEquals(1, found.size());
		actual = found.iterator().next();
		articleIds = actual.getArticleIds();
		assertEquals(2, articleIds.size());
		assertTrue(articleIds.contains("gl19_rs_138754"));
		assertTrue(articleIds.contains("rssalemprimaryencyc_20190214_46"));
		
		found = result.stream().filter(topic -> topic.getTitle().equals("The Beatles")).collect(Collectors.toSet());
		assertEquals(1, found.size());
		actual = found.iterator().next();
		articleIds = actual.getArticleIds();
		assertEquals(2, articleIds.size());
		assertTrue(articleIds.contains("gl20c_rs_31828"));
		
		found = result.stream().filter(topic -> topic.getTitle().equals("George Harrison")).collect(Collectors.toSet());
		assertEquals(1, found.size());
		actual = found.iterator().next();
		articleIds = actual.getArticleIds();
		assertEquals(1, articleIds.size());
		assertTrue(articleIds.contains("musc_rs_79605"));
		
		found = result.stream().filter(topic -> topic.getTitle().equals("Joseph Stalin")).collect(Collectors.toSet());
		assertEquals(1, found.size());
		actual = found.iterator().next();
		articleIds = actual.getArticleIds();
		assertEquals(3, articleIds.size());
		assertTrue(articleIds.contains("ell_2341_sp_ency_pri_591141"));
		assertTrue(articleIds.contains("gln_rs_48180"));
		assertTrue(articleIds.contains("rssalemprimaryencyc_20160901_93"));
		
		found = result.stream().filter(topic -> topic.getTitle().equals("Paul McCartney")).collect(Collectors.toSet());
		assertEquals(1, found.size());
		actual = found.iterator().next();
		articleIds = actual.getArticleIds();
		assertEquals(1, articleIds.size());
		assertTrue(articleIds.contains("musc_sp_ency_bio_245124"));
		
		found = result.stream().filter(topic -> topic.getTitle().equals("Michael Jackson")).collect(Collectors.toSet());
		assertEquals(1, found.size());
		actual = found.iterator().next();
		articleIds = actual.getArticleIds();
		assertEquals(2, articleIds.size());
		assertTrue(articleIds.contains("glaa_rs_64186"));
		assertTrue(articleIds.contains("rssalemprimaryencyc_20160901_123"));
		
		found = result.stream().filter(topic -> topic.getTitle().equals("Thomas Cole (painter)")).collect(Collectors.toSet());
		assertEquals(1, found.size());
		actual = found.iterator().next();
		articleIds = actual.getArticleIds();
		assertEquals(2, articleIds.size());
		assertTrue(articleIds.contains("rsbioencyc_20170720_338"));
		assertTrue(articleIds.contains("rsbioencyc_sp_ency_bio_596951"));
	}
	
	@Test
	public void checkScience() throws Exception {
		TopicXmlDataEnricher enricher = new TopicXmlDataEnricher().then(new DuplicatesResolverPostProcessor());
		String [] folder = {
				"/topic_science/amazon-river/"
				};
		
		List<String> xmlFileNames = new ArrayList<>();
		for (String dir : folder) {
			xmlFileNames.addAll(TestUtils.xmlFileNames(dir));
		}
		
		TestUtils.TestXmlDocumentReader xmlDocumentReader = new TestUtils.TestXmlDocumentReader(folder);
		enricher.setXmlDocumentReader(xmlDocumentReader);
		
		Map<String, Topic> article2Topic = article2Topic(TopicScience.class, xmlFileNames, xmlDocumentReader);
		
		enricher.enrich(article2Topic);
		
		Set<Topic> result = new HashSet<>(article2Topic.values());
		for (Topic topic : result) {
			System.out.println(topic.getTitle());
		}
		
		assertEquals(1, result.size());
		
		Set<Topic> found = result.stream().filter(topic -> topic.getTitle().equals("Amazon River")).collect(Collectors.toSet());
		assertEquals(1, found.size());
		Topic actual = found.iterator().next();
		Set<String> articleIds = actual.getArticleIds();
		assertEquals(2, articleIds.size());
		assertTrue(articleIds.contains("rsbiomes_92590"));
		assertTrue(articleIds.contains("rssalemprimaryencyc_136419"));
	}
	
	@Test
	public void charlesTaylor() throws Exception {
		TopicXmlDataEnricher enricher = new TopicXmlDataEnricher().then(new DuplicatesResolverPostProcessor());
		String [] folder = {
				"/topic_biography/charles-taylor/",
				};
		
		List<String> xmlFileNames = new ArrayList<>();
		for (String dir : folder) {
			xmlFileNames.addAll(TestUtils.xmlFileNames(dir));
		}
		
		TestUtils.TestXmlDocumentReader xmlDocumentReader = new TestUtils.TestXmlDocumentReader(folder);
		enricher.setXmlDocumentReader(xmlDocumentReader);
		
		Map<String, Topic> article2Topic = article2Topic(TopicBiography.class, xmlFileNames, xmlDocumentReader);
		
		enricher.enrich(article2Topic);
		
		Set<Topic> result = new HashSet<>(article2Topic.values());
		for (Topic topic : result) {
			System.out.println(topic.getTitle());
		}
	}
	
	private static Map<String, Topic> article2Topic(Class<? extends Topic> clz, List<String> xmlFileNames,
			TestUtils.TestXmlDocumentReader xmlDocumentReader) throws Exception {
		Map<String, Topic> articleId2Topic = new HashMap<>();
		
		for (String articleId : xmlFileNames) {
			Document xml = xmlDocumentReader.read(articleId);
			articleId2Topic.put(articleId, newTopic(clz, articleId, xml));
		}
		
		return articleId2Topic;
	}
	
	private static Topic newTopic(Class<? extends Topic> clz, String articleId, Document xml) {
		try {
			Topic result = clz.getDeclaredConstructor().newInstance();
			String title = articleId;
			Element e = xml.selectXpath("//book-part-meta/title-group/title").first();
			if (Objects.nonNull(e)) {
				title = e.ownText();
			}
			result.setTitle(title);
			result.setArticleIds(Set.of(articleId));
			Collection<String> tags = new LinkedHashSet<>();
			tags.add("mock brstCategory");
			result.setTags(tags);
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}
}
