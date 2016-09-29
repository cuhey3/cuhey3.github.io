package com.heroku.myapp.commons.consumers;

import com.heroku.myapp.commons.util.actions.SnapshotUtil;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.bson.Document;

public abstract class SnapshotQueueConsumer extends QueueConsumer {
    
    public SnapshotQueueConsumer() {
        util().snapshot();
    }
    
    @Override
    public void configure() {
        from(util().ironmqConsumeUri())
                .routeId(util().id())
                .filter(util().camelBatchComplete())
                .process(fillDefaultMessage())
                .filter(doSnapshotPredicate())
                .choice()
                .when(constant(util().kind().isSkipDiff()))
                .to(util().copy().completion().ironmqPostUri())
                .otherwise()
                .to(util().copy().diff().ironmqPostUri());
    }
    
    protected Predicate doSnapshotPredicate() {
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
    
    protected Processor fillDefaultMessage() {
        return (Exchange exchange) -> {
            String body = exchange.getIn().getBody(String.class);
            if (body == null || body.isEmpty()) {
                Map map = new LinkedHashMap<>();
                map.put("kind", util().kind().expression());
                exchange.getIn().setBody(map, String.class);
            }
        };
    }
}
