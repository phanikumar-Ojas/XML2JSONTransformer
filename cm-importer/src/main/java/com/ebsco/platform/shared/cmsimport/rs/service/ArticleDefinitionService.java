package com.ebsco.platform.shared.cmsimport.rs.service;

import java.util.Collection;

import com.ebsco.platform.shared.cmsimport.rs.domain.ArticleDefinition;
import com.ebsco.platform.shared.cmsimport.rs.domain.TitleSource;
import com.ebsco.platform.shared.cmsimport.rs.repository.ArticleDefinitionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class ArticleDefinitionService {

    private final ArticleDefinitionRepository repository;

    public Collection<ArticleDefinition> entries(DependOn<Collection<TitleSource>> titleSources) {
        log.info("Requesting ArticleDefinition from DB");
        Collection<ArticleDefinition> entries = repository.find(
        		new ArticleDefinitionRepository.TitleSourceReferenceBinder(titleSources.dependency()));
        log.info("Found {} items", entries.size());
        return entries;
    }
}
