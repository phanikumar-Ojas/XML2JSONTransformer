package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ebsco.platform.shared.cmsimport.rs.domain.ContentTypeReference;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.Topic;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBiography;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.TopicXmlParser.Output;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TopicBiographyReferenceBinder implements TopicXmlDataEnricher.PostProcessor {
    
    private Function<String, String> titleValueResolver = Function.identity();
	
	@Override
	public void process(Collection<Output<?>> outputs, Map<String, Topic> articleId2Topic) {
		log.info("GROUPING START:");
		postProcessing(outputs, articleId2Topic);
		log.info("GROUPING COMPLETE:");
	}
	
	private void postProcessing(Collection<Output<?>> outputs, Map<String, Topic> articleId2Topic) {
		for (Output<?> output : outputs) {
			TopicBiography topic = (TopicBiography) output.getWriteTo();
			
			if (TopicBiography.MULTIPLE_PEOPLE_TYPE.equals(topic.getBiographyType())) {
				Elements elements = output.getInput().getDocument().selectXpath("//book-front/sec/list[@list-content='bio_meta']/title");
				for (Element titleElement : elements) {
					String title = StringUtils.trim(titleElement.ownText());
					if (StringUtils.isNoneBlank(title)) {
						title = title.replaceAll("<italic>", StringUtils.EMPTY)
								.replaceAll("</italic>", StringUtils.EMPTY);
						TopicBiography example = create(title, titleElement.parent());
						TopicBiography related = find(example, topic);
						if (Objects.isNull(related)) {
							related = find(example, articleId2Topic.values());
							if (Objects.nonNull(related)) {
								log.info("Found TopicBiography group current:{}, parent:{}", related.getTitle(), output.getInput().getFileName());
								addSafe(topic, related);
							} else {
								log.info("Found TopicBiography without articleId:{}, parent:{}", example.getTitle(), output.getInput().getFileName());
								addSafe(topic, example);
							}
						}
					}
				}
			}
		}
	}
	
	private static TopicBiography find(TopicBiography byExample, Collection<Topic> in) {
		TopicBiography found = null;
		for (Topic topic : in) {
			TopicBiography candidate = (TopicBiography) topic;
			if (areEqual(candidate, byExample)) {
				return candidate;
			}
			
			found = find(byExample, candidate);
			if (Objects.nonNull(found)) {
				return found;
			}
		}
		return null;
	}
	
	private static TopicBiography find(TopicBiography byExample, TopicBiography in) {
		Collection<ContentTypeReference<TopicBiography>> refs = in.getMultiple_biography_topics();
		if (Objects.nonNull(refs)) {
			for (ContentTypeReference<TopicBiography> ref : refs) {
				if (areEqual(ref.getReferable(), byExample)) {
					return ref.getReferable();
				}
			}
		}
		return null;
	}
	
	private static void addSafe(TopicBiography in, TopicBiography referable) {
		Collection<ContentTypeReference<TopicBiography>> refs = in.getMultiple_biography_topics();
		if (Objects.isNull(refs)) {
			in.setMultiple_biography_topics(refs = new HashSet<>());
		}
		refs.add(new ContentTypeReference<>(referable));
	}
	
	private TopicBiography create(String title, Element bioMeta) {
		TopicBiography result = new TopicBiography();
		result.setTitle(titleValueResolver.apply(title));
		result.setBiographyType(TopicBiography.SINGLE_PEOPLE_TYPE);
		
		String commonPartOfXpath = String.format("//title[contains(text(), '%s')]/parent::list/list-item/label", title);
		
		Element birthElem = bioMeta.selectXpath(commonPartOfXpath
				+ "[contains(text(), 'Born') or "
				+ "contains(text(), 'Birth Date') or "
				+ "contains(text(), 'Birth date') or "
				+ "contains(text(), 'Date of Birth') or "
				+ "contains(text(), 'Date of birth') or "
				+ "contains(text(), 'Birth:') ]/ancestor::list-item/p").first();
		TopicBiographyXmlParser.setBirthDateInfo(birthElem, result);
		
		Element placeOfBirth = bioMeta.selectXpath(commonPartOfXpath
				+ "[contains(text(), 'Birthplace') or "
				+ "contains(text(), 'Place of birth')]/ancestor::list-item/p").first();
		TopicBiographyXmlParser.setBirthPlaceInfo(placeOfBirth, result);
		
		
		Element dethElem = bioMeta.selectXpath(commonPartOfXpath
				+ "[contains(text(), 'Death Date') or "
				+ "contains(text(), 'Died:') or "
				+ "contains(text(), 'Date of Death') or "
				+ "contains(text(), 'Death:')]/ancestor::list-item/p").first();
		TopicBiographyXmlParser.setDethDateInfo(dethElem, result);
		
		Element placeOfDeth = bioMeta.selectXpath(commonPartOfXpath
				+ "[contains(text(), 'Deathplace') or "
				+ "contains(text(), 'Place of death')]/ancestor::list-item/p").first();
		TopicBiographyXmlParser.setDethPlaceInfo(placeOfDeth, result);
		
		return result;
	}
	
	public static boolean areEqual(TopicBiography first, TopicBiography second) {
		if (Objects.nonNull(first) && Objects.nonNull(second)) {
			return Objects.equals(first.getTitle(), second.getTitle()) &&
					Objects.equals(first.getBirth_year(), second.getBirth_year()) &&
					Objects.equals(first.getBirth_month(), second.getBirth_month()) &&
					Objects.equals(first.getBirth_day(), second.getBirth_day());
		}
		return false;
	}

    public TopicBiographyReferenceBinder setTitleValueResolver(Function<String, String> titleValueResolver) {
        this.titleValueResolver = titleValueResolver;
        return this;
    }
}
