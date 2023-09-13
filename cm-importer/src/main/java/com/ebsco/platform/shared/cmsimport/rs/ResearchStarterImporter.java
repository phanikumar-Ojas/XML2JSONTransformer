package com.ebsco.platform.shared.cmsimport.rs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.ebsco.platform.shared.cmsimport.rs.domain.Article;
import com.ebsco.platform.shared.cmsimport.rs.domain.ArticleCollection;
import com.ebsco.platform.shared.cmsimport.rs.domain.ArticleDefinition;
import com.ebsco.platform.shared.cmsimport.rs.domain.Contributor;
import com.ebsco.platform.shared.cmsimport.rs.domain.Product;
import com.ebsco.platform.shared.cmsimport.rs.domain.TitleSource;
import com.ebsco.platform.shared.cmsimport.rs.domain.term.BasicTerm;
import com.ebsco.platform.shared.cmsimport.rs.domain.term.SearchTermSet;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.Topic;
import com.ebsco.platform.shared.cmsimport.rs.repository.ArticleCollectionRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.ArticleDefinitionRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.ArticleRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.DatabaseClient;
import com.ebsco.platform.shared.cmsimport.rs.repository.MediaRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.TitleSourceRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.term.AltMainTermRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.term.BenchmarkRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.term.PrimaryRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.term.SearchTermSetRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.term.SubjectGeoTermRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.term.SubjectRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicBiographyRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicBusinessRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicEducationRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicHealthRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicHistoryRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicLiteratureRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicScienceRepository;
import com.ebsco.platform.shared.cmsimport.rs.repository.topic.TopicSocialScienceRepository;
import com.ebsco.platform.shared.cmsimport.rs.service.ArticleCollectionService;
import com.ebsco.platform.shared.cmsimport.rs.service.ArticleDefinitionService;
import com.ebsco.platform.shared.cmsimport.rs.service.ArticleService;
import com.ebsco.platform.shared.cmsimport.rs.service.ContributorService;
import com.ebsco.platform.shared.cmsimport.rs.service.JsonWriter;
import com.ebsco.platform.shared.cmsimport.rs.service.MediaService;
import com.ebsco.platform.shared.cmsimport.rs.service.ProductService;
import com.ebsco.platform.shared.cmsimport.rs.service.TestJsonFileWriter;
import com.ebsco.platform.shared.cmsimport.rs.service.TitleSourceService;
import com.ebsco.platform.shared.cmsimport.rs.service.api.CascadeContentTypeLoader;
import com.ebsco.platform.shared.cmsimport.rs.service.api.ContentTypeApi;
import com.ebsco.platform.shared.cmsimport.rs.service.term.BasicTermService;
import com.ebsco.platform.shared.cmsimport.rs.service.term.SearchTermSetService;
import com.ebsco.platform.shared.cmsimport.rs.service.topic.TopicBiographyService;
import com.ebsco.platform.shared.cmsimport.rs.service.topic.TopicLiteratureService;
import com.ebsco.platform.shared.cmsimport.rs.service.topic.TopicService;
import com.ebsco.platform.shared.cmsimport.rs.util.CommonUtil;
import com.ebsco.platform.shared.cmsimport.rs.util.ContentTypeJsonSchemaValidator;
import com.ebsco.platform.shared.cmsimport.rs.util.DurationMeter;
import com.ebsco.platform.shared.cmsimport.rs.util.HttpClientUtil;
import com.ebsco.platform.shared.cmsimport.rs.xml.ContributorXmlReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class ResearchStarterImporter {
	
	private static final JsonWriter JSON_WRITER = new TestJsonFileWriter();
	
	private final DatabaseClient dbClient;
	private final ContentTypeApi api;
	private final CascadeContentTypeLoader contentTypeLoader;
	
	public static void main(String[] args) {
	    ResearchStarterImporter importer = new ResearchStarterImporter(new DatabaseClient(), new ContentTypeApi(), new CascadeContentTypeLoader(new ContentTypeApi())
	            .jsonWriter(JSON_WRITER));
	    try {
	        importer.importToContentstack();
	    } catch (Throwable e) {
	        log.error("", e);
	    }
	    
	}
	
	public void importToContentstack(String... articleId) {
	    log.info("Research Starter Import START...");
	    log.info(ArrayUtils.toString(articleId));
	    
	    DurationMeter time = DurationMeter.start();
        
	    JsonWriter topicFileWriter = new TestJsonFileWriter();
        Map<String, Topic> articleId2Topic = new LinkedHashMap<>();
        articleId2Topic.putAll(new TopicBiographyService(new TopicBiographyRepository(dbClient)).jsonWriter(topicFileWriter).entries(articleId));
        articleId2Topic.putAll(new TopicLiteratureService(new TopicLiteratureRepository(dbClient)).jsonWriter(topicFileWriter).entries(articleId));
        articleId2Topic.putAll(new TopicService(new TopicBusinessRepository(dbClient)).jsonWriter(topicFileWriter).entries(articleId));
        articleId2Topic.putAll(new TopicService(new TopicEducationRepository(dbClient)).jsonWriter(topicFileWriter).entries(articleId));
        articleId2Topic.putAll(new TopicService(new TopicHealthRepository(dbClient)).jsonWriter(topicFileWriter).entries(articleId));
        articleId2Topic.putAll(new TopicService(new TopicHistoryRepository(dbClient)).jsonWriter(topicFileWriter).entries(articleId));
        articleId2Topic.putAll(new TopicService(new TopicScienceRepository(dbClient)).jsonWriter(topicFileWriter).entries(articleId));
        articleId2Topic.putAll(new TopicService(new TopicSocialScienceRepository(dbClient)).jsonWriter(topicFileWriter).entries(articleId));
        
        
        Map<String, Set<BasicTerm>> articleId2Term = new HashMap<>(new BasicTermService(
                new PrimaryRepository(dbClient)).entries());
        CommonUtil.mergeValues(articleId2Term, new BasicTermService(new AltMainTermRepository(dbClient))
                .entries());
        
        Map<String, Set<BasicTerm>> mfsAn2Term = new HashMap<>(new BasicTermService(
                new SubjectRepository(dbClient)).entries());
        CommonUtil.mergeValues(mfsAn2Term, new BasicTermService(new SubjectGeoTermRepository(dbClient)).entries());
        CommonUtil.mergeValues(mfsAn2Term, new BasicTermService(new BenchmarkRepository(dbClient)).entries());

        Collection<SearchTermSet> searchTearmSets = new SearchTermSetService(
                new SearchTermSetRepository(dbClient)).entries(mfsAn2Term, articleId2Term);
        
        Collection<ArticleCollection> articleCollections = new ArticleCollectionService(
                new ArticleCollectionRepository(dbClient)).entries();
        
        Map<String, Set<Contributor>> articleId2contributors = new ContributorService(new ContributorXmlReader()).entries();
        
        Collection<Product> products = new ProductService(api).importToContentstack();
        
        Collection<TitleSource> titleSources = new TitleSourceService(new TitleSourceRepository(dbClient))
                .entries(() -> products);
        
        Collection<ArticleDefinition> articleDefinitions = new ArticleDefinitionService(
                new ArticleDefinitionRepository(dbClient)).entries(() -> titleSources);
        
        Collection<Article> articles = new ArticleService(new ArticleRepository(dbClient))
                .entries(articleId)
                .products(products)
                .articleDefinitions(articleDefinitions)
                .articleCollections(articleCollections)
                .contributors(articleId2contributors)
                .searchTermSets(searchTearmSets)
                .topic(articleId2Topic)
                .get();
        
        MediaService mediaService = new MediaService(new MediaRepository(dbClient));
        
        contentTypeLoader.medias(mediaService.entries());
        
        for (Article article : articles) {
            contentTypeLoader.sendToContentstack(article);
        }
        
        Map<String, Set<Object>> invalidValuesInfos = ContentTypeJsonSchemaValidator.getInvalidValuesInfo();
        log.error("Found {} Json schema errors", invalidValuesInfos.size());
        log.error("Building json schema report ...");
        String jsonSchemaValidationReport = String.format("\nJSON SCHEMA VALIDATION REPORT: (%s)\n", invalidValuesInfos.size());
        for (Map.Entry<String, Set<Object>> record : invalidValuesInfos.entrySet()) {
            Set<Object> entryIdentifiers = record.getValue();
            if (Objects.nonNull(entryIdentifiers)) {
                jsonSchemaValidationReport += record.getKey() +"   (" + String.join(", ", entryIdentifiers.stream().map(String::valueOf).limit(10).toList()) + ")  total entries: "+entryIdentifiers.size()+"\n";
            }
        }
        log.error(jsonSchemaValidationReport);
        log.error("ok");
        
        Collection<String> filenames = contentTypeLoader.getUnknownAssets();
        log.error("Building asset files report ...");
        String assetFilesReport = "\nUnknown asset files: "+filenames.size()+ "\n";
        for (String record : filenames) {
            assetFilesReport += record + "\n";
        }
        log.error(assetFilesReport);
        
        Map<String, Integer> entryCounts = contentTypeLoader.getEntryCounts();
        log.error("Building entry counts report ...");
        String entryCountsReport = "\nENTRY COUNTS:\n";
        for (Map.Entry<String, Integer> record : entryCounts.entrySet()) {
            entryCountsReport += record.getKey() +"="+record.getValue()+"\n";
        }
        
        int total = 0;
        for (int contentTypeCount : entryCounts.values()) {
            total += contentTypeCount;
        }
        entryCountsReport += "\n";
        entryCountsReport += String.format("Rate limits exceesed: %s\n", HttpClientUtil.wasRatesLimitExceesed());
        entryCountsReport += String.format("TOTAL ENTRIES: %s\n", total);
        entryCountsReport += String.format("TIME TOOK: %s\n", time.took());
        
        log.info(entryCountsReport);
        log.info("ok");
        
        log.info("Research Starter Import COMPLETE.");
	}
}
