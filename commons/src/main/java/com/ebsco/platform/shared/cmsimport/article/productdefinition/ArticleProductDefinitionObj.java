package com.ebsco.platform.shared.cmsimport.article.productdefinition;

import java.sql.Date;

public class ArticleProductDefinitionObj {

	
	private String title;
	private String an;
	private String titleSource;
	private java.sql.Date dtformat; 
	


	public ArticleProductDefinitionObj(String title, String an, String titleSource, Date dtformat) {
		super();
		this.title = title;
		this.an = an;
		this.titleSource = titleSource;
		this.dtformat = dtformat;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAn() {
		return an;
	}

	public void setAn(String an) {
		this.an = an;
	}

	public String getTitleSource() {
		return titleSource;
	}

	public void setTitleSource(String titleSource) {
		this.titleSource = titleSource;
	}

	public java.sql.Date getDtformat() {
		return dtformat;
	}

	public void setDtformat(java.sql.Date dtformat) {
		this.dtformat = dtformat;
	}


	
	
	
	
}
