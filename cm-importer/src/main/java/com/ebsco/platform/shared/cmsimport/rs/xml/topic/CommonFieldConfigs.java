package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import com.ebsco.platform.shared.cmsimport.rs.xml.XmlToJsonRteConvertor;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig.FieldConfig;

public class CommonFieldConfigs {
	
	private static final String ALSO_KNOWN_AS_LABEL = "Also known as";
	
	public static final FieldConfig ALSO_KNOWN_AS = FieldConfig.of("also_known_as", 
			new XmlToJsonRteConvertor(ALSO_KNOWN_AS_LABEL),
			String.format("//book-front/sec/*/node()/node()[contains(text(), '%s')]/ancestor::p", ALSO_KNOWN_AS_LABEL),
			String.format("//book-front/sec/*/node()[contains(text(), '%s')]/ancestor::p", ALSO_KNOWN_AS_LABEL),
			String.format("//book-front/sec/p[contains(text(), '%s')]", ALSO_KNOWN_AS_LABEL));
	
	public static final String BOOK_ID_XPATH = "//book/book-meta/book-id[@pub-id-type='publisher-id' or @pub-id-type='doi']";
	
	public static final String CUSTOM_META_VALUE_XPATH_PATTERN = "//book-part/book-part-meta/custom-meta-wrap/custom-meta[@type='simple']/meta-name[contains(text(), '%s')]/parent::custom-meta/meta-value";
	public static final String BIO_REF_XPATH = String.format(CUSTOM_META_VALUE_XPATH_PATTERN, "Bio Ref");
	
}
