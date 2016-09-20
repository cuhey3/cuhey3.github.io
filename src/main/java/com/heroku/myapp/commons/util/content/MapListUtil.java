package com.heroku.myapp.commons.util.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

}
