package com.ebsco.platform.shared.cmsimport.rs.domain.topic;

import java.util.Collection;

import com.ebsco.platform.shared.cmsimport.rs.domain.JsonRTE;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicHealth extends Topic {
	
	public static final String CONTENT_TYPE_UID = "topic_health";
	
	@JsonProperty(value = "anatomy_or_systems_affected")
	private String anatomyOrSystemsAffected;
	@JsonProperty(value = "also_known_as")
	private JsonRTE alsoKnownAs;//JSON RTE
	private Collection<String> category;
	private JsonRTE definitions;//JSON RTE
	
	@Override
	public String getContentTypeUid() {
		return CONTENT_TYPE_UID;
	}

}
