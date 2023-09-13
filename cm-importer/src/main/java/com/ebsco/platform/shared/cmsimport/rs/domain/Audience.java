package com.ebsco.platform.shared.cmsimport.rs.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Audience {
    SECONDARY_PREFERRED("Public Schools"), CONSUMER_PREFERRED("Consumer Preferred"),
    CORPORATE_PREFERRED("Corporate Preferred"), ACADEMIC_PREFERRED("Academic Preferred"),
    T5O_PRODUCT_PREFERRED("K-5");


    private final String audienceName;

    Audience(String audienceName) {
        this.audienceName = audienceName;
    }

    @JsonValue
    public String getAudienceName() {
        return audienceName;
    }

    public static Audience of(String audiencePreferred) {
        if (audiencePreferred == null || audiencePreferred.isBlank()) {
            return null;
        }
        return Arrays.stream(Audience.values()).filter(x -> x.toString().equalsIgnoreCase(audiencePreferred))
                .findAny()
                .orElse(null);
    }


}
