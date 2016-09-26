package com.heroku.myapp.commons.util.content;

import java.io.IOException;
import java.util.Iterator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class IterableMediawikiApiRequest {

    private final String apiUrl = "https://ja.wikipedia.org/w/api.php";
    private String apiParam, listName, mapName;
    private String[] continueElementNames;
    private boolean debugFlag = false;

    public IterableMediawikiApiRequest setApiParam(String apiParam) {
        this.apiParam = apiParam;
        return this;
    }

    public IterableMediawikiApiRequest setListName(String listName) {
        this.listName = listName;
        return this;
    }

    public IterableMediawikiApiRequest setMapName(String mapName) {
        this.mapName = mapName;
        return this;
    }

    public IterableMediawikiApiRequest setContinueElementNames(String[] names) {
        this.continueElementNames = names;
        return this;
    }

    public Document getDocument() throws IOException {
        return getDocument("");
    }

    public Document getDocument(String continueParam) throws IOException {
        String requestUrl = apiUrl + "?" + apiParam + continueParam;
        if (debugFlag) {
            System.out.println("connecting... " + requestUrl);
        }
        Document get
                = Jsoup.connect(requestUrl).timeout(Integer.MAX_VALUE).get();
        if (debugFlag) {
            System.out.println("connected. " + requestUrl);
        }
        return get;
    }

    public IterableMediawikiApiRequest debug() {
        debugFlag = true;
        return this;
    }

    public Iterator<Elements> iterator() {
        return new Iterator() {
            boolean hasNext = true;
            String continueParam = "";

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public Elements next() {
                try {
                    Document get = getDocument(continueParam);
                    if (continueElementNames != null && continueElementNames.length > 0) {
                        Elements continueElements = get.select("continue");
                        if (continueElements.isEmpty()) {
                            hasNext = false;
                        } else {
                            Element continueElement = continueElements.first();
                            continueParam = "";
                            for (String cen : continueElementNames) {
                                if (continueElement.hasAttr(cen)) {
                                    continueParam
                                            += "&" + cen
                                            + "=" + continueElement.attr(cen);
                                }
                            }
                        }
                    }
                    return get.select("page");
                } catch (IOException ex) {
                    throw new RuntimeException();
                }
            }
        };
    }
}
