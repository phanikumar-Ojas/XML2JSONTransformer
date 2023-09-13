package com.ebsco.platform.shared.cmsimport.rs.service.term;

import com.ebsco.platform.shared.cmsimport.rs.domain.ContentTypeReference;
import com.ebsco.platform.shared.cmsimport.rs.domain.term.AltMainTerm;
import com.ebsco.platform.shared.cmsimport.rs.domain.term.BasicTerm;
import com.ebsco.platform.shared.cmsimport.rs.domain.term.BenchmarkTerm;
import com.ebsco.platform.shared.cmsimport.rs.domain.term.PrimaryTerm;
import com.ebsco.platform.shared.cmsimport.rs.domain.term.SearchTermSet;
import com.ebsco.platform.shared.cmsimport.rs.domain.term.SubjectGeoTerm;
import com.ebsco.platform.shared.cmsimport.rs.domain.term.SubjectTerm;
import com.ebsco.platform.shared.cmsimport.rs.repository.term.SearchTermSetRepository;
import com.ebsco.platform.shared.cmsimport.rs.util.TagContainer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@RequiredArgsConstructor
public class SearchTermSetService {

    private final SearchTermSetRepository repository;

    public Collection<SearchTermSet> entries(Map<String, Set<BasicTerm>> mfsAn2Terms, Map<String, Set<BasicTerm>> articleId2Terms) {

        log.info("Reading SearchTermSet from db ...");
        Collection<SearchTermSet> items = repository.find();
        log.info("Found {} items", items.size());
        
        Map<String, PrimaryTerm> searchTermSetTitle2PrimaryArticleTitleTerm = new HashMap<>();

        for (SearchTermSet searchTermSet : items) {
            String articleId = searchTermSet.getArticleId();
            String mfsAn = searchTermSet.getArticleAn().toString();

            List<ContentTypeReference<PrimaryTerm>> primaryTerms = getTerms(articleId2Terms.get(articleId), PrimaryTerm.class);
            for (ContentTypeReference<PrimaryTerm> primaryTermRef : primaryTerms) {
                PrimaryTerm referable = primaryTermRef.getReferable();
                searchTermSetTitle2PrimaryArticleTitleTerm.put(referable.getTitle(), referable);
            }
            
            searchTermSet.setPrimaryTerm(primaryTerms);
            
            searchTermSet.setAltTerm(getTerms(articleId2Terms.get(articleId), AltMainTerm.class));
            searchTermSet.setSubjectTerm(getTerms(mfsAn2Terms.get(mfsAn), SubjectTerm.class));
            searchTermSet.setSubjectGeoTerm(getTerms(mfsAn2Terms.get(mfsAn), SubjectGeoTerm.class));
            searchTermSet.setBenchmarkTerm(getTerms(mfsAn2Terms.get(mfsAn), BenchmarkTerm.class));

            populateSetWithTags(searchTermSet);

        }
        
        for (SearchTermSet searchTermSet : items) {
            PrimaryTerm primaryTerm = searchTermSetTitle2PrimaryArticleTitleTerm.get(searchTermSet.getTitle());
            if (Objects.isNull(primaryTerm)) {
                primaryTerm = new PrimaryTerm(searchTermSet.getTitle());
                searchTermSetTitle2PrimaryArticleTitleTerm.put(searchTermSet.getTitle(), primaryTerm);
            }
            searchTermSet.setPrimaryArticleTitleTerm(List.of(new ContentTypeReference<>(primaryTerm)));
        }
        
        log.info("done");
        return items;
    }

    private static void populateSetWithTags(SearchTermSet currentSet) {
        TagContainer tagContainer = new TagContainer();
        tagContainer.addTag(currentSet.getArticleTitle().substring(0, currentSet.getArticleTitle().length() - 1));
        tagContainer.addTags(getTagsFromTerms(currentSet.getPrimaryTerm()));
        tagContainer.addTags(getTagsFromTerms(currentSet.getAltTerm()));
        tagContainer.addTags(getTagsFromTerms(currentSet.getSubjectTerm()));
        tagContainer.addTags(getTagsFromTerms(currentSet.getSubjectGeoTerm()));
        tagContainer.addTags(getTagsFromTerms(currentSet.getBenchmarkTerm()));
        currentSet.setTags(tagContainer.getTags());
    }

    private <T extends BasicTerm> List<ContentTypeReference<T>> getTerms(Collection<BasicTerm> terms, Class<T> clazz) {
        return Stream.ofNullable(terms)
                .flatMap(Collection::stream)
                .filter(t -> t.getClass() == clazz)
                .map(clazz::cast)
                .map(ContentTypeReference::new).collect(Collectors.toList());
    }

    private static <T extends BasicTerm> List<String> getTagsFromTerms(List<ContentTypeReference<T>> terms) {
        return Stream.ofNullable(terms)
                .flatMap(Collection::stream)
                .map(ContentTypeReference::getReferable)
                .map(BasicTerm::getTitle)
                .toList();
    }
}
