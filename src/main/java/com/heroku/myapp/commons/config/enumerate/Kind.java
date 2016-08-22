package com.heroku.myapp.commons.config.enumerate;

import com.google.gson.Gson;
import static com.heroku.myapp.commons.util.actions.DiffUtil.commonDiff;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Kind {

    in("noPremessage"),
    out("noPremessage"),
    all("noPremessage"),
    summary("noPremessage"),
    female_seiyu_category_members(commonDiff(), "period=5m&delay=1m"),
    male_seiyu_category_members(commonDiff(), "period=5m&delay=2m"),
    seiyu_category_members(commonDiff()),
    seiyu_template_include_pages(commonDiff(), "period=5m&delay=3m"),
    seiyu_category_members_include_template(commonDiff()),
    koepota_events(commonDiff("url"), "period=30m&delay=10m"),
    koepota_seiyu(commonDiff()),
    seiyu_has_recentchanges(commonDiff()),
    koepota_seiyu_all(commonDiff()),
    amiami_item("period=60m&delay=30m"),
    amiami_original_titles(commonDiff("amiami_title")),
    amiami_original_titles_all(commonDiff("amiami_title")),
    google_trends_seiyu_all(commonDiff()),
    google_trends("period=65m&delay=10m"),
    test("noPremessage");

    private String timerUri, preMessage, commonDiffKey, timerPeriod, timerDelay;
    private String message;
    private boolean useCommonDiff = false;
    private boolean useDevelop = false;
    private boolean loadPremessage = true;
    private boolean hasFillField = false;
    private boolean isSkipDiff = false;
    private String fillField;
    private Map<String, Object> map;
    private List<String> stringAffects;

    private Kind(String... token) {
        loadJson();
//        parseToken(token);
//        setPremessage();
    }

    private void parseToken(String... token) {
        for (String t : token) {
            if (t.contains("common_diff_key=")) {
                this.useCommonDiff = true;
                this.commonDiffKey = t.replace("common_diff_key=", "");
            } else if (t.contains("period=")) {
                this.timerUri = String.format("timer:%s?%s", this.name(), t);
            } else if (t.equals("develop")) {
                this.useDevelop = true;
            } else if (t.equals("noPremessage")) {
                this.loadPremessage = false;
            }
        }
    }

    private void loadJson() {
        if (loadPremessage) {
            Gson gson = new Gson();
            map = gson.fromJson(loadLocalSource(), Map.class);
            setOptions();
        }
    }

    public Optional<String> optionalFillField() {
        return Optional.ofNullable(this.fillField);
    }

    private void setOptions() {
        if (map.containsKey("common_diff_key")) {
            this.useCommonDiff = true;
            this.commonDiffKey = (String) map.get("common_diff_key");
        }
        if (map.containsKey("fill_field")) {
            this.hasFillField = true;
            this.fillField = (String) map.get("fill_field");
        }
        this.timerPeriod = (String) ofNullable(map.get("period")).orElse(null);
        this.timerDelay = (String) ofNullable(map.get("delay")).orElse(null);
        this.useDevelop = (boolean) ofNullable(map.get("develop")).orElse(false);
        this.isSkipDiff = (boolean) ofNullable(map.get("skip_diff")).orElse(false);
        Map messageMap = (Map) Optional.ofNullable(map.get("message"))
                .orElse(new LinkedHashMap<>());
        messageMap.put("kind", this.name());
        this.message = new Gson().toJson(messageMap);
        this.stringAffects = (List) Optional.ofNullable(map.get("affects"))
                .orElse(new ArrayList<>());
    }

    private void setPremessage() {
        if (loadPremessage) {
            preMessage = loadLocalSource();
        } else {
            preMessage = "{}";
        }
    }

    public String expression() {
        if (useDevelop) {
            return this.name() + "_develop";
        } else {
            return this.name();
        }
    }

    public String timerUri() {
        return this.timerUri;
    }

    public String commonDiffKey() {
        return this.commonDiffKey;
    }

    public void timerParam(String timerParam) {
        this.timerUri = String.format("timer:%s?%s", expression(), timerParam);
    }

    public boolean isUsedCommonDiffRoute() {
        return this.useCommonDiff;
    }

    public static Optional<Kind> optionalKindFromClassName(Object object) {
        String kindCamel = object.getClass().getSimpleName()
                .replace("Snapshot", "").replace("Diff", "")
                .replace("Consumer", "");
        String kindSnake
                = String.join("_", kindCamel.split("(?=[\\p{Upper}])"))
                .toLowerCase(Locale.US);
        return optionalKindFromString(kindSnake);
    }

    public String preMessage() {
        return this.preMessage;
    }

    public static Optional<Kind> optionalKindFromString(String str) {
        if (str == null) {
            return Optional.empty();
        } else {
            return Stream.of(Kind.values())
                    .filter((kind) -> kind.name().equals(str))
                    .findFirst();
        }
    }

    private String loadLocalSource() {
        String resourcePath
                = "../../../../../../message/" + this.name() + ".json";
        InputStream resourceAsStream
                = this.getClass().getResourceAsStream(resourcePath);
        try (BufferedReader buffer = new BufferedReader(
                new InputStreamReader(resourceAsStream, "UTF-8"))) {
            String collect = buffer.lines().collect(Collectors.joining("\n"));
            System.out.println("loaded " + resourcePath);
            return collect;
        } catch (Exception ex) {
            System.out.println("premessage initialization failed..."
                    + "\nSystem is shutting down.");
            System.out.println(resourcePath);
            System.exit(1);
            return null;
        }
    }

    public boolean isSkipDiff() {
        return this.isSkipDiff;
    }
}
