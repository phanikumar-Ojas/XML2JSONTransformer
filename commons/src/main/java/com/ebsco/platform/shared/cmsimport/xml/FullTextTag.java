package com.ebsco.platform.shared.cmsimport.xml;

public class FullTextTag {

	
	
	private String ftTagType;
	private String value;
	private String ftStartTag;
	private Integer startIndex;
	private Integer endIndex;
	
	
	


	public FullTextTag(String ftTagType, String value, String ftStartTag, Integer startIndex, Integer endIndex) {
		super();
		this.ftTagType = ftTagType;
		this.value = value;
		this.ftStartTag = ftStartTag;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}


	public Integer getStartIndex() {
		return startIndex;
	}


	public void setStartIndex(Integer startIndex) {
		this.startIndex = startIndex;
	}


	public Integer getEndIndex() {
		return endIndex;
	}


	public void setEndIndex(Integer endIndex) {
		this.endIndex = endIndex;
	}


	public String getFtTagType() {
		return ftTagType;
	}


	public void setFtTagType(String ftTagType) {
		this.ftTagType = ftTagType;
	}


	public String getFtStartTag() {
		return ftStartTag;
	}


	public void setFtStartTag(String ftStartTag) {
		this.ftStartTag = ftStartTag;
	}


	@Override
	public String toString() {
		return "FullTextTag [ftTagType=" + ftTagType + ", value=" + value + ", ftStartTag=" + ftStartTag
				+ ", startIndex=" + startIndex + ", endIndex=" + endIndex + "]";
	}
	
	
	
}
