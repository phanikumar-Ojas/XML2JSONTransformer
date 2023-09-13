package com.ebsco.platform.shared.cmsimport.rs.domain.topic;

import java.util.Collection;

import com.ebsco.platform.shared.cmsimport.rs.domain.ContentTypeReference;
import com.ebsco.platform.shared.cmsimport.rs.domain.JsonRTE;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicBiography extends Topic {
	
	public static final String CONTENT_TYPE_UID = "topic_biography";
	
	public static final String MULTIPLE_PEOPLE_TYPE = "Article is on Multiple People";
	public static final String SINGLE_PEOPLE_TYPE = "Article is on a Single Person";
	
	@JsonProperty("biography_type")
	private String biographyType;
	
	@JsonProperty("multiple_biography_topics")
	private Collection<ContentTypeReference<TopicBiography>> multiple_biography_topics;
	
	@JsonProperty("first_name")
	private String first_name;
	
	@JsonProperty("middle_name")
	private String middle_name;
	
	@JsonProperty("last_name")
	private String last_name;
	
	private Collection<String> category;
	
	@JsonProperty("secondary_category")
	private Collection<String> secondaryCategory;
	
	@JsonProperty("curriculum_american_history")
	private Collection<String> curriculum_american_history;
	
	@JsonProperty("curriculum_ancient_history")
	private Collection<String> curriculum_ancient_history;
	
	@JsonProperty("curriculum_western_civilization_european_history")
	private Collection<String> curriculum_western_civilization_european_history;
	
	@JsonProperty("curriculum_women_s_history")
	private Collection<String> curriculum_women_s_history;
	
	@JsonProperty("curriculum_world_history")
	private Collection<String> curriculum_world_history;
	
	@JsonProperty("also_known_as")
	private JsonRTE also_known_as;
	
	@JsonProperty("geographical_category")
	private Collection<String> geographical_category;
	
	@JsonProperty("sub_geographical_category")
	private Collection<String> sub_geographical_category;
	
	@JsonProperty("geo_keyword")
	private Collection<String> geo_keyword;
	
	@JsonProperty("birth_day")
	private Integer birth_day;
	
	@JsonProperty("birth_month")
	private String birth_month;
	
	@JsonProperty("birth_year")
	private Integer birth_year;
	
	@JsonProperty("non_numeric_birth_date")
	private String nonNumericBirthDate;
	
	@JsonProperty("place_of_birth")
	private String place_of_birth;
	
	@JsonProperty("death_day")
	private Integer death_day;
	
	@JsonProperty("death_month")
	private String death_month;
	
	@JsonProperty("death_year")
	private Integer death_year;
	
	@JsonProperty("non_numeric_death_date")
	private String non_numeric_death_date;
	
	@JsonProperty("place_of_death")
	private String place_of_death;
	
	@JsonProperty("main_occupation_or_related_field")
	private Collection<String> main_occupation_or_related_field;
	
	@JsonProperty("other_occupation_related_fields")
	private Collection<String> other_occupation_related_fields;
	
	private Collection<String> gender;
	
	private Collection<String> race;
	
	private String nationality;
	
	@JsonProperty("associated_figures")
	private Collection<String> associated_figures;
	
	@JsonProperty("referenced_associated_figures")
	private String referenced_associated_figures;
	
	@JsonProperty("key_events")
	private String key_events;
	
	@JsonProperty("referenced_key_events")
	private String referenced_key_events;
	
	@JsonProperty("principal_works")
	private JsonRTE principal_works;
	
	@JsonProperty("referenced_works")
	private String referenced_works;
	
	@JsonProperty("awards")
	private String awards;
	
	@JsonProperty("bio_reference_id")
	private Collection<String> bio_reference_id;
	
	private Collection<String> movement;

	@Override
	public String getContentTypeUid() {
		return CONTENT_TYPE_UID;
	}

}
