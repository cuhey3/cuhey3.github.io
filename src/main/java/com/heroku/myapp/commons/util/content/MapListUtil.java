package com.heroku.myapp.commons.util.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapListUtil {

    private final List<Map<String, Object>> mapList;

    public MapListUtil() {
        this.mapList = new ArrayList<>();
    }

    public MapListUtil(List<Map<String, Object>> mapList) {
        this.mapList = mapList;
    }
}
