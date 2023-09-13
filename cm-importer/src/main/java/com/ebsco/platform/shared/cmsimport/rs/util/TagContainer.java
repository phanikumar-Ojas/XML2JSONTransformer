package com.ebsco.platform.shared.cmsimport.rs.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TagContainer {

    private static final int MAXIMUM_CHARACTER_LENGTH = 50;

    private static final int MAXIMUM_SIZE = 50;

    private final Set<String> tags = new HashSet<>();


    public void addTag(String tag) {
        if (tags.size() < 50) {
            tags.add(tag);
        }
    }

    public void addTags(Collection<String> tags) {
        List<String> filteredTags = Stream.ofNullable(tags)
                .flatMap(Collection::stream)
                .filter(Predicate.not(String::isBlank))
                .filter(tag -> tag.length() <= MAXIMUM_CHARACTER_LENGTH)
                .distinct()
                .toList();

        for (String tag : filteredTags) {
            if (this.tags.size() == MAXIMUM_SIZE) {
                break;
            }
            this.tags.add(tag);
        }
    }

    public Set<String> getTags() {
        return tags;
    }
}
