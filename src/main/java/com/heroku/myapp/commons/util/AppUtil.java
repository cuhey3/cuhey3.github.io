package com.heroku.myapp.commons.util;

import java.util.function.Consumer;

public class AppUtil {

    public static Consumer<String> shuttingDownConsumer() {
        return (String message) -> {
            System.out.println(message);
            System.out.println("System is shutting down.");
            System.exit(1);
        };
    }
}
