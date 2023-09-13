package com.ebsco.platform.shared.cmsimport.rs.service;

import java.util.Map;
import java.util.Set;

import com.ebsco.platform.shared.cmsimport.rs.domain.Contributor;
import com.ebsco.platform.shared.cmsimport.rs.xml.ContributorXmlReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class ContributorService {

    private final ContributorXmlReader contributorXmlReader;

    public Map<String, Set<Contributor>> entries() {
        log.info("Reading Contributors from xml files");
        Map<String, Set<Contributor>> articleId2contributors = contributorXmlReader.getContributors();
        log.info("Created {} articleId2contributors", articleId2contributors.size());
    	
    	log.info("done");
    	return articleId2contributors;
    }
}
