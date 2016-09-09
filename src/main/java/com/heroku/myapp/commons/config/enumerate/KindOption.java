package com.heroku.myapp.commons.config.enumerate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum KindOption {
    polling {
        @Override
        public Object toObject(Kind kind, Map<String, Object> optionsBody) {

            return String.format("timer:%s?period=%s&delay=%s",
                    kind.expression(),
                    optionsBody.get("period"),
                    optionsBody.get("delay"));
        }
    }, common_diff {
        @Override
        public Object toObject(Kind kind, Map<String, Object> optionsBody) {
            return optionsBody.get("key");
        }
    }, skip_diff {
        @Override
        public Object toObject(Kind kind, Map<String, Object> optionsBody) {
            return Boolean.TRUE;
        }
    }, fill {
        @Override
        public Object toObject(Kind kind, Map<String, Object> optionsBody) {
            return optionsBody.get("field");
        }
    }, affect {
        @Override
        public Object toObject(Kind kind, Map<String, Object> optionsBody) {
            List<String> affectsString
                    = (List<String>) optionsBody.get("affects");
            return affectsString.stream().map((str) -> Kind.valueOf(str))
                    .collect(Collectors.toList());
        }
    }, always_affect {
        @Override
        public Object toObject(Kind kind, Map<String, Object> optionsBody) {
            List<String> affectsString
                    = (List<String>) optionsBody.get("always_affects");
            return affectsString.stream().map((str) -> Kind.valueOf(str))
                    .collect(Collectors.toList());
        }
    };

    public abstract Object toObject(Kind kind, Map<String, Object> optionsBody);

}
