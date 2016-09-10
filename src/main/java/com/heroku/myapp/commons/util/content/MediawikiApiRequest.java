package com.heroku.myapp.commons.util.content;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class MediawikiApiRequest {

    private final String apiUrl = "https://ja.wikipedia.org/w/api.php";
    private String apiParam, listName, mapName, continueElementName;
    private final List<String> ignoreFieldNameList = new ArrayList<>();
    private boolean debugFlag = false;

    public MediawikiApiRequest setApiParam(String apiParam) {
        this.apiParam = apiParam;
        return this;
    }

    public MediawikiApiRequest setListName(String listName) {
        this.listName = listName;
        return this;
    }

    public MediawikiApiRequest setMapName(String mapName) {
        this.mapName = mapName;
        return this;
    }

    public MediawikiApiRequest setContinueElementName(String continueElementName) {
        this.continueElementName = continueElementName;
        return this;
    }

    public MediawikiApiRequest setIgnoreFields(String ignoreFieldsString) {
        this.ignoreFieldNameList
                .addAll(Arrays.asList(ignoreFieldsString.split(",")));
        return this;
    }

    public List<Map<String, Object>> getResultByMapList() throws IOException {
        String requestUrl = apiUrl + "?" + apiParam;
        if (debugFlag) {
            System.out.println("connecting... " + requestUrl);
        }
        Document get
                = Jsoup.connect(requestUrl).timeout(Integer.MAX_VALUE).get();
        if (debugFlag) {
            System.out.println("connected. " + requestUrl);
        }
        ArrayList<Map<String, Object>> resultMapList = new ArrayList<>();
        addElementsAsMap(resultMapList, get.select(listName).select(mapName));
        if (continueElementName != null) {
            while (true) {
                Elements continueElements
                        = get.select("continue[" + continueElementName + "]");
                if (continueElements.isEmpty()) {
                    break;
                } else {
                    String continueElementValue
                            = continueElements.first()
                            .attr(continueElementName);
                    get = Jsoup.connect(
                            requestUrl
                            + "&" + continueElementName
                            + "=" + continueElementValue)
                            .timeout(Integer.MAX_VALUE).get();
                    addElementsAsMap(resultMapList,
                            get.select(listName).select(mapName));
                }
            }
        }
        return resultMapList;
    }

    public Set<String> getResultBySet(String attr) throws IOException {
        String requestUrl = apiUrl + "?" + apiParam;
        if (debugFlag) {
            System.out.println("connecting... " + requestUrl);
        }
        Document get
                = Jsoup.connect(requestUrl).timeout(Integer.MAX_VALUE).get();
        if (debugFlag) {
            System.out.println("connected. " + requestUrl);
        }
        HashSet<String> resultSet = new HashSet<>();
        get.select(listName).select(mapName).stream()
                .map((el) -> el.attr(attr)).forEach(resultSet::add);
        if (continueElementName != null) {
            while (true) {
                Elements continueElements
                        = get.select("continue[" + continueElementName + "]");
                if (continueElements.isEmpty()) {
                    break;
                } else {
                    String continueElementValue
                            = continueElements.first()
                            .attr(continueElementName);
                    get = Jsoup.connect(
                            requestUrl
                            + "&" + continueElementName
                            + "=" + continueElementValue)
                            .timeout(Integer.MAX_VALUE).get();
                    get.select(listName).select(mapName).stream()
                            .map((el) -> el.attr(attr))
                            .forEach(resultSet::add);
                }
            }
        }
        return resultSet;
    }

    public void addElementsAsMap(List addingList, Elements elements) {
        elements.stream().map((element) -> {
            Map<String, String> m = new HashMap<>();
            StreamSupport.stream(element.attributes().spliterator(), false)
                    .filter((entry) -> ignoreFieldNameList.isEmpty()
                            || !ignoreFieldNameList.contains(entry.getKey()))
                    .forEach((entry) -> {
                        m.put(entry.getKey(), entry.getValue());
                    });
            return m;
        }).forEach(addingList::add);
    }

    public MediawikiApiRequest debug() {
        debugFlag = true;
        return this;
    }
}
