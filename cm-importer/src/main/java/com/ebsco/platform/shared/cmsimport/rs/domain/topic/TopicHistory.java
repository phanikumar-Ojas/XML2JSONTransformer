package com.ebsco.platform.shared.cmsimport.rs.domain.topic;

import java.util.Collection;

import com.ebsco.platform.shared.cmsimport.rs.domain.JsonRTE;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicHistory extends Topic {
	
	public static final String CONTENT_TYPE_UID = "topic_history";
	
	private Collection<String> category;
	
	@JsonProperty("secondary_category")
	private Collection<String> secondaryCategory;
	
	@JsonProperty("curriculum_american_history")
	private Collection<String> curriculumAmericanHistory;
	
	@JsonProperty("curriculum_ancient_history")
	private Collection<String> curriculumAncientHistory;
	
	
	@JsonProperty("curriculum_western_civilization_european_history")
	private Collection<String> curriculumWesternCivilizationEuropeanHistory;
	
	@JsonProperty("curriculum_women_s_history")
	private Collection<String> curriculumWomensHistory;

	
	@JsonProperty("curriculum_world_history")
	private Collection<String> curriculumWorldHistory;
	
	@JsonProperty("geographical_category")
	private Collection<String> geographicalCategory;
	
	@JsonProperty("sub_geographical_category")
	private Collection<String> subGeographicalCategory;
	
	@JsonProperty("geo_keyword")
	private String geoKeyword;
	
	private String date;
	private String location;
	
	@JsonProperty("also_known_as")
	private JsonRTE alsoKnownAs;
	
	private String quotes;
	
	@JsonProperty("defining_moment")
	private String definingMoment;

	@Override
	public String getContentTypeUid() {
		return CONTENT_TYPE_UID;
	}
}
