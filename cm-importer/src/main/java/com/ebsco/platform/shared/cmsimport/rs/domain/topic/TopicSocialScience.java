package com.ebsco.platform.shared.cmsimport.rs.domain.topic;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicSocialScience extends Topic {
	
	public static final String CONTENT_TYPE_UID = "topic_social_science";
	
	private String category;
	@JsonProperty("secondary_category")
	private Collection<String> secondaryCategory;
	
	@JsonProperty("curriculum_american_history")
	private Collection<String> curriculumAmericanHistory;
	
	@JsonProperty("curriculum_ancient_history")
	private Collection<String> curriculum_ancient_history;
	
	@JsonProperty("curriculum_western_civilization_european_history")
	private Collection<String> curriculum_western_civilization_european_history;
	
	@JsonProperty("curriculum_women_s_history")
	private Collection<String> curriculum_women_s_history;
	
	@JsonProperty("curriculum_world_history")
	private Collection<String> curriculum_world_history;
	
	@JsonProperty("geographical_category")
	private Collection<String> geographical_category;
	
	@JsonProperty("sub_geographical_category")
	private Collection<String> sub_geographical_category;
	
	@JsonProperty("geo_keyword")
	private String geo_keyword;
	
	@JsonProperty("state_region")
	private String state_region;
	
	@JsonProperty("state_general_population")
	private String state_general_population;
	
	@JsonProperty("state_capital")
	private String state_capital;
	
	@JsonProperty("state_largest_city")
	private String state_largest_city;
	
	@JsonProperty("state_numbers_of_counties")
	private String state_numbers_of_counties;
	
	@JsonProperty("state_nickname")
	private String state_nickname;
	
	@JsonProperty("state_motto")
	private String state_motto;
	
	@JsonProperty("state_flag")
	private String state_flag;
	
	@JsonProperty("state_demographic_population_density")
	private String state_demographic_population_density;
	
	@JsonProperty("state_demographic_state_urban_population")
	private String state_demographic_state_urban_population;
	
	@JsonProperty("state_demographic_rural_population")
	private String state_demographic_rural_population;
	
	@JsonProperty("state_demographic_population_under_18")
	private String state_demographic_population_under_18;
	
	@JsonProperty("state_demographic_population_over_65")
	private String state_demographic_population_over_65;
	
	@JsonProperty("state_demographic_white_alone")
	private String state_demographic_white_alone;
	
	@JsonProperty("state_demographic_black_or_african_american_alone")
	private String state_demographic_black_or_african_american_alone;
	
	@JsonProperty("state_demographic_hispanic_or_latino")
	private String state_demographic_hispanic_or_latino;
	
	@JsonProperty("state_demographic_american_indian_and_alaska_native_alone")
	private String state_demographic_american_indian_and_alaska_native_alone;
	
	@JsonProperty("state_demographic_asian_alone")
	private String state_demographic_asian_alone;
	
	@JsonProperty("state_demographic_native_hawaiian_and_other_pacific_islander_alone")
	private String state_demographic_native_hawaiian_and_other_pacific_islander_alone;
	
	@JsonProperty("state_demographic_some_other_race_alone")
	private String state_demographic_some_other_race_alone;
	
	@JsonProperty("state_demographic_two_or_more_races")
	private String state_demographic_two_or_more_races;
	
	@JsonProperty("state_demographic_per_capita_income")
	private String state_demographic_per_capita_income;
	
	@JsonProperty("state_demographic_unemployment")
	private String state_demographic_unemployment;
	
	@JsonProperty("state_economy_gross_domestic_product")
	private String state_economy_gross_domestic_product;
	
	@JsonProperty("state_economy_gdp")
	private String state_economy_gdp;
	
	@JsonProperty("state_government_governor")
	private String state_government_governor;
	
	@JsonProperty("state_government_present_constitution_date")
	private String state_government_present_constitution_date;
	
	@JsonProperty("state_government_electoral_votes")
	private String state_government_electoral_votes;
	
	@JsonProperty("state_government_number_of_counties")
	private String state_government_number_of_counties;
	
	@JsonProperty("state_government_violent_crime_rate")
	private String state_government_violent_crime_rate;

	@JsonProperty("state_government_death_penalty")
	private String state_government_death_penalty;
	
	@JsonProperty("country_full_name_of_country")
	private String country_full_name_of_country;
	
	@JsonProperty("country_region")
	private String country_region;
	
	@JsonProperty("country_official_language")
	private String country_official_language;
	
	@JsonProperty("country_population")
	private String country_population;
	
	@JsonProperty("country_nationality")
	private String country_nationality;
	
	@JsonProperty("country_currency_money_")
	private String country_currency_money_;
	
	@JsonProperty("country_land_area")
	private String country_land_area;
	
	@JsonProperty("country_water_area")
	private String country_water_area;
	
	@JsonProperty("country_capital")
	private String country_capital;
	
	@JsonProperty("country_national_anthem")
	private String country_national_anthem;
	
	@JsonProperty("country_extended_national_holiday")
	private String country_extended_national_holiday;
	
	@JsonProperty("country_extended_population_growth")
	private String country_extended_population_growth;
	
	@JsonProperty("country_extended_time_zone")
	private String country_extended_time_zone;
	
	@JsonProperty("country_extended_flag")
	private String country_extended_flag;
	
	@JsonProperty("country_extended_motto")
	private String country_extended_motto;
	
	@JsonProperty("country_extended_independence")
	private String country_extended_independence;
	
	@JsonProperty("country_extended_government_type")
	private String country_extended_government_type;
	
	@JsonProperty("country_extended_suffrage")
	private String country_extended_suffrage;
	
	@JsonProperty("country_extended_legal_system")
	private String country_extended_legal_system;
	
	@Override
	public String getContentTypeUid() {
		return CONTENT_TYPE_UID;
	}
}
