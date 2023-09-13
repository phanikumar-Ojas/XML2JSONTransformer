package com.ebsco.platform.shared.cmsimport.rs.service.api;

import com.ebsco.platform.shared.cmsimport.rs.domain.ContentType;
import com.ebsco.platform.shared.cmsimport.rs.domain.ContentTypeMapper;
import com.ebsco.platform.shared.cmsimport.rs.util.CommonUtil;
import com.ebsco.platform.shared.cmsimport.rs.util.HttpClientUtil;
import com.ebsco.platform.shared.cmsimport.rs.util.PojoUtil;
import lombok.extern.log4j.Log4j2;
import okhttp3.MultipartBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
public class ContentTypeApi {
	
	private static final int PARTITION_SIZE = 100;
	private static final int MAX_BULK_ITEMS_SIZE = 10;
	private static final ContentTypeMapper JSON_MAPPER = new ContentTypeMapper();
	
	private boolean rewrite;
	
	private ContentstackUidCache uidCache = new ContentstackUidCache();
	
	public int getTotalCount(String contentTypeId) {
		String url = "/v3/content_types/" + contentTypeId +"/entries?count=true";
        String json = HttpClientUtil.get(url);
        return new JSONObject(json).getInt("entries");
	}
	
	public Map<String, String> getBindFieldValue2Uid(String contentTypeId, String bindFieldName, Collection<Object> values) {
		log.info("Request {} from contentstack", contentTypeId);
		
		Map<String, String> result = new HashMap<>();
		
		String url = "/v3/content_types/" + contentTypeId + 
				"/entries?query={\"" + bindFieldName + "\" : { \"$in\":[" + CommonUtil.quotateValues(values) + "]}}&only[BASE][]=" + bindFieldName;
		
        String json = HttpClientUtil.get(url);
        JSONArray entries = new JSONObject(json).getJSONArray("entries");
        for (int i = 0; i < entries.length(); i++) {
            JSONObject item = entries.getJSONObject(i);
            String uid = item.getString("uid");
            String title = item.getString(bindFieldName);
            result.put(title, uid);
        }
		log.info("Found {} of {} entries: {}", result.size(), values.size(), result);
		return result;
    }
	
	public <T extends ContentType> void sendToContentstack(List<T> entries, String contentTypeUid,
			String bindFieldName, Function<T, T> onPreLoad) {
		List<List<T>> partitions = CommonUtil.partition(entries, PARTITION_SIZE);
		int count = 0;
		for (List<T> p : partitions) {
			List<T> partition = p.stream().filter(item -> Objects.isNull(item.getUid())).toList();
			Collection<Object> bindFieldValues = partition.stream()
					.map(entry -> PojoUtil.getJsonField(entry, bindFieldName))
					.collect(Collectors.toSet());
			log.info("Retrieve existing items({}) from contentstack ...", contentTypeUid);
			Map<String, String> bindFieldValue2Uids = getBindFieldValue2Uid(contentTypeUid, bindFieldName, bindFieldValues);
			for (T item : partition) {
				count++;
				
				String bindFieldValue = PojoUtil.getJsonField(item, bindFieldName);
				if (Objects.nonNull(item.getUid())) {
					log.info("({})({})({}) same item was previously loaded", count, bindFieldValue, item);
					continue;
				}
				
				item = onPreLoad.apply(item);
				
				String uid = bindFieldValue2Uids.get(bindFieldValue);
				if (uid != null) {
					if (rewrite) {
						log.info("({}) ({}) found in contentstack, deleting... {}", count, uid, bindFieldValue);
						String url = "/v3/content_types/" + contentTypeUid + "/entries/" + uid;
						HttpClientUtil.delete(url);
					} else {
						log.info("({}) ({}) found in contentstack {}", count, uid, bindFieldValue);
						item.setUid(uid);
						continue;
					}
				}
				
				if (Objects.nonNull(item.getUid())) {
					log.info("({})({})({}) was loaded on pre-load action", count, bindFieldValue, item);
					continue;
				}
				
				log.info("({}) {}{{}={}} not found in contentstack, uploading ...",
						count, contentTypeUid, bindFieldName, bindFieldValue);
				
				String url = "/v3/content_types/" + contentTypeUid + "/entries?locale=en-us";
				String json = HttpClientUtil.post(url, JSON_MAPPER.map(item));

				JSONObject jo = new JSONObject(json);
				JSONObject entry = jo.getJSONObject("entry");
				item.setUid(entry.getString("uid"));
			}
		}
	}
	
	public <T extends ContentType> void sendToContentstack(T entry, String contentTypeUid,
            String bindFieldName, Function<T, T>...onPreLoad) {
	    String bindFieldValue = PojoUtil.getJsonField(entry, bindFieldName);
	    if (rewrite && Objects.nonNull(entry.getUid())) {
            log.info("({}) found in contentstack, deleting... {}", entry.getUid(), bindFieldValue);
            String url = "/v3/content_types/" + contentTypeUid + "/entries/" + entry.getUid();
            HttpClientUtil.delete(url);
            entry.setUid(null);
            uidCache.setUid(contentTypeUid, bindFieldValue, null);
        }
	    
	    for (Function<T, T> function : onPreLoad) {
	        entry = function.apply(entry);
        }
	    
        if (Objects.nonNull(entry.getUid())) {
            log.info("({})({}) was loaded on pre-load action", bindFieldValue, entry);
            return;
        }
        
        log.info("{}{{}={}}, creating ...", contentTypeUid, bindFieldName, bindFieldValue);
        
        String url = "/v3/content_types/" + contentTypeUid + "/entries?locale=en-us";
        
        String json = HttpClientUtil.post(url, JSON_MAPPER.map(entry));

        JSONObject respJson = new JSONObject(json).getJSONObject("entry");
        entry.setUid(respJson.getString("uid"));
        uidCache.setUid(contentTypeUid, bindFieldValue, entry.getUid());
    }

