package com.heroku.myapp.commons.util.content;

import com.heroku.myapp.commons.exceptions.DataNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bson.Document;
import org.bson.types.ObjectId;

public final class DocumentUtil {

    public static String objectIdHexString(Document document) {
        return document.get("_id", ObjectId.class).toHexString();
    }

    public static Document restorePrefix(Document document) {
        Map<String, String> prefixs = document.get("prefix", Map.class);
        MapList mapList = new MapList(document);
        if (prefixs != null) {
            prefixs.entrySet().stream().forEach((entry)
                    -> restorePrefixSpecific(
                            mapList, entry.getKey(), entry.getValue()));
        }
        return new DocumentUtil(mapList).getDocument();
    }

    private static void restorePrefixSpecific(MapList mapList, String key, String prefix) {
        mapList.stream().forEach((map) -> map.put(key, prefix + map.get(key)));
    }

    private final Document document;

    public DocumentUtil() {
        this.document = new Document();
    }

    public DocumentUtil(Document document) {
        this.document = document;
    }

    public DocumentUtil(List list) {
        this.document = new Document();
        document.append("data", list);
    }

    public DocumentUtil createPrefix(String... keys) {
        document.append("prefix", new LinkedHashMap());
        for (String key : keys) {
            createPrefixSpecific(key);
        }
        return this;
    }

    private void createPrefixSpecific(String key) {
        MapList list = new MapList(document);
        String firstValue = (String) list.get(0).get(key);
        int len = firstValue.length();
        String prefix = null;
        for (int i = len; i > 10; i--) {
            String prefixSuggest = firstValue.substring(0, i);
            if (list.stream().allMatch((map)
                    -> ((String) map.get(key)).startsWith(prefixSuggest))) {
                prefix = prefixSuggest;
                int length = prefix.length();
                list.stream().forEach((map) -> {
                    map.put(key, ((String) map.get(key))
                            .substring(length));
                });
                break;
            }
        }
        if (prefix != null) {
            Map prefixs = document.get("prefix", Map.class);
            prefixs.put(key, prefix);
            document.put("prefix", prefixs);
        }
    }

    public MapList getDiff() {
        return new MapList(Optional.ofNullable(document.get("diff", List.class))
                .orElseThrow(() -> new DataNotFoundException()));
    }

    public DocumentUtil setDiff(List list) {
        document.append("diff", list);
        return this;
    }

    public Document getDocument() {
        return this.document;
    }

    public Optional<Document> nullable() {
        return Optional.ofNullable(document);
    }
}
