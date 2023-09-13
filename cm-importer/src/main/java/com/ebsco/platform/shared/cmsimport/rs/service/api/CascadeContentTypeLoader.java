package com.ebsco.platform.shared.cmsimport.rs.service.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.ebsco.platform.shared.cmsimport.rs.domain.ContentType;
import com.ebsco.platform.shared.cmsimport.rs.domain.ContentTypeReference;
import com.ebsco.platform.shared.cmsimport.rs.domain.JsonRTE;
import com.ebsco.platform.shared.cmsimport.rs.domain.Media;
import com.ebsco.platform.shared.cmsimport.rs.domain.Media.Asset;
import com.ebsco.platform.shared.cmsimport.rs.domain.RTENode;
import com.ebsco.platform.shared.cmsimport.rs.service.JsonWriter;
import com.ebsco.platform.shared.cmsimport.rs.util.ContentTypeJsonSchemaValidator;
import com.ebsco.platform.shared.cmsimport.rs.util.ContentTypeUtil;
import com.ebsco.platform.shared.cmsimport.rs.util.PojoUtil;
import com.ebsco.platform.shared.cmsimport.utilities.AppPropertiesUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

@RequiredArgsConstructor
@Log4j2
public class CascadeContentTypeLoader {
    
    private static final String ASSET_FOLDER_PATH= AppPropertiesUtil.getProperty("ASSET_FOLDER_PATH");
    private static final String CONTENTSTACK_ASSET_FOLDER_NAME = AppPropertiesUtil.getProperty("CONTENTSTACK_ASSET_FOLDER_NAME");
    
    private final ContentTypeApi api;
    
    private String assetFolderUid;
    private JsonWriter jsonWriter = JsonWriter.DEFAULT;
    private Function<String, String> titleValueResolver = Function.identity();
    
    private final Map<String, Integer> entryCounts = new TreeMap<>(Comparator.naturalOrder());
    private final Set<String> unknownAssets = new TreeSet<>(Comparator.naturalOrder());
    
    
    private Map<String, Media> medias = new HashMap<>();
    
    public void sendToContentstack(ContentType entry) {
        Set<String> refPropNames = PojoUtil.fieldNamesOfTypeOrParametrizedType(ContentTypeReference.class, entry);
        for (String refPropName : refPropNames) {
            Object refObj = PojoUtil.get(entry, refPropName);
            if (Objects.nonNull(refObj)) {
                if (refObj instanceof Collection refValues) {
                    for (Object refValue : refValues) {
                        ContentTypeReference<ContentType> ref = (ContentTypeReference<ContentType>) refValue;
                        sendToContentstack(ref.getReferable());
                    }
                } else if (refObj instanceof ContentTypeReference refValue) {
                    ContentTypeReference<ContentType> ref = (ContentTypeReference<ContentType>) refValue;
                    sendToContentstack(ref.getReferable());
                }
            }
        }
        
        Set<String> jsonRtePropNames = PojoUtil.fieldNamesOfTypeOrParametrizedType(JsonRTE.class, entry);
        for (String jsonRtePropName : jsonRtePropNames) {
            JsonRTE refObj = PojoUtil.get(entry, jsonRtePropName);
            if (Objects.nonNull(refObj)) {
                List<RTENode> assetRefs = findAllAssetRefs(refObj);
                for (RTENode jsonRteAssetReference : assetRefs) {
                    Map<String, Object> attrs = jsonRteAssetReference.getAttrs();
                    String filename = (String) attrs.get("asset-name");
                    Media media = medias.get(filename);
                    if (Objects.nonNull(media)) {
                        if (Objects.isNull(media.getUid())) {
                            media = ContentTypeJsonSchemaValidator.validate(media);
                            
                            String modifiedTitle = titleValueResolver.apply(media.getTitle());
                            media.setTitle(modifiedTitle);
                            String bindFieldName = ContentTypeUtil.bindFieldName(media);
                            api.sendToContentstack(List.of(media), Media.CONTENT_TYPE_UID, bindFieldName, this::uploadAsset);
                            
                            Integer count = count(media.getContentTypeUid());
                            jsonWriter.write(count + ") ", media, bindFieldName);
                        }
                        if (Objects.isNull((String) attrs.get("asset-uid"))) {
                            Asset asset = media.getAsset();
                            attrs.put("asset-uid", asset.getUid());
                            attrs.put("asset-link", asset.getUrl());
                            attrs.put("asset-type", asset.getContentType());
                            
                            Map<String, Object> redactorAttrs = 
                                    (Map<String, Object>) attrs.get("redactor-attributes");
                            redactorAttrs.put("asset-uid", asset.getUid());
                            redactorAttrs.put("src", asset.getUrl());
                        }
                    } else {
                        if (StringUtils.isNoneBlank(filename)) {
                            String bindFieldValue = ContentTypeUtil.bindFieldValue(entry);
                            unknownAssets.add(entry.getContentTypeUid() + "-" + bindFieldValue +"-" +filename);
                        } else {
                            log.error("File name is blank");
                        }
                        log.error("Media asset file not found: {}", filename);
                        log.error("Fix JSON RTE reference(change 'reference' -> 'img')...");
                        jsonRteAssetReference.type("img");
                    }
                }
            }
        }
        
        if (Objects.isNull(entry.getUid())) {
            entry = ContentTypeJsonSchemaValidator.validate(entry);
            String title = PojoUtil.get(entry, "title");
            String modifiedTitle = titleValueResolver.apply(title);
            PojoUtil.set(entry, "title", modifiedTitle);
            String bindFieldName = ContentTypeUtil.bindFieldName(entry);
            api.sendToContentstack(List.of(entry), entry.getContentTypeUid(), bindFieldName);
            Integer count = count(entry.getContentTypeUid());
            jsonWriter.write(count + ") ", entry, bindFieldName);
        }
    }
    
