package com.ebsco.platform.shared.cmsimport.contributor;

public class ContributorObj {

	
	
	private String fullName;
	
	private String firstName;
	
	private String lastName;
	private String degrees;
	
	
	
	
	public ContributorObj(String fullName, String firstName, String lastName, String degrees) {
		super();
		this.fullName = fullName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.degrees = degrees;
	}
	
	public ContributorObj(String fullName) {
		super();
		this.fullName = fullName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getDegrees() {
		return degrees;
	}

	public void setDegrees(String degrees) {
		this.degrees = degrees;
	}

	@Override
	public String toString() {
		return "ContributorObj [fullName=" + fullName + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", degrees=" + degrees + "]";
	}
	
	
	
	
}
