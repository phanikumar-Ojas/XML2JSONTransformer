package com.ebsco.platform.shared.cmsimport.export.utils;

import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;

public class UrlUtil {


    private static final String CONTENT_STACK_BASE_URL = AppPropertiesUtil.getProperty("contentstack-base-url");
    private static final String NEXTJS_APP_URL = AppPropertiesUtil.getProperty("nextjs-app-url");

    public static String getBaseCmsUrl() {
        return CONTENT_STACK_BASE_URL;
    }

    public static String getEntriesCmsUrl(String contentType) {
        return getBaseCmsUrl() + "/content_types/" + contentType + "/entries";
    }

    public static String getEntriesWithFilter(String contentType, String query) {
        return getEntriesCmsUrl(contentType) + "?query={" + query + "}";
    }

    public static String getArticleByIdUrl(String articleId) {
        return NEXTJS_APP_URL + articleId + "?pdf=true";
    }
}
