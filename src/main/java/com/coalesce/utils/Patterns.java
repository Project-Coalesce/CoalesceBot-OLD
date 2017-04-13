package com.coalesce.utils;

import java.util.regex.Pattern;

public class Patterns {
    public static final Pattern TEMPERATURE_KELVIN = Pattern.compile("K*", Pattern.CASE_INSENSITIVE);
    public static final Pattern TEMPERATURE_CELSIUS = Pattern.compile("C*", Pattern.CASE_INSENSITIVE);
    public static final Pattern TEMPERATURE_FAHRENHEIT = Pattern.compile("F*", Pattern.CASE_INSENSITIVE);
}
