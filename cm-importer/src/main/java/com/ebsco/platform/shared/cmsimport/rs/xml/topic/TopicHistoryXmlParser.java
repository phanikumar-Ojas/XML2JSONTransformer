package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import static com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig.MULTIPLE_STRING_VALUES_CONVERTER;

import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.jsoup.nodes.Document;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicHistory;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig.MultipleStringValuesConverter;

public class TopicHistoryXmlParser implements TopicXmlParser<TopicHistory> {
	
	private static final String LOCATION = "Location";
	private static final String LOCALE = "Locale";
	private static final String DATE = "Date";
	private static final String CURRICULUM_PATTERN = "//book-part-categories/subj-group[@subj-group-type='curriculum']"
			+ "/subject[contains(text(), '%s')]/parent::subj-group/subj-group/subject";
	private static final String BOOK_FRONT_PATTERN_1 = "//book-front/sec/*/bold/sc[contains(text(), '%s')]/ancestor::p";
	private static final String BOOK_FRONT_PATTERN_2 = "//book-front/sec/*/bold[contains(text(), '%s')]/ancestor::p";
	
	private static final MappingConfig FIELD_MAPPING_CFG = new MappingConfig().
		of("category", new MultipleStringValuesConverter().valueTransformer(value -> XPathValueToPojoExtractor.fixApostroph(value)), "//book-part/book-part-meta/book-part-categories/subj-group[@subj-group-type='category']/subject[1]").
		
		of("secondary_category", new MultipleStringValuesConverter().valueTransformer(value -> XPathValueToPojoExtractor.fixApostroph(value)), "//book-part/book-part-meta/book-part-categories/subj-group[@subj-group-type='category']/subj-group/subject").
		of("curriculum_american_history", MULTIPLE_STRING_VALUES_CONVERTER, String.format(CURRICULUM_PATTERN, "American History")).
		of("curriculum_ancient_history", MULTIPLE_STRING_VALUES_CONVERTER, String.format(CURRICULUM_PATTERN, "Ancient History")).
		of("curriculum_western_civilization_european_history", MULTIPLE_STRING_VALUES_CONVERTER, String.format(CURRICULUM_PATTERN, "Western Civilization/European History")).
		of("curriculum_women_s_history", MULTIPLE_STRING_VALUES_CONVERTER, String.format(CURRICULUM_PATTERN, "Women")).
		of("curriculum_world_history", MULTIPLE_STRING_VALUES_CONVERTER, String.format(CURRICULUM_PATTERN, "World History")).
		of("geographical_category", MULTIPLE_STRING_VALUES_CONVERTER, "//book-part-categories/subj-group[@subj-group-type='geographical']/subject[1]").
		of("sub_geographical_category", MULTIPLE_STRING_VALUES_CONVERTER,  "//book-part-categories/subj-group[@subj-group-type='geographical']/subj-group/subject[2]").
		
		of("geo_keyword", "//book-part/book-part-meta/book-part-categories/subj-group[@subj-group-type='geo-keyword']/subject[1]").
		of("category", "//book-part/book-part-meta/book-part-categories/subj-group[@subj-group-type='category']/subject[1]").
		of("date", String.format(BOOK_FRONT_PATTERN_1, DATE),
				   String.format(BOOK_FRONT_PATTERN_2, DATE)).
		of("location", String.format(BOOK_FRONT_PATTERN_1, LOCALE),
				   	   String.format(BOOK_FRONT_PATTERN_2, LOCALE),
				   	   String.format(BOOK_FRONT_PATTERN_1, LOCATION),
				   	   String.format(BOOK_FRONT_PATTERN_2, LOCATION)
		).
		
		of(CommonFieldConfigs.ALSO_KNOWN_AS);
	
	@Override
	public Output<TopicHistory> parse(Input input, TopicHistory writeTo) {
		Document readFrom = input.getDocument();
		
		XPathValueToPojoExtractor simpleValuesExtractor = new XPathValueToPojoExtractor();
		simpleValuesExtractor.extract(readFrom, writeTo, FIELD_MAPPING_CFG);
		
		new BoundedAndOrderedTagsExtractor().extract(readFrom, writeTo);
		
		if (Objects.nonNull(writeTo.getCategory())) {
			if (CollectionUtils.isNotEmpty(writeTo.getSecondaryCategory())) {
				writeTo.getSecondaryCategory().removeAll(writeTo.getCategory());
			}
		}
		
		return Output.<TopicHistory>builder().input(input).writeTo(writeTo).build();
	}
}
