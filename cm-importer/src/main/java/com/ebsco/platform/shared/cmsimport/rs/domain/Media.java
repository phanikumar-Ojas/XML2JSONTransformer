package com.ebsco.platform.shared.cmsimport.rs.domain;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString(callSuper = true)
public class Media extends ContentType {
	
	public static final String CONTENT_TYPE_UID = "image";
	
	private String title;

    @JsonProperty(value = "image_file")
    private String imageFile;
    
    @JsonProperty(value = "vendor_source")
    private String vendorSource;
    
    private String rights;
    
    @JsonProperty(value = "copyright_notes")
    private JsonRTE copyrightNotes;
    
    private JsonRTE license;
    private JsonRTE caption;
    private JsonRTE credit;
    
    @JsonProperty(value = "content_type")
    private String contentType;
    
    @JsonProperty(value = "alt_text")
    private JsonRTE altText;
    
    @JsonProperty(value = "webpage_url")
    private Set<String> webpageUrl;
    
    @JsonProperty(value = "position_inline")
    private String positionInline;
    
    private String format;
    
    @JsonProperty(value = "placard_image")
    private Boolean placardImage;
    
    @JsonProperty(value = "not_placard")
    private Boolean notPlacard;
    
    @JsonProperty(value = "force_placard")
    private Boolean forcePlacard;

    @JsonIgnore
    private Asset asset;
	
	@Override
	public String getContentTypeUid() {
		return CONTENT_TYPE_UID;
	}
	
	@Setter
	@Getter
	public static class Asset {
	    private String uid;
	    private String filename;
	    private String url;
	    private String description;
	    private String contentType;
	}

}
