package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import static com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig.MULTIPLE_STRING_VALUES_CONVERTER;
import static com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig.SingleStringValueConverter;
import static com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig.MultipleStringValuesConverter;

import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.jsoup.nodes.Document;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicSocialScience;
import com.ebsco.platform.shared.cmsimport.rs.xml.JsonSchemaValueConfig;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig.FieldConfig;

public class TopicSocialScienceXmlParser implements TopicXmlParser<TopicSocialScience> {
	
	private static final String CURRICULUM_PATTERN = "//book-part-categories/subj-group[@subj-group-type='curriculum']"
			+ "/subject[contains(text(), '%s')]/parent::subj-group/subj-group/subject";
	private static final String STATE_COUNTRY_PATTERN = "//sec/list/list-item/p/bold[contains(text(), '%s')]/parent::p";
	
	private static final MappingConfig FIELD_MAPPING_CFG = new MappingConfig().
		of("category", new SingleStringValueConverter().valueTransformer(XPathValueToPojoExtractor::fixApostroph), "//book-part/book-part-meta/book-part-categories/subj-group[@subj-group-type='category']/subject[1]").
		
		of("secondary_category", new MultipleStringValuesConverter().valueTransformer(XPathValueToPojoExtractor::fixApostroph), "//book-part/book-part-meta/book-part-categories/subj-group[@subj-group-type='category']/subj-group/subject").
		of("curriculum_american_history", MULTIPLE_STRING_VALUES_CONVERTER, String.format(CURRICULUM_PATTERN, "American History")).
		of("curriculum_ancient_history", MULTIPLE_STRING_VALUES_CONVERTER, String.format(CURRICULUM_PATTERN, "Ancient History")).
		
		of(new JsonSchemaValueConfig(TopicSocialScience.CONTENT_TYPE_UID).of(
				FieldConfig.of("curriculum_western_civilization_european_history", String.format(CURRICULUM_PATTERN, "Western Civilization/European History")))
					.aliases(
						Map.of(
								"British history", "British History",
								"Medieval history", "Medieval History/Middle Ages"
						))).
		
		of("curriculum_women_s_history", MULTIPLE_STRING_VALUES_CONVERTER, String.format(CURRICULUM_PATTERN, "Women")).
		of("curriculum_world_history", MULTIPLE_STRING_VALUES_CONVERTER, String.format(CURRICULUM_PATTERN, "World History")).
		of("geographical_category", MULTIPLE_STRING_VALUES_CONVERTER, "//book-part-categories/subj-group[@subj-group-type='geographical']/subject[1]").
		of("sub_geographical_category", MULTIPLE_STRING_VALUES_CONVERTER, "//book-part-categories/subj-group[@subj-group-type='geographical']/subj-group/subject[2]").
		
