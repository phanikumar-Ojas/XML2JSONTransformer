package com.ebsco.platform.shared.cmsimport.collection;

import java.sql.Date;

public class CollectionObj {

	
	private String collectionId;
	private String collectionTitle;
	private String publisher;
	private String seriesIdXML;
	private java.sql.Date publicationDate;
	private int pubYear;
	private String publisherLocation;
	private String copyrightStatement;
	private String copyrightHolder;
	private String rights;
	
	public CollectionObj(String collectionId, String collectionTitle, String publisher, String seriesIdXML,
			Date publicationDate, int pubYear, String publisherLocation, String copyrightStatement,
			String copyrightHolder, String rights) {
		super();
		this.collectionId = collectionId;
		this.collectionTitle = collectionTitle;
		this.publisher = publisher;
		this.seriesIdXML = seriesIdXML;
		this.publicationDate = publicationDate;
		this.pubYear = pubYear;
		this.publisherLocation = publisherLocation;
		this.copyrightStatement = copyrightStatement;
		this.copyrightHolder = copyrightHolder;
		this.rights = rights;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	public String getCollectionTitle() {
		return collectionTitle;
	}

	public void setCollectionTitle(String collectionTitle) {
		this.collectionTitle = collectionTitle;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getSeriesIdXML() {
		return seriesIdXML;
	}

	public void setSeriesIdXML(String seriesIdXML) {
		this.seriesIdXML = seriesIdXML;
	}

	public java.sql.Date getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(java.sql.Date publicationDate) {
		this.publicationDate = publicationDate;
	}

	public int getPubYear() {
		return pubYear;
	}

	public void setPubYear(int pubYear) {
		this.pubYear = pubYear;
	}

	public String getPublisherLocation() {
		return publisherLocation;
	}

	public void setPublisherLocation(String publisherLocation) {
		this.publisherLocation = publisherLocation;
	}

	public String getCopyrightStatement() {
		return copyrightStatement;
	}

	public void setCopyrightStatement(String copyrightStatement) {
		this.copyrightStatement = copyrightStatement;
	}

	public String getCopyrightHolder() {
		return copyrightHolder;
	}

	public void setCopyrightHolder(String copyrightHolder) {
		this.copyrightHolder = copyrightHolder;
	}

	public String getRights() {
		return rights;
	}

	public void setRights(String rights) {
		this.rights = rights;
	}
	
	

}
