package com.ebsco.platform.shared.cmsimport.rs.domain.term;

import com.ebsco.platform.shared.cmsimport.rs.domain.ContentType;
import com.ebsco.platform.shared.cmsimport.rs.domain.ContentTypeReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
public class SearchTermSet extends ContentType {

    public static final String CONTENT_TYPE_UID = "search_term_set";

    private String title;

    @JsonProperty(value = "primary_term")
    private List<ContentTypeReference<PrimaryTerm>> primaryTerm;

    @JsonProperty(value = "alt_term")
    private List<ContentTypeReference<AltMainTerm>> altTerm;

    @JsonProperty(value = "subject_term")
    private List<ContentTypeReference<SubjectTerm>> subjectTerm;

    @JsonProperty(value = "benchmark_term")
    private List<ContentTypeReference<BenchmarkTerm>> benchmarkTerm;

    @JsonProperty(value = "subject_geo_term")
    private List<ContentTypeReference<SubjectGeoTerm>> subjectGeoTerm;

    @JsonProperty(value = "primary_article_title_term")
    private List<ContentTypeReference<PrimaryTerm>> primaryArticleTitleTerm;

    @JsonProperty(value = "article_an")
    private Long articleAn;

    @JsonProperty(value = "article_title")
    private String articleTitle;

    @JsonProperty(value = "page_number")
    private Integer pageNumber;

    private Integer lexile;

    @JsonProperty(value = "doc_type")
    private String docType;

    @JsonProperty(value = "article_type")
    private String articleType;

    private Integer words;

    private Set<String> tags;

    @JsonIgnore
    private String articleId;

    @Override
    public String getContentTypeUid() {
        return CONTENT_TYPE_UID;
    }
}
