package com.heroku.myapp.commons.config.enumerate;

import com.google.gson.Gson;
import com.heroku.myapp.commons.util.consumers.QueueConsumerUtil;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Kind {

    in(false),
    out(false),
    all(false),
    summary(false),
    female_seiyu_category_members,
    male_seiyu_category_members,
    seiyu_category_members,
    seiyu_template_include_pages,
    seiyu_category_members_include_template,
    koepota_events,
    koepota_seiyu,
    seiyu_has_recentchanges,
    koepota_seiyu_all,
    amiami_item,
    amiami_original_titles,
    amiami_original_titles_all,
    google_trends_seiyu_all,
    google_trends,
    test(false);

    private String message;
    private boolean useDevelop = false;
    private static Map<String, Object> kindJsonMap;
    private final Map<KindOptions, Map<String, Object>> kindOptionsBody
            = new LinkedHashMap<>();

    private Kind() {
        loadKindJson();
        loadOptions();
    }

    private Kind(boolean loadOptionFlag) {
        if (loadOptionFlag) {
            loadKindJson();
            loadOptions();
        }
    }

    private Kind(boolean loadOptionFlag, boolean developFlag) {
        if (loadOptionFlag) {
            loadKindJson();
            loadOptions();
        }
        if (developFlag) {
            useDevelop = true;
        }
    }

    public boolean isEnable(KindOptions kindOptions) {
        return kindOptionsBody.containsKey(kindOptions);
    }

    public boolean optionIsEnable() {
        return !kindOptionsBody.isEmpty();
    }

    public Object getRaw(KindOptions kindOptions) {
        return kindOptions.toObject(this, kindOptionsBody.get(kindOptions));
    }

    public String get(KindOptions kindOptions) {
        return String.class.cast(getRaw(kindOptions));
    }

    public <T> T get(KindOptions kindOptions, Class<T> clazz) {
        return clazz.cast(getRaw(kindOptions));
    }

    public boolean getBool(KindOptions kindOptions) {
        return get(kindOptions, Boolean.class);
    }

    public List getByList(KindOptions kindOptions) {
        return get(kindOptions, List.class);
    }

    public List<Kind> getByKindList(KindOptions kindOptions) {
        return get(kindOptions, List.class);
    }

    private void loadOptions() {
        Map<String, Object> kindMap
                = (Map<String, Object>) kindJsonMap.get(this.name());
        Map<String, Boolean> kindFlag
                = (Map<String, Boolean>) kindMap.get("flag");
        Map<String, Map<String, Object>> options
                = (Map<String, Map<String, Object>>) kindMap.get("options");
        List<KindOptions> collect = kindFlag.entrySet()
                .stream().filter((entry) -> entry.getValue())
                .map((entry) -> KindOptions.valueOf(entry.getKey()))
                .collect(Collectors.toList());
        collect.stream()
                .forEach((kindOption) -> {
                    kindOptionsBody
                            .put(kindOption, options.get(kindOption.name()));
                });
    }

    public String expression() {
        if (useDevelop) {
            return this.name() + "_develop";
        } else {
            return this.name();
        }
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

    public static Optional<Kind> optionalKindFromString(String str) {
        if (str == null) {
            return Optional.empty();
        } else {
            return Stream.of(Kind.values())
                    .filter((kind) -> kind.name().equals(str))
                    .findFirst();
        }
    }

    public void loadKindJson() {
        if (kindJsonMap == null) {
            kindJsonMap = new LinkedHashMap<>();
            String resourcePath
                    = "../../../../../../kind.json";
            InputStream resourceAsStream
                    = this.getClass().getResourceAsStream(resourcePath);
            try (BufferedReader buffer = new BufferedReader(
                    new InputStreamReader(resourceAsStream, "UTF-8"))) {
                kindJsonMap.putAll(new Gson().fromJson(buffer.lines()
                        .collect(Collectors.joining("\n")), Map.class));
            } catch (Exception ex) {
                System.out.println("kind initialization failed..."
                        + "\nSystem is shutting down.");
                System.out.println(resourcePath);
                System.exit(1);
            }
        }
    }

    public String timerUri() {
        return this.get(KindOptions.polling);
    }

    public String commonDiffKey() {
        return this.get(KindOptions.common_diff);
    }

    public boolean isSkipDiff() {
        return this.isEnable(KindOptions.skip_diff)
                && this.getBool(KindOptions.skip_diff);
    }

    public String fillField() {
        return this.get(KindOptions.fill);
    }

    public List<Kind> affects() {
        return this.getByKindList(KindOptions.affect);
    }

    public String preMessage() {
        return Optional.ofNullable(message).orElseGet(() -> {
            Map map = new LinkedHashMap<>();
            map.put("kind", this.name());
            return message = new Gson().toJson(map);
        });
    }

    public String[] affectQueueUriArray() {
        List<String> collect = this.affects().stream()
                .map((k) -> new QueueConsumerUtil(k).snapshot().ironmqPostUri())
                .collect(Collectors.toList());
        return collect.toArray(new String[collect.size()]);
    }
}
