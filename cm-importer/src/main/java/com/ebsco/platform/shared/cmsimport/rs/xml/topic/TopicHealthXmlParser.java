package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import org.jsoup.nodes.Document;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicHealth;
import com.ebsco.platform.shared.cmsimport.rs.xml.JsonSchemaValueConfig;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor;
import com.ebsco.platform.shared.cmsimport.rs.xml.XmlToJsonRteConvertor;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig.FieldConfig;

public class TopicHealthXmlParser implements TopicXmlParser<TopicHealth> {
	
	
	private static final MappingConfig FIELD_MAPPING_CFG = new MappingConfig().
		of("anatomy_or_systems_affected", "//book-front/sec[@sec-type='quickreference']/*/bold["
				+ "contains(text(), 'Anatomy or system affected') or "
				+ "contains(text(), 'Anatomy or system involved') or "
				+ "contains(text(), 'Anatomy or systems affected')]/ancestor::p",
				String.format("//book-front/sec/*/sc/bold[contains(text(), '%s')]/ancestor::p", "Anatomy or system affected")).
		
		of(CommonFieldConfigs.ALSO_KNOWN_AS).
		
		of(new JsonSchemaValueConfig(TopicHealth.CONTENT_TYPE_UID).of(FieldConfig.of(
				"category",
				"//book-part-categories/subj-group[@subj-group-type='subject']/subject",
				"//book-front/sec[@sec-type='quickreference']/*/bold[contains(text(), 'Category')]/ancestor::p",
				"//book-part/book-part-meta/book-part-categories/subj-group[@subj-group-type='category']/subject[1]"))).
		
		of("definitions", 
				new XmlToJsonRteConvertor(
						"<bold>Definition:</bold>",
						"<bold>Definition</bold>",
						"<bold>Definitions:</bold>",
						"<sc>Definition:</sc>",
						"<sc>Definition</sc>",
						"<sc>Definitions:</sc>"
				),
				"//book-front/sec/*/node()/node()[contains(text(), 'Definition')]/ancestor::p",
				"//book-front/sec[@sec-type='quickreference']/*/bold[contains(text(), 'Definition') or contains(text(), 'Definitions')]/ancestor::p");
	
	
	@Override
	public  Output<TopicHealth> parse(Input input, TopicHealth writeTo) {
		Document readFrom = input.getDocument();
		
		XPathValueToPojoExtractor simpleValuesExtractor = new XPathValueToPojoExtractor();
		simpleValuesExtractor.extract(readFrom, writeTo, FIELD_MAPPING_CFG);
		
		new BoundedAndOrderedTagsExtractor().extract(readFrom, writeTo);
		
		return Output.<TopicHealth>builder().input(input).writeTo(writeTo).build();
	}
	
}
