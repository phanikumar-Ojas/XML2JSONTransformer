package com.ebsco.platform.shared.cmsimport.utilities;

import java.util.HashMap;
import java.util.Map;

public class ContentstackHelper {

    private static final Map<String, String> STANDARD_CONTENTSTACK_HEADERS = new HashMap<>();

    static {
        STANDARD_CONTENTSTACK_HEADERS.put("api_key", AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY"));
        STANDARD_CONTENTSTACK_HEADERS.put("authorization", AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN"));
        STANDARD_CONTENTSTACK_HEADERS.put("Content-Type", "application/json");
    }

    public static Map<String, String> getContentstackHeaders() {
        return new HashMap<>(STANDARD_CONTENTSTACK_HEADERS);
    }
}
