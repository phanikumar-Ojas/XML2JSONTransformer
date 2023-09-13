package com.ebsco.platform.shared.cmsimport.rs.xml.topic;

import org.jsoup.nodes.Document;

import com.ebsco.platform.shared.cmsimport.rs.domain.topic.Topic;

import lombok.Builder;
import lombok.Getter;

public interface TopicXmlParser<T extends Topic> {
	
	@Builder
	@Getter
	public static class Input {
		private Document document;
		private String fileName;
	}
	
	@Builder
	@Getter
	public static class Output <T extends Topic>{
		private Input input;
		private T writeTo;
	}
	
	public interface ProcessingStatistic {
		default void print() {};
	}
	
	Output <T> parse(Input input, T writeTo);
	
	default ProcessingStatistic getStatistic() {
		return new ProcessingStatistic() {};
	}
}
