package com.ebsco.platform.shared.cmsimport.rs.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder
@ToString(callSuper = true)
public class ArticleDefinition extends ContentType {
	
	public static final String CONTENT_TYPE_UID = "article_product_definition";
	
    private String title;

    private String an;

    @JsonProperty(value = "title_source")
    private Set<ContentTypeReference<TitleSource>> titleSource;

    private LocalDate dtformat;

	@Override
	public String getContentTypeUid() {
		return CONTENT_TYPE_UID;
	}
}
