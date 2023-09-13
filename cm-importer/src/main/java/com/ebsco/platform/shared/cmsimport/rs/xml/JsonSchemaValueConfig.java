package com.ebsco.platform.shared.cmsimport.rs.xml;

import static com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig.MULTIPLE_STRING_VALUES_CONVERTER;
import static com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig.SINGLE_STRING_VALUE_CONVERTER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.select.Elements;

import com.ebsco.platform.shared.cmsimport.rs.util.CommonUtil;
import com.ebsco.platform.shared.cmsimport.rs.xml.XPathValueToPojoExtractor.MappingConfig.FieldConfig;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class JsonSchemaValueConfig extends FieldConfig {
	
	private static final Map<String, JSONObject> SCHEMA_CACHE = new HashMap<>();
	
	private String contentType;
	
	private Map<String, String> alias2Value;
	
	public JsonSchemaValueConfig(String contentType) {
		this.contentType = contentType;
	}
	
	public JsonSchemaValueConfig of(FieldConfig cfg) {
		setJsonFieldName(cfg.getJsonFieldName());
		setXpath(cfg.getXpath());
		setValueConverter(cfg.getValueConverter());
		return this;
	}
	
	public JsonSchemaValueConfig aliases( Map<String, String> aliases) {
		this.alias2Value = aliases;
		return this;
	}
	
	@Override
	public Function getValueConverter() {
		var originalFunc = super.getValueConverter();
		
		return new Function() {

			@Override
			public Object apply(Object elements) {
				if (FieldConfig.EMPTY == originalFunc) {
					return defaultProcess((Elements) elements);
				} else {
					return process((Elements) elements, originalFunc);
				}
			}}; 
	}
	
	private Object defaultProcess(Elements elements) {
		JSONObject fieldSchema = fieldSchema();
		boolean multiple = fieldSchema.getBoolean("multiple");
		if (multiple) {
			Collection<String> result = MULTIPLE_STRING_VALUES_CONVERTER.apply(elements);
			return filter(tryAlias(result), fieldSchema);
		} else {
			String result = SINGLE_STRING_VALUE_CONVERTER.apply(elements);
			return filter(tryAlias(result), fieldSchema);
		}
	}
	
	private Object process(Elements elements, Function func) {
		Object value = func.apply(elements);
		if (Objects.isNull(value)) {
			return null;
		}
		
		JSONObject fieldSchema = fieldSchema();
		if (value instanceof String) {
			return filter(tryAlias((String) value), fieldSchema);
		} else if (value instanceof Collection) {
			return filter(tryAlias((Collection<String>) value), fieldSchema);
		}
		return value;
	}
	
	private JSONObject fieldSchema() {
		JSONObject schema = SCHEMA_CACHE.get(contentType);
		if (Objects.isNull(schema)) {
			String json = CommonUtil.readClasspathResourceFile("/content-types/" + contentType + ".json");
			SCHEMA_CACHE.put(contentType, schema = new JSONObject(json));
		}
		JSONArray fieldSchemas = schema.getJSONArray("schema");
		for (int i = 0; i < fieldSchemas.length(); i++) {
			JSONObject fieldSchema = (JSONObject) fieldSchemas.getJSONObject(i);
			if (fieldSchema.getString("uid").equals(getJsonFieldName())) {
				return fieldSchema;
			}
		}
		throw new RuntimeException("field not found in json schema: " + contentType + "." + getJsonFieldName());
	}
	
	private Collection<String> filter(Collection<String> values, JSONObject fieldSchema) {
		if (Objects.isNull(values)) {
			return null;
		}
		Set<String> possibleValues = choices(fieldSchema);
		if (!possibleValues.isEmpty()) {
			Set<String> badValues = new HashSet<>();
			for (String value : values) {
				if (!possibleValues.contains(value)) {
					badValues.add(value);
				}
			}
			if (!badValues.isEmpty()) {
				log.warn("Found values not matching to schema {}.{} bad values: ({}). REMOVEING....", 
						contentType, getJsonFieldName(), String.join(", ", badValues.toArray(new String [0])));
				values.removeAll(badValues);
			}
		}
		return values;
	}
	
	private String filter(String value, JSONObject fieldSchema) {
		if (Objects.isNull(value)) {
			return null;
		}
		Set<String> possibleValues = choices(fieldSchema);
		if (!possibleValues.isEmpty()) {
			if (!possibleValues.contains(value)) {
				log.warn("Found value not matching to schema {}.{} bad value: ({})", 
						contentType, getJsonFieldName(), value);
				return null;
			}
		}
		return value;
	}
	
	private String tryAlias(String value) {
		if (Objects.nonNull(alias2Value)) {
			String realValue = alias2Value.get(value);
			if (Objects.nonNull(realValue)) {
				return realValue;
			}
		}
		return value;
	}
	
	private Collection<String> tryAlias(Collection<String> values) {
		if (Objects.nonNull(alias2Value) && Objects.nonNull(values)) {
			List<String> copiedValues  = new ArrayList<>(values);
			for (int i = 0; i < copiedValues.size(); i++) {
				String value = copiedValues.get(i);
				String realValue = alias2Value.get(value);
				if (Objects.nonNull(realValue)) {
					copiedValues.set(i, realValue);
				}
			}
			values.clear();
			values.addAll(copiedValues);
		}
		return values;
	}
	
	private static Set<String> choices(JSONObject fieldSchema) {
		Set<String> result = new HashSet<>();
		if (fieldSchema.has("enum")) {
			JSONArray choices = fieldSchema.getJSONObject("enum").getJSONArray("choices");
			for (int i = 0; i < choices.length(); i++) {
				JSONObject choice = (JSONObject) choices.getJSONObject(i);
				result.add(choice.getString("value"));
			}
		}
		return result;
	}

}