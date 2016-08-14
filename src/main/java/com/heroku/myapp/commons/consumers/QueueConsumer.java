package com.heroku.myapp.commons.consumers;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.util.consumers.IronmqUtil;
import com.heroku.myapp.commons.util.consumers.RouteUtil;
import java.util.Optional;
import org.apache.camel.builder.RouteBuilder;

public abstract class QueueConsumer extends RouteBuilder {

    protected Kind kind;
    private IronmqUtil ironmqUtil;
    private RouteUtil routeUtil;

    public final void kind(Kind kind) {
        this.kind = kind;
        ironmq().kind(kind);
        route().kind(kind);
    }

    public final IronmqUtil ironmq() {
        return Optional.ofNullable(ironmqUtil)
                .orElseGet(() -> ironmqUtil = new IronmqUtil());
    }

    public final RouteUtil route() {
        return Optional.ofNullable(routeUtil)
                .orElseGet(() -> routeUtil = new RouteUtil());
    }
}
