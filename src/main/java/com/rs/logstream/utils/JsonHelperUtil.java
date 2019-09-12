package com.rs.logstream.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;

public class JsonHelperUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonHelperUtil.class);
    public static   String[] fieldsToIgnore = {"ssl_enabled", "service_definition_endpoint_uuid", "request_id", "plan_uuid", "package_uuid", "oauth_access_token", "quota_value", "qps_throttle_value"};

    public static JSONObject loadJson(String socketMessage) {
        JSONObject parsedMessage = null;
        try {
            Object obj = new JSONParser().parse(socketMessage);
            // typecasting obj to JSONObject
            parsedMessage = (JSONObject) obj;

        } catch (Exception e) {
            parsedMessage = null;
            logger.error("Could not load JSON from message={}", e.getMessage());
        }

        //removeFieldsFromJson(parsedMessage);
        return parsedMessage;
    }

    public static JSONArray getMasheryDataFromJson(JSONObject masheryMessage) {
        return (JSONArray) masheryMessage.get("data");
    }

    public static JSONObject removeFieldsFromMasheryMessage(JSONObject masheryJsonMessage) {
        Arrays.stream(fieldsToIgnore).forEach((field) -> masheryJsonMessage.remove(field));

        String rawApiKey = (String) masheryJsonMessage.get("api_key");
        rawApiKey = rawApiKey.equalsIgnoreCase("unknown") ? rawApiKey : "*masked api key*";
        masheryJsonMessage.put("api_key", rawApiKey);

        //Stored the keyset to an array to avoid concurrency errors during removal of keys from messageMap
        Object[] remainFields = masheryJsonMessage.keySet().toArray();
        Arrays.stream(remainFields).forEach((field) -> {
            if ("-".equals((String) masheryJsonMessage.get(field))) {
                masheryJsonMessage.remove(field);
            }
        });

        return masheryJsonMessage;
    }
}
