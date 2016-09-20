package com.heroku.myapp.commons.util.actions;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.config.enumerate.MongoTarget;
import com.heroku.myapp.commons.consumers.QueueConsumer;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import static com.heroku.myapp.commons.util.content.DocumentUtil.getData;
import com.mongodb.client.MongoCursor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
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

    public boolean enableDiff(QueueConsumer consumer) {
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
            } else if (diffByIsExists()) {
                String diffBy = queueMessage().optionalGet("diff_by").get();
                Document filter = new Document("diff_by", diffBy);
                MongoCursor<Document> iterator = collection().find(filter).iterator();
                boolean isEnable = false;
                while (iterator.hasNext()) {
                    isEnable = isEnable || iterator.next().get("enable", Boolean.class);
                    if (isEnable) {
                        break;
                    }
                }
                if (isEnable) {
                    return false;
                } else {
                    collection().updateMany(filter, new Document("$set",
                            new Document("enable", true)));
                    iterator = collection().find(filter).iterator();
                    isEnable = true;
                    List<Document> documents = new ArrayList<>();
                    while (iterator.hasNext()) {
                        Document next = iterator.next();
                        documents.add(next);
                        isEnable = isEnable && next.get("enable", Boolean.class);
                        if (!isEnable) {
                            break;
                        }
                    }
                    if (isEnable) {
                        documents.stream()
                                .forEach((document) -> saveToSummary(document));
                    }
                }
                return isEnable;
            } else {
                return true;
            }
        } catch (Exception e) {
            consumer.util().sendError("enableDiff", e);
            return false;
        }
    }

    public DiffUtil updateMessageComparedId(Document masterDocument) {
        queueMessage().writeObjectId("compared_master_id", masterDocument);
        return this;
    }

    public boolean diffIdIsExists() {
        return queueMessage().optionalGet("diff_id").isPresent();
    }

    public boolean diffByIsExists() {
        return queueMessage().optionalGet("diff_by").isPresent();
    }

    @Override
    public void writeDocument(Document document) {
        document.append("enable", false);
        this.insertOne(document);
        queueMessage().writeObjectId(target.expression() + "_id", document);
    }

    public void writeDocumentWhenDiffIsNotEmpty(Document document) {
        if (!new DocumentUtil(document).getDiff().isEmpty()) {
            writeDocument(document);
        }
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

    public MongoCursor<Document> findUnmergedDiffIterator(Map<String, Date> mergedDiffMap) {
        Date date = mergedDiffMap.get(this.kind.expression());
        return collection()
                .find(new Document("creationDate", new Document("$gt", date))
                        .append("enable", true))
                .sort(new Document("creationDate", 1)).iterator();
    }

    public void writeDocuments(Document snapshot, Document diff) {
        String snapshotId = DocumentUtil.objectIdHexString(snapshot);
        List<Document> collect = new DocumentUtil(diff).getDiff().stream()
                .map((map) -> {
                    List<Map<String, Object>> list = new ArrayList<>();
                    list.add(map);
                    Document document = new DocumentUtil().setDiff(list).getDocument();
                    document.append("diff_by", snapshotId);
                    document.append("enable", false);
                    return document;
                })
                .collect(Collectors.toList());
        if (!collect.isEmpty()) {
            this.insertMany(collect);
            queueMessage().writeObjectId("diff_by", snapshot);
        }
    }
}
