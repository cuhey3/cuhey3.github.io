package com.heroku.myapp.commons.config.enumerate;

import java.util.Locale;

public enum QueueType {

    SNAPSHOT, DIFF, COMPLETION, CHANGED, EXCEPTION;

    private final String expression;

    private QueueType() {
        this.expression = this.name().toLowerCase(Locale.US);
    }

    public String expression() {
        return this.expression;
    }
}
