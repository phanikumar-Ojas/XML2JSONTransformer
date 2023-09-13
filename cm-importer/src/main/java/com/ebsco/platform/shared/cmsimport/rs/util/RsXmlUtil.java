package com.ebsco.platform.shared.cmsimport.rs.util;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class RsXmlUtil {
    
    public static void processImages(Elements elements) {
        processFigure(elements);
        processMedia(elements);
    }
    
    private static void processFigure(Elements elements) {
        Elements figs = elements.select("fig");
        for (Element fig : figs) {
            String copyrightText = fig.select("copyright-statement").stream()
                    .map(Element::text).reduce("", RsXmlUtil::accumulate);
            String figcaption = caption(fig);
            fig.appendElement("figcaption").text(String.format("%s %s", figcaption, copyrightText));
            
            fig.select("permissions").remove();
            fig.select("caption").remove();
            fig.select("copyright-statement").remove();
            fig.clearAttributes();
            fig.tagName("figure");
            
            Elements extLinks = fig.select("ext-link[ext-link-type$=db-image]");
            for (Element extLink : extLinks) {
                String filename = extLink.attr("xlink:href");
                extLink.clearAttributes();
                extLink.attr("src", filename);
                extLink.attr("max-width", "700");
                extLink.attr("height", "auto");
                extLink.tagName("img");
            }
        }
    }
    
    private static void processMedia(Elements element) {
        Elements medias = element.select("media");
        for (Element media : medias) {
            Element figure = media.tagName("figure");
            String copyrightText = media.select("copyright-statement").stream()
                    .map(Element::text).reduce("", RsXmlUtil::accumulate);
            String figcaption = caption(figure);
            figure.appendElement("figcaption").text(String.format("%s %s", figcaption, copyrightText));
            figure.select("permissions").remove();
            figure.select("caption").remove();
            figure.select("copyright-statement").remove();
            
            String filename = figure.attr("xlink:href");
            figure.clearAttributes();
            figure.appendElement("img").attr("src", filename);
        }
    }
    
    private static String caption(Element elementContainingCaptionTag) {
        StringBuilder result = new StringBuilder();
        Element caption = elementContainingCaptionTag.getElementsByTag("caption").first();
        if (caption != null) {
            for (Node captionNode : caption.childNodes()) {
                if (captionNode instanceof TextNode textNode) {
                    String text = textNode.text().trim();
                    result.append(" ").append(text);
                    if (!text.isBlank() && !text.endsWith(".")) {
                        result.append(".");
                    }
                }
                if (captionNode instanceof Element child) {
                    String text = child.text().trim();
                    result.append(text);
                    if (!text.isBlank() && !text.endsWith(".")) {
                        result.append(".");
                    }
                }
            }
        }
        return result.toString().trim();
    }

    private static String accumulate(String first, String second) {
        if (StringUtils.isBlank(first)) {
            return StringUtils.defaultString(second);
        }
        if (StringUtils.isBlank(second)) {
            return first.trim().endsWith(".") ? first : first + ".";
        }
        if (first.trim().endsWith(".")) {
            return first + " " + second;
        }
        
        return first + ". " + second;
    }
}
