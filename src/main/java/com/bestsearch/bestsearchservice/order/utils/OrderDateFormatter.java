package com.bestsearch.bestsearchservice.order.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class OrderDateFormatter {

    public static String formatForUI(LocalDate date) {
        return date.isEqual(LocalDate.now()) ? OrderConstants.TODAY : OrderConstants.formatter.format(date);
    }
}
