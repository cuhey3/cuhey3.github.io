package com.heroku.myapp.commons.util.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bson.Document;

public class MapListUtil {

    private final List<Map<String, Object>> mapList;

    public MapListUtil() {
        this.mapList = new ArrayList<>();
    }

    public MapListUtil(Document document) {
        this.mapList = new DocumentUtil(document).getData();
    }

    public MapListUtil(List<Map<String, Object>> mapList) {
        this.mapList = mapList;
    }

    public Set attrSet(String attr) {
        return this.mapList.stream().map((map) -> map.get(attr))
                .collect(Collectors.toSet());
    }

    public Stream<Map<String, Object>> stream() {
        return this.mapList.stream();
    }

    public Stream<Map<String, Object>> filtered(Predicate<Map<String, Object>> filter) {
        return this.mapList.stream().filter(filter);
    }

    public List<Map<String, Object>> filteredList(Predicate<Map<String, Object>> filter) {
        return this.filtered(filter).collect(Collectors.toList());
    }

    public Stream<Map<String, Object>> intersection(String key, Set set, boolean flag) {
        return this.mapList.stream()
                .filter((map) -> flag == set.contains(map.get(key)));
    }

    public Stream<Map<String, Object>> intersection(String key, Set set) {
        return MapListUtil.this.intersection(key, set, true);
    }

    public List<Map<String, Object>> intersectionList(String key, Set set) {
        return this.intersectionList(key, set, true);
    }

    public List<Map<String, Object>> intersectionList(String key, Set set, boolean flag) {
        return this.intersection(key, set, flag).collect(Collectors.toList());
    }

    public <T> Stream<T> attrStream(String attr, Class<T> clazz) {
        return this.mapList.stream().map((map) -> clazz.cast(map.get(attr)));
    }
}
