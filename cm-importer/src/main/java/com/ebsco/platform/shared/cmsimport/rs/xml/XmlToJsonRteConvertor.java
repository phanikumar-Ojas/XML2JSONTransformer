package com.ebsco.platform.shared.cmsimport.rs.xml;

import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ebsco.platform.shared.cmsimport.rs.domain.JsonRTE;
import com.ebsco.platform.shared.cmsimport.rs.util.ContentTypeUtil;
import com.ebsco.platform.shared.cmsimport.rs.util.RsXmlUtil;

public class XmlToJsonRteConvertor implements Function<Elements, JsonRTE> {
	
	private String [] tokensToRemove;
	
	public XmlToJsonRteConvertor(String...tokenToRemove) {
		this.tokensToRemove = tokenToRemove;
	}

	@Override
	public JsonRTE apply(Elements elements) {
	    RsXmlUtil.processImages(elements);
	    String rteXml = "";
	    
	    for (Element element : elements) {
	        String fieldValue = element.html();
            if (Objects.nonNull(tokensToRemove)) {
                for (String fieldToken : tokensToRemove) {
                    fieldValue = RegExUtils.removeFirst(fieldValue, fieldToken);
                }
            }
            
            fieldValue = RegExUtils.removeAll(fieldValue, "<[^/>]+>[ \\n\\r\\t]*</[^>]+>");
            fieldValue = RegExUtils.removeAll(fieldValue, "<[^/>]+>[ \\n\\r\\t]*</[^>]+>");
            fieldValue = StringUtils.trim(RegExUtils.removeAll(fieldValue, "<[^/>]+>[ \\n\\r\\t]*</[^>]+>"));
            
            
            fieldValue = RegExUtils.removeFirst(fieldValue, "<bold>: </bold>");
            fieldValue = RegExUtils.removeAll(fieldValue, "<sc>");
            fieldValue = RegExUtils.removeAll(fieldValue, "</sc>");
            fieldValue = StringUtils.trim(fieldValue);
            if(StringUtils.startsWith(fieldValue, ":")) {
                fieldValue = RegExUtils.removeFirst(fieldValue, ":");
            }
            fieldValue = StringUtils.trim(RegExUtils.removeFirst(fieldValue, "<[^/>]+>[ \\n\\r\\t:]*</[^>]+>"));
            
            fieldValue = StringUtils.trim(fieldValue);
            if (StringUtils.isNoneBlank(fieldValue)) {
                rteXml += fieldValue;
            }
        }
	    
	    if (StringUtils.isNoneBlank(rteXml)) {
	        return ContentTypeUtil.toJsonRteFromXml(rteXml);
	    }
		return null;
	}
}
