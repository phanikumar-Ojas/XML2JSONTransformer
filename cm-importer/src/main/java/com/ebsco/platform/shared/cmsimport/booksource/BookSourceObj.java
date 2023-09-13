package com.ebsco.platform.shared.cmsimport.booksource;

import java.sql.Date;

public class BookSourceObj {
	
	
	private String bookTitle;
	private String bookId;
	private String bookSeriesId;
	private String publisher;
	private String isbn;
	private String seriesIdXML;
	private java.sql.Date publicationDate;
	private String source;
	private String docType;
	private String bookNote;
	private int pubYear;
	private String publisherLocation;
	private String copyrightStatement;
	private String copyrightHolder;
	private java.sql.Date  dateAdded;
	private String dataFormat;
	private String rights;
	private String mid;
	


	public BookSourceObj(String bookTitle, String bookId, String bookSeriesId, String publisher, String isbn,
			String seriesIdXML, Date publicationDate, String source, String docType, String bookNote, int pubYear,
			String publicationLocation, String copyrightStatement, String copyrightHolder, Date dateAdded,
			String dataFormat, String rights, String mid) {
		super();
		this.bookTitle = bookTitle;
		this.bookId = bookId;
		this.bookSeriesId = bookSeriesId;
		this.publisher = publisher;
		this.isbn = isbn;
		this.seriesIdXML = seriesIdXML;
		this.publicationDate = publicationDate;
		this.source = source;
		this.docType = docType;
		this.bookNote = bookNote;
		this.pubYear = pubYear;
		this.publisherLocation = publicationLocation;
		this.copyrightStatement = copyrightStatement;
		this.copyrightHolder = copyrightHolder;
		this.dateAdded = dateAdded;
		this.dataFormat = dataFormat;
		this.rights = rights;
		this.mid = mid;
	}
	
	
	public BookSourceObj(String bookTitle, String bookId, String bookSeriesId, String publisher, Date publicationDate,
			String mid) {
		super();
		this.bookTitle = bookTitle;
		this.bookId = bookId;
		this.bookSeriesId = bookSeriesId;
		this.publisher = publisher;
		this.publicationDate = publicationDate;
		this.mid = mid;
	}

	
	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getBookTitle() {
		return bookTitle;
	}

	public void setBookTitle(String bookTitle) {
		this.bookTitle = bookTitle;
	}

	public String getBookId() {
		return bookId;
	}

	public void setBookId(String bookId) {
		this.bookId = bookId;
	}

	public String getBookSeriesId() {
		return bookSeriesId;
	}

	public void setBookSeriesId(String bookSeriesId) {
		this.bookSeriesId = bookSeriesId;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
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

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public String getBookNote() {
		return bookNote;
	}

	public void setBookNote(String bookNote) {
		this.bookNote = bookNote;
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

	public java.sql.Date getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(java.sql.Date dateAdded) {
		this.dateAdded = dateAdded;
	}

	public String getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
	}

	public String getRights() {
		return rights;
	}

	public void setRights(String rights) {
		this.rights = rights;
	}
	
	
	


}
