package com.heroku.myapp.commons.util;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ResourceUtil {

    final String path;

    public ResourceUtil(String path) {
        this.path = path;
    }

    public <T> T getJson(Class<T> clazz) {
        InputStream resourceAsStream = this.getClass()
                .getResourceAsStream(path);
        try (BufferedReader buffer = new BufferedReader(
                new InputStreamReader(resourceAsStream, "UTF-8"))) {
            return new Gson().fromJson(buffer.lines()
                    .collect(Collectors.joining("\n")), clazz);
        } catch (IOException ex) {
            throw new RuntimeException();
        }
    }
}
