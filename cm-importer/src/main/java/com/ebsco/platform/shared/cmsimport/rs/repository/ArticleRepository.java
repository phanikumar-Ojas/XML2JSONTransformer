package com.ebsco.platform.shared.cmsimport.rs.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.ebsco.platform.shared.cmsimport.rs.domain.Article;
import com.ebsco.platform.shared.cmsimport.rs.domain.Audience;
import com.ebsco.platform.shared.cmsimport.rs.domain.ContentTypeReference;
import com.ebsco.platform.shared.cmsimport.rs.domain.FieldName;
import com.ebsco.platform.shared.cmsimport.rs.domain.Product;
import com.ebsco.platform.shared.cmsimport.rs.util.DataUtil;
import com.ebsco.platform.shared.cmsimport.rs.xml.article.ArticleXmlParser;
import com.ebsco.platform.shared.cmsimport.rs.xml.article.XmlReader;
import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class ArticleRepository {

    private static final String ROOT_ARTICLE_XML_FOLDER_PATH = AppPropertiesUtil.getProperty("ARTICLE_XML_FOLDER");

    private static final String QUERY = """
            SELECT article_title, word_count, ta.article_id, date_orig, review_date, update_date, brst_topic, brst_category,
            		search_title, xml_version_ui, last_update_type, data_type, primary_article, date_repository_updated,
            		date_in_repository, derived_from_id, ta.rs_title, ta.date_added, update_cycle, source_note, status,
            		copied_to_build, updated_build, do_not_use, usage_note, article_an, mfs_an, ta.book_id, ta.rights, art_type, parent_article_id,
            		secondary_preferred, consumer_preferred, corporate_preferred, academic_preferred, pa.product
            FROM t_articles ta, t_books tb, t_product_assignment pa
            WHERE ta.book_id = tb.book_id and ta.mfs_an = pa.an and ta.do_not_use = 'FALSE' and tb.mid in ('GY4R', 'GY4P', 'HDMJ', 'GYF8', 'GY4S', 'GY4Q')
                        """;

    private final DatabaseClient databaseClient;

    public Collection<Article> find(ReferenceBinder<String, Product> productBinder, String...onlyThisArticleId) {
        log.info("Reading articles from db ...");
        try (Statement statement = databaseClient.getConnection().createStatement()) {
            ResultSet rs = statement.executeQuery(QUERY);
            
            Set<String> knownMfsAns = new HashSet<>();
            
            Map<String, Article> articleId2Article = new HashMap<>();
            Map<String, Set<String>> articleId2ProductCodes = new HashMap<>();
            while (rs.next()) {
                knownMfsAns.add(rs.getString("mfs_an"));
                String articleId = rs.getString("article_id");
                
                if (onlyThisArticleId.length > 0 && !ArrayUtils.contains(onlyThisArticleId, articleId)) {
                    continue;
                }
                
                String productCode = rs.getString("product");
                Set<String> productCodes = articleId2ProductCodes.get(articleId);
                if (Objects.isNull(productCodes)) {
                    articleId2ProductCodes.put(articleId, productCodes = new HashSet<>());
                }
                if (Objects.nonNull(articleId2Article)) {
                    productCodes.add(productCode);
                } else {
                    log.info("Product is null.");
                }
                
                Article article = articleId2Article.get(articleId);
                if (Objects.isNull(article)) {
                    article = getArticleFromResultSet(rs);
                    articleId2Article.put(articleId, article);
                }
                if ("2".equals(productCode)) {
                    article.addAudience(Audience.T5O_PRODUCT_PREFERRED);
                }
            }
            
            Collection<Article> articles = articleId2Article.values();
            log.info("Created {} article entries.", articles.size());
            log.info("Set products ...");
            for (Article article : articles) {
                Set<String> productCodes = articleId2ProductCodes.get(article.getArticleId());
                List<ContentTypeReference<Product>> productRefs = new ArrayList<>();
                for (String productCode : productCodes) {
                    Product product = productBinder.bind(productCode);
                    if (Objects.nonNull(articles)) {
                        ContentTypeReference<Product> ref = new ContentTypeReference<>(product);
                        productRefs.add(ref);
                    }
                }
                if (!productRefs.isEmpty()) {
                    article.setProducts(productRefs);
                }
            }
            log.info("ok");
            log.info("Extract xml fields...");
            int count = 1;
            for (Article article : articles) {
                log.info("({} of {}) : parse xml : {}", count++, articles.size(), article.getArticleId());
                XmlReader xmlReader = new XmlReader(ROOT_ARTICLE_XML_FOLDER_PATH + "/"
                        + article.getFilaName());
                ArticleXmlParser.extractFieldsFromXml(article, xmlReader, knownMfsAns);
            }
            log.info("ok");
            return articles;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Article getArticleFromResultSet(ResultSet rs) throws SQLException {
        LocalDate dateCreated = DataUtil.from(rs.getString(FieldName.ArticleField.DATE_CREATED));
        LocalDate reviewDate = DataUtil.from(rs.getString(FieldName.ArticleField.UPDATE_DATE));
        LocalDate updateDate = DataUtil.from(rs.getString(FieldName.ArticleField.REVIEW_DATE));
        LocalDate dateInRepoUpdated = DataUtil.from(rs.getString(FieldName.ArticleField.DATE_IN_REPOSITORY_UPDATED));
        LocalDate dateInRepo = DataUtil.from(rs.getString(FieldName.ArticleField.DATE_IN_REPOSITORY));
        LocalDate dateAdded = DataUtil.from(rs.getString(FieldName.ArticleField.DATE_ADDED));
        LocalDate copiedToBuild = DataUtil.from(rs.getString(FieldName.ArticleField.COPIED_TO_BUILD),
                DataUtil.M_D_YYYY_FORMATTER);
        LocalDate updatedBuild = DataUtil.from(rs.getString(FieldName.ArticleField.UPDATED_BUILD),
                DataUtil.M_D_YYYY_FORMATTER);
        String articleId = rs.getString(FieldName.ArticleField.ARTICLE_ID);
        Article article = Article.builder()
                .title(rs.getString(FieldName.ArticleField.ARTICLE_TITLE))
                .wordCount(rs.getInt(FieldName.ArticleField.WORD_COUNT))
                .articleId(articleId)
                .dateCreated(dateCreated)
                .reviewDate(reviewDate)
                .lastUpdatedDate(updateDate)
                .collectionTopic(rs.getString(FieldName.ArticleField.BRST_TOPIC))
                .searchTitle(rs.getString(FieldName.ArticleField.SEARCH_TITLE))
                .xmlVersionUi(rs.getString(FieldName.ArticleField.XML_VERSION_UI))
                .lastUpdateType(listOrNull(rs.getString(FieldName.ArticleField.LAST_UPDATE_TYPE)))
                .dataType(rs.getString(FieldName.ArticleField.DATA_TYPE))
                .primaryArticle(rs.getString(FieldName.ArticleField.PRIMARY_ARTICLE))
                .dateInRepoUpdated(dateInRepoUpdated)
                .dateInRepo(dateInRepo)
                .reused(rs.getString(FieldName.ArticleField.DERIVED_FROM_ID))
                .rsTitle(rs.getString(FieldName.ArticleField.RS_TITLE))
                .dateAdded(dateAdded)
                .updateCycle(rs.getString(FieldName.ArticleField.UPDATE_CYCLE))
                .sourceNote(rs.getString(FieldName.ArticleField.SOURCE_NOTE))
                .status(rs.getString(FieldName.ArticleField.STATUS))
                .copiedToBuild(copiedToBuild)
                .updatedBuild(updatedBuild)
                .doNotUse(rs.getBoolean(FieldName.ArticleField.DO_NOT_USE))
                .usageNote(rs.getString(FieldName.ArticleField.USAGE_NOTE))
                .articleAn(rs.getString(FieldName.ArticleField.ARTICLE_AN))
                .mfsAn(rs.getString("mfs_an"))
                .bookId(rs.getString("book_id"))
                .rights(rs.getString(FieldName.ArticleField.RIGHTS))
                .artTopic(rs.getString(FieldName.ArticleField.ART_TYPE))
                .parentArticleId(rs.getString(FieldName.ArticleField.PARENT_ARTICLE_ID))
                .filaName(articleId + ".xml")
                .build();
        setAudience(rs, article);
        String brstCategoryTag = rs.getString(FieldName.ArticleField.BRST_CATEGORY);
        if (brstCategoryTag != null && !brstCategoryTag.isBlank()) {
            article.addTag(brstCategoryTag);
        }
        return article;
    }
    
    private static List<String> listOrNull(String value) {
        if (Objects.isNull(value)) {
            return null;
        }
        return List.of(value);
    }

    private void setAudience(ResultSet rs, Article article) throws SQLException {
        setAudienceFromFlag(FieldName.ArticleField.SECONDARY_PREFERRED, rs, article);
        setAudienceFromFlag(FieldName.ArticleField.CONSUMER_PREFERRED, rs, article);
        setAudienceFromFlag(FieldName.ArticleField.CORPORATE_PREFERRED, rs, article);
        setAudienceFromFlag(FieldName.ArticleField.ACADEMIC_PREFERRED, rs, article);
    }

    private void setAudienceFromFlag(String audiencePreferredName, ResultSet rs, Article article) throws SQLException {
        if (rs.getBoolean(audiencePreferredName)) {
            article.addAudience(Audience.of(audiencePreferredName));
        }
    }

}
