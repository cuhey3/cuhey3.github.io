package com.heroku.myapp.commons.consumers;

import com.heroku.myapp.commons.util.JsonUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.MapList;
import com.heroku.myapp.commons.util.content.MediawikiApiRequest;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.camel.Exchange;
import org.bson.Document;

public abstract class SnapshotCategoryAggregationConsumer extends SnapshotQueueConsumer {

    private final String prefix = "Category:";
    protected JsonUtil jsonRoot;

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        DigStatus status = new DigStatus(jsonRoot);
        digCategories(status);
        List result = result(status);
        return new DocumentUtil(result).nullable();
    }

    private void digCategories(DigStatus status) {
        int i = 0;
        while (status.completionFlags().values().contains(false)) {
            if (++i > 7) {
                break;
            }
            status.completionFlags().keySet().parallelStream()
                    .filter((key) -> !status.completionFlags().get(key))
                    .flatMap(dig(status))
                    .map((map)->(String)map.get(("title")))
                    .distinct()
                    .forEach((title) -> {
                        if (!status.completionFlags().containsKey(title)) {
                            status.completionFlags().put(title, Boolean.FALSE);
                        }
                    });
        }
    }

    private Function<String, Stream<Map<String, Object>>> dig(DigStatus status) {
        return (key) -> {
            status.completionFlags().put(key, Boolean.TRUE);
            if (isNoContinue(status, key)) {
                return new MapList().stream();
            } else {
                return apiRequestMain(status, key);
            }
        };
    }

    private boolean isNoContinue(DigStatus status, String key) {
        return jsonRoot.get(status.categoryParents().get(key))
                .get("filter").get("no_continue").list().get().stream()
                .anyMatch((str) -> key.equals(prefix + str));
    }

    private Stream<Map<String, Object>> apiRequestMain(DigStatus status, String key) {
        String parent = status.categoryParents().get(key);
        final List<Pattern> includePattern, excludePattern;
        includePattern = patterns(parent, "include");
        excludePattern = patterns(parent, "exclude");
        return apiRequest(key).stream()
                .filter(patternMatch(includePattern, true))
                .map((map) -> {
                    map.put("from", key);
                    status.categoryParents()
                            .put((String) map.get("title"), parent);
                    return map;
                })
                .filter(patternMatch(excludePattern, false));
    }

    private List<Pattern> patterns(String parent, String type) {
        return jsonRoot.get(parent).get("filter").get(type).mapList().get()
                .stream().map(mapToPattern()).collect(Collectors.toList());
    }

    private Function<Map<String, Object>, Pattern> mapToPattern() {
        return (map) -> {
            Object[] args = Optional.ofNullable((List<String>) map.get("args"))
                    .orElse(new ArrayList<>()).stream().map((str) -> {
                switch (str) {
                    case "recent_years":
                        return "(2015|2016|2017)年";
                    case "years":
                        return "[\\d]{4}年";
                }
                return "";
            }).toArray();
            String pattern = (String) map.get("pattern");
            if (args.length > 0) {
                return Pattern.compile("^Category:.*" + String.format(pattern, args));
            } else {
                return Pattern.compile("^Category:.*" + pattern);
            }
        };
    }

    private List<Map<String, Object>> apiRequest(String key) {
        try {
            return new MediawikiApiRequest().setApiParam(
                    "action=query&list=categorymembers"
                    + "&cmtitle=" + URLEncoder.encode(key, "UTF-8")
                    + "&cmlimit=500&cmnamespace=14"
                    + "&cmprop=title|ids|sortkeyprefix&format=xml")
                    .setListName("categorymembers").setMapName("cm")
                    .setContinueElementName("cmcontinue")
                    .getResultByMapList();
        } catch (Throwable t) {
            System.out.println("failed!!!");
            throw new RuntimeException();
        }
    }

    private Predicate<Map<String, Object>> patternMatch(List<Pattern> patterns, boolean flag) {
        return (map) -> {
            if (patterns.isEmpty()) {
                return true;
            } else {
                String title = (String) map.get("title");
                return patterns.stream()
                        .allMatch((p) -> flag == p.matcher(title).find());
            }
        };
    }

    private List result(DigStatus status) {
        return status.completionFlags().keySet().stream().map((key) -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("title", key);
            map.put("from", prefix + status.categoryParents().get(key));
            return map;
        }).collect(Collectors.toList());
    }
}

class DigStatus {

    private final String prefix = "Category:";
    private Map<String, Boolean> completionFlags;
    private Map<String, String> categoryParents;

    protected DigStatus(JsonUtil jsonRoot) {
        init(jsonRoot);
    }

    protected void completionFlags(Map<String, Boolean> map) {
        this.completionFlags = map;
    }

    protected Map<String, Boolean> completionFlags() {
        return this.completionFlags;
    }

    protected Map<String, String> categoryParents() {
        return this.categoryParents;
    }

    private void init(JsonUtil jsonRoot) {
        this.completionFlags = new LinkedHashMap<>();
        this.categoryParents = new LinkedHashMap<>();
        jsonRoot.map().get().keySet().forEach((key)
                -> completionFlags.put(prefix + key, Boolean.FALSE));
        jsonRoot.map().get().keySet().forEach((key)
                -> categoryParents.put(prefix + key, (String) key));
    }
}
