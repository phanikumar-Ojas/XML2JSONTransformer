package com.ebsco.platform.shared.cmsimport.rs.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@ToString(callSuper = true)
public class ArticleCollection extends ContentType {
	
	public static final String CONTENT_TYPE_UID = "collection";
	
    private String title;

    @JsonProperty(value = "collection_title")
    private String collection_title;

    private String publisher;

    @JsonProperty(value = "series_id_xml")
    private String seriesIdXml;

    @JsonProperty(value = "publication_date")
    private LocalDate publicationDate;

    @JsonProperty(value = "publication_year")
    private int publicationYear;

    @JsonProperty(value = "publication_location")
    private String publicationLocation;

    @JsonProperty(value = "copyright_statement")
    private String copyrightStatement;

    @JsonProperty(value = "copyright_holder")
    private String copyrightHolder;

    private String rights;

    @JsonProperty(value = "article_count")
    private int articleCount;

    @JsonProperty(value = "old_book_title")
    private String oldBookTitle;

    @JsonProperty(value = "reading_level")
    private String readingLevel;

    private String tags;

	@Override
	public String getContentTypeUid() {
		return CONTENT_TYPE_UID;
	}
}
