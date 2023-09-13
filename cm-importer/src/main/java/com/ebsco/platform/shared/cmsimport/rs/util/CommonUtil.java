package com.ebsco.platform.shared.cmsimport.rs.util;

import org.apache.commons.collections.CollectionUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CommonUtil {
	
	public static String quotateValues(Collection<Object> value) {
		return value.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", "));
	}
	
	public static <T> List<List<T>> partition(List<T> items, int n) {
		List<List<T>> partitions = IntStream.range(0, items.size())
			    .filter(i -> i % n == 0)
			    .mapToObj(i -> items.subList(i, Math.min(i + n, items.size() )))
			    .collect(Collectors.toList());
		return partitions;
	}
	
	public static String readClasspathResourceFile(String relativePath) {
        try (InputStream in = CommonUtil.class.getResourceAsStream(relativePath)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String content = reader.lines().collect(Collectors.joining());
            return content;
        } catch (Exception e) {
            throw new RuntimeException("Can't read file: "+relativePath, e);
        }
    }

	public static <K, V extends Collection<?>> void mergeValues(Map<K, V> target, Map<K, V> source) {
		for (Map.Entry<K, V> entry : source.entrySet()) {
			target.computeIfPresent(entry.getKey(), (k, v) ->
					(V) CollectionUtils.union(v, entry.getValue()));
			target.putIfAbsent(entry.getKey(), entry.getValue());
		}
	}
}
