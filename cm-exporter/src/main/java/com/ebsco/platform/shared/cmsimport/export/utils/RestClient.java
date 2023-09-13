package com.ebsco.platform.shared.cmsimport.export.utils;

import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Log4j2
public class RestClient {
    private static final OkHttpClient CLIENT = new OkHttpClient().newBuilder()
            .build();

    public static String sendGetRequest(String url) {
        return sendGetRequest(url, Collections.emptyMap());
    }

    public static String sendGetRequest(String url, Map<String, String> headers) {

        Request request = createBaseRequest(url, headers)
                .get()
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.warn("Api call " + request.method() + " " + request.url() + " is not successful: "
                        + response.code() + " " + response.message());
                return null;
            }
            ResponseBody body = response.body();

            return Objects.nonNull(body) ? body.string() : "";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendPostRequest(String url, Map<String, String> headers, RequestBody body) {

        Request request = createBaseRequest(url, headers)
                .post(body)
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.warn("Api call " + request.method() + " " + request.url() + " is not successful: "
                        + response.code() + " " + response.message());
            }
        } catch (IOException e) {
            log.error("Error sending POST request", e);
            throw new RuntimeException(e);
        }
    }

    private static Request.Builder createBaseRequest(String url, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder()
                .url(url);
        headers.forEach(builder::addHeader);
        return builder;
    }

}
