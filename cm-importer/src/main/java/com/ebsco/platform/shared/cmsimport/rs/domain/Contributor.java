package com.ebsco.platform.shared.cmsimport.rs.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

@Builder
@Getter
@Setter
@ToString(callSuper = true)
public class Contributor extends ContentType {
	
	public static final String CONTENT_TYPE_UID = "contributor";

    private String title;

    @JsonProperty(value = "first_name")
    private String firstName;

    @JsonProperty(value = "last_name")
    private String lastName;

    @JsonProperty(value = "full_name")
    private String fullName;

    private String degrees;

    private String affiliated;

    private String prefix;

    private String suffix;

    @JsonProperty(value = "middle_name")
    private String middleName;

    @JsonProperty(value = "preferred_byline")
    private String preferredByline;

    @JsonProperty(value = "short_biography")
    private String shortBiography;

    @JsonProperty(value = "related_fields")
    private String relatedFields;

    @JsonProperty(value = "long_biography")
    private String longBiography;

    @JsonProperty(value = "contributor_line")
    private String contributorLone;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Contributor that = (Contributor) o;
        return title.equals(that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }

	@Override
	public String getContentTypeUid() {
		return CONTENT_TYPE_UID;
	}
}

