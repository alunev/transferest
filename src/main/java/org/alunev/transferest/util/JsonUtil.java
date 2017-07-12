package org.alunev.transferest.util;

import com.google.gson.Gson;
import spark.ResponseTransformer;

public class JsonUtil {

    public static String toJson(Object object) {
        return new Gson().toJson(object);
    }

    public static <T> T fromJson(String s, Class<T> tClass) {
        return new Gson().fromJson(s, tClass);
    }

    public static ResponseTransformer toJson() {
        return JsonUtil::toJson;
    }
}