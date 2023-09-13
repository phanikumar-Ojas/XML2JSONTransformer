package com.ebsco.platform.shared.cmsimport.rs.xml;

import com.ebsco.platform.shared.cmsimport.rs.domain.Contributor;
import com.ebsco.platform.shared.cmsimport.rs.util.ConfigurationUtil;
import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


@Log4j2
@RequiredArgsConstructor
public class ContributorXmlReader {

    private static final String ROOT_ARTICLE_XML_FOLDER_PATH = AppPropertiesUtil.getProperty("ARTICLE_XML_FOLDER");
    
    public Map<String, Set<Contributor>> getContributors() {

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();

            log.info("Scanning root xml directory: {}", ROOT_ARTICLE_XML_FOLDER_PATH);
            
            List<String> filePaths = ConfigurationUtil.filePaths(ROOT_ARTICLE_XML_FOLDER_PATH, "xml");
            log.info("Found: {} xml files", filePaths.size());

            int count = 1;
            int emptyFullNameCount = 0;
            Map<String, Exception> errors = new HashMap<>();
            Map<String, Set<Contributor>> articleId2contributors = new HashMap<>();
            Map<String, Contributor> title2contributor = new HashMap<>();
            for (String path : filePaths) {
            	log.info("({} of {}) : {}", count++, filePaths.size(), path);
            	Document document = null;
            	File xml = new File(path);
            	try {
            		document = documentBuilder.parse(xml);
            	} catch (Exception parseException) {
            		errors.put(path, parseException);
            		continue;
            	}

                document.getDocumentElement().normalize();
                
                NodeList nodeList = document.getElementsByTagName("contrib");

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);


                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        String degrees = StringUtils.trim(getNodeValue(element, "degrees")) ;
                        
                        String affiliated = getNodeValue(element, "aff");
                        String prefix = getNodeValue(element, "prefix");
                        String suffix = getNodeValue(element, "suffix");
                        
                        String fullName = getNodeValue(element, "string-name");
                        String surname = getNodeValue(element, "surname");
                        String givenName = getNodeValue(element, "given-names");
                        String title = fullName;

                        if (StringUtils.isBlank(fullName)) {
                            fullName = (StringUtils.defaultIfBlank(givenName, StringUtils.EMPTY) + " "
                                    + StringUtils.defaultIfBlank(surname, StringUtils.EMPTY)
                                    + StringUtils.defaultIfBlank(suffix, StringUtils.EMPTY)).trim();
                        }

                        if (StringUtils.isBlank(title)) {
                            title = (StringUtils.defaultIfBlank(prefix, StringUtils.EMPTY) + " " + fullName + " "
                                    + StringUtils.defaultIfBlank(suffix, StringUtils.EMPTY)).trim();
                        }
                        
                        if (StringUtils.isBlank(fullName)) {
                        	emptyFullNameCount++;
                        	continue;
                        }
                        
                        Contributor contributor = title2contributor.get(fullName);
                        if (Objects.isNull(contributor)) {
                            contributor = Contributor.builder()
                                    .title(title)
                                    .fullName(fullName)
                                    .firstName(givenName)
                                    .lastName(surname)
                                    .affiliated(affiliated)
                                    .degrees(degrees)
                                    .prefix(prefix)
                                    .suffix(suffix)
                                    .build();
                        	title2contributor.put(fullName, contributor);
                        }
                        
                        String articleId = articleId(xml.getName());
                        Set<Contributor> articleContributors = articleId2contributors.computeIfAbsent(articleId, k -> new LinkedHashSet<>());
                        articleContributors.add(contributor);
                    }
                }
            }
            log.info("emptyFullNameCount={}", emptyFullNameCount);
            log.warn("Found:{} wrong files", errors.size());
            errors.forEach((key, value) -> log.error("Wrong xml file:{}, {}", key, value));
            log.info("Created {} uniq contributors", title2contributor.size());
            return articleId2contributors;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getNodeValue(Element element, String tagName) {
        Node item = element.getElementsByTagName(tagName).item(0);
        return item != null ? StringUtils.strip(StringUtils.trim(item.getTextContent()), ",") : null;
    }
    
    private static String articleId(String fileName) {
    	int dotIdx = fileName.indexOf('.');
    	return dotIdx != -1 ? fileName.substring(0, dotIdx) : fileName;
    }
}
