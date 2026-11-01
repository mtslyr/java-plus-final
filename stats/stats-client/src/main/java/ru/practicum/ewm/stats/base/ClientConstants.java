package ru.practicum.ewm.stats.base;

import java.time.format.DateTimeFormatter;

public class ClientConstants {

    public static final String API_PREFIX_HIT = "/hit";
    public static final String API_PREFIX_STATS = "/stats";

    public static final String PARAM_START = "start";
    public static final String PARAM_END = "end";
    public static final String PARAM_URIS = "uris";
    public static final String PARAM_UNIQUE = "unique";

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
}