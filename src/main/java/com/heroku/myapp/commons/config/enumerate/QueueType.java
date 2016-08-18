package com.heroku.myapp.commons.config.enumerate;

import java.util.Locale;

public enum QueueType {

    TIMER, SNAPSHOT, DIFF, COMPLETION, CHANGING, CHANGED, EXCEPTION;

    private final String expression;

    private QueueType() {
        this.expression = this.name().toLowerCase(Locale.US);
    }

    public String expression() {
        return this.expression;
    }
}
