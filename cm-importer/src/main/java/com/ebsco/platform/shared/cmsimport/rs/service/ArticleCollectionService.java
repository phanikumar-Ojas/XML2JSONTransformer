package com.ebsco.platform.shared.cmsimport.rs.service;

import java.util.Collection;

import com.ebsco.platform.shared.cmsimport.rs.domain.ArticleCollection;
import com.ebsco.platform.shared.cmsimport.rs.repository.ArticleCollectionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class ArticleCollectionService {

    private final ArticleCollectionRepository repository;

    public Collection<ArticleCollection> entries()  {
        log.info("Requesting Collection from DB");
        Collection<ArticleCollection> entries   = repository.getCollections();
        log.info("Found {} items", entries.size());
    	log.info("done");
    	return entries;
    }

}
