package com.ebsco.platform.shared.cmsimport.rs.domain;


import java.time.LocalDate;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper = true)
public class TitleSource extends ContentType {
	
	public static final String CONTENT_TYPE_UID = "book_source";
	
	private String title;
	@JsonProperty(value = "book_title")
	private String bookTitle;
	@JsonProperty(value = "book_series_id")
	private String bookSeriesId;
	private String publisher;
	private String mid;
	private String isbn;
	@JsonProperty(value = "series_id_xml")
	private String seriesIdXml;
	private Set<ContentTypeReference<Product>> products;
	@JsonProperty(value = "publication_date")
	private LocalDate publicationDate;
	private String source;
	@JsonProperty(value = "doc_type")
	private String docType;
	@JsonProperty(value = "book_note")
	private String bookNote;
	@JsonProperty(value = "publication_year")
	private Integer publicationYear;
	@JsonProperty(value = "publisher_location")
	private String publisherLocation;
	@JsonProperty(value = "copyright_statement")
	private String copyrightStatement;
	@JsonProperty(value = "copyright_holder")
	private String copyrightHolder;
	@JsonProperty(value = "date_added")
	private LocalDate dateAdded;
	@JsonProperty(value = "dataFormat")
	private String dataFormat;
	private String rights;
	@JsonProperty(value = "is_new")
	private Boolean isNew;
	
	@Override
	public String getContentTypeUid() {
		return CONTENT_TYPE_UID;
	}
}
