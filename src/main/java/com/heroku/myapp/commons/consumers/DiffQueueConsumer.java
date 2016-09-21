package com.heroku.myapp.commons.consumers;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.util.actions.DiffUtil;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.actions.SnapshotUtil;
import com.heroku.myapp.commons.util.consumers.QueueMessage;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.bson.Document;

public abstract class DiffQueueConsumer extends QueueConsumer {

    public DiffQueueConsumer() {
        util().diff();
    }

    public DiffQueueConsumer(Kind kind) {
        util().diff().kind(kind);
    }

    @Override
    public void configure() {
        from(util().ironmqConsumeUri())
                .routeId(util().id())
                .filter(util().camelBatchComplete())
                .filter(comparePredicate())
                .to(util().copy().completion().ironmqPostUri());
    }

    public Optional<Document> calculateDiff(Document master, Document snapshot) {
        return DiffUtil.basicDiff(
                master, snapshot, util().kind().commonDiffKey());
    }

    public Predicate comparePredicate() {
        return (Exchange exchange) -> {
            Optional<Document> optSnapshot, optMaster, optDiff;
            try {
                optSnapshot = new SnapshotUtil(exchange).loadDocument();
                if (!optSnapshot.isPresent()) {
                    return false;
                }
                MasterUtil util = new MasterUtil(exchange);
                optMaster = util.optionalLatest();
                if (!optMaster.isPresent()) {
                    return new QueueMessage(exchange).hasChange(true);
                }
                Document master = optMaster.get();
                Document snapshot = optSnapshot.get();
                optDiff = calculateDiff(master, snapshot);
                if (optDiff.isPresent()) {
                    toDoWhenDiffIsPresent(
                            optDiff.get(), exchange, master, snapshot);
                    return new QueueMessage(exchange).hasChange(true);
                } else if (util.checkNotFilled(master)) {
                    new DiffUtil(exchange).updateMessageComparedId(master);
                    return new QueueMessage(exchange).hasChange(true);
                } else {
                    return new QueueMessage(exchange).hasChange(false);
                }
            } catch (Exception e) {
                util().sendError("comparePredicate", e);
                return false;
            }
        };
    }

    public void toDoWhenDiffIsPresent(Document diff, Exchange exchange, Document master, Document snapshot) {
        new DiffUtil(exchange).updateMessageComparedId(master)
                .writeDocumentWhenDiffIsNotEmpty(diff);
    }
}
