package com.heroku.myapp.commons.config.enumerate;

import java.util.Locale;

public enum MongoTarget {

    SNAPSHOT, DIFF, MASTER, DUMMY, SEIYULAB;

    private final String expression;

    private MongoTarget() {
        this.expression = this.name().toLowerCase(Locale.US);
    }

    public String expression() {
        return this.expression;
    }
}
