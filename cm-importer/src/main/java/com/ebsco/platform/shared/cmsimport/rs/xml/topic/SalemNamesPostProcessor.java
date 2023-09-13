package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.Topic;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBiography;
import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicBiographyRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicBiographyRepository.SalemName;
import com.ebsco.platform.shared.cmsimport.rs.util.DataUtil;
import com.ebsco.platform.shared.cmsimport.rs.util.PojoUtil;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.TopicXmlParser.Output;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class SalemNamesPostProcessor implements TopicXmlDataEnricher.PostProcessor {
	
	public static final String BIO_META_XPATH = "//book-front/sec/list[@list-content='bio_meta']";
	
	private TopicBiographyRepository repossitory;
	
	private Map<String, SalemName> key2SalemNales;
	
	public SalemNamesPostProcessor(TopicBiographyRepository repossitory) {
		this.repossitory = repossitory;
	}

	@Override
	public void process(Collection<Output<?>> outputs, Map<String, Topic> articleId2Topic) {
		init();
		int count = 1;
		for (Output<?> output : outputs) {
			log.info("({} of {})] {}", count++, outputs.size(), output.getWriteTo().getContentTypeUid());
			TopicBiography writeTo = (TopicBiography) output.getWriteTo();
			if (TopicBiography.SINGLE_PEOPLE_TYPE.equals(writeTo.getBiographyType())) {
				Element element = output.getInput().getDocument().selectXpath(BIO_META_XPATH).first();
				if (Objects.nonNull(element)) {
					String id = element.id();
					if (StringUtils.isNotEmpty(id)) {
						String bioKey = DataUtil.onlyDigitsOrNull(id);
						if (Objects.nonNull(bioKey)) {
							SalemName salemName = key2SalemNales.get(bioKey);
							if (Objects.nonNull(salemName)) {
								PojoUtil.set(writeTo, "first_name", salemName.getFirstName());
								PojoUtil.set(writeTo, "last_name", salemName.getLastName());
							}
						}
					}
				}
			}
		}
	}
	
	private void init() {
		if (Objects.isNull(key2SalemNales)) {
			log.info("Loading salem names info...");
			key2SalemNales = repossitory.getSalemNames();
			log.info("Salem names size {}", key2SalemNales.size());
		} else {
			log.info("Using cached movements info...");
		}
	}
}
