package com.heroku.myapp.commons.consumers;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.MapList;
import com.heroku.myapp.commons.util.content.MediawikiApiRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;

public abstract class SnapshotCategoryPagesAggregationConsumer extends SnapshotQueueConsumer {

    protected Kind targetKind;
    protected MapList targetCategories;
    protected boolean includesCategoryFlag = true;
    protected String cmpropParam = "title";

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        if (targetCategories == null) {
            if (targetKind != null) {
                targetCategories = new MasterUtil(exchange).mapList(targetKind);
            } else {
                throw new RuntimeException();
            }
        }
        LinkedHashMap<String, List<String>> reduce
                = targetCategories.attrStream("title", String.class)
                .map(this.apiRequest())
                .reduce(new LinkedHashMap<String, List<String>>(),
                        this.reduceMain(), (foo, bar) -> foo);
        MapList mapList = new MapList(reduce.entrySet().stream()
                .map((entry) -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("title", entry.getKey());
                    if (includesCategoryFlag) {
                        List<String> value = entry.getValue();
                        Collections.sort(value);
                        map.put("categories", value);
                    }
                    return map;
                }).collect(Collectors.toList()));
        afterProcess(mapList);
        return new DocumentUtil(mapList).nullable();
    }

    private Function<String, Object[]> apiRequest() {
        return (category) -> {
            try {
                return new Object[]{category, new MediawikiApiRequest()
                    .setApiParam("action=query&list=categorymembers"
                    + "&cmtitle=" + URLEncoder.encode(category, "UTF-8")
                    + "&cmlimit=500&cmnamespace=0"
                    + "&cmprop=" + cmpropParam
                    + "&format=xml")
                    .setListName("categorymembers").setMapName("cm")
                    .setContinueElementName("cmcontinue")
                    .setIgnoreFields("ns").getResultByMapList()};
            } catch (UnsupportedEncodingException ex) {
                util().sendError("apiRequest", ex);
                throw new RuntimeException();
            } catch (IOException ex) {
                util().sendError("apiRequest", ex);
                throw new RuntimeException();
            }
        };
    }

    private BiFunction<LinkedHashMap<String, List<String>>, Object[], LinkedHashMap<String, List<String>>> reduceMain() {
        return (result, objArray) -> {
            String category = (String) objArray[0];
            MapList list = new MapList((List) objArray[1]);
            list.stream().forEach((map) -> {
                String title = (String) map.get("title");
                if (includesCategoryFlag) {
                    List<String> categoryList = result.getOrDefault(title,
                            new ArrayList<>());
                    categoryList.add(category.replaceFirst("^Category:", ""));
                    result.put(title, categoryList);
                } else {
                    result.put(title, null);
                }
            });
            return result;
        };
    }

    protected void afterProcess(MapList mapList) {
    }

    protected final void addTargetCategory(String category) {
        if (targetCategories == null) {
            targetCategories = new MapList();
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("title", category);
        targetCategories.add(map);
    }

    protected final void cmpropParam(String param) {
        this.cmpropParam = param;
    }

    protected final void includesCategoryFlag(boolean bool) {
        this.includesCategoryFlag = bool;
    }
}
