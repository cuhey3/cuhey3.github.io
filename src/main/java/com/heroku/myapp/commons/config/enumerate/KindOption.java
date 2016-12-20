package com.heroku.myapp.commons.config.enumerate;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public enum KindOption {
    polling((kind, options)
            -> String.format("timer:%s?period=%s&delay=%s", kind.expression(),
                    options.get("period"), options.get("delay"))),
    common_diff((kind, options) -> options.get("key")),
    skip_diff((kind, options) -> Boolean.TRUE),
    fill((kind, options) -> options.get("field")),
    affect((kind, options)
            -> ((List<String>) options.get("affects"))
            .stream().map((str) -> Kind.valueOf(str))
            .collect(Collectors.toList())),
    always_affect((kind, optionsBody)
            -> ((List<String>) optionsBody.get("always_affects"))
            .stream().map((str) -> Kind.valueOf(str))
            .collect(Collectors.toList()));

    public BiFunction<Kind, Map<String, Object>, Object> toObjFunc;

    private KindOption(BiFunction<Kind, Map<String, Object>, Object> toObjFunc) {
        this.toObjFunc = toObjFunc;
    }
}
