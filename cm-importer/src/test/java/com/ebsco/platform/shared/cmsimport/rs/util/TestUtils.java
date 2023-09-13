package com.ebsco.platform.shared.cmsimport.rs.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.skyscreamer.jsonassert.JSONAssert;

import com.ebsco.platform.shared.cmsimport.rs.config.HTMLCharacterEscapes;
import com.ebsco.platform.shared.cmsimport.rs.service.api.ContentTypeApi;
import com.ebsco.platform.shared.cmsimport.rs.service.topic.TopicServiceIntegrationTest;
import com.ebsco.platform.shared.cmsimport.rs.xml.topic.TopicXmlDataEnricher.XmlDocumentReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class TestUtils {
	
	private static final String UUID_REGEXP = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}").pattern();
	private static final String UID_REGEXP = Pattern.compile("blt[0-9a-f]{16}").pattern();
	private static final int HTTP_GET_PAGE_SIZE = 100;
	
	private static final ObjectMapper JACKSON = configureMapper();
	
	private static ObjectMapper configureMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.getFactory().setCharacterEscapes(new HTMLCharacterEscapes());
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        
        return objectMapper;
    }
	
	public static String readResourceFile(String relativePath) {
        try {
            return Files.readString(Paths.get(TestUtils.class.getResource(relativePath).toURI()));
        } catch (Exception e) {
            throw new RuntimeException("Can't read file: "+relativePath, e);
        }
    }
	
	public static String classpathResourceAbsolutePath(String relativePath) {
        try {
            return Paths.get(TestUtils.class.getResource(relativePath).toURI()).toFile().getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException("Can't read path: " + relativePath, e);
        }
    }
	
	public static Document readDocument(String relativePath) {
		Document document = null;
		String xml = readResourceFile(relativePath);
		try {
			document = Jsoup.parse(xml);
	        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
	        document.outputSettings().prettyPrint(false);
		} catch (Exception parseException) {
			throw new RuntimeException(parseException);
		}
		return document;
    }
	
	public static void writeStringToFile(Path pathTofile, String value) {
		try {
			Files.writeString(pathTofile, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String toJSON(Object pojo){
		try {
            return JACKSON.writeValueAsString(pojo);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
	}
	
	public static void assertJsonEquals(String expectedJson, String actualJson) throws JSONException {
		JSONAssert.assertEquals(mockIds(expectedJson), mockIds(actualJson), true);
	}
	
	public static void assertJsonEquals(String message, String expectedJson, String actualJson) throws JSONException {
		JSONAssert.assertEquals(message + "\n", mockIds(expectedJson), mockIds(actualJson), true);
	}
	
	private static String mockIds(String json) {
		return json.replaceAll(UUID_REGEXP, "mock-uuid").replaceAll(UID_REGEXP, "mock-uuid");
	}
	
	public static List<String> xmlFileNames(String relativePathDir) {
        try {
            return Files.list(Paths.get(TestUtils.class.getResource(relativePathDir).toURI()))
            		.map(path -> path.toFile())
            		.filter(file -> !file.isDirectory() && FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("xml"))
            		.map(file -> FilenameUtils.getBaseName(file.getName())).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Can't read file: " + relativePathDir, e);
        }
    }
	
	public static void removeFromContentstack(String contentTypeUid, String titleMarker) {
        //TODO add pagination case when > 100 items per conten_type
	    String url = "/v3/content_types/" + contentTypeUid + "/entries?query={\"title\" :{ \"$regex\": \""+
                titleMarker+"\" }}&only[BASE][]=title";
        try {
            int totalEntries = new JSONObject(HttpClientUtil.get(url + "&count=true")).getInt("entries");
            System.out.println(totalEntries);
            
            if (totalEntries == 0) {
                return;
            }
            
            Map<String, String> uid2ContentType = new HashMap<>();
            int index = (totalEntries/HTTP_GET_PAGE_SIZE)+1;
            for (int i = 0; i<index; i++) {
                int skip = i * HTTP_GET_PAGE_SIZE;
                
                String json = HttpClientUtil.get(url + "&skip=" + skip);
                JSONArray entries = new JSONObject(json).getJSONArray("entries");
                for (int j = 0; j < entries.length(); j++) {
                    JSONObject item = entries.getJSONObject(j);
                    String uid = item.getString("uid");
                    uid2ContentType.put(uid, contentTypeUid);
                }
            }
            ContentTypeApi.bulkDelete(uid2ContentType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
	
	public static class TestXmlDocumentReader implements XmlDocumentReader {
		
		private String[] folder;
		
		public TestXmlDocumentReader(String...folder) {
			this.folder = folder;
		}

		@Override
		public Document read(String articleId) throws IOException {
			for (String dir : folder) {
				try {
					if (!dir.endsWith("/")) {
						dir += "/";
					}
					return TestUtils.readDocument(dir + articleId + ".xml");
				} catch (Exception ignore) {}
			}
			
			throw new RuntimeException("No file " + articleId + ".xml found in folders:" + ArrayUtils.toString(folder));
		}
	}
	
}
