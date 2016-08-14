package com.heroku.myapp.commons.config;

import com.heroku.myapp.commons.config.converters.MyConverters;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConverterConfig {

    @Autowired
    private CamelContext context;

    @Bean
    ConverterConfig converterReady() {
        MyConverters myConverters = new MyConverters(context);
        context.getTypeConverterRegistry().addTypeConverters(myConverters);
        return this;
    }
}
