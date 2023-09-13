package com.ebsco.platform.shared.cmsimport.rs.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import com.ebsco.platform.shared.cmsimport.rs.config.HTMLCharacterEscapes;
import com.ebsco.platform.shared.cmsimport.rs.domain.Media;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ConfigurationUtil {
	
    private static final ObjectMapper OBJECT_MAPPER = configureMapper();

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
    
    private static ObjectMapper configureMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.getFactory().setCharacterEscapes(new HTMLCharacterEscapes());
        
        return objectMapper;
    }
    
    public static List<String> fileNames(String dir, String...fileExtention) {
        try {
            return Files.list(Paths.get(dir))
                    .map(path -> path.toFile())
                    .filter(file -> {
                        if (fileExtention.length == 0) {
                            return !file.isDirectory();
                        }
                        String extension = FilenameUtils.getExtension(file.getName());
                        boolean knownExtention = Objects.nonNull(Arrays.stream(fileExtention).filter(ext -> ext.equalsIgnoreCase(extension)).findAny().orElse(null));
                        return !file.isDirectory() && knownExtention;
                    })
                    .map(file -> FilenameUtils.getBaseName(file.getName())).collect(Collectors.toList());
        } catch (Exception e) {
           throw new RuntimeException("Can't read file: " + dir, e);
        }
    }
    
    public static List<String> filePaths(String dir, String...fileExtention) {
        try {
            return Files.list(Paths.get(dir))
                    .map(path -> path.toFile())
                    .filter(file -> {
                        if (fileExtention.length == 0) {
                            return !file.isDirectory();
                        }
                        String extension = FilenameUtils.getExtension(file.getName());
                        boolean knownExtention = Objects.nonNull(Arrays.stream(fileExtention).filter(ext -> ext.equalsIgnoreCase(extension)).findAny().orElse(null));
                        return !file.isDirectory() && knownExtention;
                    })
                    .map(file -> file.getAbsolutePath()).collect(Collectors.toList());
        } catch (Exception e) {
           throw new RuntimeException("Can't read file: " + dir, e);
        }
    }
    
    public static File getFile(String folderAbsPath, Media media) {
        File file = new File(folderAbsPath + "/" + media.getTitle());
        String extension = FilenameUtils.getExtension(file.getName());
        if (extension.isBlank()) {
            file = new File(folderAbsPath + "/" + media.getTitle() + media.getFormat());
        }
        return file;
    }
}
