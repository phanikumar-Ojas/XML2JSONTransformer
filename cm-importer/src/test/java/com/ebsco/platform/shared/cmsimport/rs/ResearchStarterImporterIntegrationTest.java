package com.ebsco.platform.shared.cmsimport.rs;

import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ebsco.platform.shared.cmsimport.rs.domain.Article;
import com.ebsco.platform.shared.cmsimport.rs.domain.ArticleCollection;
import com.ebsco.platform.shared.cmsimport.rs.domain.ArticleDefinition;
import com.ebsco.platform.shared.cmsimport.rs.domain.Contributor;
import com.ebsco.platform.shared.cmsimport.rs.domain.Media;
import com.ebsco.platform.shared.cmsimport.rs.domain.TitleSource;
import com.ebsco.platform.shared.cmsimport.rs.domain.term.AltMainTerm;
import com.ebsco.platform.shared.cmsimport.rs.domain.term.PrimaryTerm;
import com.ebsco.platform.shared.cmsimport.rs.domain.term.SearchTermSet;
import com.ebsco.platform.shared.cmsimport.rs.domain.term.SubjectGeoTerm;
import com.ebsco.platform.shared.cmsimport.rs.domain.term.SubjectTerm;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBiography;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBusiness;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicEducation;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicHealth;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicHistory;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicLiterature;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicScience;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicSocialScience;
import com.ebsco.platform.shared.cmsimport.rs.repository.DatabaseClient;
import com.ebsco.platform.shared.cmsimport.rs.service.JsonWriter;
import com.ebsco.platform.shared.cmsimport.rs.service.TestJsonFileWriter;
import com.ebsco.platform.shared.cmsimport.rs.service.api.CascadeContentTypeLoader;
import com.ebsco.platform.shared.cmsimport.rs.service.api.ContentTypeApi;
import com.ebsco.platform.shared.cmsimport.rs.util.ConfigurationUtil;
import com.ebsco.platform.shared.cmsimport.rs.util.TestUtils;
import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;

@Ignore
public class ResearchStarterImporterIntegrationTest {
    
    private static final String XML_FOLDER_PATH = TestUtils.classpathResourceAbsolutePath("/test-integ/input/xml/");
    private static final String ASSET_FOLDER_PATH = TestUtils.classpathResourceAbsolutePath("/test-integ/input/asset/");
    private static final String CONTENTSTACK_ASSET_FOLDER_NAME = "rs-draft";
    
    private static final String[] CONTENT_TYPES = {
            Article.CONTENT_TYPE_UID,
            ArticleDefinition.CONTENT_TYPE_UID,
            TitleSource.CONTENT_TYPE_UID,
            ArticleCollection.CONTENT_TYPE_UID,
            Contributor.CONTENT_TYPE_UID,
            
            AltMainTerm.CONTENT_TYPE_UID,
            PrimaryTerm.CONTENT_TYPE_UID,
            SubjectGeoTerm.CONTENT_TYPE_UID,
            SubjectTerm.CONTENT_TYPE_UID,
            SearchTermSet.CONTENT_TYPE_UID,
            
            TopicBiography.CONTENT_TYPE_UID,
            TopicBusiness.CONTENT_TYPE_UID,
            TopicScience.CONTENT_TYPE_UID,
            TopicSocialScience.CONTENT_TYPE_UID,
            TopicEducation.CONTENT_TYPE_UID,
            TopicHealth.CONTENT_TYPE_UID,
            TopicHistory.CONTENT_TYPE_UID,
            TopicLiterature.CONTENT_TYPE_UID,
            
            Media.CONTENT_TYPE_UID
    };
    
    public static final String TITLE_MARKER = "(draft)";

    private static final JsonWriter JSON_WRITER = new TestJsonFileWriter(/*"D:\\workspaces\\0\\platform.shared.cms-import\\cm-importer\\src\\test\\resources\\test-integ\\output\\"*/);

    protected static final Function<String, String> TEST_INTEG_TITLE_VALUE_RESOLVER = title -> {
        if (StringUtils.contains(title, TITLE_MARKER)) {
           return title; 
        }
        title = title + TITLE_MARKER;
        return title;
    };
    
    @Test
    public void all() throws JSONException {
        ResearchStarterImporter importer = new ResearchStarterImporter(new DatabaseClient(), new ContentTypeApi(),
                new CascadeContentTypeLoader(new ContentTypeApi()).titleValueResolver(TEST_INTEG_TITLE_VALUE_RESOLVER)
                .jsonWriter(JSON_WRITER));
        
        List<String> xmlFileNames = ConfigurationUtil.fileNames(XML_FOLDER_PATH, "xml");
        
        importer.importToContentstack(xmlFileNames.toArray(new String [0]));
    }
    
    @BeforeClass
    public static void beforeClass() {
        setupTestFolders();
        cleanAll();
    }
    
    @AfterClass
    public static void afterClass() {
        //cleanAll();
    }
    
    private static void cleanAll() {
        for (String contentType : CONTENT_TYPES) {
            TestUtils.removeFromContentstack(contentType, TITLE_MARKER);
        }
    }
    
    private static void setupTestFolders() {
        Properties props = AppPropertiesUtil.loadProperties();
        props.setProperty("ARTICLE_XML_FOLDER", XML_FOLDER_PATH);
        props.setProperty("ASSET_FOLDER_PATH", ASSET_FOLDER_PATH);
        props.setProperty("CONTENTSTACK_ASSET_FOLDER_NAME", CONTENTSTACK_ASSET_FOLDER_NAME);
        
        AppPropertiesUtil.setProperties(props);
    }
}
