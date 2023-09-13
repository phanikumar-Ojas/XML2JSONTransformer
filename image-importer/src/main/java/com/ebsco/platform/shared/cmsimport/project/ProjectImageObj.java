package com.ebsco.platform.shared.cmsimport.project;

import java.util.List;

public class ProjectImageObj {

	
	
	//Caption	Credit	Content Type	Alt Text	Webpage URL	Position Inline	Image Notes	Art Spec	Tags (use commas to separate tags	Existing Contentstack Image Uid
	
	

	private String vendorSource;
	private String rights;
	private String copyrightNotes;
	private String license;
	private String caption;
	private String credit;
	private String contentType;
	private String altText;
	private List<String> webpageUrls;
	private String positionInline;
	private String imageNotes;
	private String artSpec;
	private List<String> tags;
	private String articleId;
	private String fileName;
	private String existingContentStackId;


	private String assetUid;
	
	public ProjectImageObj() {
		super();
	}



	public ProjectImageObj(String vendorSource, String rights, String copyrightNotes,
			String license, String caption, String credit, String contentType, String altText, List<String> webpageUrls,
			String positionInline, String imageNotes, String artSpec, List<String> tags, String articleId,
			String fileName, String existingContentStackId) {
		super();

		this.vendorSource = vendorSource;
		this.rights = rights;
		this.copyrightNotes = copyrightNotes;
		this.license = license;
		this.caption = caption;
		this.credit = credit;
		this.contentType = contentType;
		this.altText = altText;
		this.webpageUrls = webpageUrls;
		this.positionInline = positionInline;
		this.imageNotes = imageNotes;
		this.artSpec = artSpec;
		this.tags = tags;
		this.articleId = articleId;
		this.fileName = fileName;
		this.existingContentStackId = existingContentStackId;
	}



	public String getAssetUid() {
		return assetUid;
	}



	public void setAssetUid(String assetUid) {
		this.assetUid = assetUid;
	}



	public String getVendorSource() {
		return vendorSource;
	}



	public void setVendorSource(String vendorSource) {
		this.vendorSource = vendorSource;
	}



	public String getRights() {
		return rights;
	}



	public void setRights(String rights) {
		this.rights = rights;
	}



	public String getCopyrightNotes() {
		return copyrightNotes;
	}



	public void setCopyrightNotes(String copyrightNotes) {
		this.copyrightNotes = copyrightNotes;
	}



	public String getLicense() {
		return license;
	}



	public void setLicense(String license) {
		this.license = license;
	}



	public String getCaption() {
		return caption;
	}



	public void setCaption(String caption) {
		this.caption = caption;
	}



	public String getCredit() {
		return credit;
	}



	public void setCredit(String credit) {
		this.credit = credit;
	}



	public String getContentType() {
		return contentType;
	}



	public void setContentType(String contentType) {
		this.contentType = contentType;
	}



	public String getAltText() {
		return altText;
	}



	public void setAltText(String altText) {
		this.altText = altText;
	}



	public List<String> getWebpageUrl() {
		return webpageUrls;
	}



	public void setWebpageUrl(List<String> webpageUrls) {
		this.webpageUrls = webpageUrls;
	}



	public String getPositionInline() {
		return positionInline;
	}



	public void setPositionInline(String positionInline) {
		this.positionInline = positionInline;
	}



	public String getImageNotes() {
		return imageNotes;
	}



	public void setImageNotes(String imageNotes) {
		this.imageNotes = imageNotes;
	}



	public String getArtSpec() {
		return artSpec;
	}



	public void setArtSpec(String artSpec) {
		this.artSpec = artSpec;
	}



	public List<String> getTags() {
		return tags;
	}



	public void setTags(List<String> tags) {
		this.tags = tags;
	}



	public String getArticleId() {
		return articleId;
	}



	public void setArticleId(String articleId) {
		this.articleId = articleId;
	}



	public String getFileName() {
		return fileName;
	}



	public void setFileName(String fileName) {
		this.fileName = fileName;
	}



	public String getExistingContentStackId() {
		return existingContentStackId;
	}



	public void setExistingContentStackId(String existingContentStackId) {
		this.existingContentStackId = existingContentStackId;
	}



	





	
	
}
