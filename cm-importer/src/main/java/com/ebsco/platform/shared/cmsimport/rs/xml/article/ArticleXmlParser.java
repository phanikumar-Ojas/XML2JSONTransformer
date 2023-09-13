package com.ebsco.platform.shared.cmsimport.rs.xml.article;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import com.ebsco.platform.shared.cmsimport.rs.domain.Article;
import com.ebsco.platform.shared.cmsimport.rs.domain.JsonRTE;
import com.ebsco.platform.shared.cmsimport.rs.util.DataUtil;
import com.ebsco.platform.shared.cmsimport.rs.util.HtmlToJsonRteConverter;
import com.ebsco.platform.shared.cmsimport.rs.util.RsXmlUtil;
import com.ebsco.platform.shared.cmsimport.rs.util.TagContainer;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ArticleXmlParser {
    
    private static final String COMPLETE_INSIDE_RTE_IMAGE_STATUS = "Complete inside RTE";
    
    private static void removeQuickreferenceInfoForTopic(Elements element) {
        element.select("section[sec-type*=quickreference]  p:contains(Also Known As)").remove();
        element.select("section[sec-type*=quickreference]  p:contains(Anatomy or system affected)").remove();
        element.select("section[sec-type*=quickreference]  p:contains(Date Founded:)").remove();
        element.select("section[sec-type*=quickreference]  p:contains(Industry:)").remove();
        element.select("section[sec-type*=quickreference]  p:contains(Corporate Headquarters:)").remove();
        element.select("section[sec-type*=quickreference]  p:contains(Type:)").remove();
        element.select("section[sec-type*=quickreference]  p:contains(Category)").remove();
        element.select("section[sec-type*=quickreference]  p:contains(Definition)").remove();
        element.select("section[sec-type*=quickreference]  p:contains(Date)").remove();
        element.select("section[sec-type*=quickreference]  p:contains(Locale)").remove();

        element.select("section[sec-type*=quickreference] li:contains(Born)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Died)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Birthplace)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Place of death)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Region)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Population)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Capital)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Largest city)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Number of counties)").remove();
        element.select("section[sec-type*=quickreference] li:contains(State nickname)").remove();
        element.select("section[sec-type*=quickreference] li:contains(State motto)").remove();
        element.select("section[sec-type*=quickreference] li:contains(State flag)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Population density)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Urban population)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Rural population)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Population under 18)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Population over 65)").remove();
        element.select("section[sec-type*=quickreference] li:contains(White alone)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Black or African American alone)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Hispanic or Latino)").remove();
        element.select("section[sec-type*=quickreference] li:contains(American Indian and Alaska Native alone)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Asian alone)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Native Hawaiian and Other Pacific Islander alone)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Some Other Race alone)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Two or More Races)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Per capita income)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Unemployment)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Gross domestic product (in millions $USD))").remove();
        element.select("section[sec-type*=quickreference] li:contains(GDP percent change)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Governor)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Present constitution date)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Electoral votes)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Number of counties)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Violent crime rate)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Death penalty)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Full name of country)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Official language)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Nationality)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Currency (money))").remove();
        element.select("section[sec-type*=quickreference] li:contains(Land area)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Water area)").remove();
        element.select("section[sec-type*=quickreference] li:contains(National anthem)").remove();
        element.select("section[sec-type*=quickreference] li:contains(National holiday)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Population growth)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Time zone)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Flag)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Motto)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Independence)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Government type)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Suffrage)").remove();
        element.select("section[sec-type*=quickreference] li:contains(Legal system)").remove();

        element.select("section[sec-type*=quickreference] ul:not(:has(li))").remove();
        element.select("section[sec-type*=quickreference] ol:not(:has(li))").remove();
    }

    public static void extractFieldsFromXml(Article article, XmlReader xmlReader, Set<String> knownMfsAns) {
        handlePoetrySpaces(xmlReader);
        
        article.setAuthorNote(extractAuthorNote(xmlReader));

        String lexile = xmlReader.getChildElementValue("custom-meta:has(> meta-name:contains(lexile))",
                "meta-value");
        article.setLexile(DataUtil.parseIntOrRange(lexile));

        boolean hasLinks = xmlReader.isElementPresent("related-article[related-article-type=rs]");
        article.setRsLinksApplied(hasLinks);

        boolean hasFigures = xmlReader.isElementPresent("fig[fig-type=elec_use], graphic");
        if (hasFigures) {
            article.setImageStatus(COMPLETE_INSIDE_RTE_IMAGE_STATUS);
        }

        boolean isTableInMainBody = xmlReader.isElementWithTagNamePresent("table-wrap");
        article.setTableInMainBody(isTableInMainBody);

        article.setTags(extractTags(xmlReader, article, knownMfsAns));
        
        
        Elements body = xmlReader.getElement("book > body > book-part > body");
        
        RsXmlUtil.processImages(body);
        
        article.setAbstractValue(extractAbstractValue(xmlReader));
        article.setCitations(extractCitations(xmlReader));
        article.setMainBody(extractMainBody(xmlReader));
    }
    
    private static void handlePoetrySpaces(XmlReader xmlReader) {
        Elements elements = xmlReader.getElement("disp-quote[content-type=poetry1] > p");
        for (Element element : elements) {
            addSpacesBeforeInnerContent(element, 6);
        }
        
        elements = xmlReader.getElement("disp-quote[content-type=poetry2] > p");
        for (Element element : elements) {
            addSpacesBeforeInnerContent(element, 12);
        }
        
        elements = xmlReader.getElement("disp-quote[content-type=poetry3] > p");
        for (Element element : elements) {
            addSpacesBeforeInnerContent(element, 24);
        }
    }
    
    private static void addSpacesBeforeInnerContent(Element element, int numOfSpaces) {
        String ownHtml = element.html();
        for (int i = 0; i < numOfSpaces; i++) {
            ownHtml = HtmlToJsonRteConverter.SPACE_TOKEN + ownHtml;
        }
        element.html(ownHtml);
    }

    private static JsonRTE extractAuthorNote(XmlReader xmlReader) {
        String authorNote = xmlReader.getAnyElementByAttributeValue("sec-type", "au_note").html();
        if (authorNote.isBlank()) {
            return null;
        }
        return HtmlToJsonRteConverter.convertFromHtml(authorNote);
    }
    
    private static Set<String> extractTags(XmlReader xmlReader, Article article, Set<String> knownMfsAns) {
        TagContainer container = new TagContainer();
        List<String> currentTags = xmlReader.getElement(
                        "book > body > book-part > book-part-meta > book-part-categories > subj-group > subject")
                .stream()
                .map(Element::text)
                .toList();
        container.addTags(currentTags);

        container.addTags(article.getTags());

        currentTags = xmlReader.getElement("related-article").stream().filter(relatedArticle -> {
            String mfsAn = relatedArticle.attr("xlink:href");
            return knownMfsAns.contains(mfsAn);
        })
        .map(Element::text).toList();
        
        container.addTags(currentTags);

        currentTags = xmlReader.getElement("index-term:contains([)").stream()
                .map(Element::text)
                .toList();
        container.addTags(currentTags);

        currentTags = xmlReader.getElement(
                        "section > p > index-term:first-child > primary:first-child:not(:contains([))").stream()
                .map(Element::text)
                .toList();
        container.addTags(currentTags);

        xmlReader.removeTags("index-term");
        return container.getTags();

    }

    private static JsonRTE extractCitations(XmlReader xmlReader) {
        Elements element = xmlReader.getElement("book > body > book-part > body > section:has(ref-list:has(ref:has(citation)))");
        element.select("citation").tagName("p");
        element.select("ref").unwrap();
        element.select("ref-list").unwrap();
        unwrapSections(element);
        String citations = element.outerHtml();
        element.remove();
        if (citations.isBlank()) {
            return null;
        }
        return HtmlToJsonRteConverter.convertFromHtml(citations);
    }

    private static JsonRTE extractMainBody(XmlReader xmlReader) {
        Elements elements = xmlReader.getElement("book-front > section, book-part > body > section");

        elements.select("section > def-list > def-item > def > p > related-article").unwrap();
        elements.select("section > def-list > def-item > term > related-article").unwrap();
        elements.select("section > p > underline").unwrap();

        removeUnnecessaryTags(elements);


        elements.select("section > boxed-text > disp-quote").unwrap();
        elements.select("section > boxed-text > sig-block").tagName("blockquote");
        elements.select("section > boxed-text > p").tagName("blockquote");
        elements.select("section > boxed-text").unwrap();
        
        elements.select("section > verse-group > verse-line").wrap("<blockquote></blockquote>").tagName("i");
        elements.select("section > verse-group").unwrap();
        elements.select("section > p > disp-quote > speech > speaker").tagName("p");
        elements.select("section > p > disp-quote:has(> p)").tagName("blockquote");
        elements.select("section > p > disp-quote:has(> speech > p)").tagName("blockquote");
        elements.select("section > disp-quote").tagName("blockquote");
        elements.select("section > pre").tagName("blockquote").attr("style", "white-space: pre-wrap;");
        /*for (Element pre : pres) {
            List<Node> children =  pre.childNodesCopy();
            pre.empty();
            pre.tagName("blockquote");
            pre.appendElement("pre").appendChildren(children);
        }*/
        
        elements.select("p[content-type*=poetry]").tagName("blockquote");
        elements.select("p[content-type*=extract]").tagName("blockquote");
        elements.select("related-article[related-article-type=no]").unwrap();


        Elements relatedArticles = elements.select("related-article")
                .tagName("a");
        for (Element related : relatedArticles) {
            related.attr("url", related.attr("xlink:href"));
            related.removeAttr("xmlns:xlink");
            related.removeAttr("related-article-type");
            related.removeAttr("xlink:href");
            related.removeAttr("xlink:type");
        }

        elements.select("def-list[list-type*=bold]").attr("style", "font-weight: bold;");
        elements.select("def-list > def-item > term").tagName("strong");
        elements.select("def-list > def-item > def > p").unwrap();
        elements.select("def-list > def-item > def").tagName("span");
        elements.select("def-list > def-item").tagName("li");
        elements.select("def-list").tagName("ul");
        elements.select("section > ul  > li:has(label) > p").unwrap();
        elements.select("section > ol  > li:has(label) > p").unwrap();
        elements.select("section > ul > li > label").tagName("strong");
        elements.select("section > ol > li > label").tagName("strong");

        ArticleXmlParser.removeQuickreferenceInfoForTopic(elements);

        Elements scElements = elements.select("sc").tagName("span");
        for (Element scElement : scElements) {
            scElement.text(scElement.text().toUpperCase());
        }

        Elements links = elements.select("ext-link[ext-link-type$=uri]").tagName("a");
        for (Element link : links) {
            link.attr("href", link.text());
        }

        links = elements.select("ext-link[ext-link-type$=AN]").tagName("a");

        for (Element link : links) {
            link.attr("href", link.attr("xlink:href"));
            link.text(link.attr("xlink:title"));

        }
        
        elements.select("blockquote > p").unwrap();
        elements.select("blockquote").removeAttr("content-type");
        
        elements.select("span").unwrap();
        
        Elements timelineStrongs = elements.select("section[sec-type=timeline] > ul > h1").tagName("strong");
        for (Element strong : timelineStrongs) {
            String text = strong.text();
            if (!StringUtils.endsWith(text, ":")) {
                strong.text(text + ":");
            }
        }
        elements.select("section[sec-type=timeline] > ul > li > p").unwrap();
        elements.select("section[sec-type=timeline] > ul > li").unwrap();
        elements.select("section[sec-type=timeline] > ul").tagName("p").forEach(e -> e.clearAttributes());
        
        unwrapSections(elements);
        
        String mainBodyHtml = elements.toString();
        if (mainBodyHtml.isBlank()) {
            log.warn("No main body");
            return null;
        }
        //mainBodyHtml = mainBodyHtml.replaceAll("<section>", "").replaceAll("</section>", "");;
        //System.out.println(mainBodyHtml);
        return HtmlToJsonRteConverter.convertFromHtml(mainBodyHtml);
    }
    
    private static Elements unwrapSections(Elements sectionsContainer) {
        Elements sections = sectionsContainer.select("section");
        for (int i = 0; i < sections.size(); i++) {
            Element section = sections.get(i);
            /*if ((sections.size() - i) > 1) {
                section.appendElement("br");
            }*/
            Elements children = section.children();
            sectionsContainer.addAll(children);
        }
        sectionsContainer.removeAll(sections);
        return sectionsContainer;
    }

    private static void removeUnnecessaryTags(Elements element) {
        element.select("section[sec-type=seealso]").empty();
        element.select("section[sec-type=reviewed]").empty();
        element.select("section[sec-type=rs_citation]").empty();
        element.select("section[sec-type=derived]").empty();
        element.select("section[sec-type=orig_cite_text]").empty();
        element.select("section[sec-type=sidebar]").empty();
        element.select("section[sec-type=cover-image]").empty();
        element.select("section[sec-type=a_head]").empty();
        element.select("list[list-content=state-general]").empty();
        element.select("list[list-content=state-demo]").empty();
        element.select("list[list-content=state-gov]").empty();
        element.select("list[list-content=state-eco]").empty();
        element.select("section:has(list[list-content=demographics])").empty();
        element.select("section:has(h1:contains(Principal Works))").empty();
    }


    private static JsonRTE extractAbstractValue(XmlReader xmlReader) {
        String abstractValue = xmlReader.getElement("abstract[abstract-type=placard]").html();
        if (StringUtils.isBlank(abstractValue)) {
            Element firstSecElement = xmlReader.getElement("section[sec-type^=bodytext]").first();
            if (firstSecElement != null) {
                Element firstParagraph = firstSecElement.getElementsByTag("p").first();
                if (Objects.nonNull(firstParagraph)) {
                    firstParagraph.select("fig").remove();
                    abstractValue = firstParagraph.toString();
                }
            }
        }
        if (StringUtils.isBlank(abstractValue)) {
            return null;
        }
        return HtmlToJsonRteConverter.convertFromHtml(abstractValue);
    }
}
