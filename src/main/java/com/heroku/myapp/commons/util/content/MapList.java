package com.heroku.myapp.commons.util.content;

import java.util.ArrayList;
import java.util.Iterator;
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
        super(document.get("data", List.class));
    }

    public MapList(List list) {
        super(list);
    }

    public Set attrSet(String attr) {
        return this.stream().map((map) -> map.get(attr))
                .collect(Collectors.toSet());
    }

    public Stream<Map<String, Object>> filtered(Predicate<Map<String, Object>> filter) {
        return this.stream().filter(filter);
    }

    public MapList filteredList(Predicate<Map<String, Object>> filter) {
        return new MapList(this.filtered(filter).collect(Collectors.toList()));
    }

    public Stream<Map<String, Object>> intersection(String key, Set set, boolean flag) {
        return this.stream()
                .filter((map) -> flag == set.contains(map.get(key)));
    }

    public Stream<Map<String, Object>> intersection(String key, Set set) {
        return MapList.this.intersection(key, set, true);
    }

    public MapList intersectionList(String key, Set set) {
        return this.intersectionList(key, set, true);
    }

    public MapList intersectionList(String key, Set set, boolean flag) {
        return new MapList(this.intersection(key, set, flag)
                .collect(Collectors.toList()));
    }

    public <T> Stream<T> attrStream(String attr, Class<T> clazz) {
        return this.stream().map((map) -> clazz.cast(map.get(attr)));
    }

    public MapList productByKey(MapList filter, final String key) {
        Set filterSet = filter.attrSet(key);
        return this.intersectionList(key, filterSet);
    }

    public MapList productByTitle(MapList filter) {
        return this.productByKey(filter, "title");
    }

    public MapList addNewByKey(MapList newUtil, final String key) {
        MapList result = new MapList(this);
        Set oldSet = result.attrSet(key);
        newUtil.intersection(key, oldSet, false)
                .forEach(result::add);
        return result;
    }

    public MapList replace(Map<String, Object> map, final String identifier) {
        Iterator<Map<String, Object>> iterator = this.iterator();
        Object keyValue = map.get(identifier);
        while (iterator.hasNext()) {
            if (keyValue.equals(iterator.next().get(identifier))) {
                iterator.remove();
                break;
            }
        }
        this.add(map);
        return this;
    }
}
