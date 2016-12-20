package com.heroku.myapp.commons.util;

import com.heroku.myapp.commons.util.content.MapList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import static java.util.Optional.empty;

public class JsonUtil {

    private final Optional<Object> obj;

    public JsonUtil(Object obj) {
        this.obj = ofNullable(obj);
    }

    public JsonUtil() {
        this.obj = empty();
    }

    public Optional<Map> map() {
        return collection(Map.class, new LinkedHashMap<>());
    }

    public Optional<List> list() {
        return collection(List.class, new MapList());
    }

    public Optional<MapList> mapList() {
        return collection(MapList.class, new MapList());
    }

    public JsonUtil of(String key) {
        return this.map().isPresent()
                ? new JsonUtil(this.map().get().get(key)) : new JsonUtil();
    }

    public <T> Optional<T> collection(Class<T> clazz, T elseObj) {
        return ofNullable(obj.isPresent() && clazz.isInstance(obj.get())
                ? clazz.cast(obj.get()) : elseObj);
    }
}
