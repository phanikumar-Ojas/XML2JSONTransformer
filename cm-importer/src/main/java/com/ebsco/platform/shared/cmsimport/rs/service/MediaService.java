package com.ebsco.platform.shared.cmsimport.rs.service;

import java.util.Collection;
import java.util.List;

import com.ebsco.platform.shared.cmsimport.rs.domain.Media;
import com.ebsco.platform.shared.cmsimport.rs.repository.MediaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class MediaService {

	private final MediaRepository repository;

    public Collection<Media> entries() {
		log.info("Reading media from db ...");
		List<Media> items = repository.find();
		log.info("Found {} items", items.size());
		log.info("done");
        return items;
	}
}