		of("geo_keyword", "//book-part/book-part-meta/book-part-categories/subj-group[@subj-group-type='geo-keyword']/subject[1]").
		of("state_region", String.format(STATE_COUNTRY_PATTERN, "Region:")).
		of("state_general_population", String.format(STATE_COUNTRY_PATTERN, "Population:")).
		of("state_capital", String.format(STATE_COUNTRY_PATTERN, "Capital:")).
		of("state_largest_city", String.format(STATE_COUNTRY_PATTERN, "Largest city:")).
		of("state_numbers_of_counties", String.format(STATE_COUNTRY_PATTERN, "Number of counties:")).
		of("state_nickname", String.format(STATE_COUNTRY_PATTERN, "State nickname:")).
		of("state_motto", String.format(STATE_COUNTRY_PATTERN, "State motto:")).
		of("state_flag", String.format(STATE_COUNTRY_PATTERN, "State flag:")).
		of("state_demographic_population_density", String.format(STATE_COUNTRY_PATTERN, "Population density:")).
		of("state_demographic_state_urban_population", String.format(STATE_COUNTRY_PATTERN, "Urban population:")).
		of("state_demographic_rural_population", String.format(STATE_COUNTRY_PATTERN, "Rural population:")).
		of("state_demographic_population_under_18", String.format(STATE_COUNTRY_PATTERN, "Population under 18:")).
		of("state_demographic_population_over_65", String.format(STATE_COUNTRY_PATTERN, "Population over 65:")).
		of("state_demographic_white_alone", String.format(STATE_COUNTRY_PATTERN, "White alone:")).
		of("state_demographic_black_or_african_american_alone", String.format(STATE_COUNTRY_PATTERN, "Black or African American alone:")).
		of("state_demographic_hispanic_or_latino", String.format(STATE_COUNTRY_PATTERN, "Hispanic or Latino:")).
		of("state_demographic_american_indian_and_alaska_native_alone", String.format(STATE_COUNTRY_PATTERN, "American Indian and Alaska Native alone:")).
		of("state_demographic_asian_alone", String.format(STATE_COUNTRY_PATTERN, "Asian alone: ")).
		of("state_demographic_native_hawaiian_and_other_pacific_islander_alone", String.format(STATE_COUNTRY_PATTERN, "Native Hawaiian and Other Pacific Islander alone:")).
		of("state_demographic_some_other_race_alone", String.format(STATE_COUNTRY_PATTERN, "Some Other Race alone:")).
		of("state_demographic_two_or_more_races", String.format(STATE_COUNTRY_PATTERN, "Two or More Races:")).
		of("state_demographic_per_capita_income", String.format(STATE_COUNTRY_PATTERN, "Per capita income:")).
		of("state_demographic_unemployment", String.format(STATE_COUNTRY_PATTERN, "Unemployment:")).
		of("state_economy_gross_domestic_product", String.format(STATE_COUNTRY_PATTERN, "Gross domestic product")).
		of("state_economy_gdp", String.format(STATE_COUNTRY_PATTERN, "GDP percent change:")).
		of("state_government_governor", String.format(STATE_COUNTRY_PATTERN, "Governor:")).
		of("state_government_present_constitution_date", String.format(STATE_COUNTRY_PATTERN, "Present constitution date:")).
		of("state_government_electoral_votes", String.format(STATE_COUNTRY_PATTERN, "Electoral votes:")).
		of("state_government_number_of_counties", String.format(STATE_COUNTRY_PATTERN, "Number of counties:")).
		of("state_government_violent_crime_rate", String.format(STATE_COUNTRY_PATTERN, "Violent crime rate:")).
		of("state_government_death_penalty", String.format(STATE_COUNTRY_PATTERN, "Death penalty:")).
		of("country_full_name_of_country", String.format(STATE_COUNTRY_PATTERN, "Full name of country: ")).
		of("country_region", String.format(STATE_COUNTRY_PATTERN, "Region:")).
		of("country_official_language", String.format(STATE_COUNTRY_PATTERN, "Official language:")).
		of("country_population", String.format(STATE_COUNTRY_PATTERN, "Population:")).
		of("country_nationality", String.format(STATE_COUNTRY_PATTERN, "Nationality:")).
		of("country_currency_money_", String.format(STATE_COUNTRY_PATTERN, "Currency (money):")).
		of("country_land_area", String.format(STATE_COUNTRY_PATTERN, "Land area:")).
		of("country_water_area", String.format(STATE_COUNTRY_PATTERN, "Water area:")).
		of("country_capital", String.format(STATE_COUNTRY_PATTERN, "Capital:")).
		of("country_national_anthem", String.format(STATE_COUNTRY_PATTERN, "National anthem:")).
		of("country_extended_national_holiday", String.format(STATE_COUNTRY_PATTERN, "National holiday:")).
		of("country_extended_population_growth", String.format(STATE_COUNTRY_PATTERN, "Population growth:")).
		of("country_extended_time_zone", String.format(STATE_COUNTRY_PATTERN, "Time zone:")).
		of("country_extended_flag", String.format(STATE_COUNTRY_PATTERN, "Flag:")).
		of("country_extended_motto", String.format(STATE_COUNTRY_PATTERN, "Motto:")).
		of("country_extended_independence", String.format(STATE_COUNTRY_PATTERN, "Independence:")).
		of("country_extended_government_type", String.format(STATE_COUNTRY_PATTERN, "Government type:")).
		of("country_extended_suffrage", String.format(STATE_COUNTRY_PATTERN, "Suffrage:")).
		of("country_extended_legal_system", String.format(STATE_COUNTRY_PATTERN, "Legal system:"));
	
	@Override
	public Output<TopicSocialScience> parse(Input input, TopicSocialScience writeTo) {
		Document readFrom = input.getDocument();
		
		XPathValueToPojoExtractor simpleValuesExtractor = new XPathValueToPojoExtractor();
		simpleValuesExtractor.extract(readFrom, writeTo, FIELD_MAPPING_CFG);
		
		new BoundedAndOrderedTagsExtractor().extract(readFrom, writeTo);
		
		if (Objects.nonNull(writeTo.getCategory())) {
			if (CollectionUtils.isNotEmpty(writeTo.getSecondaryCategory())) {
				writeTo.getSecondaryCategory().remove(writeTo.getCategory());
			}
		}
		
		return Output.<TopicSocialScience>builder().input(input).writeTo(writeTo).build();
	}
}
