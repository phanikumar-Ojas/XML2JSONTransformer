package com.ebsco.platform.shared.cmsimport.rs.domain;

import com.ebsco.platform.shared.cmsimport.rs.domain.term.SearchTermSet;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.Topic;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@Builder
@JsonRootName(value = "entry")
public class Article extends ContentType {

    public static final String CONTENT_TYPE_UID = "article";

    private String title;

    @JsonProperty(value = "assigned_to")
    private String assignedTo;

    private String region;

    private Collection<ContentTypeReference<Contributor>> contributors;

    @JsonProperty(value = "associated_images")//TODO file
    private String associatedImages;

    @JsonProperty(value = "main_body")
    private JsonRTE mainBody;

    private JsonRTE citations;

    @JsonProperty(value = "color_picker_for_citations")//TODO
    private String colorPickerForCitations;

    @JsonProperty(value = "author_note")
    private JsonRTE authorNote;

    @JsonProperty(value = "abstract")
    private JsonRTE abstractValue;

    @JsonProperty(value = "custom_field")
    private String customField;

    @JsonProperty(value = "current_project")
    private ContentTypeReference currentProject;

    private Collection<ContentTypeReference<Topic>> topic;

    private Integer lexile;

    @JsonProperty(value = "editor_notes")
    private String editorNotes;

    @JsonProperty(value = "original_requested_word_count")
    private Integer originalRequestedWordCount;

    @JsonProperty(value = "word_count")
    private Integer wordCount;

    @JsonProperty(value = "task_hours")
    private Integer taskHours;

    @JsonProperty(value = "po_notes")
    private String poNotes;

    @JsonProperty(value = "editor_due_date")
    private LocalDate editorDueDate;

    @JsonProperty(value = "art_spec_summary")
    private String artSpecSummary;

    @JsonProperty(value = "research_starter_links_applied")
    private boolean rsLinksApplied;

    @JsonProperty(value = "bulk_project_image_placement")
    private String bulkProjectImagePlacement;

    @JsonProperty(value = "automatic_image_placement")
    private String automaticImagePlacement;

    @JsonProperty(value = "article_id")
    private String articleId;

    @JsonProperty(value = "writer_due_date")
    private LocalDate writerDueDate;

    @JsonProperty(value = "date_created")
    private LocalDate dateCreated;

    @JsonProperty(value = "review_date")
    private LocalDate reviewDate;

    @JsonProperty(value = "last_updated_date")
    private LocalDate lastUpdatedDate;

    private Set<Audience> audience;

    private boolean urgent;

    @JsonProperty(value = "indexed_by_a_i")
    private LocalDate indexedByAI;

    @JsonProperty(value = "name_record_created_date")
    private LocalDate nameRecordCreatedDate;

    @JsonProperty(value = "collection_debate_category")
    private String collectionDebateCategory;

    @JsonProperty(value = "article_work_type")
    private String articleWorkType;

    @JsonProperty(value = "image_status")
    private String imageStatus;

    private String url;

    @JsonProperty(value = "article_definitions")
    private List<ContentTypeReference<ArticleDefinition>> articleDefinitions;

    private List<ContentTypeReference<Product>> products;

    private List<ContentTypeReference<ArticleCollection>> collections;

    @JsonProperty(value = "collection_topic")
    private String collectionTopic;

    @JsonProperty(value = "search_title")
    private String searchTitle;

    @JsonProperty(value = "xml_version_ui")
    private String xmlVersionUi;

    @JsonProperty(value = "custom_pdf_only_no_full_text_")
    private boolean customPdfOnly;

    @JsonProperty(value = "current_update_type")
    private String currentUpdateType;

    @JsonProperty(value = "last_update_type")
    private List<String> lastUpdateType;

    @JsonProperty(value = "data_type")
    private String dataType;

    @JsonProperty(value = "primary_article")
    private String primaryArticle;

    @JsonProperty(value = "date_in_repository_updated")
    private LocalDate dateInRepoUpdated;

    @JsonProperty(value = "date_in_repository")
    private LocalDate dateInRepo;

    private String reused;

    @JsonProperty(value = "rs_title")
    private String rsTitle;

    @JsonProperty(value = "date_added")
    private LocalDate dateAdded;

    @JsonProperty(value = "update_cycle")
    private String updateCycle;

    @JsonProperty(value = "source_note")
    private String sourceNote;

    @JsonProperty(value = "search_term_set")
    private Collection<ContentTypeReference<SearchTermSet>> searchTermSet;

    private String status;

    @JsonProperty(value = "copied_to_build")
    private LocalDate copiedToBuild;

    @JsonProperty(value = "updated_build")
    private LocalDate updatedBuild;

    @JsonProperty(value = "do_not_use")
    private boolean doNotUse;

    @JsonProperty(value = "usage_note")
    private String usageNote;

    @JsonProperty(value = "article_an")
    private String articleAn;
    
    @JsonIgnore
    private String mfsAn;
    
    @JsonIgnore
    private String bookId;
    
    //@JsonIgnore
    //private String productCode;

    private String rights;

    @JsonProperty(value = "art_topic")
    private String artTopic;

    @JsonProperty(value = "past_projects")
    private List<ContentTypeReference> pastProjects;

    @JsonProperty(value = "data_pulls")
    private ContentTypeReference dataPulls;

    @JsonProperty(value = "parent_article_id")
    private String parentArticleId;

    private Set<String> tags;

    @JsonProperty(value = "table_in_main_body")
    private boolean tableInMainBody;

    @JsonProperty(value = "adhoc_terms")
    private String adhocTerms;

    @JsonIgnore
    private String filaName;

    public void addAudience(Audience audience) {
        if (this.audience == null) {
            this.audience = new HashSet<>();
        }
        this.audience.add(audience);
    }

    @Override
    public String getContentTypeUid() {
        return CONTENT_TYPE_UID;
    }

    public void addTag(String tag) {
        if ((tags == null)) {
            tags = new HashSet<>();
        }
        tags.add(tag);
    }
}
