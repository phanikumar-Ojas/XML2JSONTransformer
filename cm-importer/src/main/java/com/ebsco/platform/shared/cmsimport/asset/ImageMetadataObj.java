package com.ebsco.platform.shared.cmsimport.asset;

import java.sql.Date;

public class ImageMetadataObj {

	
	private String imageTitle;
	
	private String imageFile;
	private String caption;
	private String description;
	private String copyright;
	private String source;
	private String contentType;
	
	private String webpageUrl;
	private String position;
	private String rightsNote;
	private java.sql.Date useEndDate;
	private int height;
	private int width;
	private java.sql.Date dateAdded;
	private java.sql.Date dateUpdated;
	private String terms;
	



	public ImageMetadataObj(String imageTitle, String imageFile, String caption, String description, String copyright,
			String source, String contentType, String webpageUrl, String position, String rightsNote, Date useEndDate,
			int height, int width, Date dateAdded, Date dateUpdated, String terms) {
		super();
		this.imageTitle = imageTitle;
		this.imageFile = imageFile;
		this.caption = caption;
		this.description = description;
		this.copyright = copyright;
		this.source = source;
		this.contentType = contentType;
		this.webpageUrl = webpageUrl;
		this.position = position;
		this.rightsNote = rightsNote;
		this.useEndDate = useEndDate;
		this.height = height;
		this.width = width;
		this.dateAdded = dateAdded;
		this.dateUpdated = dateUpdated;
		this.terms = terms;
	}


	public String getImageTitle() {
		return imageTitle;
	}


	public void setImageTitle(String imageTitle) {
		this.imageTitle = imageTitle;
	}


	public String getImageFile() {
		return imageFile;
	}


	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
	}


	public String getCaption() {
		return caption;
	}


	public void setCaption(String caption) {
		this.caption = caption;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getCopyright() {
		return copyright;
	}


	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}


	public String getSource() {
		return source;
	}


	public void setSource(String source) {
		this.source = source;
	}


	public String getContentType() {
		return contentType;
	}


	public void setContentType(String contentType) {
		this.contentType = contentType;
	}


	public String getWebpageUrl() {
		return webpageUrl;
	}


	public void setWebpageUrl(String webpageUrl) {
		this.webpageUrl = webpageUrl;
	}


	public String getPosition() {
		return position;
	}


	public void setPosition(String position) {
		this.position = position;
	}


	public String getRightsNote() {
		return rightsNote;
	}


	public void setRightsNote(String rightsNote) {
		this.rightsNote = rightsNote;
	}


	public java.sql.Date getUseEndDate() {
		return useEndDate;
	}


	public void setUseEndDate(java.sql.Date useEndDate) {
		this.useEndDate = useEndDate;
	}


	public int getHeight() {
		return height;
	}


	public void setHeight(int height) {
		this.height = height;
	}


	public int getWidth() {
		return width;
	}


	public void setWidth(int width) {
		this.width = width;
	}


	public java.sql.Date getDateAdded() {
		return dateAdded;
	}


	public void setDateAdded(java.sql.Date dateAdded) {
		this.dateAdded = dateAdded;
	}


	public java.sql.Date getDateUpdated() {
		return dateUpdated;
	}


	public void setDateUpdated(java.sql.Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}


	public String getTerms() {
		return terms;
	}


	public void setTerms(String terms) {
		this.terms = terms;
	}
	
	
	
	
	
	
}
