package com.ebsco.platform.shared.cmsimport.rs.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ebsco.platform.shared.cmsimport.rs.domain.ContentType;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ContentTypeJsonSchemaValidator {
    
    private static final Map<String, JSONObject> SCHEMA_CACHE = new HashMap<>();
    private static final Map<String, Map<String, String>> ALIAS_2_VALUE = new HashMap<>();
    static {
        ALIAS_2_VALUE.put("article.collection_topic", Map.of(
                "Social Sciences", "Social Sciences and Humanities")
        );
    }
    
    private static final Map<String, Set<Object>> errors = new TreeMap<>(Comparator.naturalOrder());
    
    private String contentType;
    
    private ContentTypeJsonSchemaValidator(String contentType) {
        this.contentType = contentType;
    }
    
    public static <T extends ContentType> T validate(T entry) {
        ContentTypeJsonSchemaValidator validator = new ContentTypeJsonSchemaValidator(entry.getContentTypeUid());
        try {
            Collection<String> jsonFieldNames = validator.jsonFieldNamesToCheck();
            for (String jsonFieldName : jsonFieldNames) {
                Object value = PojoUtil.getJsonField(entry, jsonFieldName);
                String bindFieldName = ContentTypeUtil.bindFieldName(entry);
                Object bindFieldValue = PojoUtil.getJsonField(entry, bindFieldName);
                Object newValue = validator.process(value, jsonFieldName, bindFieldValue);
                if (value != newValue) {
                    log.warn("Found wrong value {}.{}=({}). REPLACING with ({})....", 
                            validator.contentType, jsonFieldName, value, newValue);
                    String errorKey = String.format("%s.%s=%s", validator.contentType, jsonFieldName, value);
                    error(errorKey, bindFieldValue);
                    PojoUtil.setJsonField(entry, jsonFieldName, newValue);
                }
            }
        } catch (Exception e) {
            error(e.getClass().getName(), ExceptionUtils.getStackTrace(e) );
        }
        
        return entry;
    }
    
    public static Map<String, Set<Object>> getInvalidValuesInfo() {
        return errors;
    }

    private Object process(Object fieldValue, String jsonFieldName, Object bindFieldValue) {
        if (Objects.isNull(fieldValue)) {
            return null;
        }
        
        JSONObject fieldSchema = fieldSchema(jsonFieldName);
        boolean multiple = fieldSchema.getBoolean("multiple");
        if (fieldValue instanceof String) {
            if (multiple) {
                String errorKey = String.format("%s.%s : Json schema says  is multiple, but actual value is single '%s'", contentType, jsonFieldName, fieldValue);
                error(errorKey, bindFieldValue);
            }
            return filter(tryAlias(jsonFieldName, (String) fieldValue), fieldSchema);
        } else if (fieldValue instanceof Collection) {
            if (!multiple) {
                String errorKey = String.format("%s.%s : Json schema says is single, but actual value is multiple '%s'", contentType, jsonFieldName, fieldValue);
                error(errorKey, bindFieldValue);
            }
            Collection<Object> multipleValue = (Collection<Object>) fieldValue;
            if (CollectionUtils.isNotEmpty(multipleValue)) {
                Object first = multipleValue.iterator().next();
                if (first instanceof String) {
                    return filter(tryAlias(jsonFieldName, (Collection<String>) fieldValue), fieldSchema);
                }
            }
        }
        return fieldValue;
    }
    
    private Collection<String> jsonFieldNamesToCheck() {
        Collection<String> result = new ArrayList<>();
        JSONObject schema = schema();
        JSONArray fieldSchemas = schema.getJSONArray("schema");
        for (int i = 0; i < fieldSchemas.length(); i++) {
            JSONObject fieldSchema = (JSONObject) fieldSchemas.getJSONObject(i);
            if (fieldSchema.has("enum")) {
                result.add(fieldSchema.getString("uid"));
            }
        }
        return result;
    }
    
    private JSONObject schema() {
        JSONObject schema = SCHEMA_CACHE.get(contentType);
        if (Objects.isNull(schema)) {
            String json = CommonUtil.readClasspathResourceFile("/content-types/" + contentType + ".json");
            SCHEMA_CACHE.put(contentType, schema = new JSONObject(json));
        }
        return schema;
    }
    
    private JSONObject fieldSchema(String jsonFieldName) {
        JSONObject schema = schema();
        JSONArray fieldSchemas = schema.getJSONArray("schema");
        for (int i = 0; i < fieldSchemas.length(); i++) {
            JSONObject fieldSchema = (JSONObject) fieldSchemas.getJSONObject(i);
            if (fieldSchema.getString("uid").equals(jsonFieldName)) {
                return fieldSchema;
            }
        }
        throw new RuntimeException("field not found in json schema: " + contentType + "." + jsonFieldName);
    }
    
    private static String getJsonFieldName(JSONObject fieldSchema) {
        return fieldSchema.getString("uid");
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
                        contentType, getJsonFieldName(fieldSchema), String.join(", ", badValues.toArray(new String [0])));
                try {
                    values.removeAll(badValues);
                } catch(Exception e) {
                    log.error("Can't remove, trying to clone and then remove", e);
                    values = new ArrayList<>(values);
                    values.removeAll(badValues);
                }
                
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
                        contentType, getJsonFieldName(fieldSchema), value);
                return null;
            }
        }
        return value;
    }
    
    private String tryAlias(String jsonFieldName, String value) {
        String fieldPath = contentType + "." + jsonFieldName;
        Map<String, String> alias2Value = ALIAS_2_VALUE.get(fieldPath);
        if (Objects.nonNull(alias2Value)) {
            String realValue = alias2Value.get(value);
            if (Objects.nonNull(realValue)) {
                return realValue;
            }
        }
        return value;
    }
    
    private Collection<String> tryAlias(String jsonFieldName, Collection<String> values) {
        String fieldPath = contentType + "." + jsonFieldName;
        Map<String, String> alias2Value = ALIAS_2_VALUE.get(fieldPath);
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
    
    private static void error(String key, Object entryIdentifier) {
        Set<Object> entryIdentifiers = errors.get(key);
        if (Objects.isNull(entryIdentifiers)) {
            errors.put(key, entryIdentifiers = new HashSet<>());
        }
        entryIdentifiers.add(entryIdentifier);
    }
}
