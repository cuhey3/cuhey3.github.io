package com.heroku.myapp.commons.util.actions;

import com.heroku.myapp.commons.config.enumerate.MongoTarget;
import org.apache.camel.Exchange;

public class SnapshotUtil extends ActionUtil {

    public SnapshotUtil(Exchange exchange) {
        super(exchange);
        this.target(MongoTarget.SNAPSHOT);
    }
}
