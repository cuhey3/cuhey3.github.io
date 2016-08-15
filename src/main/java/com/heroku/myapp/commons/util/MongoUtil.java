package com.heroku.myapp.commons.util;

import com.heroku.myapp.commons.config.MongoConfig;
import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.config.enumerate.MongoTarget;
import com.heroku.myapp.commons.exceptions.DocumentNotFoundException;
import com.heroku.myapp.commons.exceptions.MongoUtilTypeNotSetException;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import org.apache.camel.Exchange;
import org.apache.camel.spi.Registry;
import org.bson.Document;
import org.bson.types.ObjectId;

public class MongoUtil {

    private final Registry registry;
    protected MongoTarget target;
    protected MongoTarget customTarget;
    protected Kind kind;

    public MongoUtil(Exchange exchange) {
        this.registry = exchange.getContext().getRegistry();
        Optional<Kind> optionalKind = MessageUtil.optionalGetKind(exchange);
        if (optionalKind.isPresent()) {
            this.kind = optionalKind.get();
        }
    }

    public final MongoUtil target(MongoTarget target) {
        this.target = target;
        return this;
    }

    public MongoUtil useDummy() {
        this.customTarget = MongoTarget.DUMMY;
        return this;
    }

    public final MongoUtil kind(Kind kind) {
        this.kind = kind;
        return this;
    }

    public MongoUtil snapshot() {
        this.target = MongoTarget.SNAPSHOT;
        return this;
    }

    public MongoUtil diff() {
        this.target = MongoTarget.DIFF;
        return this;
    }

    public MongoUtil master() {
        this.target = MongoTarget.MASTER;
        return this;
    }

    private MongoDatabase database(MongoTarget t) {
        return registry.lookupByNameAndType(t.expression(), MongoClient.class)
                .getDatabase(MongoConfig.getMongoClientURI(t).getDatabase());
    }

    public MongoDatabase database() {
        return database(target);
    }

    public MongoCollection<Document> collection() {
        return database(ofNullable(
                ofNullable(this.customTarget).orElse(this.target))
                .orElseThrow(() -> new MongoUtilTypeNotSetException()))
                .getCollection(collectionName());
    }

    public Optional<Document> optionalLatest() {
        return nextDocument(latestIterable(), false);
    }

    private FindIterable<Document> latestIterable() {
        return collection().find()
                .sort(new Document("creationDate", -1)).limit(1);
    }

    private Optional<Document> optionalFindById(String objectIdHexString) {
        return nextDocument(collection().find(
                new Document("_id", new ObjectId(objectIdHexString))), false);
    }

    public Optional<Document> optionalFindByMessage(Map message) {
        return optionalFindById((String) message.get(targetIdKey()));
    }

    protected String insertOne(Document document) {
        if (!document.containsKey("creationDate")) {
            document.append("creationDate", new Date());
        }
        collection().insertOne(document);
        return DocumentUtil.objectIdHexString(document);
    }

    private String collectionName() {
        return target.expression() + "_" + kind.expression();
    }

    private String targetIdKey() {
        return target.expression() + "_id";
    }

    private Optional<Document> nextDocument(FindIterable<Document> iterable, boolean bool) {
        MongoCursor<Document> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            return Optional.ofNullable(iterator.next());
        } else if (bool) {
            throw new DocumentNotFoundException();
        } else {
            return Optional.empty();
        }
    }

    public Document findOrElseThrow() {
        return nextDocument(latestIterable(), true).get();
    }

    public Document findOrElseThrow(Kind kind) {
        Kind kind0 = kind;
        kind(kind);
        try {
            return findOrElseThrow();
        } finally {
            kind(kind0);
        }
    }
}
