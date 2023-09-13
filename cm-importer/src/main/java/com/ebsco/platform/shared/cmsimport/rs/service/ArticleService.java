package com.ebsco.platform.shared.cmsimport.rs.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import com.ebsco.platform.shared.cmsimport.rs.domain.Article;
import com.ebsco.platform.shared.cmsimport.rs.domain.ArticleCollection;
import com.ebsco.platform.shared.cmsimport.rs.domain.ArticleDefinition;
import com.ebsco.platform.shared.cmsimport.rs.domain.ContentTypeReference;
import com.ebsco.platform.shared.cmsimport.rs.domain.Contributor;
import com.ebsco.platform.shared.cmsimport.rs.domain.Product;
import com.ebsco.platform.shared.cmsimport.rs.domain.term.PrimaryTerm;
import com.ebsco.platform.shared.cmsimport.rs.domain.term.SearchTermSet;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.Topic;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBiography;
import com.ebsco.platform.shared.cmsimport.rs.repository.ArticleRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.NumCode2ProductBinder;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository repository;

    public EntriesBuilder entries(String... articleId) {
        return new EntriesBuilder(articleId);
    }
    
    public class EntriesBuilder {
        
        private String[] onlyThisArticleIds;
        
        private Collection<Product> products = Collections.emptyList();
        private Collection<ArticleDefinition> articleDefinitions = Collections.emptyList();
        private Collection<ArticleCollection> articleCollections = Collections.emptyList();
        private Collection<SearchTermSet> searchTermSets  = Collections.emptyList();
        
        private Map<String, Set<Contributor>> articleId2contributors = Collections.emptyMap();
        private Map<String, Topic> articleId2Topic = Collections.emptyMap();
        
        public EntriesBuilder(String ...onlyThisArticleIds) {
            this.onlyThisArticleIds = onlyThisArticleIds;
        }
        
        public Collection<Article> get() {
            log.info("Requesting Article from DB");
            
            NumCode2ProductBinder productBinder = new NumCode2ProductBinder(products);
            Collection<Article> articles = repository.find(productBinder, onlyThisArticleIds);
            log.info("Found {} items", articles.size());
            
            Map<String, ArticleDefinition> mfsAn2ArticleDefinition = articleDefinitions.stream().collect(
                    Collectors.toMap(ArticleDefinition::getAn, Function.identity()));
            Map<String, ArticleCollection> bookId2ArticleCollection = articleCollections.stream().collect(
                    Collectors.toMap(ArticleCollection::getTitle, Function.identity()));
            Map<String, SearchTermSet> articleId2SearchTearmSet = searchTermSets.stream().collect(
                    Collectors.toMap(SearchTermSet::getArticleId, Function.identity()));
            
            List<Article> articlesHavingParent = new ArrayList<>();
            for (Article article : articles) {
                if (Objects.nonNull(article.getParentArticleId())) {
                    Article parent = articles.stream().filter(a -> article.getParentArticleId().equals(a.getArticleId())).findFirst().orElse(null);
                    if (Objects.nonNull(parent)) {
                        addArticleDefinition(parent, article.getMfsAn(), mfsAn2ArticleDefinition);
                        addArticleCollection(parent, article.getBookId(), bookId2ArticleCollection);
                        copyProducts(article, parent);
                        addSearchTermSet(parent, article.getArticleId(), articleId2SearchTearmSet);
                        articlesHavingParent.add(article);
                    }
                }
            }
            
            log.info("{} articles having parent, merge and remove...", articlesHavingParent.size());
            
            articles.removeAll(articlesHavingParent);
            
            log.info("remains {} articles to import", articles.size());
            Map<String, PrimaryTerm> title2WhoIsTerm  = new HashMap<>();
            for (Article article : articles) {
                addArticleDefinition(article, article.getMfsAn(), mfsAn2ArticleDefinition);
                addArticleCollection(article, article.getBookId(), bookId2ArticleCollection);
                addSearchTermSet(article, article.getArticleId(), articleId2SearchTearmSet);
                
                Topic topic = articleId2Topic.get(article.getArticleId());
                if (Objects.nonNull(topic)) {
                    ContentTypeReference<Topic> topicRef = new ContentTypeReference<>(topic);
                    article.setTopic(Collections.singletonList(topicRef));
                    if (topic instanceof TopicBiography) {
                        addWhoIsPrimaryTerm(article, title2WhoIsTerm);
                    }
                }
                
                Set<Contributor> contributors = articleId2contributors.get(article.getArticleId());
                if (Objects.nonNull(contributors)) {
                    List<ContentTypeReference<Contributor>> contributorRefs = 
                            contributors.stream().map(contributor -> new ContentTypeReference<>(contributor)).toList();
                    article.setContributors(contributorRefs);
                }
            }
            
            return articles;
        }
        
        public EntriesBuilder products(Collection<Product> products) {
            this.products = products;
            return this;
        }
        
        public EntriesBuilder articleDefinitions(Collection<ArticleDefinition> articleDefinitions) {
            this.articleDefinitions = articleDefinitions;
            return this;
        }
        
        public EntriesBuilder articleCollections(Collection<ArticleCollection> articleCollections) {
            this.articleCollections = articleCollections;
            return this;
        }
        
        public EntriesBuilder searchTermSets(Collection<SearchTermSet> searchTermSets) {
            this.searchTermSets = searchTermSets;
            return this;
        }
        
        public EntriesBuilder contributors(Map<String, Set<Contributor>> articleId2contributors) {
            this.articleId2contributors = articleId2contributors;
            return this;
        }
        
        public EntriesBuilder topic(Map<String, Topic> articleId2Topic) {
            this.articleId2Topic = articleId2Topic;
            return this;
        }
        
        private static void addArticleDefinition(Article article, String mfsAn, Map<String, ArticleDefinition> an2ArticleDefinition) {
            ArticleDefinition toAdd = an2ArticleDefinition.get(mfsAn);
            if (Objects.isNull(toAdd)) {
                return;
            }
            List<ContentTypeReference<ArticleDefinition>> articleDefinitions = article.getArticleDefinitions();
            if (Objects.isNull(articleDefinitions)) {
                article.setArticleDefinitions(articleDefinitions = new ArrayList<>());
            }
            
            ContentTypeReference<ArticleDefinition> found = articleDefinitions.stream().filter(
                    ref -> ref.getReferable().getAn().equals(mfsAn)).findFirst().orElse(null);
            if (Objects.isNull(found)) {
                ContentTypeReference<ArticleDefinition> ref = new ContentTypeReference<>(toAdd);
                articleDefinitions.add(ref);
            }
        }
        
        private static void addArticleCollection(Article article, String bookId, Map<String, ArticleCollection> bookId2ArticleCollection) {
            ArticleCollection toAdd = bookId2ArticleCollection.get(bookId);
            if (Objects.isNull(toAdd)) {
                return;
            }
            
            List<ContentTypeReference<ArticleCollection>> articleCollections = article.getCollections();
            if (Objects.isNull(articleCollections)) {
                article.setCollections(articleCollections = new ArrayList<>());
            }
            
            ContentTypeReference<ArticleCollection> found = articleCollections.stream().filter(
                    ref -> ref.getReferable().getTitle().equals(bookId)).findFirst().orElse(null);
            if (Objects.isNull(found)) {
                ContentTypeReference<ArticleCollection> ref = new ContentTypeReference<>(toAdd);
                articleCollections.add(ref);
            }
        }
        
        private static void addSearchTermSet(Article article, String articleId, Map<String, SearchTermSet> articleId2SearchTearmSet) {
            SearchTermSet toAdd = articleId2SearchTearmSet.get(articleId);
            if (Objects.isNull(toAdd)) {
                return;
            }
            
            Collection<ContentTypeReference<SearchTermSet>> searchTermSets = article.getSearchTermSet();
            if (Objects.isNull(searchTermSets)) {
                article.setSearchTermSet(searchTermSets = new ArrayList<>());
            }
            
            ContentTypeReference<SearchTermSet> found = searchTermSets.stream().filter(
                    ref -> ref.getReferable().getArticleId().equals(articleId)).findFirst().orElse(null);
            if (Objects.isNull(found)) {
                ContentTypeReference<SearchTermSet> ref = new ContentTypeReference<>(toAdd);
                searchTermSets.add(ref);
            }
        }
        
        private static void addWhoIsPrimaryTerm(Article article, Map<String, PrimaryTerm> title2WhoIsTerm) {
            final String termTitle = "who is " + article.getTitle();
            PrimaryTerm toAdd = title2WhoIsTerm.get(termTitle);
            if (Objects.isNull(toAdd)) {
                toAdd = new PrimaryTerm(termTitle);
                title2WhoIsTerm.put(termTitle, toAdd);
            }
            
            Collection<ContentTypeReference<SearchTermSet>> searchTermSets = article.getSearchTermSet();
            if (Objects.nonNull(searchTermSets)) {
                for (ContentTypeReference<SearchTermSet> refSearchTermSet : searchTermSets) {
                    SearchTermSet searchTermSet = refSearchTermSet.getReferable();
                    List<ContentTypeReference<PrimaryTerm>> primaryTerms = searchTermSet.getPrimaryTerm();
                    if (Objects.isNull(primaryTerms)) {
                        searchTermSet.setPrimaryTerm(primaryTerms = new ArrayList<>());
                    }
                    PrimaryTerm found = primaryTerms.stream().map(ref -> ref.getReferable()).filter(
                            pTerm -> pTerm.getPrimaryTerm().equals(termTitle)).findFirst().orElse(null);
                    if (Objects.isNull(found)) {
                        ContentTypeReference<PrimaryTerm> ref = new ContentTypeReference<>(toAdd);
                        primaryTerms.add(ref);
                    }
                }
            }
        }
        
        private static void copyProducts(Article from, Article to) {
            if (Objects.isNull(from) || Objects.isNull(to)) {
                return;
            }
            
            List<ContentTypeReference<Product>> productsToCopy = from.getProducts();
            if (CollectionUtils.isEmpty(productsToCopy)) {
                return;
            }
            
            List<ContentTypeReference<Product>> whereCopyTo = to.getProducts();
            
            if (Objects.isNull(whereCopyTo)) {
                to.setProducts(whereCopyTo = new ArrayList<>());
            }
            
            for (ContentTypeReference<Product> productToCopy : productsToCopy) {
                ContentTypeReference<Product> found = whereCopyTo.stream().filter(
                        ref -> ref.getReferable().getTitle().equals(productToCopy.getReferable().getTitle())).findFirst().orElse(null);
                if (Objects.isNull(found)) {
                    whereCopyTo.add(productToCopy);
                }
            }
        }
    }
}
