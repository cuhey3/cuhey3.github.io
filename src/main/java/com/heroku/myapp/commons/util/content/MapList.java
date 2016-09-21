package com.heroku.myapp.commons.util.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bson.Document;

public class MapList extends ArrayList<Map<String, Object>> {

    public MapList() {
        super();
    }

    public MapList(Document document) {
        super(new DocumentUtil(document).getData());
    }

    public MapList(List<Map<String, Object>> mapList) {
        super(mapList);
    }

    public Set attrSet(String attr) {
        return this.stream().map((map) -> map.get(attr))
                .collect(Collectors.toSet());
    }

    public Stream<Map<String, Object>> filtered(Predicate<Map<String, Object>> filter) {
        return this.stream().filter(filter);
    }

    public List<Map<String, Object>> filteredList(Predicate<Map<String, Object>> filter) {
        return this.filtered(filter).collect(Collectors.toList());
    }

    public Stream<Map<String, Object>> intersection(String key, Set set, boolean flag) {
        return this.stream()
                .filter((map) -> flag == set.contains(map.get(key)));
    }

    public Stream<Map<String, Object>> intersection(String key, Set set) {
        return MapList.this.intersection(key, set, true);
    }

    public List<Map<String, Object>> intersectionList(String key, Set set) {
        return this.intersectionList(key, set, true);
    }

    public List<Map<String, Object>> intersectionList(String key, Set set, boolean flag) {
        return this.intersection(key, set, flag).collect(Collectors.toList());
    }

    public <T> Stream<T> attrStream(String attr, Class<T> clazz) {
        return this.stream().map((map) -> clazz.cast(map.get(attr)));
    }

    public List<Map<String, Object>> productByKey(MapList filter, final String key) {
        Set filterSet = filter.attrSet(key);
        return this.intersectionList(key, filterSet);
    }

    public List<Map<String, Object>> productByTitle(MapList filter) {
        return this.productByKey(filter, "title");
    }

    public List<Map<String, Object>> addNewByKey(MapList newUtil, final String key) {
        List<Map<String, Object>> result = new ArrayList<>(this);
        Set oldSet = this.attrSet(key);
        newUtil.intersection(key, oldSet, false)
                .forEach(result::add);
        return result;
    }
}
