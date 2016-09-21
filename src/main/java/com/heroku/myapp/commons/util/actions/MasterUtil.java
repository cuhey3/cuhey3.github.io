package com.heroku.myapp.commons.util.actions;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.config.enumerate.KindOption;
import com.heroku.myapp.commons.config.enumerate.MongoTarget;
import com.heroku.myapp.commons.consumers.QueueConsumer;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.MapList;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.bson.Document;

public class MasterUtil extends ActionUtil {

    public static Predicate isNotFilled(QueueConsumer consumer) {
        return (Exchange exchange) -> {
            try {
                return new MasterUtil(exchange).checkNotFilled(null);
            } catch (Exception ex) {
                consumer.util().sendError("isNotFilled", ex);
                return false;
            }
        };
    }

    private final SnapshotUtil snapshotUtil;

    public MasterUtil(Exchange exchange) {
        super(exchange);
        this.target(MongoTarget.MASTER);
        snapshotUtil = new SnapshotUtil(exchange);
    }

    @Override
    public MasterUtil useDummy() {
        super.useDummy();
        this.snapshotUtil.useDummy();
        return this;
    }

    public boolean toCompleteLogic(RouteBuilder rb) {
        if (this.optionalKind().get().isSkipDiff()) {
            return true;
        } else {
            Optional<Document> optionalLatest = optionalLatest();
            if (optionalLatest.isPresent()) {
                Optional<String> optionalComparedMasterId
                        = queueMessage().optionalGet("compared_master_id");
                return optionalComparedMasterId.isPresent()
                        && DocumentUtil.objectIdHexString(optionalLatest.get())
                        .equals(optionalComparedMasterId.get());
            } else {
                return true;
            }
        }
    }

    public boolean snapshotSaveToMaster(RouteBuilder rb) {
        Optional<Document> optionalSnapshot = snapshotUtil.loadDocument();
        if (optionalSnapshot.isPresent()) {
            this.writeDocument(optionalSnapshot.get());
            return true;
        } else {
            return false;
        }
    }

    public boolean checkNotFilled(Document document) {
        Optional<Kind> optionalKind = this.optionalKind();
        if (optionalKind.isPresent()) {
            Kind k = optionalKind.get();
            if (k.isEnable(KindOption.fill)) {
                String fillField = k.fillField();
                return new MapList(document).stream()
                        .anyMatch((map) -> !map.containsKey(fillField));
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
