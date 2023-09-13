package com.ebsco.platform.shared.cmsimport.rs.util;

import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;
import lombok.extern.log4j.Log4j2;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http.RetryAndFollowUpInterceptor;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

@Log4j2
public class HttpClientUtil {
	
	private static final String MANAGEMENT_TOKEN = AppPropertiesUtil.getProperty("CONTENTSTACK_MANAGEMENT_TOKEN");
	private static final String API_KEY = AppPropertiesUtil.getProperty("CONTENTSTACK_API_KEY");
	
	private static final String API_MANAGEMENT_BASE_URL = "https://api.contentstack.io";
	private static final MediaType APPLICATION_JSON_MEDIA_TYPE = MediaType.parse("application/json");
	private static final Headers HEADERS = Headers.of(Map.of(
			"api_key", API_KEY,
			"authorization", MANAGEMENT_TOKEN,
			"Content-Type", APPLICATION_JSON_MEDIA_TYPE.toString()
	));
	
	private static boolean wasRatesLimitExceesed;
	
	private static final OkHttpClient HTTP = new OkHttpClient().newBuilder().addInterceptor(new Interceptor() {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            if (!response.isSuccessful() && response.code() == 429) {
                wasRatesLimitExceesed = true;
                log.info(" {} Rate limit exceeded, wait 200 millis and retry...", response.message());
                try {
                    response.close();
                    Thread.sleep(200);
                } catch (InterruptedException e) {}
                response = chain.proceed(chain.request());
            }
            return response;
        }}).build();
	
	public static String get(String relativeUrl) {
		String url = toAbsolute(relativeUrl);
		log.info("  GET {}", url);
		DurationMeter time = DurationMeter.start();
		try (Response response = HTTP.newCall(new Request.Builder().headers(HEADERS).get().url(url).build()).execute()) {
			String responseText = response.body().string();
			log.info("     ({}) RESPONSE {}", time.took(), responseText);
			return responseText;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String post(String relativeUrl, String body) {
		RequestBody requestBody = RequestBody.create(StringUtils.defaultString(body).getBytes(), APPLICATION_JSON_MEDIA_TYPE);
		return post(relativeUrl, requestBody, body);
	}
	
	public static String post(String relativeUrl, RequestBody requestBody) {
		return post(relativeUrl, requestBody, "Content-Type: " + requestBody.contentType());
	}

	private static String post(String relativeUrl, RequestBody requestBody, String requestBodyToLog) {
		String url = toAbsolute(relativeUrl);
		log.info("  POST {}", url);
		log.info("     BODY: {}", requestBodyToLog);
		Request request = new Request.Builder()
				.url(url)
				.post(requestBody)
				.headers(HEADERS)
				.build();
		DurationMeter time = DurationMeter.start();
		try(Response response = HTTP.newCall(request).execute()) {
			String responseText = response.body().string();
			log.info("  ({}) RESPONSE: {}", time.took(), responseText);
			return responseText;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String delete(String relativeUrl) {
		String url = toAbsolute(relativeUrl);
		log.info("  DELETE {}", url);
		DurationMeter time = DurationMeter.start();
		try (Response response = HTTP.newCall(new Request.Builder().headers(HEADERS).delete().url(url).build()).execute()) {
			String responseText = response.body().string();
			log.info("    ({}) RESPONSE {}", time.took(), responseText);
			return responseText;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String toAbsolute(String relativeUrl) {
		return baseUrl() + relativeUrl;
	}
	
	private static String baseUrl() {
		return API_MANAGEMENT_BASE_URL;
	}

    public static boolean wasRatesLimitExceesed() {
        return wasRatesLimitExceesed;
    }
}
