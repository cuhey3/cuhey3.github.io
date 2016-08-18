package com.heroku.myapp.commons.consumers;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.util.consumers.ConsumerUtil;
import java.util.Optional;
import org.apache.camel.builder.RouteBuilder;

public abstract class QueueConsumer extends RouteBuilder {

    protected Kind kind;
    private ConsumerUtil routeUtil;

    public final void kind(Kind kind) {
        this.kind = kind;
        route().kind(kind);
    }

    public final ConsumerUtil route() {
        return Optional.ofNullable(routeUtil)
                .orElseGet(() -> routeUtil = new ConsumerUtil());
    }

    public final void setKindFromClassName() {
        kind(Kind.optionalKindFromClassName(this).orElse(null));
    }
}
