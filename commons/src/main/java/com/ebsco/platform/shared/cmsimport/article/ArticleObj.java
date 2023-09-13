package com.ebsco.platform.shared.cmsimport.article;

import java.sql.Date;
import java.util.List;

public class ArticleObj {
	
	private String articleId;
	private String articleTitle;
	private String articleMid;
	private String mfsAn;
	private java.sql.Date dateCreated;
	private java.sql.Date reviewDate;
	private java.sql.Date lastUpdatedDate;
	private String region;
	private int  wordCount;
	private String derivedFromId;
	private String parentArticleId;
	private String altMid;
	private java.sql.Date previousDate;
	
	private String searchTitle;
	private String xmlVersionUi;
	private String lastUpdateType;
	private String dataType;
	private boolean researchStarter;
	private String primaryArticle;
	private String idOfPrimary;
	private String sidebar;
	private String image;
	private String artType;
	private java.sql.Date dateInRepository;
	private java.sql.Date dateInRepositoryUpdated;
	private String pdf;
	private String artTopic;
	private String rsTitle;
	private java.sql.Date dateAdded;
	private String updateCycle;
	private String status;
	private String sourceNote;
	
	private java.sql.Date copiedToBuild;
	private java.sql.Date updatedToBuild;
	private boolean doNotUse;
	private String usageNote;
	private boolean primaryPreferred;
	private boolean secondaryPreferred;
	private boolean consumerPreferred;
	private boolean corporatePreffered;
	private boolean academicPreffered;
	
	private String articleAn;
	private String rootAn;
	private String rsAn;
	private String bAn;
	
	private String brstTopic;
	private String brstCategory;
	private String rights;
	private List<String>contributors;
	private List<String>product;
	private String bookId;
	
	private List<String>articleDefinitions;
	
	
	private int lexile;
	private List<String>collections;

	//this is for mfs sync
	private List<String> mfsAns;
	private String ftAbstract;
	
	private String artSpec;
	
