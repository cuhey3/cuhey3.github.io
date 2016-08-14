package com.heroku.myapp.commons.config.converters;

import com.google.gson.Gson;
import io.iron.ironmq.Client;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;

public class MyConverters implements TypeConverters {

    private final CamelContext context;

    public MyConverters(CamelContext context) {
        this.context = context;
    }

    @Converter
    public Client toClient(String beanName) {
        return context.getRegistry()
                .lookupByNameAndType(beanName, Client.class);
    }

    @Converter
    public Map gsonStringToMap(String gsonString) {
        return new Gson().fromJson(gsonString, Map.class);
    }

    @Converter
    public String mapToGsonString(Map map) {
        return new Gson().toJson(map);
    }
}
