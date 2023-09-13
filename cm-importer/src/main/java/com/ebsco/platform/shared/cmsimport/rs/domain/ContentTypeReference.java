package com.ebsco.platform.shared.cmsimport.rs.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ContentTypeReference<T extends ContentType> {
	
	public ContentTypeReference(T referable) {
		this.referable = referable;
	}

	@JsonIgnore
	private T referable;
    
	public T getReferable() {
		return referable;
	}

	@JsonProperty
	public String getUid() {
		String value = referable.getUid();
		return value != null ? value : null;
	}
	
	@JsonProperty(value = "_content_type_uid")
	public String getContentTypeUid() {
		String value = referable.getContentTypeUid();
		return value != null ? value : null;
	}

	@Override
	public String toString() {
		return (referable != null ? "" + referable.getContentTypeUid() + "[uid=" + referable.getUid() + "]" : "null");
	}
	
	
}
