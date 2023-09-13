package com.ebsco.platform.shared.cmsimport.rs.domain.topic;


import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicScience extends Topic {
	
	public static final String CONTENT_TYPE_UID = "topic_science";
	
	private Collection<String> category;
	
	@JsonProperty(value = "secondary_category")
	private Collection<String> secondaryCategory;
	
	@Override
	public String getContentTypeUid() {
		return CONTENT_TYPE_UID;
	}

}
