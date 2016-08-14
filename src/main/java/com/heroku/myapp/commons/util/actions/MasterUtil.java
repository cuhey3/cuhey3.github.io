package com.heroku.myapp.commons.util.actions;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.config.enumerate.MongoTarget;
import com.heroku.myapp.commons.config.enumerate.SenseType;
import com.heroku.myapp.commons.util.consumers.IronmqUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
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

    public boolean comparedIsEmpty() {
        Optional<String> getOptional = message().get("compared_master_id");
        return getOptional.isPresent()
                && SenseType.EMPTY.expression().equals(getOptional.get());
    }

    public boolean isSkipDiff() {
        return message().getBool("skip_diff");
    }

    public boolean comparedIsValid(RouteBuilder rb) {
        try {
            Optional<String> getOptional = message().get("compared_master_id");
            return getOptional.isPresent()
                    && DocumentUtil.objectIdHexString(optionalFind().get())
                    .equals(getOptional.get());
        } catch (Exception ex) {
            IronmqUtil.sendError(rb, "comparedIsValid", ex);
            return false;
        }
    }

    public boolean isSkipComparedValidation() {
        return isSkipDiff() || comparedIsEmpty();
    }

    public boolean snapshotSaveToMaster(RouteBuilder rb) {
        try {
            this.writeDocument(snapshotUtil.loadDocument().get());
            return true;
        } catch (Exception ex) {
            IronmqUtil.sendError(rb, "snapshotSaveToMaster", ex);
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
        Optional<String> getOptional = message().get("fill");
        if (getOptional.isPresent()) {
            String fillField = getOptional.get();
            return DocumentUtil.getData(Optional.ofNullable(document)
                    .orElse(findOrElseThrow())).stream()
                    .anyMatch((map) -> !map.containsKey(fillField));
        } else {
            return false;
        }
    }
}
