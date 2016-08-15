package com.heroku.myapp.commons.util;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.exceptions.MessageElementNotFoundException;
import com.heroku.myapp.commons.exceptions.MessageNotSetException;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.bson.Document;

public class MessageUtil {

    public static Map getMessage(Exchange ex) {
        return ofNullable(ex.getIn().getBody(Map.class))
                .orElseThrow(() -> new MessageNotSetException());
    }

    public static boolean hasMessage(Exchange ex) {
        return ex.getIn().getBody(Map.class) != null;
    }

    public static Optional<Kind> optionalGetKind(Exchange ex) {
        Optional<String> optionalKindString = get(ex, "kind", String.class);
        if (optionalKindString.isPresent()) {
            return Kind.optionalGetKindFromString(optionalKindString.get());
        } else {
            return Optional.empty();
        }
    }

    private static <T> Optional<T> get(Exchange ex, String key, Class<T> clazz) {
        return ofNullable(clazz.cast(getMessage(ex).get(key)));
    }

    public static Predicate loadAffect() {
        return (Exchange ex) -> {
            Optional<List> affect = get(ex, "affect", List.class);
            if (affect.isPresent() && !affect.get().isEmpty()) {
                ex.getIn().setBody(affect.get());
                return true;
            } else {
                return false;
            }
        };
    }

    public static Predicate messageKindIs(Kind kind) {
        return (Exchange ex)
                -> {
            Optional<Kind> k = optionalGetKind(ex);
            return k.isPresent() && k.get() == kind;
        };
    }

    private final Exchange exchange;

    public MessageUtil(Exchange exchange) {
        this.exchange = exchange;
    }

    public void updateMessage(String key, Object value) {
        Map message = getMessage();
        message.put(key, value);
        exchange.getIn().setBody(message, String.class);
    }

    public Map getMessage() {
        return ofNullable(exchange.getIn().getBody(Map.class))
                .orElseThrow(() -> new MessageNotSetException());
    }

    public void writeObjectId(String key, Document document) {
        updateMessage(key, DocumentUtil.objectIdHexString(document));
    }

    private <T> Optional<T> optionalGet(String key, Class<T> clazz) {
        return ofNullable(clazz.cast(getMessage().get(key)));
    }

    public <T> T getOrElseThrow(String key, Class<T> clazz) {
        return this.optionalGet(key, clazz)
                .orElseThrow(() -> new MessageElementNotFoundException());
    }

    public Optional<String> optionalGet(String key) {
        return this.optionalGet(key, String.class);
    }

    public String getOrElseThrow(String key) {
        return getOrElseThrow(key, String.class);
    }

    public boolean getBool(String key) {
        return this.optionalGet(key, Boolean.class).orElse(false);
    }

    public boolean isSkipDiff() {
        return getBool("skip_diff");
    }
}
