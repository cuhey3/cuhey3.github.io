package com.heroku.myapp.commons.util.consumers;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.bson.Document;

public class QueueMessage {

    public static Predicate messageKindIs(Kind kind) {
        return (Exchange exchange)
                -> {
            Optional<Kind> k = new QueueMessage(exchange).optionalKind();
            return k.isPresent() && k.get() == kind;
        };
    }

    private final Map map;
    private final Exchange exchange;

    public QueueMessage(Exchange exchange) {
        this.exchange = exchange;
        Map body = exchange.getIn().getBody(Map.class);
        this.map = new LinkedHashMap<>();
        if (body != null) {
            map.putAll(body);
        }
    }

    public Optional<Kind> optionalKind() {
        Optional<String> optionalKindString = optionalGet("kind", String.class);
        if (optionalKindString.isPresent()) {
            return Kind.optionalKindFromString(optionalKindString.get());
        } else {
            return Optional.empty();
        }
    }

    public <T> Optional<T> optionalGet(String key, Class<T> clazz) {
        return ofNullable(clazz.cast(map.get(key)));
    }

    public <T> Optional<String> optionalGet(String key) {
        return optionalGet(key, String.class);
    }

    public Map map() {
        return map;
    }

    public void writeObjectId(String key, Document document) {
        save(key, DocumentUtil.objectIdHexString(document));
    }

    public void save(String key, String value) {
        map.put(key, value);
        exchange.getIn().setBody(map, String.class);
    }
}
