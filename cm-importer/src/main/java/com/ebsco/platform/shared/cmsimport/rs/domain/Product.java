package com.ebsco.platform.shared.cmsimport.rs.domain;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString(callSuper = true)
public class Product extends ContentType {
	
	public static final String CONTENT_TYPE_UID = "product";
	
	public static final List<Product> RS_PRODUCTS = List.of(
			of("ers"),
			of("tol"),
			of("t6o"),
			of("t5o")
	);
	
	
	
	private String title;

	@Override
	public String getContentTypeUid() {
		return CONTENT_TYPE_UID;
	}
	
	public static Product of(String title) {
		Product result  = new Product();
		result.setTitle(title);
		return result;
	}
	
}
