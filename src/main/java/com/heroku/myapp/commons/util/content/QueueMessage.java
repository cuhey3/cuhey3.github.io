package com.heroku.myapp.commons.util.content;

import com.heroku.myapp.commons.config.enumerate.Kind;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QueueMessage {

    private Kind kind;
    private List<Kind> affects = new ArrayList<>();
    private boolean skipDiff = false;
    private boolean hasFillField = false;
    private String fillField;
    private Map<String, String> otherKeyValue = new LinkedHashMap<>();

    public QueueMessage(Map map) {
        setKind(map.remove("kind"));
        setAffects(map.remove("affect"));
        setSkipDiff(map.remove("skip_diff"));
        setFillField(map.remove("fill"));
        setOtherKeyValue(map);
    }

    public final void setKind(Object obj) {
        this.kind = Kind.optionalGetKindFromString((String) obj).orElse(null);
    }

    private void setAffects(Object obj) {
        if (obj != null && obj instanceof List) {
            List<String> list = (List<String>) obj;
            list.stream().map((kindString)
                    -> Kind.optionalGetKindFromString(kindString))
                    .filter((optionalKind) -> optionalKind.isPresent())
                    .map((optionalKind) -> optionalKind.get())
                    .forEach(affects::add);
        }
    }

    private void setSkipDiff(Object obj) {
        if (obj != null && obj instanceof Boolean) {
            skipDiff = (Boolean) obj;
        }
    }

    private void setFillField(Object obj) {
        if (obj != null && obj instanceof String) {
            fillField = (String) obj;
            hasFillField = true;
        }
    }

    private void setOtherKeyValue(Map map) {
        otherKeyValue.putAll(map);
    }
}
