package com.heroku.myapp.commons.consumers;

import com.heroku.myapp.commons.util.actions.SnapshotUtil;
import com.heroku.myapp.commons.util.consumers.QueueMessage;
import com.heroku.myapp.commons.util.consumers.ConsumerUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.bson.Document;

public abstract class SnapshotQueueConsumer extends QueueConsumer {

    public SnapshotQueueConsumer() {
        route().snapshot();
        setKindFromClassName();
    }

    @Override
    public void configure() {
        from(route().snapshot().consumeUri())
                .routeId(route().id())
                .filter(route().camelBatchComplete())
                .filter(defaultPredicate())
                .choice()
                .when((Exchange exchange)
                        -> new QueueMessage(exchange).isSkipDiff())
                .to(route().completionPostUri())
                .otherwise()
                .to(route().diff().postUri());
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
                ConsumerUtil.sendError(this, "defaultPredicate", e);
                return false;
            }
        };
    }

    protected abstract Optional<Document> doSnapshot(Exchange exchange);
}
