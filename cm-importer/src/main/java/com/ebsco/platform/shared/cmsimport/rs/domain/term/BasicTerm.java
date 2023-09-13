package com.ebsco.platform.shared.cmsimport.rs.domain.term;

import com.ebsco.platform.shared.cmsimport.rs.domain.ContentType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public abstract class BasicTerm extends ContentType {

    private String title;

    public BasicTerm(String title) {
        this.title = title;
    }
}
