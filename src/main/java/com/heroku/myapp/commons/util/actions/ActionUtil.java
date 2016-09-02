package com.heroku.myapp.commons.util.actions;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.util.MongoUtil;
import com.heroku.myapp.commons.util.consumers.QueueMessage;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;

public abstract class ActionUtil extends MongoUtil {

    private final QueueMessage queueMessage;

    public ActionUtil(Exchange exchange) {
        super(exchange);
        this.queueMessage = new QueueMessage(exchange);
    }

    public Optional<Document> loadDocument() {
        return optionalFindByMessage(queueMessage.map());
    }

    public void writeDocument(Document document) {
        this.insertOne(document);
        queueMessage().writeObjectId(target.expression() + "_id", document);
    }

    public QueueMessage queueMessage() {
        return queueMessage;
    }

    public Optional<Document> optionalDocumentFromKindString(String str) {
        Optional<Kind> optionalKind = Kind.optionalKindFromString(str);
        if (optionalKind.isPresent()) {
            return kind(optionalKind.get()).optionalLatest();
        } else {
            return Optional.empty();
        }
    }

    public Optional<Kind> optionalKind() {
        return Optional.ofNullable(kind);
    }
}
