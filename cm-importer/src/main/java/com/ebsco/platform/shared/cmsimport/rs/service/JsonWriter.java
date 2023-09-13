package com.ebsco.platform.shared.cmsimport.rs.service;

import java.util.Map;

import com.ebsco.platform.shared.cmsimport.rs.domain.ContentType;

public interface JsonWriter {
	
	public static JsonWriter DEFAULT = new JsonWriter() {};
	
	default void write(Map<String, ? extends ContentType> fileName2ContentType) {}
	
	default void write(ContentType entry, String bindFieldName) {
	    write("", entry, bindFieldName);
	}
	
	default void write(String logPrefix, ContentType entry, String bindFieldName) {}
}
