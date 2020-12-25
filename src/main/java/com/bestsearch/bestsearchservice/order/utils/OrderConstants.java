package com.bestsearch.bestsearchservice.order.utils;

import java.time.format.DateTimeFormatter;

public class OrderConstants {
    public static final String PATTERN_PREFIX = "%{";
    public static final String PATTERN_SUFFIX = "}";

    public static final String TODAY = "Today";
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

}
