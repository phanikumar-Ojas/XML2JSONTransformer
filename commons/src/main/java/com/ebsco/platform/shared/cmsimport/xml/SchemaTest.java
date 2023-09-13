package com.ebsco.platform.shared.cmsimport.xml;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SchemaTest {

	
	public static void main (String [] args) throws IOException {
		
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
				MediaType mediaType = MediaType.parse("application/json");
				RequestBody body = RequestBody.create(mediaType, "{\n\t\"content_type\": {\n\t\t\"title\": \"Article\",\n\t\t\"uid\": \"article\",\n\t\t\"schema\": [{\n\t\t\t\t\"display_name\": \"Title\",\n\t\t\t\t\"uid\": \"title\",\n\t\t\t\t\"data_type\": \"text\",\n\t\t\t\t\"field_metadata\": {\n\t\t\t\t\t\"_default\": true\n\t\t\t\t},\n\t\t\t\t\"unique\": false,\n\t\t\t\t\"mandatory\": true,\n\t\t\t\t\"multiple\": false\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"display_name\": \"URL\",\n\t\t\t\t\"uid\": \"url\",\n\t\t\t\t\"data_type\": \"text\",\n\t\t\t\t\"field_metadata\": {\n\t\t\t\t\t\"_default\": true\n\t\t\t\t},\n\t\t\t\t\"unique\": false,\n\t\t\t\t\"multiple\": false\n\t\t\t}\n\t\t],\n\t\t\"options\": {\n\t\t\t\"title\": \"title\",\n\t\t\t\"publishable\": true,\n\t\t\t\"is_page\": true,\n\t\t\t\"singleton\": false,\n\t\t\t\"sub_title\": [\n\t\t\t\t\"url\"\n\t\t\t],\n\t\t\t\"url_pattern\": \"/:title\",\n\t\t\t\"url_prefix\": \"/\"\n\t\t}\n\t}\n}");
				Request request = new Request.Builder()
				  .url("https://api.contentstack.io/v3/content_types/articledef")
				  .method("PUT", body)
				  .addHeader("api_key", "blt575edc14fa8659d9 ")
				  .addHeader("authorization", "csa326ded28162980439894439 ")
				  .addHeader("Content-Type", "application/json")
				  .build();
				Response response = client.newCall(request).execute();
				
				System.out.println(response.body().string());

	}
}
