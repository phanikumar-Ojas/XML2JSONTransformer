package com.ebsco.platform.shared.cmsimport.rs.domain;

import com.ebsco.platform.shared.cmsimport.rs.util.ConfigurationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ContentTypeMapper {

    private final ObjectMapper objectMapper;

    public ContentTypeMapper() {
        objectMapper = ConfigurationUtil.getObjectMapper();
    }

    public String map(Object entity) {
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
