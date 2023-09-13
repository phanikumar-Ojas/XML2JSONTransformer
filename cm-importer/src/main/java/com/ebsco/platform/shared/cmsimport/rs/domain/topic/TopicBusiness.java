package com.ebsco.platform.shared.cmsimport.rs.domain.topic;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicBusiness extends Topic {
	
	public static final String CONTENT_TYPE_UID = "topic_business";
	
	@JsonProperty(value = "date_founded")
	private String dateFounded;
	private String industry;
	@JsonProperty(value = "corporate_headquarters")
	private String corporateHeadquarters;
	private String type;
	
	@Override
	public String getContentTypeUid() {
		return CONTENT_TYPE_UID;
	}

}
