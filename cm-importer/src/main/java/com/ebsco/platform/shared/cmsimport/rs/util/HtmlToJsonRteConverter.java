package com.ebsco.platform.shared.cmsimport.rs.util;

import com.ebsco.platform.shared.cmsimport.rs.domain.JsonRTE;
import com.ebsco.platform.shared.cmsimport.rs.domain.JsonRTE.BlockNode;
import com.ebsco.platform.shared.cmsimport.rs.domain.RTENode;
import com.ebsco.platform.shared.cmsimport.rs.domain.TextStyleHolder;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class HtmlToJsonRteConverter {

    private static final List<String> ELEMENT_TAGS = List.of("blockquote", "h1", "h2", "h3", "h4", "h5", "h6",
            "li", "ol", "p", "ul", "table", "thead", "tbody", "tr", "td", "th", "span", "div", "script", "hr",
            "figure", "figcaption", "br", "pre");

    private static final List<String> TEXT_TAGS = List.of("code", "del", "em", "i", "s", "strong", "u", "sub",
            "sup");
    
    public static final String SPACE_TOKEN = "SPACE_TOKEN;";


    public static JsonRTE convertFromHtml(String html) {
        if (html == null) {
            return null;
        }
        Document parse = Jsoup.parse(html);
        return convertFromHtml(parse.body());
    }
    
    public static JsonRTE convertFromHtml(Element html) {
        JsonRTE rte = new JsonRTE();
        rte.setUid(generateUid());
        
        for (Node node : html.childNodes()) {
            if (!hasOnlyEmptyTextWithLineBreak(node)) {
                processTag(node, rte, null);
            }
        }
        
        List<RTENode> figures = findAllByType(rte, "figure");
        for (RTENode figureNode : figures) {
            JsonRTE.BlockNode figure = (BlockNode) figureNode;
            changeFigureToReference(figure);
        }
        
        List<RTENode> tables = findAllByType(rte, "table");
        for (RTENode tableNode : tables) {
            JsonRTE.BlockNode table = (BlockNode) tableNode;
            setTableSettings(table);
        }
        return rte;
    }
    
    private static void changeFigureToReference(JsonRTE.BlockNode figure) {
        RTENode img = findAllByType(figure, "img").stream().findFirst().orElse(null);
        if (Objects.nonNull(img)) {
            String caption = "";
            RTENode figcaption = findAllByType(figure, "figcaption").stream().findFirst().orElse(null);
            if (Objects.nonNull(figcaption)) {
                caption = StringUtils.defaultString(getText(figcaption));
                caption = StringUtils.trim(caption);
            }
            
            Map<String, Object> attrs = new HashMap<>();
            attrs.put("display-type", "display");
            attrs.put("asset-type", null);
            attrs.put("asset-caption", caption);
            attrs.put("asset-name", img.getAttrs().get("src"));
            attrs.put("asset-uid", null);
            attrs.put("asset-link", null);
            attrs.put("type", "asset");
            attrs.put("content-type-uid", "sys_assets");
            attrs.put("class-name", "embedded-asset");
            attrs.put("style", new HashMap<>());
            
            Map<String, Object> redactorAttrs = new HashMap<>();
            redactorAttrs.put("max-width", "700");
            redactorAttrs.put("src", null);
            redactorAttrs.put("asset-caption", caption);
            redactorAttrs.put("width", "auto");
            redactorAttrs.put("caption", caption);
            redactorAttrs.put("asset_uid", null);
            redactorAttrs.put("position", null);
            redactorAttrs.put("height", "auto");
            attrs.put("redactor-attributes", redactorAttrs);
            figure.addAttributes(attrs);
            figure.setChildren(null);
            addEmptyTextNode(figure);
            figure.type("reference");
        }
    }
    
    private static boolean hasOnlyEmptyTextWithLineBreak(Node node) {
        if (node instanceof TextNode textNode) {
            String wholeText = textNode.getWholeText();
            return StringUtils.isBlank(wholeText) && StringUtils.contains(wholeText, "\n");
        }
        return false;
    }

    private static void addEmptyTextNode(JsonRTE.BlockNode current) {
        JsonRTE.TextNode textNode = new JsonRTE.TextNode();
        textNode.setText("");
        current.addChild(textNode);
    }

    private static void processTag(Node node, RTENode parent, TextStyleHolder holder) {
        JsonRTE.BlockNode current;
        JsonRTE.TextNode rteNode = new JsonRTE.TextNode();
        if (node instanceof TextNode textNode) {
            if (!hasOnlyEmptyTextWithLineBreak(textNode)) {
                String value = preformat(parent) ? textNode.getWholeText() : textNode.text();
                rteNode.setText(text(unescape(value)));
                parent.addChild(rteNode);
            }
        } else if (node instanceof Element childElement) {
            if (TEXT_TAGS.contains(childElement.nodeName())) {
                if (childElement.ownText().isBlank()) {
                    TextStyleHolder styleHolder = holder == null ? new TextStyleHolder() : holder;
                    processTextTag(childElement, styleHolder);
                    for (Node styledTag : childElement.childNodes()) {
                        processTag(styledTag, parent, styleHolder);
                    }
                } else {
                    rteNode.setText(text(unescape(childElement.text())));
                    TextStyleHolder tempHolder = holder == null ? new TextStyleHolder() : holder;
                    processTextTag(childElement, tempHolder);
                    rteNode.copyStylesFromHolder(tempHolder);
                    parent.addChild(rteNode);
                }
            } else if (holder != null) {
                current = createCurrentTag(childElement);
                current.addChild(rteNode);
                rteNode.setText(text(childElement.ownText()));
                rteNode.copyStylesFromHolder(holder);
                parent.addChild(current);
            } else {
                current = createCurrentTag(childElement);
                current.addAttributes(getAttributesForElement(node));
                if (parent != null) {
                    parent.addChild(current);
                }
                List<Node> childNodes = node.childNodes().stream()
                        .filter(s -> !s.outerHtml().isBlank())
                        .toList();
                if (childNodes.isEmpty()) {
                    addEmptyTextNode(current);
                }

                for (Node childNode : childNodes) {
                    processTag(childNode, current, null);
                }
            }
        }
    }
    
    private static boolean preformat(RTENode node) {
        boolean preformat = "pre".equals(node.type()) || "code".equals(node.type());
        if (!preformat && Objects.nonNull(node.getAttrs())) {
            String value = (String) node.getAttrs().get("style");
            preformat = StringUtils.contains(value, "pre-wrap") || StringUtils.contains(value, "pre");
        }
        return preformat;
    }
    
    private static JsonRTE.BlockNode lineBreak(JsonRTE.BlockNode br) {
        br.type("p");
        br.setUid(generateUid());
        addEmptyTextNode(br);
        return br;
    }
    
    private static JsonRTE.BlockNode setTableSettings(JsonRTE.BlockNode table) {
        RTENode firstTr = findFirstByType(table, "tr");
        List<RTENode> tdsOrThs = null;
        if (Objects.nonNull(firstTr) && CollectionUtils.isNotEmpty(firstTr.children())) {
            tdsOrThs = firstTr.children();
        } 
        if (CollectionUtils.isNotEmpty(tdsOrThs)) {
            List<Integer> colWidths = new ArrayList<>();
            for (int i = 0; i < tdsOrThs.size(); i++) {
                colWidths.add(250);
            }
            table.addAttribute("colWidths", colWidths);
        }
        return table;
    }
    
    private static JsonRTE.BlockNode spaceP() {
        JsonRTE.BlockNode p = new JsonRTE.BlockNode();
        p.type("p");
        p.setUid(generateUid());
        JsonRTE.TextNode textNode = new JsonRTE.TextNode();
        textNode.setText(" ");
        p.addChild(textNode);
        return p;
    }
    
    private static List<RTENode> findAllByType(RTENode current, String type) {
        List<RTENode> result = new ArrayList<>();
        if (type.equals(current.type())) {
            result.add(current);
        }
        List<RTENode> children = current.children();
        if (Objects.nonNull(children)) {
            for (RTENode child : children) {
                result.addAll(findAllByType(child, type));
            }
        }
        return result;
    }
    
    private static RTENode findFirstByType(RTENode current, String type) {
        if (Objects.nonNull(current)) {
            if (type.equals(current.type())) {
                return current;
            }
            List<RTENode> children = current.children();
            if (Objects.nonNull(children)) {
                for (RTENode child : children) {
                    return findFirstByType(child, type);
                }
            }
        }
        return null;
    }
    
    private static String getText(RTENode parent) {
        List<RTENode> children = parent.children();
        for (RTENode child : children) {
            if (child instanceof JsonRTE.TextNode textNode) {
                return textNode.getText();
            }
        }
        return null;
    }
    
    private static String text(String input) {
        return RegExUtils.replaceAll(input, SPACE_TOKEN, StringUtils.SPACE);
    }

    private static JsonRTE.BlockNode createCurrentTag(Element element) {
        JsonRTE.BlockNode current = new JsonRTE.BlockNode();
        current.setUid(generateUid());
        switch (element.nodeName()) {
            case "pre" -> current.setType("code");
            case "style" -> setStyleType(element, current);
            case "a" -> setLinkType(element, current);
            case "img" -> setImageType(element, current);
            case "iframe" -> current.setType("embed");
            case "br" -> lineBreak(current);
            default -> setBlockNodeType(element, current);
        }
        current.addAttributes(getAttributesForElement(element));
        return current;
    }
    
    private static void setImageType(Element img, JsonRTE.BlockNode current) {
        current.setType(img.nodeName());
        current.addAttribute("src", img.attr("src"));
    }

    private static void setLinkType(Element element, JsonRTE.BlockNode current) {
        current.setType(element.nodeName());
        String ref = !element.attr("href").isBlank() ? element.attr("href") : "#";
        current.addAttribute("url", ref);
    }

    private static void setStyleType(Element element, JsonRTE.BlockNode current) {
        current.setType(element.nodeName());
        current.addAttribute("style-text", element.ownText());
    }

    private static void setBlockNodeType(Element element, JsonRTE.BlockNode current) {
        if (ELEMENT_TAGS.contains(element.nodeName().toLowerCase())) {
            current.setType(element.nodeName());
        }
        if (current.getType() == null) {
            current.setType("span");
            element.clearAttributes();
        }
    }

    private static Map<String, Object> getAttributesForElement(Node node) {
        Map<String, Object> attrs = new HashMap<>();
        for (Attribute attr : node.attributes()) {
            attrs.put(attr.getKey(), attr.getValue());
        }
        return attrs;
    }

    private static String unescape(String input) {
        return Parser.unescapeEntities(input, true);
    }

    private static String generateUid() {
        return UUID.randomUUID().toString();
    }

    private static void processTextTag(Node element, TextStyleHolder holder) {
        switch (element.nodeName()) {
            case "code" -> holder.setCode(Boolean.TRUE);
            case "del", "s" -> holder.setStrikethrough(Boolean.TRUE);
            case "em", "i" -> holder.setItalic(Boolean.TRUE);
            case "strong" -> holder.setBold(Boolean.TRUE);
            case "u" -> holder.setUnderline(Boolean.TRUE);
            case "sup" -> holder.setSuperscript(Boolean.TRUE);
            case "sub" -> holder.setSubscript(Boolean.TRUE);
        }
    }

}
