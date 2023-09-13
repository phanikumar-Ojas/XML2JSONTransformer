package com.ebsco.platform.shared.cmsimport.rs.domain.topic;

import java.util.Collection;
import java.util.Set;

import com.ebsco.platform.shared.cmsimport.rs.domain.ContentType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Topic extends ContentType {

	private String title;
	private Collection<String> tags;
	
	@Override
	public abstract String getContentTypeUid();
	
	@JsonIgnore
	private Set<String> articleIds;
}
