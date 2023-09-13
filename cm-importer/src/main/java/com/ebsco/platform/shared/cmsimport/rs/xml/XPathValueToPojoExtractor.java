package com.ebsco.platform.shared.cmsimport.rs.xml;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ebsco.platform.shared.cmsimport.rs.util.PojoUtil;

import lombok.Getter;
import lombok.Setter;

public class XPathValueToPojoExtractor {
	
	public void extract(Document readFrom, Object writeTo, MappingConfig mappingConfig) {
		for (MappingConfig.FieldConfig fieldCfg : mappingConfig.fields()) {
			String fieldName = PojoUtil.fieldName(writeTo, fieldCfg.getJsonFieldName());
			Object existingValue = PojoUtil.get(writeTo, fieldName);
			if (Objects.isNull(existingValue)) {
				for (String xpath : fieldCfg.getXpath()) {
					if (StringUtils.isNotBlank(xpath)) {
						Elements elements = readFrom.selectXpath(xpath);
						Object value = fieldCfg.getValueConverter().apply(elements);
						if (Objects.nonNull(value)) {
							PojoUtil.set(writeTo, fieldName, value);
							break;
						}
					}
				}
			}
		}
	}
	
	public static class MappingConfig {
		
		public static final Function<Elements, String> SINGLE_STRING_VALUE_CONVERTER = new SingleStringValueConverter();
		public static final Function<Elements, Collection<String>> MULTIPLE_STRING_VALUES_CONVERTER = new MultipleStringValuesConverter();
		
		public static class SingleStringValueConverter implements Function<Elements, String> {
			
			private Function<String, String> valueTransformer = Function.identity();
			
			@Override
			public String apply(Elements elements) {
				Element element = elements.first();
				if (Objects.nonNull(element)) {
					String fieldValue = StringUtils.trim(element.ownText());
					
					if (StringUtils.isNoneBlank(fieldValue)) {
						return valueTransformer.apply(fieldValue);
					}
				}
				return null;
			}

			public SingleStringValueConverter valueTransformer(Function<String, String> valueTransformer) {
				this.valueTransformer = valueTransformer;
				return this;
			}
			
		}
		
		public static class MultipleStringValuesConverter implements Function<Elements, Collection<String>> {
			
			private Function<String, String> valueTransformer = Function.identity();
			
			@Override
			public Collection<String> apply(Elements elements) {
				List<String> values = elements.eachText();
				if (CollectionUtils.isNotEmpty(values)) {
					Collection<String> transformedValues = new LinkedHashSet<>();
					for (String value : values) {
						value = valueTransformer.apply(value);
						transformedValues.add(value);
					}
					return transformedValues;
				}
				return null;
			}
			
			public MultipleStringValuesConverter valueTransformer(Function<String, String> valueTransformer) {
				this.valueTransformer = valueTransformer;
				return this;
			}
		}
		
		private List<FieldConfig> fields = Lists.newArrayList();
		
		public MappingConfig of(String jsonFieldName, String...xpath) {
			return of(jsonFieldName, SINGLE_STRING_VALUE_CONVERTER, xpath);
		}
		
		public MappingConfig of(String jsonFieldName, Function valueConverter, String...xpath) {
			fields.add(new FieldConfig(jsonFieldName, valueConverter, xpath));
			return this;
		}
		
		public MappingConfig of(FieldConfig fieldConfig) {
			fields.add(fieldConfig);
			return this;
		}
		
		public List<FieldConfig> fields() {
			return fields;
		}
		
		@Getter
		@Setter
		public static class FieldConfig {
			
			public static final Function EMPTY = Function.identity();
			
			private String jsonFieldName;
			private List<String> xpath;
			private Function valueConverter = EMPTY;
			
			public FieldConfig() {}
			
			public FieldConfig(String jsonFieldName, String xpath) {
				this.jsonFieldName = jsonFieldName;
				this.xpath = Collections.singletonList(xpath);
			}
			
			public FieldConfig(String jsonFieldName, String...xpath) {
				this.jsonFieldName = jsonFieldName;
				this.xpath = Arrays.asList(xpath);
			}
			
			public FieldConfig(String jsonFieldName, Function valueConverter, String...xpath) {
				this(jsonFieldName, xpath);
				this.valueConverter = valueConverter;
			}
			
			public static FieldConfig of(String jsonFieldName, String...xpath) {
				return new FieldConfig(jsonFieldName, xpath);
			}
			
			public static FieldConfig of(String jsonFieldName, Function valueConverter, String...xpath) {
				return new FieldConfig(jsonFieldName, valueConverter, xpath);
			}
		}
	}
	
	public static String fixApostroph(String value) {
		if (StringUtils.isNoneBlank(value)) {
			return value.replaceAll("’", "'");//works only if UTF-8 compiler source files specified
		}
		return value;
	}
}
