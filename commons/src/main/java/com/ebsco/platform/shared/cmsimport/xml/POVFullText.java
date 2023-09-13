package com.ebsco.platform.shared.cmsimport.xml;

public class POVFullText {

	
	
	private String bodyTxt;
	private String bibliographyTxt;	
	private String authorNoteTxt;
	
	public POVFullText(String bodyTxt, String bibliographyTxt, String authorNoteTxt) {
		super();
		this.bodyTxt = bodyTxt;
		this.bibliographyTxt = bibliographyTxt;
		this.authorNoteTxt = authorNoteTxt;
	}

	public String getBodyTxt() {
		return bodyTxt;
	}

	public void setBodyTxt(String bodyTxt) {
		this.bodyTxt = bodyTxt;
	}

	public String getBibliographyTxt() {
		return bibliographyTxt;
	}

	public void setBibliographyTxt(String bibliographyTxt) {
		this.bibliographyTxt = bibliographyTxt;
	}

	public String getAuthorNoteTxt() {
		return authorNoteTxt;
	}

	public void setAuthorNoteTxt(String authorNoteTxt) {
		this.authorNoteTxt = authorNoteTxt;
	}
	
	
	
	
}
