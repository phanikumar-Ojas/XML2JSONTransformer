package com.ebsco.platform.shared.cmsimport.rs.domain.term;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AltMainTerm extends BasicTerm {

    public static final String CONTENT_TYPE_UID = "alt_main_term";

    @JsonProperty(value = "alt_main_term")
    private String altMainTerm;

    public AltMainTerm(String altMainTerm) {
        super(altMainTerm);
        this.altMainTerm = altMainTerm;
    }

    @Override
    public String getContentTypeUid() {
        return CONTENT_TYPE_UID;
    }
}
