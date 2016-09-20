package com.heroku.myapp.commons.util.content;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MediawikiApiRequestBuilder {

    private final MediawikiApiRequest request;
    private final String commonParameter = "&format=xml";
    private Action action = Action.QUERY;
    private List list;
    private Namespace[] namespaces;
    private String title, otherParameter;
    private int limit = 500;

    public MediawikiApiRequestBuilder() {
        request = new MediawikiApiRequest();
    }

    public MediawikiApiRequestBuilder action(Action action) {
        this.action = action;
        return this;
    }

    public MediawikiApiRequestBuilder list(List list) {
        this.list = list;
        return this;
    }

    public MediawikiApiRequestBuilder namespaces(Namespace[] namespaces) {
        this.namespaces = namespaces;
        return this;
    }

    public MediawikiApiRequestBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    public MediawikiApiRequestBuilder title(String title) {
        try {
            this.title = URLEncoder.encode(title, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException();
        }
        return this;
    }

    public MediawikiApiRequestBuilder otherParameter(String otherParameter) {
        this.otherParameter = otherParameter;
        return this;
    }

    public MediawikiApiRequest build() {
        StringBuilder sb = new StringBuilder();
        sb.append("action=").append(this.action.name().toLowerCase(Locale.US));
        if (list == null) {
            throw new RuntimeException();
        } else {
            sb.append(list.paramExpression());
        }
        String prefix = this.list.prefix;
        if (namespaces != null && action.equals(Action.QUERY)) {
            String join = String.join("|", Stream.of(namespaces)
                    .map((namespace) -> namespace.numberCode)
                    .collect(Collectors.toList()));
            sb.append("&").append(prefix).append("namespace=").append(join);
        }
        if (action.equals(Action.QUERY)) {
            sb.append("&").append(prefix).append("limit=").append(limit);
        }
        if (title != null) {
            if (action.equals(Action.QUERY)) {
                sb.append("&").append(prefix).append("title=").append(title);
            } else if (action.equals(Action.PARSE)) {
                sb.append("&page=").append(title);
            }
        }
        if (otherParameter != null) {
            sb.append("&").append(otherParameter);
        }
        sb.append(commonParameter);
        this.request.setApiParam(new String(sb));
        this.request.setListName(this.list.name().toLowerCase(Locale.US));
        this.request.setMapName(this.list.prefix);
        this.request.setContinueElementName(prefix + "continue");
        return this.request;
    }

    public enum Action {
        PARSE, QUERY;
    }

    public enum List {
        CATEGORYMEMBERS("cm", "list"),
        RECENTCHANGES("rc", "list"),
        BACKLINKS("bl", "list"),
        LINKS("pl", "prop");

        final String prefix, type;

        private List(String prefix, String type) {
            this.prefix = prefix;
            this.type = type;
        }

        private String paramExpression() {
            return "&" + this.type + "=" + this.name().toLowerCase(Locale.US);
        }
    }

    public enum Namespace {
        ARTICLE("0"), CATEGORY("14");
        final String numberCode;

        private Namespace(String numberCode) {
            this.numberCode = numberCode;
        }
    }
}
