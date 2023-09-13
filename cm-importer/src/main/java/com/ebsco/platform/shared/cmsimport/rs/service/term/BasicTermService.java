package com.ebsco.platform.shared.cmsimport.rs.service.term;

import com.ebsco.platform.shared.cmsimport.rs.domain.term.BasicTerm;
import com.ebsco.platform.shared.cmsimport.rs.repository.term.BasicTermRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Log4j2
@RequiredArgsConstructor
public class BasicTermService {

    private final BasicTermRepository repository;

    public Map<String, Set<BasicTerm>> entries() {
        log.info("Reading {} from db ...", repository.getClass().getSimpleName());
        Map<String, Set<BasicTerm>> id2Terms = repository.find();
        List<BasicTerm> allTerms = id2Terms.values().stream()
                .flatMap(Collection::stream)
                .toList();
        log.info("Found {} items", allTerms.size());
        return id2Terms;
    }
}
