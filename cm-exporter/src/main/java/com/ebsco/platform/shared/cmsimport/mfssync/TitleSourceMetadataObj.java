package com.ebsco.platform.shared.cmsimport.mfssync;

import java.sql.Date;

public class TitleSourceMetadataObj {

	
	private String title; //book title
	private String mid; //mmid
	private String bookId; //book id
	private java.sql.Date pubDate; //pub date
	private String publisher; //publisher
	private int seriesId;//series id
	
	public TitleSourceMetadataObj(String title, String mid, String bookId, Date pubDate, String publisher,
			int seriesId) {
		super();
		this.title = title;
		this.mid = mid;
		this.bookId = bookId;
		this.pubDate = pubDate;
		this.publisher = publisher;
		this.seriesId = seriesId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getBookId() {
		return bookId;
	}

	public void setBookId(String bookId) {
		this.bookId = bookId;
	}

	public java.sql.Date getPubDate() {
		return pubDate;
	}

	public void setPubDate(java.sql.Date pubDate) {
		this.pubDate = pubDate;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public int getSeriesId() {
		return seriesId;
	}

	public void setSeriesId(int seriesId) {
		this.seriesId = seriesId;
	}

	@Override
	public String toString() {
		return "TitleSourceMetadataObj [title=" + title + ", mid=" + mid + ", bookId=" + bookId + ", pubDate=" + pubDate
				+ ", publisher=" + publisher + ", seriesId=" + seriesId + "]";
	}
	
	


	
	
	
}
