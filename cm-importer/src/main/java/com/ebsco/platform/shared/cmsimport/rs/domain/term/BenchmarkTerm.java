package com.ebsco.platform.shared.cmsimport.rs.domain.term;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BenchmarkTerm extends BasicTerm {

    public static final String CONTENT_TYPE_UID = "benchmark_term";

    @JsonProperty(value = "bench_mark_term")
    private String benchMarkTerm;

    public BenchmarkTerm(String benchMarkTerm) {
        super(benchMarkTerm);
        this.benchMarkTerm = benchMarkTerm;
    }

    @Override
    public String getContentTypeUid() {
        return CONTENT_TYPE_UID;
    }
}

