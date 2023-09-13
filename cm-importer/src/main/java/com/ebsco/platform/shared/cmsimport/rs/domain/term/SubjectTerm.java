package com.ebsco.platform.shared.cmsimport.rs.domain.term;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubjectTerm extends BasicTerm {

    public static final String CONTENT_TYPE_UID = "subject_term";

    @JsonProperty(value = "subject_term")
    private String subjectTerm;

    public SubjectTerm(String subjectTerm) {
        super(subjectTerm);
        this.subjectTerm = subjectTerm;
    }

    @Override
    public String getContentTypeUid() {
        return CONTENT_TYPE_UID;
    }
}