    public Media uploadAsset(Media media) {
        Asset asset = media.getAsset();
        if (Objects.isNull(asset.getUid())) {
            JSONObject assetJson = api.getAssetByFileName(assetFolderUid(), asset.getFilename());
            if (Objects.isNull(assetJson)) {
                MultipartBody multipartBody = prepareMultipartBody(media);
                String response = api.sendAssetToContentstack(multipartBody);
                JSONObject json = new JSONObject(response);
                assetJson = json.getJSONObject("asset");
            }
            setFromJson(assetJson, asset);
            media.setImageFile(asset.getUid());
            Integer count = count("asset");
            log.info("asset=" + count);
        }
        return media;
    }
    
    private static void setFromJson(JSONObject source, Media.Asset destination) {
        destination.setUid(source.getString("uid"));
        destination.setFilename(source.getString("filename"));
        destination.setContentType(source.getString("content_type"));//mime type
        destination.setUrl(source.getString("url"));
    }
    
    private String assetFolderUid() {
        if (Objects.isNull(assetFolderUid)) {
            assetFolderUid = api.getAssetFolderUid(CONTENTSTACK_ASSET_FOLDER_NAME);
            if (Objects.isNull(assetFolderUid)) {
                assetFolderUid = api.createAssetFolder(CONTENTSTACK_ASSET_FOLDER_NAME);
            }
        }
        return assetFolderUid;
    }
    
    private MultipartBody prepareMultipartBody(Media media) {
        try {
            Path filePath = getFilePath(getFileName(media));
            
            //Files.copy(filePath, Path.of("D:\\workspaces\\0\\platform.shared.cms-import\\cm-importer\\src\\test\\resources\\test-integ\\input\\asset", getFileName(media)), StandardCopyOption.REPLACE_EXISTING);
            
            String mimeType = Files.probeContentType(filePath);
            File file = filePath.toFile();
            RequestBody fileBody = RequestBody.create(file, MediaType.parse(mimeType));
            MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("asset[upload]", file.getName(), fileBody)
                    .addFormDataPart("asset[parent_uid]", assetFolderUid())
                    .addFormDataPart("asset[title]", file.getName());

            if (media.getAsset().getDescription() != null) {
                bodyBuilder.addFormDataPart("asset[description]", media.getAsset().getDescription());
            }
            return bodyBuilder.build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static Collection<Media> havingFiles(Collection<Media> medias) {
        return medias.stream().filter(media -> getFilePath(getFileName(media)).toFile().exists()).toList();
    }
    
    private static String getFileName(Media media) {
        String filename = media.getAsset().getFilename();
        String extension = FilenameUtils.getExtension(filename);
        if (extension.isBlank()) {
            filename = filename + media.getFormat();
        }
        return filename;
    }
    
    private static Path getFilePath(String filename) {
        return Path.of(ASSET_FOLDER_PATH, filename);
    }
    
    private static List<RTENode> findAllAssetRefs(RTENode jsonRteValue) {
        List<RTENode> result = new ArrayList<>();
        if ("reference".equals(jsonRteValue.type())) {
            result.add(jsonRteValue);
        }
        List<RTENode> children = jsonRteValue.children();
        if (Objects.nonNull(children)) {
            for (RTENode child : children) {
                result.addAll(findAllAssetRefs(child));
            }
        }
        return result;
    }

    public CascadeContentTypeLoader jsonWriter(JsonWriter jsonWriter) {
        this.jsonWriter = jsonWriter;
        return this;
    }

    public CascadeContentTypeLoader titleValueResolver(Function<String, String> titleValueResolver) {
        this.titleValueResolver = titleValueResolver;
        return this;
    }

    public CascadeContentTypeLoader medias(Collection<Media> medias) {
        Collection<Media> havingFiles = havingFiles(medias);
        for (Media media : havingFiles) {
            String filename = media.getAsset().getFilename();
            Media found = this.medias.get(filename);
            if (Objects.isNull(found)) {
                this.medias.put(filename, media);
            } else {
                log.info("Found duplicates of media(image) entry of {}.  Merge them ...", filename);
                Media merged = PojoUtil.merge(new ArrayList<>(List.of(found, media)), false);
                this.medias.put(filename, merged);
            }
        }
        return this;
    }
    
    private Integer count(String key) {
        Integer counter = entryCounts.getOrDefault(key, 0);
        counter++;
        entryCounts.put(key, counter);
        return counter;
    }

    public Map<String, Integer> getEntryCounts() {
        return entryCounts;
    }

    public Set<String> getUnknownAssets() {
        return unknownAssets;
    }
}
