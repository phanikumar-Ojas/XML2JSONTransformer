package com.ebsco.platform.shared.cmsimport.rs.util;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class DataUtil {

    public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yy");
    public static final DateTimeFormatter M_D_YYYY_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");

    public static final Pattern SIMPLE_NUMBER_PATTERN = Pattern.compile("^\\d+$");
    public static final Pattern RANGE_NUMBER_PATTERN = Pattern.compile("^\\d+\\p{Pd}\\d+L$");

    public static LocalDate from(String dateString, DateTimeFormatter formatter) {
        if (dateString != null && !dateString.isBlank()) {
            return LocalDate.parse(dateString, formatter);
        }
        return null;
    }

    public static LocalDate from(String dateString) {
        return from(dateString, DEFAULT_FORMATTER);
    }

    public static Integer parseIntOrRange(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        value = value.trim();
        if (SIMPLE_NUMBER_PATTERN.matcher(value).find()) {
            return Integer.parseInt(value);
        }
        if (RANGE_NUMBER_PATTERN.matcher(value).find()) {
            return parseRange(value);
        }
        return null;
    }

    public static Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (SIMPLE_NUMBER_PATTERN.matcher(value).find()) {
            return Long.parseLong(value);
        }
        return null;
    }

    public static Integer calculatePages(Integer value) {
        if (value == null) {
            return null;
        }
        BigDecimal divideValue = new BigDecimal(value).divide(new BigDecimal(500), RoundingMode.UP);
        return Math.min(divideValue.intValue(), 3);
    }

    private static Integer parseRange(String value) {
        return Integer.parseInt(value.split("\\p{Pd}")[1].replaceAll("L", ""));
    }

    public static String onlyDigitsOrNull(String bioMetaId) {
		String value = StringUtils.trim(bioMetaId);
		if (NumberUtils.isDigits(value)) {
			return value;
		}
		if (StringUtils.contains(value, "h1-")) {
			value = StringUtils.remove(value, "h1-");
		}
		if (NumberUtils.isDigits(value)) {
			return value;
		}
		
		if (StringUtils.contains(value, "AN")) {
			value = StringUtils.remove(value, "AN");
		}
		if (NumberUtils.isDigits(value)) {
			return value;
		}
		return null;
	}
}