	//contributors
	public ArticleObj(String articleId, String articleTitle, String articleMid, String mfsAn, Date dateCreated,
			Date reviewDate, Date lastUpdatedDate, String region, int wordCount, String derivedFromId,
			String parentArticleId, String altMid, Date previousDate, String searchTitle,
			String xmlVersionUi, String lastUpdateType, String dataType, boolean researchStarter, String primaryArticle,
			String idOfPrimary, String sidebar, String image, String artType, Date dateInRepository,
			Date dateInRepositoryUpdated, String pdf, String artTopic, String rsTitle, Date dateAdded,
			String updateCycle, String status, String sourceNote, Date copiedToBuild, Date updatedToBuild,
			boolean doNotUse, String usageNote, boolean primaryPreferred, boolean secondaryPreferred,
			boolean consumerPreferred, boolean corporatePreffered, boolean academicPreffered, String articleAn,
			String rootAn, String rsAn, String bAn, String brstTopic, String brstCategory, String rights,
			List<String> contributors, List<String> product, String bookId, List<String>articleDefinitions, int lexile, List<String> collections, String ftAbstract) {
		super();
		this.articleId = articleId;
		this.articleTitle = articleTitle;
		this.articleMid = articleMid;
		this.mfsAn = mfsAn;
		this.dateCreated = dateCreated;
		this.reviewDate = reviewDate;
		this.lastUpdatedDate = lastUpdatedDate;
		this.region = region;
		this.wordCount = wordCount;
		this.derivedFromId = derivedFromId;
		this.parentArticleId = parentArticleId;
		this.altMid = altMid;
		this.previousDate = previousDate;
	
		this.searchTitle = searchTitle;
		this.xmlVersionUi = xmlVersionUi;
		this.lastUpdateType = lastUpdateType;
		this.dataType = dataType;
		this.researchStarter = researchStarter;
		this.primaryArticle = primaryArticle;
		this.idOfPrimary = idOfPrimary;
		this.sidebar = sidebar;
		this.image = image;
		this.artType = artType;
		this.dateInRepository = dateInRepository;
		this.dateInRepositoryUpdated = dateInRepositoryUpdated;
		this.pdf = pdf;
		this.artTopic = artTopic;
		this.rsTitle = rsTitle;
		this.dateAdded = dateAdded;
		this.updateCycle = updateCycle;
		this.status = status;
		this.sourceNote = sourceNote;
		this.copiedToBuild = copiedToBuild;
		this.updatedToBuild = updatedToBuild;
		this.doNotUse = doNotUse;
		this.usageNote = usageNote;
		this.primaryPreferred = primaryPreferred;
		this.secondaryPreferred = secondaryPreferred;
		this.consumerPreferred = consumerPreferred;
		this.corporatePreffered = corporatePreffered;
		this.academicPreffered = academicPreffered;
		this.articleAn = articleAn;
		this.rootAn = rootAn;
		this.rsAn = rsAn;
		this.bAn = bAn;
		this.brstTopic = brstTopic;
		this.brstCategory = brstCategory;
		this.rights = rights;
		this.contributors = contributors;
		this.product = product;
		this.bookId = bookId;
		this.articleDefinitions = articleDefinitions;
		this.lexile = lexile; 
		this.collections = collections;
		this.ftAbstract = ftAbstract;
	}
	public ArticleObj() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getArticleId() {
		return articleId;
	}
	public void setArticleId(String articleId) {
		this.articleId = articleId;
	}
	public String getArticleTitle() {
		return articleTitle;
	}
	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}
	public String getArticleMid() {
		return articleMid;
	}
	public void setArticleMid(String articleMid) {
		this.articleMid = articleMid;
	}
	public String getMfsAn() {
		return mfsAn;
	}
	public void setMfsAn(String mfsAn) {
		this.mfsAn = mfsAn;
	}
	public java.sql.Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(java.sql.Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public java.sql.Date getReviewDate() {
		return reviewDate;
	}
	public void setReviewDate(java.sql.Date reviewDate) {
		this.reviewDate = reviewDate;
	}
	public java.sql.Date getLastUpdatedDate() {
		return lastUpdatedDate;
	}
	public void setLastUpdatedDate(java.sql.Date lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public int getWordCount() {
		return wordCount;
	}
	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}
	public String getDerivedFromId() {
		return derivedFromId;
	}
	public void setDerivedFromId(String derivedFromId) {
		this.derivedFromId = derivedFromId;
	}
	public String getParentArticleId() {
		return parentArticleId;
	}
	public void setParentArticleId(String parentArticleId) {
		this.parentArticleId = parentArticleId;
	}
	public String getAltMid() {
		return altMid;
	}
	public void setAltMid(String altMid) {
		this.altMid = altMid;
	}
	public java.sql.Date getPreviousDate() {
		return previousDate;
	}
	public void setPreviousDate(java.sql.Date previousDate) {
		this.previousDate = previousDate;
	}

	public String getSearchTitle() {
		return searchTitle;
	}
	public void setSearchTitle(String searchTitle) {
		this.searchTitle = searchTitle;
	}
	public String getXmlVersionUi() {
		return xmlVersionUi;
	}
	public void setXmlVersionUi(String xmlVersionUi) {
		this.xmlVersionUi = xmlVersionUi;
	}
	public String getLastUpdateType() {
		return lastUpdateType;
	}
	public void setLastUpdateType(String lastUpdateType) {
		this.lastUpdateType = lastUpdateType;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public boolean isResearchStarter() {
		return researchStarter;
	}
	public void setResearchStarter(boolean researchStarter) {
		this.researchStarter = researchStarter;
	}
	public String getPrimaryArticle() {
		return primaryArticle;
	}
	public void setPrimaryArticle(String primaryArticle) {
		this.primaryArticle = primaryArticle;
	}
	public String getIdOfPrimary() {
		return idOfPrimary;
	}
	public void setIdOfPrimary(String idOfPrimary) {
		this.idOfPrimary = idOfPrimary;
	}
	public String getSidebar() {
		return sidebar;
	}
	public void setSidebar(String sidebar) {
		this.sidebar = sidebar;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getArtType() {
		return artType;
	}
	public void setArtType(String artType) {
		this.artType = artType;
	}
	public java.sql.Date getDateInRepository() {
		return dateInRepository;
	}
	public void setDateInRepository(java.sql.Date dateInRepository) {
		this.dateInRepository = dateInRepository;
	}
	public java.sql.Date getDateInRepositoryUpdated() {
		return dateInRepositoryUpdated;
	}
	public void setDateInRepositoryUpdated(java.sql.Date dateInRepositoryUpdated) {
		this.dateInRepositoryUpdated = dateInRepositoryUpdated;
	}
	public String getPdf() {
		return pdf;
	}
	public void setPdf(String pdf) {
		this.pdf = pdf;
	}
	public String getArtTopic() {
		return artTopic;
	}
	public void setArtTopic(String artTopic) {
		this.artTopic = artTopic;
	}
	public String getRsTitle() {
		return rsTitle;
	}
	public void setRsTitle(String rsTitle) {
		this.rsTitle = rsTitle;
	}
	public java.sql.Date getDateAdded() {
		return dateAdded;
	}
	public void setDateAdded(java.sql.Date dateAdded) {
		this.dateAdded = dateAdded;
	}
	public String getUpdateCycle() {
		return updateCycle;
	}
	public void setUpdateCycle(String updateCycle) {
		this.updateCycle = updateCycle;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getSourceNote() {
		return sourceNote;
	}
	public void setSourceNote(String sourceNote) {
		this.sourceNote = sourceNote;
	}
	public java.sql.Date getCopiedToBuild() {
		return copiedToBuild;
	}
	public void setCopiedToBuild(java.sql.Date copiedToBuild) {
		this.copiedToBuild = copiedToBuild;
	}
	public java.sql.Date getUpdatedToBuild() {
		return updatedToBuild;
	}
	public void setUpdatedToBuild(java.sql.Date updatedToBuild) {
		this.updatedToBuild = updatedToBuild;
	}
	public boolean isDoNotUse() {
		return doNotUse;
	}
	public void setDoNotUse(boolean doNotUse) {
		this.doNotUse = doNotUse;
	}
	public String getUsageNote() {
		return usageNote;
	}
	public void setUsageNote(String usageNote) {
		this.usageNote = usageNote;
	}
	public boolean isPrimaryPreferred() {
		return primaryPreferred;
	}
	public void setPrimaryPreferred(boolean primaryPreferred) {
		this.primaryPreferred = primaryPreferred;
	}
	public boolean isSecondaryPreferred() {
		return secondaryPreferred;
	}
	public void setSecondaryPreferred(boolean secondaryPreferred) {
		this.secondaryPreferred = secondaryPreferred;
	}
	public boolean isConsumerPreferred() {
		return consumerPreferred;
	}
	public void setConsumerPreferred(boolean consumerPreferred) {
		this.consumerPreferred = consumerPreferred;
	}
	public boolean isCorporatePreffered() {
		return corporatePreffered;
	}
	public void setCorporatePreffered(boolean corporatePreffered) {
		this.corporatePreffered = corporatePreffered;
	}
	public boolean isAcademicPreffered() {
		return academicPreffered;
	}
	public void setAcademicPreffered(boolean academicPreffered) {
		this.academicPreffered = academicPreffered;
	}
	public String getArticleAn() {
		return articleAn;
	}
	public void setArticleAn(String articleAn) {
		this.articleAn = articleAn;
	}
	public String getRootAn() {
		return rootAn;
	}
	public void setRootAn(String rootAn) {
		this.rootAn = rootAn;
	}
	public String getRsAn() {
		return rsAn;
	}
	public void setRsAn(String rsAn) {
		this.rsAn = rsAn;
	}
	public String getbAn() {
		return bAn;
	}
	public void setbAn(String bAn) {
		this.bAn = bAn;
	}
	public String getBrstTopic() {
		return brstTopic;
	}
	public void setBrstTopic(String brstTopic) {
		this.brstTopic = brstTopic;
	}
	public String getBrstCategory() {
		return brstCategory;
	}
	public void setBrstCategory(String brstCategory) {
		this.brstCategory = brstCategory;
	}
	public String getRights() {
		return rights;
	}
	public void setRights(String rights) {
		this.rights = rights;
	}
	public List<String> getContributors() {
		return contributors;
	}
	public void setContributors(List<String> contributors) {
		this.contributors = contributors;
	}
	public List<String> getProduct() {
		return product;
	}
	public void setProduct(List<String> product) {
		this.product = product;
	}
	public String getBookId() {
		return bookId;
	}
	public void setBookId(String bookId) {
		this.bookId = bookId;
	}
	
	
	
	public List<String> getArticleDefinitions() {
		return articleDefinitions;
	}
	public void setArticleDefinitions(List<String> articleDefinitions) {
		this.articleDefinitions = articleDefinitions;
	}
	
	
	
	public List<String> getMfsAns() {
		return mfsAns;
	}
	public void setMfsAns(List<String> mfsAns) {
		this.mfsAns = mfsAns;
	}
	
	
	
	public int getLexile() {
		return lexile;
	}
	public void setLexile(int lexile) {
		this.lexile = lexile;
	}
	
	
	public List<String> getCollections() {
		return collections;
	}
	public void setCollections(List<String> collections) {
		this.collections = collections;
	}
	
	
	
	public String getFtAbstract() {
		return ftAbstract;
	}
	public void setFtAbstract(String ftAbstract) {
		this.ftAbstract = ftAbstract;
	}
	
	
	
	public String getArtSpec() {
		return artSpec;
	}
	public void setArtSpec(String artSpec) {
		this.artSpec = artSpec;
	}
	
	@Override
	public String toString() {
		return "ArticleObj [articleId=" + articleId + ", articleTitle=" + articleTitle + ", articleMid=" + articleMid
				+ ", mfsAn=" + mfsAn + ", dateCreated=" + dateCreated + ", reviewDate=" + reviewDate
				+ ", lastUpdatedDate=" + lastUpdatedDate + ", region=" + region + ", wordCount=" + wordCount
				+ ", derivedFromId=" + derivedFromId + ", parentArticleId=" + parentArticleId + ", altMid=" + altMid
				+ ", previousDate=" + previousDate + ", searchTitle=" + searchTitle
				+ ", xmlVersionUi=" + xmlVersionUi + ", lastUpdateType=" + lastUpdateType + ", dataType=" + dataType
				+ ", researchStarter=" + researchStarter + ", primaryArticle=" + primaryArticle + ", idOfPrimary="
				+ idOfPrimary + ", sidebar=" + sidebar + ", image=" + image + ", artType=" + artType
				+ ", dateInRepository=" + dateInRepository + ", dateInRepositoryUpdated=" + dateInRepositoryUpdated
				+ ", pdf=" + pdf + ", artTopic=" + artTopic + ", rsTitle=" + rsTitle + ", dateAdded=" + dateAdded
				+ ", updateCycle=" + updateCycle + ", status=" + status + ", sourceNote=" + sourceNote
				+ ", copiedToBuild=" + copiedToBuild + ", updatedToBuild=" + updatedToBuild + ", doNotUse=" + doNotUse
				+ ", usageNote=" + usageNote + ", primaryPreferred=" + primaryPreferred + ", secondaryPreferred="
				+ secondaryPreferred + ", consumerPreferred=" + consumerPreferred + ", corporatePreffered="
				+ corporatePreffered + ", academicPreffered=" + academicPreffered + ", articleAn=" + articleAn
				+ ", rootAn=" + rootAn + ", rsAn=" + rsAn + ", bAn=" + bAn + ", brstTopic=" + brstTopic
				+ ", brstCategory=" + brstCategory + ", rights=" + rights + ", contributors=" + toTxt(contributors)
				+ ", product=" + toTxt(product )+ ", bookId=" + bookId + "]";
	}
	private String toTxt(List<String> strs) {
		if (strs==null) {
			return "";
		}
		if (strs.size()==0) {
			return "";
		}
		// TODO Auto-generated method stub
		String txt = "";
		for (String s : strs) {
			txt+=s+",";
		}
		txt=txt.substring(0,txt.length()-1);
		return txt;
	}
	
	//product
	//book id
	
	
	
	
}
