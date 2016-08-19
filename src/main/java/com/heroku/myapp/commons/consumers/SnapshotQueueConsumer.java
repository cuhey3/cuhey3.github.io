package com.heroku.myapp.commons.consumers;

import com.heroku.myapp.commons.util.actions.SnapshotUtil;
import com.heroku.myapp.commons.util.consumers.QueueMessage;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.bson.Document;

public abstract class SnapshotQueueConsumer extends QueueConsumer {

    public SnapshotQueueConsumer() {
        util().snapshot();
    }

    @Override
    public void configure() {
        from(util().consumeUri())
                .routeId(util().id())
                .filter(util().camelBatchComplete())
                .filter(defaultPredicate())
                .choice()
                .when((Exchange exchange)
                        -> new QueueMessage(exchange).isSkipDiff())
                .to(util().copy().completion().postUri())
                .otherwise()
                .to(util().copy().diff().postUri());
    }

    protected Predicate defaultPredicate() {
        return (Exchange exchange) -> {
            Optional<Document> snapshot = doSnapshot(exchange);
            try {
                if (snapshot.isPresent()) {
                    new SnapshotUtil(exchange).writeDocument(snapshot.get());
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                util().sendError("defaultPredicate", e);
                return false;
            }
        };
    }

    protected abstract Optional<Document> doSnapshot(Exchange exchange);
}
