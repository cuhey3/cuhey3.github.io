package com.heroku.myapp.commons.util.consumers;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.config.enumerate.MongoTarget;
import org.apache.camel.builder.SimpleBuilder;

public class RouteUtil {

    private String target;
    private Kind kind;

    public RouteUtil kind(Kind kind) {
        this.kind = kind;
        return this;
    }

    public RouteUtil timer() {
        this.target = "timer";
        return this;
    }

    public RouteUtil snapshot() {
        this.target = MongoTarget.SNAPSHOT.expression();
        return this;
    }

    public RouteUtil diff() {
        this.target = MongoTarget.DIFF.expression();
        return this;
    }

    public RouteUtil completion() {
        this.target = "completion";
        this.kind = Kind.all;
        return this;
    }

    public RouteUtil changing() {
        this.target = "changing";
        this.kind = Kind.all;
        return this;
    }

    public RouteUtil exception() {
        this.target = "exception";
        this.kind = Kind.in;
        return this;
    }

    public String id() {
        return target + "_" + kind.expression();
    }

    public SimpleBuilder camelBatchComplete() {
        return SimpleBuilder.simple("${exchangeProperty.CamelBatchComplete}");
    }
}
