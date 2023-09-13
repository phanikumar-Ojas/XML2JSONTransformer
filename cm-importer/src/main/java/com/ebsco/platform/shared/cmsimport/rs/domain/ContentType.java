package com.ebsco.platform.shared.cmsimport.rs.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "entry")
public abstract class ContentType {
    
	@JsonIgnore
	private String uid;
	
    @JsonIgnore
	public abstract String getContentTypeUid();

    public String getUid() {
		return uid;
	}

    public void setUid(String uid) {
		this.uid = uid;
	}

	@Override
	public String toString() {
		return "[uid=" + uid + "]";
	}
}
