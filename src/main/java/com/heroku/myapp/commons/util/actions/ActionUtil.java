package com.heroku.myapp.commons.util.actions;

import com.heroku.myapp.commons.util.MessageUtil;
import com.heroku.myapp.commons.util.MongoUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;

public abstract class ActionUtil extends MongoUtil {

    private final MessageUtil messageUtil;

    public ActionUtil(Exchange exchange) {
        super(exchange);
        this.messageUtil = new MessageUtil(exchange);
    }

    public Optional<Document> loadDocument() {
        return optionalFindByMessage(message().getMessage());
    }

    public void writeDocument(Document document) {
        this.insertOne(document);
        message().writeObjectId(target.expression() + "_id", document);
    }

    public MessageUtil message() {
        return this.messageUtil;
    }
}
