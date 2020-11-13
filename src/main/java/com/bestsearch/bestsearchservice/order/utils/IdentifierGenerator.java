package com.bestsearch.bestsearchservice.order.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.TextStringBuilder;

import java.util.Map;

import static com.bestsearch.bestsearchservice.order.utils.OrderConstants.PATTERN_PREFIX;
import static com.bestsearch.bestsearchservice.order.utils.OrderConstants.PATTERN_SUFFIX;

@UtilityClass
public class IdentifierGenerator {

    /**
     * return identifier for the pattern with args maps
     *
     * @param properties the map of key value pairs which use for replacing in
     *                   pattern
     * @param pattern    the string pattern of %{<key>} which needs to substitute
     *                   with values
     * @return the String value for according to pattern
     */
    public static String generateIdentifier(Map<String, Object> properties, String pattern) {
        TextStringBuilder strBuilder = new TextStringBuilder(pattern);
        StringSubstitutor sub = new StringSubstitutor(properties, PATTERN_PREFIX, PATTERN_SUFFIX);
        return sub.replace(strBuilder);
    }

}
