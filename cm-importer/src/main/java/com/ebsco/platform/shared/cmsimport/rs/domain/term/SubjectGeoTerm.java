package com.ebsco.platform.shared.cmsimport.rs.domain.term;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubjectGeoTerm extends BasicTerm {

    public static final String CONTENT_TYPE_UID = "subject_geo_term";

    @JsonProperty(value = "subject_geo_term")
    private String subjectGeoTerm;

    public SubjectGeoTerm(String subjectGeoTerm) {
        super(subjectGeoTerm);
        this.subjectGeoTerm = subjectGeoTerm;
    }

    @Override
    public String getContentTypeUid() {
        return CONTENT_TYPE_UID;
    }
}
