package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBusiness;
import com.ebsco.platform.shared.cmsimport.rs.xml.JsonSchemaValueConfig;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig.FieldConfig;

public class TopicBusinessXmlParser implements TopicXmlParser<TopicBusiness> {
	
	private static final MappingConfig FIELD_MAPPING_CFG = new MappingConfig()
			.of(new JsonSchemaValueConfig(TopicBusiness.CONTENT_TYPE_UID).of(
					FieldConfig.of("date_founded", "//book-front/sec[@sec-type='quickreference']/p/bold[text()[normalize-space()='Date Founded:']]/parent::p")))
			
			.of(new JsonSchemaValueConfig(TopicBusiness.CONTENT_TYPE_UID).of(
					FieldConfig.of("industry", "//book-front/sec[@sec-type='quickreference']/p/bold[text()[normalize-space()='Industry:']]/parent::p")))
			
			.of(new JsonSchemaValueConfig(TopicBusiness.CONTENT_TYPE_UID).of(
					FieldConfig.of("corporate_headquarters", "//book-front/sec[@sec-type='quickreference']/p/bold[text()[normalize-space()='Corporate Headquarters:']]/parent::p")))
			
			.of(new JsonSchemaValueConfig(TopicBusiness.CONTENT_TYPE_UID).of(
					FieldConfig.of("type", "//book-front/sec[@sec-type='quickreference']/p/bold[text()[normalize-space()='Type:']]/parent::p")));
	
	@Override
	public Output<TopicBusiness> parse(Input input, TopicBusiness writeTo) {
		Document readFrom = input.getDocument();
		
		XPathValueToPojoExtractor simpleValuesExtractor = new XPathValueToPojoExtractor();
		simpleValuesExtractor.extract(readFrom, writeTo, FIELD_MAPPING_CFG);
		
		List<String> relatedArticleTitles = readFrom.selectXpath("//related-article").eachText();
		if (CollectionUtils.isNotEmpty(relatedArticleTitles)) {
			writeTo.setTags(new HashSet<>(relatedArticleTitles));
		}
		return Output.<TopicBusiness>builder().input(input).writeTo(writeTo).build();
	}
}
