package com.ebsco.platform.shared.cmsimport.project;

import java.util.Date;

public class NewProjectArticleObj {

	private String csId;
	private String mid;
	private String articleTitle;
	private int targetWordCount;
	
	private String subject;
	private String productCode;
	private String topicType;
	private String existingTopicId;
	
	private String newTopicTitle;
	private String projectOwnerNotes;
	private String region;
	private String workType;
	
	private boolean urgent;
	private String assignedTo;	
	private String updateCycle;
	private String usageNote;
	private String audience;
	
	private String imageStatus;
	private String bulkImagePlacement;
	private String artSpec;
	private String workflowStage;
	private String reuseEntryId;
	private String rsAn;
	private String articleUid;
	private String collectionName; 	
	private Date writerDueDate;


	public NewProjectArticleObj( String articleTitle, int targetWordCount, String subject, 
			String productCode, String topicType, String existingTopicId, String newTopicTitle,
			String projectOwnerNotes,  String region, String workType, boolean urgent,
			 String updateCycle, String usageNote, String audience, String imageStatus,
			String bulkImagePlacement, String artSpec, String reuseEntryId) {
		super();

		this.articleTitle = articleTitle;
		this.targetWordCount = targetWordCount;
		this.subject = subject;
		this.productCode = productCode;
		this.topicType = topicType;
		this.existingTopicId = existingTopicId;
		this.newTopicTitle = newTopicTitle;
		this.projectOwnerNotes = projectOwnerNotes;
		this.region = region;
		this.workType = workType;
		this.urgent = urgent;
		this.updateCycle = updateCycle;
		this.usageNote = usageNote;
		this.audience = audience;
		this.imageStatus = imageStatus;
		this.bulkImagePlacement = bulkImagePlacement;
		this.artSpec = artSpec;
		this.reuseEntryId = reuseEntryId;
	}



	public NewProjectArticleObj() {
		super();
	}



	public String getCsId() {
		return csId;
	}


	public void setCsId(String csId) {
		this.csId = csId;
	}


	public String getMid() {
		return mid;
	}


	public void setMid(String mid) {
		this.mid = mid;
	}


	public String getArticleTitle() {
		return articleTitle;
	}


	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}


	public int getTargetWordCount() {
		return targetWordCount;
	}


	public void setTargetWordCount(int targetWordCount) {
		this.targetWordCount = targetWordCount;
	}


	public String getSubject() {
		return subject;
	}


	public void setSubject(String subject) {
		this.subject = subject;
	}



	public String getProductCode() {
		return productCode;
	}


	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}


	public String getTopicType() {
		return topicType;
	}


	public void setTopicType(String topicType) {
		this.topicType = topicType;
	}


	public String getExistingTopicId() {
		return existingTopicId;
	}


	public void setExistingTopicId(String existingTopicId) {
		this.existingTopicId = existingTopicId;
	}


	public String getNewTopicTitle() {
		return newTopicTitle;
	}


	public void setNewTopicTitle(String newTopicTitle) {
		this.newTopicTitle = newTopicTitle;
	}


	public String getProjectOwnerNotes() {
		return projectOwnerNotes;
	}


	public void setProjectOwnerNotes(String projectOwnerNotes) {
		this.projectOwnerNotes = projectOwnerNotes;
	}





	public String getRegion() {
		return region;
	}


	public void setRegion(String region) {
		this.region = region;
	}


	public String getWorkType() {
		return workType;
	}


	public void setWorkType(String workType) {
		this.workType = workType;
	}


	public boolean isUrgent() {
		return urgent;
	}


	public void setUrgent(boolean urgent) {
		this.urgent = urgent;
	}


	public String getAssignedTo() {
		return assignedTo;
	}


	public void setAssignedTo(String assignedTo) {
		this.assignedTo = assignedTo;
	}


	public String getUpdateCycle() {
		return updateCycle;
	}


	public void setUpdateCycle(String updateCycle) {
		this.updateCycle = updateCycle;
	}


	public String getUsageNote() {
		return usageNote;
	}


	public void setUsageNote(String usageNote) {
		this.usageNote = usageNote;
	}


	public String getAudience() {
		return audience;
	}


	public void setAudience(String audience) {
		this.audience = audience;
	}


	public String getImageStatus() {
		return imageStatus;
	}


	public void setImageStatus(String imageStatus) {
		this.imageStatus = imageStatus;
	}


	public String getBulkImagePlacement() {
		return bulkImagePlacement;
	}


	public void setBulkImagePlacement(String bulkImagePlacement) {
		this.bulkImagePlacement = bulkImagePlacement;
	}


	public String getArtSpec() {
		return artSpec;
	}


	public void setArtSpec(String artSpec) {
		this.artSpec = artSpec;
	}


	public String getWorkflowStage() {
		return workflowStage;
	}


	public void setWorkflowStage(String workflowStage) {
		this.workflowStage = workflowStage;
	}


	public String getReuseEntryId() {
		return reuseEntryId;
	}


	public void setReuseEntryId(String reuseEntryId) {
		this.reuseEntryId = reuseEntryId;
	}


	public String getRsAn() {
		return rsAn;
	}


	public void setRsAn(String rsAn) {
		this.rsAn = rsAn;
	}



	public String getArticleUid() {
		return articleUid;
	}



	public void setArticleUid(String articleUid) {
		this.articleUid = articleUid;
	}



	public String getCollectionName() {
		return collectionName;
	}



	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}
	
	
	
	public Date getWriterDueDate() {
		return writerDueDate;
	}



	public void setWriterDueDate(Date writerDueDate) {
		this.writerDueDate = writerDueDate;
	}

	
	
}
