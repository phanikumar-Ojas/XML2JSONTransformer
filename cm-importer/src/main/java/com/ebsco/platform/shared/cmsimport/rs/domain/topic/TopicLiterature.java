package com.ebsco.platform.shared.cmsimport.rs.domain.topic;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TopicLiterature extends Topic {
	
	public static final String CONTENT_TYPE_UID = "topic_literature";
	
	private Collection<String> category;
	
	@JsonProperty("primary_genres")
	private Collection<String> primary_genres;
	
	@JsonProperty("subgenres")
	private Collection<String> subgenres;
	
	@JsonProperty("geo_locale")
	private Collection<String> geo_locale;
	
	@JsonProperty("geo_keyword")
	private String geo_keyword;
	
	private Collection<String> theme;
	
	@JsonProperty("literture_curriculum")
	private String literture_curriculum;
	
	@JsonProperty("bio_reference_id")
	private Collection<String> bio_reference_id;
	
	@JsonProperty("top_reference_id")
	private Collection<String> top_reference_id;
	
	@JsonProperty("xref_id")
	private Collection<String> xref_id;
	
	private Collection<String> movement;

	@Override
	public String getContentTypeUid() {
		return CONTENT_TYPE_UID;
	}
}
