package com.ebsco.platform.shared.cmsimport.rs.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.ebsco.platform.shared.cmsimport.rs.config.HTMLCharacterEscapes;
import com.ebsco.platform.shared.cmsimport.rs.domain.ContentType;
import com.ebsco.platform.shared.cmsimport.rs.util.PojoUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TestJsonFileWriter implements JsonWriter {
    
    private static final String ILLEGAL_FILENAME_CHARS_REGEX = "[/\\?%*:|\"'<>.]";

    private static final ObjectMapper JACKSON = configureMapper();

    private String rootFolder;

    private String currentRootFolder;

    public TestJsonFileWriter(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    public TestJsonFileWriter() {
        this(createTmpFolder());
    }

    @Override
    public void write(Map<String, ? extends ContentType> fileName2ContentType) {
        int count = 1;
        for (Map.Entry<String, ? extends ContentType> row : fileName2ContentType.entrySet()) {
            try {
                ContentType entry = row.getValue();
                String jsonFileName = row.getKey() + ".json";
                String contentTypeFolder = entry.getContentTypeUid();
                Path pathToFile = Paths.get(currentRootFolder(), ensureFolder(contentTypeFolder), jsonFileName);
                log.info("({} of {}) {}", count++, fileName2ContentType.size(), pathToFile.toAbsolutePath());
                String json = toJson(entry);
                writeStringToFile(pathToFile, json);
            } catch (Exception e) {
                log.error("Can't create json file", e);
            }
        }
    }
    
    @Override
    public void write(String logPrefix, ContentType entry, String bindFieldName) {
        try {
            String fileName = StringUtils.defaultIfBlank(PojoUtil.getJsonField(entry, bindFieldName), bindFieldName + "-" + UUID.randomUUID().toString()) ;
            fileName = filenameEncode(fileName);
            String jsonFileName = fileName + ".json";
            String contentTypeFolder = entry.getContentTypeUid();
            Path pathToFile = Paths.get(currentRootFolder(), ensureFolder(contentTypeFolder), jsonFileName);
            log.info("{}{}", logPrefix, pathToFile.toAbsolutePath());
            String json = toJson(entry);
            writeStringToFile(pathToFile, json);
        } catch (Exception e) {
            log.error("Can't create json file", e);
        }
    }

    public String ensureFolder(String folderName) {
        return ensureFolder(currentRootFolder(), folderName);
    }

    public static String ensureFolder(String rootPath, String folderName) {
        Path folderPath = Paths.get(rootPath, folderName);
        if (Files.notExists(folderPath)) {
            try {
                Files.createDirectory(folderPath);
            } catch (IOException e) {
                log.error("", e);
            }
        }
        return folderName;
    }

    public static void writeStringToFile(Path pathTofile, String value) {
        try {
            if (Files.notExists(pathTofile)) {
                Files.writeString(pathTofile, value);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toJson(Object pojo) {
        try {
            return JACKSON.writeValueAsString(pojo);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String currentRootFolder() {
        if (Objects.isNull(currentRootFolder)) {
            currentRootFolder = rootFolder + ensureFolder(rootFolder, UUID.randomUUID().toString());
        }
        return currentRootFolder;
    }

    private static String createTmpFolder() {
        try {
            File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "rs-import",
                    LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            File dir = FileUtils.createParentDirectories(file);
            file = new File(file, LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            dir = FileUtils.createParentDirectories(file);
            return dir.getAbsolutePath() + File.separator;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ObjectMapper configureMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.getFactory().setCharacterEscapes(new HTMLCharacterEscapes());
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        return objectMapper;
    }
    
    private static String filenameEncode(String string) {
        if (string != null) {
            string = string.replaceAll(ILLEGAL_FILENAME_CHARS_REGEX, "");
            if (string.length() > 255) {
                string = string.substring(0, 255);
            }
        }

        return string;
    }
}
