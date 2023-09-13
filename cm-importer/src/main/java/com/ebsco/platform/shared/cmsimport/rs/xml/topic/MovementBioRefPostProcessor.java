package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.Topic;
import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicRepository;
import com.ebsco.platform.shared.cmsimport.rs.util.PojoUtil;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.TopicXmlParser.Output;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class MovementBioRefPostProcessor implements TopicXmlDataEnricher.PostProcessor {
	
	private Map<String, Collection<String>> bioRef2MovementName;
	
	private TopicRepository repossitory;
	
	public MovementBioRefPostProcessor(TopicRepository repossitory) {
		this.repossitory = repossitory;
	}

	@Override
	public void process(Collection<Output<?>> outputs, Map<String, Topic> articleId2Topic) {
		init();
		int count = 1;
		for (Output<?> output : outputs) {
			log.info("({} of {})] {}", count++, outputs.size(), output.getWriteTo().getContentTypeUid());
			List<String> values = output.getInput().getDocument().selectXpath(CommonFieldConfigs.BIO_REF_XPATH).eachText();
			if (CollectionUtils.isNotEmpty(values)) {
				Set<String> bioRefs = new HashSet<>();
				for (String value : values) {
					String bioRef = toBioRef(value);
					bioRefs.add(bioRef);
				}
				Collection<String> movementNames = getMovementNames(bioRefs);
				if (CollectionUtils.isNotEmpty(movementNames)) {
					Topic topic = output.getWriteTo();
					PojoUtil.set(topic, "movement", movementNames);
				}
			}
		}
	}
	
	private static String toBioRef(String fromXmlValue) {
		String value = StringUtils.trim(fromXmlValue);
		if (NumberUtils.isDigits(value)) {
			return "AN"+value;
		}
		
		value = StringUtils.substringBefore(value, "-");
		if (NumberUtils.isDigits(value)) {
			return "AN"+value;
		}
		return fromXmlValue;
	}
	
	public Collection<String> getMovementNames(Set<String> bioRefs) {
		Collection<String> result = new HashSet<>();
		for (String bioRef : bioRefs) {
			Collection<String> movementNames = bioRef2MovementName.get(bioRef);
			if (CollectionUtils.isNotEmpty(result)) {
				result.addAll(movementNames);
			}
		}
		return result;
	}
	
	private void init() {
		if (Objects.isNull(bioRef2MovementName)) {
			log.info("Loading movements info...");
			bioRef2MovementName = repossitory.getBioRef2MovementName();
			log.info("movements size {}", bioRef2MovementName.size());
		} else {
			log.info("Using cached movements info...");
		}
	}
}
