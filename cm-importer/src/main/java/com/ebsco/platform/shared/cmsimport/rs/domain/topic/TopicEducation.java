package com.ebsco.platform.shared.cmsimport.rs.domain.topic;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicEducation extends Topic {

	public static final String CONTENT_TYPE_UID = "topic_education_resource";
	
	@JsonProperty(value = "reading_level")
	private Collection<String> readingLevel;
	
	@JsonProperty(value = "target_audience")
	private Collection<String> targetAudience;
	
	private Collection<String> category;
	
	@Override
	public String getContentTypeUid() {
		return CONTENT_TYPE_UID;
	}

}
