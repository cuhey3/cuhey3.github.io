package com.heroku.myapp.commons.config.enumerate;

import java.util.Locale;

public enum SenseType {
    EMPTY;

    public String expression() {
        return this.name().toLowerCase(Locale.US);
    }
}
