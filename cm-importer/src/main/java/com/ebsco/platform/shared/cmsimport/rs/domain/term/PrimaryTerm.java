package com.ebsco.platform.shared.cmsimport.rs.domain.term;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrimaryTerm extends BasicTerm {

    public static final String CONTENT_TYPE_UID = "primary_term";

    @JsonProperty(value = "primary_term")
    private String primaryTerm;

    public PrimaryTerm(String primaryTerm) {
        super(primaryTerm);
        this.primaryTerm = primaryTerm;
    }

    @Override
    public String getContentTypeUid() {
        return CONTENT_TYPE_UID;
    }
}