	public String sendAssetToContentstack(MultipartBody body) {
		String url = "/v3/assets?include_dimension=true";
		String response = HttpClientUtil.post(url, body);
		return response;
	}
	
	public String getAssetFolderUid(String folderName) {
	    String url = "/v3/assets?query={\"is_dir\": true, \"name\": \"" + folderName + "\"}";
        String response = HttpClientUtil.get(url);
        JSONObject json = new JSONObject(response);
        JSONArray assets = json.getJSONArray("assets");
        if (assets.length() == 0) {
            return null;
        }
        return assets.getJSONObject(0).getString("uid");
    }
	
	public JSONObject getAssetByFileName(String folderUid, String filename) {
        String url = "/v3/assets?query={\"is_dir\": false, \"parent_uid\": \"" + folderUid + "\", \"filename\": \""+filename+"\"}";
        String response = HttpClientUtil.get(url);
        JSONObject json = new JSONObject(response);
        JSONArray assets = json.getJSONArray("assets");
        if (assets.length() == 0) {
            return null;
        }
        return assets.getJSONObject(0);
    }
	
	public String createAssetFolder(String folderName) {
        String url = "/v3/assets/folders";
        String response = HttpClientUtil.post(url, String.format(
            """
            {
                "asset": {
                "name": "%s"
                }
            }
            """, folderName));
        JSONObject json = new JSONObject(response);
        JSONObject asset = json.getJSONObject("asset");
        return asset.getString("uid");
    }

	public <T extends ContentType> void sendToContentstack(List<T> entries, String contentTypeUid, String bindFieldName) {
		sendToContentstack(entries, contentTypeUid, bindFieldName, item -> item);
	}

	public <T extends ContentType> void loadUidsFromContentstack(List<T> entries, String contentTypeUid, String bindFieldName) {
		List<List<T>> partitions = CommonUtil.partition(entries, PARTITION_SIZE);
		int count = 1;
		for (List<T> partition : partitions) {
			Collection<Object> bindFieldValues = partition.stream()
					.map(entry -> PojoUtil.get(entry, bindFieldName)
					).collect(Collectors.toSet());
			log.info("Retrieve existing items({}) from contentstack ...", contentTypeUid);
			Map<String, String> bindFieldValue2Uids = getBindFieldValue2Uid(contentTypeUid, bindFieldName, bindFieldValues);
			for (T item : partition) {
				String bindFieldValue = PojoUtil.get(item, bindFieldName);
				String uid = bindFieldValue2Uids.get(bindFieldValue);
				if (uid != null) {
					item.setUid(uid);
					log.info("({}), found in contentstack {}", count++, item);
				} else {
					log.warn("({}) {{}={}} not found in contentstack", count++, bindFieldName, bindFieldValue);
				}
			}
		}
	}

	public ContentTypeApi rewrite(boolean rewrite) {
		this.rewrite = rewrite;
		return this;
	}
	
	public static void bulkDelete(Map<String, String> uid2ContentType) {
        if (Objects.isNull(uid2ContentType)) {
            return;
        }
        
        List<List<Entry<String, String>>> partitions = CommonUtil.partition(new ArrayList<>(uid2ContentType.entrySet()), MAX_BULK_ITEMS_SIZE);
        for (List<Entry<String, String>> partition : partitions) {
            JSONObject requestBody = new JSONObject();
            JSONArray entries = new JSONArray();

            for (Entry<String, String> entry  : partition) {
                JSONObject item = new JSONObject();
                item.put("content_type", entry.getValue());
                item.put("uid", entry.getKey());
                item.put("locale", "en-us");
                entries.put(item);

            }
            requestBody.put("entries", entries);
            
            long startTimeMillis = System.currentTimeMillis();
            
            HttpClientUtil.post("/v3/bulk/delete", requestBody.toString());
            
            ensureTimeRateLimits(startTimeMillis, 1000);
        }
    }
	
	public static void ensureTimeRateLimits(long startTimeMillis, long timeRateMillis) {
        long pauseMilis = timeRateMillis - (System.currentTimeMillis() - startTimeMillis);
        if (pauseMilis < 0) {
            return;
        }
        try {
            Thread.sleep(pauseMilis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
	
	private static class ContentstackUidCache {
	    
	    private Map<String, Map<String, String>> contentType2BindFieldValue2UidCache = new HashMap<>();
	    
	    public String getUid(String contentType, String bindFieldValue) {
	        Map<String, String> bindFieldValue2Uid = contentType2BindFieldValue2UidCache.get(contentType);
	        if (Objects.isNull(bindFieldValue2Uid)) {
	            return null;
	        }
	        
	        String uid = bindFieldValue2Uid.get(bindFieldValue2Uid);
	        return uid;
	    }
	    
	    public void setUid(String contentType, String bindFieldValue, String uid) {
            Map<String, String> bindFieldValue2Uid = contentType2BindFieldValue2UidCache.get(contentType);
            if (Objects.isNull(bindFieldValue2Uid)) {
                contentType2BindFieldValue2UidCache.put(contentType, bindFieldValue2Uid = new HashMap<>());
            }
            bindFieldValue2Uid.get(uid);
        }
	}
}
