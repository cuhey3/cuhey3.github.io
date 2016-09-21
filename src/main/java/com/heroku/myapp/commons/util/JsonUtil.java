package com.heroku.myapp.commons.util;

import com.heroku.myapp.commons.util.content.MapList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JsonUtil {

    Optional<Object> obj;

    public JsonUtil(Object object) {
        obj = Optional.ofNullable(object);
    }

    public JsonUtil() {
        obj = Optional.empty();
    }

    public JsonUtil get(String key) {
        Optional<Map> map = this.map();
        if (map.isPresent()) {
            return new JsonUtil(map.get().get(key));
        } else {
            return new JsonUtil();
        }
    }

    public JsonUtil get(int i) {
        Optional<Map> map = this.map();
        if (map.isPresent()) {
            return new JsonUtil(map.get().get(i));
        } else {
            return new JsonUtil();
        }
    }

    public Optional<Map> map() {
        if (obj.isPresent()) {
            return Optional.ofNullable((Map) obj.get());
        } else {
            return Optional.ofNullable(new LinkedHashMap<>());
        }
    }

    public Optional<MapList> mapList() {
        if (obj.isPresent() && obj.get() instanceof List) {
            return Optional.ofNullable(new MapList((List) obj.get()));
        } else {
            return Optional.ofNullable(new MapList());
        }
    }
}
