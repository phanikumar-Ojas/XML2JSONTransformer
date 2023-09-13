package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.jsoup.nodes.Document;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.Topic;
import com.ebsco.platform.shared.cmsimport.rs.util.ContentTypeUtil;

public class BoundedAndOrderedTagsExtractor {
	
	private static final String LEFT_SQUARE_BRACKET = "[";
	private static final int MAX_CHARACTERS_LONG = 50;
	
	public void extract(Document readFrom, Topic writeTo) {
		List<String> bookPartCategories = readFrom.selectXpath("//book-part/book-part-meta/book-part-categories/subj-group/subject").eachText();
		Collection<String> tags = new LinkedHashSet<>();
		boolean addMoreTags = true;
		if (CollectionUtils.isNotEmpty(bookPartCategories)) {
			addMoreTags = ContentTypeUtil.addTags(tags, bookPartCategories);
		}
		if (addMoreTags) {
			if (CollectionUtils.isNotEmpty(writeTo.getTags())) {
				addMoreTags = ContentTypeUtil.addTags(tags, writeTo.getTags());
			}
		}
		if (addMoreTags) {
			List<String> relatedArticleTitles = readFrom.selectXpath("//related-article").eachText();
			if (CollectionUtils.isNotEmpty(relatedArticleTitles)) {
				addMoreTags = ContentTypeUtil.addTags(tags, relatedArticleTitles);
			}
		}
		if (addMoreTags) {
			List<String> indexTerms = readFrom.selectXpath("//index-term/primary").eachText();
			if (CollectionUtils.isNotEmpty(indexTerms)) {
				indexTerms.sort((p1, p2) -> {
					if (p1.contains(LEFT_SQUARE_BRACKET) && !p2.contains(LEFT_SQUARE_BRACKET)) { 
						return -1; 
					} else if (!p1.contains(LEFT_SQUARE_BRACKET) && p2.contains(LEFT_SQUARE_BRACKET)){ 
						return 1;
					}
					return 0;
				});
				addMoreTags = ContentTypeUtil.addTags(tags, indexTerms);
			}
		}
		writeTo.setTags(tags.stream().filter(tag -> tag.length() <= MAX_CHARACTERS_LONG).toList());
	}
}
