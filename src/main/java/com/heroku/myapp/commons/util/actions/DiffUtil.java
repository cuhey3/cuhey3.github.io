package com.heroku.myapp.commons.util.actions;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.config.enumerate.MongoTarget;
import com.heroku.myapp.commons.util.consumers.IronmqUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import static com.heroku.myapp.commons.util.content.DocumentUtil.getData;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.bson.Document;

public class DiffUtil extends ActionUtil {

    public static String commonDiff() {
        return commonDiff("title");
    }

    public static String commonDiff(String key) {
        return "common_diff_key=" + key;
    }

    public static Optional<Document> basicDiff(Document master, Document snapshot, String key) {
        List<Map<String, Object>> prev, next, collect;
        prev = getData(master);
        next = getData(snapshot);
        prev.forEach((map) -> map.put("type", "remove"));
        next.forEach((map) -> map.put("type", "add"));
        Set<String> prevTitleSet = prev.stream()
                .map((map) -> (String) map.get(key))
                .collect(Collectors.toSet());
        Set<String> nextTitleSet = next.stream()
                .map((map) -> (String) map.get(key))
                .collect(Collectors.toSet());
        collect = prev.stream()
                .filter((map) -> !nextTitleSet.contains((String) map.get(key)))
                .collect(Collectors.toList());
        next.stream()
                .filter((map) -> !prevTitleSet.contains((String) map.get(key)))
                .forEach(collect::add);
        if (collect.size() > 0) {
            return new DocumentUtil().setDiff(collect).nullable();
        } else {
            return Optional.empty();
        }
    }

    public DiffUtil(Exchange exchange) {
        super(exchange);
        target(MongoTarget.DIFF);
    }

    public boolean enableDiff(RouteBuilder rb) {
        try {
            if (diffIdIsExists()) {
                Optional<Document> optionalLoadedDocument = loadDocument();
                if (optionalLoadedDocument.isPresent()) {
                    Document diff = optionalLoadedDocument.get();
                    if (!diff.get("enable", Boolean.class)) {
                        Document query = new Document("_id", diff.get("_id"));
                        Document updateDocument = new Document(
                                "$set", new Document("enable", true));
                        this.collection().updateOne(query, updateDocument);
                        saveToSummary(diff);
                        return true;
                    }
                }
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            IronmqUtil.sendError(rb, "enableDiff", e);
            return false;
        }
    }

    public DiffUtil updateMessageComparedId(Document masterDocument) {
        message().writeObjectId("compared_master_id", masterDocument);
        return this;
    }

    public boolean diffIdIsExists() {
        return message().optionalGet("diff_id").isPresent();
    }

    @Override
    public void writeDocument(Document document) {
        document.append("enable", false);
        this.insertOne(document);
        message().writeObjectId(target.expression() + "_id", document);
    }

    private void saveToSummary(Document diff) {
        Kind kind0 = this.kind;
        diff.remove("enable");
        diff.append("kind", kind0.expression());
        this.kind(Kind.summary);
        try {
            this.insertOne(diff);
        } finally {
            this.kind = kind0;
        }
    }
}
