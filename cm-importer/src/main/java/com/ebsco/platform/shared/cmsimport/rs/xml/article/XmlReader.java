package com.ebsco.platform.shared.cmsimport.rs.xml.article;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class XmlReader {

    private Document document;

    public XmlReader(String path) {
        try {
            document = Jsoup.parse(new File(path),
                    "UTF-8", "", Parser.xmlParser());
            setDocument(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setDocument(Document document) {
        this.document = document;
        updateToValidTags();
    }

    public XmlReader(Document document) {
        setDocument(document);
    }

    public boolean isElementPresent(String query) {
        return !getElement(query).isEmpty();
    }

    public boolean isElementWithTagNamePresent(String tagName) {
        return !document.getElementsByTag(tagName).isEmpty();
    }

    public Elements getElement(String query) {
        return document.select(query);
    }

    public String getChildElementValue(String parent, String child) {
        return getElement(parent).select(child).text();
    }

    public Elements getAnyElementByAttributeValue(String attrName, String attrValue) {
        return document.getElementsByAttributeValue(attrName, attrValue);
    }

    public void removeTags(String query) {
        document.select(query).remove();
    }

    public boolean hasDocument() {
        return document != null;
    }


    private void updateToValidTags() {
        document.select("*:empty").stream()
                .filter(e -> e.attributesSize() == 0)
                .forEach(Node::remove);

        document.select("book > body > book-part > book-front > section[sec-type=cover-image]").remove();
        document.select("preformat").tagName("pre");
        document.select("sec").tagName("section");
        document.select("italic").tagName("i");
        document.select("title").tagName("h1");
        document.select("bold").tagName("strong");
        document.select("list-item").tagName("li");
        document.select("list[list-type$=order]").tagName("ol").removeAttr("list-type");
        document.select("list[list-type$=bullet]").tagName("ul").removeAttr("list-type");
        document.select("list[list-type$=simple]").tagName("ul").removeAttr("list-type");
        document.select("list").tagName("ul");
        document.select("section[sec-type=rs_citation]").remove();
        document.select("oasis|table").tagName("table").forEach(e -> e.clearAttributes());
        document.select("oasis|colspec").remove();
        document.select("oasis|tgroup").unwrap();
        document.select("oasis|thead").tagName("thead").forEach(e -> e.clearAttributes());;
        document.select("oasis|tbody").tagName("tbody").forEach(e -> e.clearAttributes());;
        document.select("oasis|row").tagName("tr").forEach(e -> e.clearAttributes());;
        document.select("oasis|entry").tagName("td").forEach(e -> e.clearAttributes());;
        document.select("thead > tr > td").tagName("th").forEach(e -> e.clearAttributes());;
        document.select("table-wrap > caption").remove();
        Elements twraps = document.select("table-wrap");
        for (Element twrap : twraps) {
            Element table = twrap.select("table").first();
            if (Objects.nonNull(table)) {
                twrap.replaceWith(table);
            }
        }
    }

}
