package com.ebsco.platform.shared.cmsimport.rs.service.topic;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.json.JSONException;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.Topic;
import com.ebsco.platform.shared.cmsimport.rs.domain.topic.TopicBiography;
import com.ebsco.platform.shared.cmsimport.rs.service.api.CascadeContentTypeLoader;
import com.ebsco.platform.shared.cmsimport.rs.service.api.ContentTypeApi;
import com.ebsco.platform.shared.cmsimport.rs.util.TestUtils;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.DuplicatesResolverPostProcessor;

public class TopicServiceIntegrationTest <T extends TopicService>{
	
    public static final String TEST_INTEG_TITLE_MARKER = "(testinteg)";
    
    private ContentTypeApi api = new ContentTypeApi().rewrite(true);
    private CascadeContentTypeLoader cascadeLoader = new CascadeContentTypeLoader(api);
    
    protected static final Function<String, String> TEST_INTEG_TITLE_VALUE_RESOLVER = title -> {
        if (title.startsWith("Charles Taylor")) {
            title = "Charles Taylor";
        }
        return addTestIntegMarker(title);
    };
    
	private T service;
	
	public TopicServiceIntegrationTest(T service) {
		this.service = service;
        this.service.setTitleValueResolver(TEST_INTEG_TITLE_VALUE_RESOLVER);
	}
	
	public TopicServiceIntegrationTest() {}

    public void test(String folder) throws JSONException{
		List<String> xmlFileNames = TestUtils.xmlFileNames(folder);
		test(folder, false, xmlFileNames.toArray(new String [0]));
	}
	
	public void test(String folder, boolean addBirthDateInTitle) throws JSONException{
        List<String> xmlFileNames = TestUtils.xmlFileNames(folder);
        test(folder, addBirthDateInTitle, xmlFileNames.toArray(new String [0]));
    }
	
	
	public void test(String folder, boolean addBirthDateInTitle, String ...articleId) throws JSONException {
		service.setXmlDocumentReader(new TestUtils.TestXmlDocumentReader(folder));
		
		String resultFolder = "/test-integ-result" + folder;
		Map<String, Topic> result = service.entries(articleId);
		Collection<Topic> entries = result.values();
		importToContentstack(entries);
		
		
        for (Topic topic : entries) {
		    topic.setTitle(removeTestIntegPrefix(topic.getTitle()));
		    String actualJson = TestUtils.toJSON(topic);
			
			String title = null;
			if (addBirthDateInTitle && (topic instanceof TopicBiography topicBiography)) {
			    title = DuplicatesResolverPostProcessor.biographyTitle(topicBiography);
			} else {
			    title = topic.getTitle();
			}
			
			String jsonFileName = title + ".json";
			jsonFileName = jsonFileName.replaceAll(":", "%3A");
			
			//TestUtils.writeStringToFile(Paths.get("D:\\workspaces\\0\\platform.shared.cms-import\\cm-importer\\src\\test\\resources", resultFolder, "tmp-" + jsonFileName), actualJson);
			
			String expectedJson = TestUtils.readResourceFile(resultFolder+"/" + jsonFileName);
			
			TestUtils.assertJsonEquals(expectedJson, actualJson);
		}
	}
	
	private void importToContentstack(Collection<Topic> entries){
	    for (Topic topic : entries) {
	        cascadeLoader.sendToContentstack(topic);
	    }
	}
	
	public T service() {
        return service;
    }

    private static String addTestIntegMarker(String title) {
	    return title = title+TEST_INTEG_TITLE_MARKER;
	}
	
	private static String removeTestIntegPrefix(String title) {
        return title.replace(TEST_INTEG_TITLE_MARKER, "");
    }
}
