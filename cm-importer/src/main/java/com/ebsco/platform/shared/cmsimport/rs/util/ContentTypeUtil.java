package com.ebsco.platform.shared.cmsimport.rs.util;

import java.util.Collection;
import java.util.Objects;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.ebsco.platform.shared.cmsimport.rs.domain.Article;
import com.ebsco.platform.shared.cmsimport.rs.domain.ArticleDefinition;
import com.ebsco.platform.shared.cmsimport.rs.domain.ContentType;
import com.ebsco.platform.shared.cmsimport.rs.domain.JsonRTE;

public class ContentTypeUtil {
	
	private static final int MAX_TAG_ITEMS = 50;
	
	public static JsonRTE toJsonRteFromXml(String xml) {
		if (Objects.isNull(xml)) {
			return null;
		}
		
		//jsoup moves the very first <title> tag into html head section and need to prevent this
		xml = RegExUtils.replaceAll(xml, "<title>", "<h1>");
		xml = RegExUtils.replaceAll(xml, "</title>", "</h1>");
		
		Document document = Jsoup.parse(xml);
		 
		document.select("*:empty").stream().filter(e -> e.attributesSize() == 0).forEach(Node::remove);

		document.select("preformat").tagName("pre");
		document.select("sec").tagName("section");
		document.select("italic").tagName("i");
		document.select("title").tagName("h1");
		document.select("bold").tagName("strong");
		document.select("list-item").tagName("li");
		document.select("list[list-type$=order]").tagName("ol").removeAttr("list-type");
		document.select("list[list-type$=bullet]").tagName("ul").removeAttr("list-type");
		document.select("section[sec-type=rs_citation]").remove();
		document.select("oasis|table").tagName("table");
		document.select("oasis|tgroup").tagName("colgroup");
		document.select("oasis|colspec").tagName("col");
		document.select("oasis|thead").tagName("thead");
		document.select("oasis|tbody").tagName("tbody");
		document.select("oasis|row").tagName("tr");
		document.select("oasis|entry").tagName("th");
		
		
		document.select("related-article[related-article-type=no]").unwrap();

        Elements relatedArticles = document.select("related-article:not(:has([related-article-type=no]))")
                .tagName("a");
        for (Element related : relatedArticles) {
            related.attr("url", related.attr("xlink:href"));
            related.removeAttr("xmlns:xlink");
            related.removeAttr("related-article-type");
            related.removeAttr("xlink:href");
            related.removeAttr("xlink:type");
        }

        RsXmlUtil.processImages(document.children());

        Elements links = document.select("ext-link[ext-link-type$=uri]").tagName("a");
        for (Element link : links) {
            link.attr("href", link.text());
        }

        links = document.select("ext-link[ext-link-type$=AN]").tagName("a");

        for (Element link : links) {
            link.attr("href", link.attr("xlink:href"));
            link.text(link.attr("xlink:title"));

        }
        
		return HtmlToJsonRteConverter.convertFromHtml(document.body());
	}
	
	public static boolean addTags(Collection<String> whereToAdd, Collection<String> valuesToAdd) {
		boolean addMore = true;
		for (String value : valuesToAdd) {
			addMore = addTag(whereToAdd, value);
			if (!addMore) {
				return addMore;
			}
		}
		return addMore;
	}
	
	public static boolean addTag(Collection<String> whereToAdd, String valueToAdd) {
		if (whereToAdd.size() >= MAX_TAG_ITEMS) {
			return false;
		} else {
			whereToAdd.add(valueToAdd);
		}
		return true;
	}
	
	public static String bindFieldName(ContentType entry) {
        if (entry instanceof ArticleDefinition) {
            return "an";
        } else if (entry instanceof Article) {
            return "article_id";
        }
        return "title";
    }
	
	public static String bindFieldValue(ContentType entry) {
	    String bindFieldName = bindFieldName(entry);
	    return PojoUtil.getJsonField(entry, bindFieldName);
    }
}
