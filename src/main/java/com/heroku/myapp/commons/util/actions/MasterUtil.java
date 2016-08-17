package com.heroku.myapp.commons.util.actions;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.config.enumerate.MongoTarget;
import com.heroku.myapp.commons.util.consumers.IronmqUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import static com.heroku.myapp.commons.util.content.DocumentUtil.getData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.bson.Document;
import static com.heroku.myapp.commons.util.content.DocumentUtil.getData;

public class MasterUtil extends ActionUtil {

    public static Predicate isNotFilled(RouteBuilder rb) {
        return (Exchange exchange) -> {
            try {
                return new MasterUtil(exchange).checkNotFilled(null);
            } catch (Exception ex) {
                IronmqUtil.sendError(rb, "isNotFilled", ex);
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
        if (queueMessage().isSkipDiff()) {
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

    public Optional<Document> latestJoinAll(Kind kind1, Kind kind2) {
        Kind kind0 = this.kind;
        try {
            List result = new ArrayList<>();
            result.addAll(getData(findOrElseThrow(kind1)));
            result.addAll(getData(findOrElseThrow(kind2)));
            return new DocumentUtil(result).nullable();
        } finally {
            kind(kind0);
        }
    }

    public boolean checkNotFilled(Document document) {
        Optional<String> optionalFillField = queueMessage().optionalFillField();
        if (optionalFillField.isPresent()) {
            String fillField = optionalFillField.get();
            return DocumentUtil.getData(
                    Optional.ofNullable(document).orElse(findOrElseThrow()))
                    .stream().anyMatch((map) -> !map.containsKey(fillField));
        } else {
            return false;
        }
    }
}
