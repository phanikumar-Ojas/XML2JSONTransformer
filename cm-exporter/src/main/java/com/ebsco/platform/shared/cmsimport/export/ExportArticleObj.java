package com.ebsco.platform.shared.cmsimport.export;

import java.util.List;

import com.ebsco.platform.shared.cmsimport.article.ArticleObj;
import com.ebsco.platform.shared.cmsimport.collection.CollectionObj;
import com.ebsco.platform.shared.cmsimport.contributor.ContributorObj;

public class ExportArticleObj extends ArticleObj{

	
	private List<ContributorObj>expContributors;
	private List<CollectionObj> expCollections;
	private String mainBody;
	private String authorNote;
	private String citations;
	private String titleSourceUid;
	private String projectAn;
	private java.sql.Date projectDtFormat;

	private String url;
	
	public ExportArticleObj() {
		super();
	}


	public ExportArticleObj( List<ContributorObj> contributors2,
			 List<CollectionObj> collections2) {
		super();
		this.expContributors = contributors2;
		this.expCollections = collections2;
	}
	

	public List<ContributorObj> getExpContributors() {
		return expContributors;
	}

	public void setExpContributors(List<ContributorObj> expContributors) {
		this.expContributors = expContributors;
	}



	public List<CollectionObj> getExpCollections() {
		return expCollections;
	}

	public void setExpCollections(List<CollectionObj> expCollections) {
		this.expCollections = expCollections;
	}


	public String getMainBody() {
		return mainBody;
	}


	public void setMainBody(String mainBody) {
		this.mainBody = mainBody;
	}


	public String getAuthorNote() {
		return authorNote;
	}


	public void setAuthorNote(String authorNote) {
		this.authorNote = authorNote;
	}


	public String getCitations() {
		return citations;
	}


	public void setCitations(String citations) {
		this.citations = citations;
	}


	public String getTitleSourceUid() {
		return titleSourceUid;
	}


	public void setTitleSourceUid(String titleSourceUid) {
		this.titleSourceUid = titleSourceUid;
	}


	public String getProjectAn() {
		return projectAn;
	}


	public void setProjectAn(String projectAn) {
		this.projectAn = projectAn;
	}


	public java.sql.Date getProjectDtFormat() {
		return projectDtFormat;
	}


	public void setProjectDtFormat(java.sql.Date projectDtFormat) {
		this.projectDtFormat = projectDtFormat;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
