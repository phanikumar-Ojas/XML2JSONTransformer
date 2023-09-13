package com.ebsco.platform.shared.cmsimport.rs.domain;

import lombok.Data;

@Data
public class TextStyleHolder {

    private Boolean bold;
    private Boolean italic;
    private Boolean code;
    private Boolean underline;
    private Boolean superscript;
    private Boolean subscript;
    private Boolean strikethrough;

}
