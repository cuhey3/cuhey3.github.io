package com.heroku.myapp.commons.util.content;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GoogleTrendsParsingUtil {

    private final Map<String, List<Map<String, String>>> body;
    private final Map<String, List<Map<String, String>>> bodyNotContainingScale
            = new LinkedHashMap<>();

    private final String scale;
    private final String threshold;
    private final Map<String, String> fromMap = new LinkedHashMap<>();
    private final Map<String, Integer> progressMap = new LinkedHashMap<>();
    private final int size;

    public GoogleTrendsParsingUtil(Map<String, List<Map<String, String>>> body, String scale, String threshold) {
        this.body = body;
        this.scale = scale;
        this.threshold = threshold;
        removeProgress(body);
        body.entrySet().forEach((entry) -> {
            String name = entry.getKey();
            List<Map<String, String>> value = entry.getValue();
            fromMap.put(name, getFromMonth(value));
            if (!name.equals(scale)) {
                bodyNotContainingScale.put(name, value);
            }
        });
        this.size = body.get(scale).size();
    }

    public void mainLogic() {
        if (scaleIsValid()) {
            createSuccessResults();
        } else {
            createFailedResults();
        }
    }

    public boolean scaleIsValid() {
        return max(scale) == 100;
    }

    private IntStream stream(String name) {
        return body.get(name).stream()
                .map((map) -> map.values().iterator().next())
                .mapToInt(Integer::parseInt)
                .filter((i) -> i > 0);
    }

    private IntStream streamFrom(String name) {
        String from = fromMap.get(name);
        return body.get(name).stream().filter((map)
                -> map.keySet().iterator().next().compareTo(from) >= 0)
                .map((map) -> map.values().iterator().next())
                .mapToInt(Integer::parseInt);
    }

    private int max(String name) {
        return stream(name).max().orElse(0);
    }

    private long count(String name) {
        return stream(name).count();
    }

    private double average(String name) {
        return streamFrom(name).average().orElse(0.0);
    }

    private int sum(String name) {
        return stream(name).sum();
    }

    private Set<String> overNames() {
        int scaleMax = max(scale);
        return bodyNotContainingScale.keySet().stream()
                .filter((name) -> {
                    return Math.max(max(name), progressMap.get(name)) > scaleMax;
                })
                .collect(Collectors.toSet());
    }

    private String getFromMonth(List<Map<String, String>> list) {
        return list.stream().filter((map)
                -> map.values().iterator().next().compareTo(threshold) >= 0)
                .map((map) -> map.keySet().iterator().next())
                .findFirst().orElse("9999");
    }

    public List<Map<String, Object>> createSuccessResults() {
        return bodyNotContainingScale.keySet().stream()
                .map((name) -> createSuccessResult(name))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> createFailedResults() {
        return overNames().stream()
                .map((name) -> createFailedResult(name))
                .collect(Collectors.toList());
    }

    private Map<String, Object> createSuccessResult(String name) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", name);
        result.put("scale", scale);
        result.put("status", "success");
        result.put("data", body.get(name));
        result.put("size", size);
        result.put("count", count(name));
        result.put("from", fromMap.get(name));
        result.put("sum", sum(name));
        result.put("max", max(name));
        result.put("avg", average(name));
        result.put("date", new Date());
        return result;
    }

    private Map<String, Object> createFailedResult(String name) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", name);
        result.put("scale", scale);
        result.put("status", "failed");
        result.put("date", new Date());
        return result;
    }

    private void removeProgress(Map<String, List<Map<String, String>>> body) {
        Set<String> keys = body.keySet();
        keys.stream().forEach((key) -> {
            List<Map<String, String>> value = body.get(key);
            for (int i = 0; i < value.size(); i++) {
                String get = value.get(i).get("progress");
                if (get != null) {
                    progressMap.put(key, Integer.parseInt(get));
                    value.remove(i);
                    break;
                }
            }
        });
    }
}
