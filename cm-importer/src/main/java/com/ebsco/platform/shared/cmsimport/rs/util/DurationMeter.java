package com.ebsco.platform.shared.cmsimport.rs.util;

import org.apache.commons.lang3.time.DurationFormatUtils;

public class DurationMeter {
    
    private long startTimeMillis;
    private long stopTimeMillis;
    
    private DurationMeter(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    public static DurationMeter start() {
        return new DurationMeter(System.currentTimeMillis());
    }
    
    public String took() {
        stopTimeMillis = System.currentTimeMillis();
        return DurationFormatUtils.formatDurationHMS(stopTimeMillis - startTimeMillis);
    }
}
