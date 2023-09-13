package com.ebsco.platform.shared.cmsimport.rs.service;

import java.util.Collection;
import java.util.List;

import com.ebsco.platform.shared.cmsimport.rs.domain.Product;
import com.ebsco.platform.shared.cmsimport.rs.domain.TitleSource;
import com.ebsco.platform.shared.cmsimport.rs.repository.NumCode2ProductBinder;
import com.ebsco.platform.shared.cmsimport.rs.repository.TitleSourceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class TitleSourceService {

    private final TitleSourceRepository repository;

    public Collection<TitleSource> entries(DependOn<Collection<Product>> products) {
    	log.info("Reading info from DB ...");
    	List<TitleSource> entries = repository.find(new NumCode2ProductBinder(products.dependency()));
    	log.info("Found: {} items", entries.size());
    	
    	log.info("done");
    	return entries;
    }
}
