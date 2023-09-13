package com.ebsco.platform.shared.cmsimport.project;

public class UpdateProjectArticleObj {

	private String articleTitle;
	private String articleUid;
	private double taskHours;
	private String notes;
	private String newDate;
	
	
	
	public UpdateProjectArticleObj(String articleTitle, String articleUid, double taskHours, String notes, String newDate) {
		super();
		this.articleTitle = articleTitle;
		this.articleUid = articleUid;
		this.taskHours = taskHours;
		this.notes = notes;
		this.newDate = newDate;
	}



	public String getArticleTitle() {
		return articleTitle;
	}



	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}



	public String getArticleUid() {
		return articleUid;
	}



	public void setArticleUid(String articleUid) {
		this.articleUid = articleUid;
	}



	public double getTaskHours() {
		return taskHours;
	}



	public void setTaskHours(double taskHours) {
		this.taskHours = taskHours;
	}



	public String getNotes() {
		return notes;
	}



	public void setNotes(String notes) {
		this.notes = notes;
	}



	public String getNewDate() {
		return newDate;
	}



	public void setNewDate(String newDate) {
		this.newDate = newDate;
	}



	@Override
	public String toString() {
		return "UpdateProjectArticleObj [articleTitle=" + articleTitle + ", an=" + articleUid + ", taskHours=" + taskHours
				+ ", notes=" + notes + ", newDate=" + newDate + "]";
	}
	

	
	
}
