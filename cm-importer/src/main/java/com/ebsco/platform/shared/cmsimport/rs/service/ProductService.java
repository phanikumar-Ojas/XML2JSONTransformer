package com.ebsco.platform.shared.cmsimport.rs.service;

import java.util.Collection;

import com.ebsco.platform.shared.cmsimport.rs.domain.Product;
import com.ebsco.platform.shared.cmsimport.rs.service.api.ContentTypeApi;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class ProductService {
	
	private final ContentTypeApi api;
	
	public Collection<Product> importToContentstack() {
		log.info("Loading Products from contentstack ...");
    	api.loadUidsFromContentstack(Product.RS_PRODUCTS, Product.CONTENT_TYPE_UID, "title");
    	log.info("done");
    	return Product.RS_PRODUCTS;
    }
}
