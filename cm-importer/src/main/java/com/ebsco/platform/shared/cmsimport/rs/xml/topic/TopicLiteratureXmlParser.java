package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import static com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig.MULTIPLE_STRING_VALUES_CONVERTER;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicLiterature;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig;

public class TopicLiteratureXmlParser implements TopicXmlParser<TopicLiterature> {
	
	private static final MappingConfig FIELD_MAPPING_CFG = new MappingConfig().
		
		of("category", MULTIPLE_STRING_VALUES_CONVERTER, "//book-part/book-part-meta/book-part-categories/subj-group[@subj-group-type='articletype']/subject").
		
		of("primary_genres", MULTIPLE_STRING_VALUES_CONVERTER, "//book-part/book-part-meta/book-part-categories/subj-group[@subj-group-type='genre']/subject").
		
		of("subgenres", MULTIPLE_STRING_VALUES_CONVERTER, "//book-part/book-part-meta/book-part-categories/subj-group[@subj-group-type='genre']/subj-group/subject").
		
		of("geo_locale", MULTIPLE_STRING_VALUES_CONVERTER, "//book-part/book-part-meta/book-part-categories/subj-group[@subj-group-type='geo-lit']/subj-group/subject").
		
		of("geo_keyword", "//book-part/book-part-meta/book-part-categories/subj-group[@subj-group-type='geo-keyword']/subject[1]").
		
		of("theme", MULTIPLE_STRING_VALUES_CONVERTER, "//book-part/book-part-meta/book-part-categories/subj-group[@subj-group-type='theme']/subject").
		
		of("bio_reference_id", MULTIPLE_STRING_VALUES_CONVERTER, CommonFieldConfigs.BIO_REF_XPATH).
		
		of("top_reference_id", MULTIPLE_STRING_VALUES_CONVERTER, String.format(CommonFieldConfigs.CUSTOM_META_VALUE_XPATH_PATTERN, "TopMatter")).
		
		of("xref_id", MULTIPLE_STRING_VALUES_CONVERTER, String.format(CommonFieldConfigs.CUSTOM_META_VALUE_XPATH_PATTERN, "XRef"));
	
	@Override
	public Output<TopicLiterature> parse(Input input, TopicLiterature writeTo) {
		Document readFrom = input.getDocument();
		
		//Elements els = readFrom.selectXpath("//book-part/book-part-meta/custom-meta-wrap/custom-meta[@type='simple']/meta-name[contains(text(), 'Bio Ref')]/parent::custom-meta/meta-value");
		
		//Elements els1 = readFrom.selectXpath(CommonFieldConfigs.BIO_REF_XPATH);
		
		XPathValueToPojoExtractor simpleValuesExtractor = new XPathValueToPojoExtractor();
		simpleValuesExtractor.extract(readFrom, writeTo, FIELD_MAPPING_CFG);
		
		
		new BoundedAndOrderedTagsExtractor().extract(readFrom, writeTo);
		
		return Output.<TopicLiterature>builder().input(input).writeTo(writeTo).build();
	}
}
