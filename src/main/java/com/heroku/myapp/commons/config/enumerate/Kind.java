package com.heroku.myapp.commons.config.enumerate;

import com.google.gson.Gson;
import com.heroku.myapp.commons.util.AppUtil;
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
    test(false),
    female_seiyu_category_members,
    male_seiyu_category_members,
    seiyu_category_members,
    seiyu_template_include_pages,
    seiyu_category_members_include_template,
    koepota_events,
    koepota_seiyu,
    seiyu_has_recentchanges,
    koepota_seiyu_all,
    koepota_ranking,
    amiami_item,
    amiami_original_titles,
    amiami_original_titles_all,
    google_trends_seiyu_all,
    google_trends,
    seiyu_multi_lang,
    categories_related_seiyu,
    pages_related_seiyu,
    recentchanges_of_seiyu,
    pages_mutual_sound_director,
    sound_director_statistics,
    categories_related_sound_director,
    pages_related_sound_director;

    private String message;
    private boolean useDevelop = false;
    private static Map<String, Object> kindJsonMap;
    private final Map<KindOption, Map<String, Object>> kindOptionMap
            = new LinkedHashMap<>();
    private final boolean isShowCompletion;

    private Kind() {
        isShowCompletion = true;
        loadKindJson();
        loadOptions();
    }

    private Kind(boolean loadOptionFlag) {
        isShowCompletion = loadOptionFlag;
        if (loadOptionFlag) {
            loadKindJson();
            loadOptions();
        }
    }

    private Kind(boolean loadOptionFlag, boolean developFlag) {
        isShowCompletion = loadOptionFlag;
        if (loadOptionFlag) {
            loadKindJson();
            loadOptions();
        }
        if (developFlag) {
            useDevelop = true;
        }
    }

    public String expression() {
        if (useDevelop) {
            return this.name() + "_develop";
        } else {
            return this.name();
        }
    }

    public void loadKindJson() {
        if (kindJsonMap == null) {
            kindJsonMap = new LinkedHashMap<>();
            String resourcePath = "../../../../../../kind.json";
            InputStream resourceAsStream = this.getClass()
                    .getResourceAsStream(resourcePath);
            try (BufferedReader buffer = new BufferedReader(
                    new InputStreamReader(resourceAsStream, "UTF-8"))) {
                kindJsonMap.putAll(new Gson().fromJson(buffer.lines()
                        .collect(Collectors.joining("\n")), Map.class));
            } catch (Exception ex) {
                String mes = ">>>" + resourcePath
                        + "¥nkind initialization failed...";
                AppUtil.shuttingDownConsumer().accept(mes);
            }
        }
    }

    private void loadOptions() {
        Map<String, Object> kindBody
                = (Map<String, Object>) kindJsonMap.get(this.name());
        Map<String, Boolean> kindOptionFlag
                = (Map<String, Boolean>) kindBody.get("flag");
        Map<String, Map<String, Object>> optionsParam
                = (Map<String, Map<String, Object>>) kindBody.get("options");
        List<KindOption> kindOptionsList = kindOptionFlag.entrySet().stream()
                .filter((entry) -> entry.getValue())
                .map((entry) -> KindOption.valueOf(entry.getKey()))
                .collect(Collectors.toList());
        kindOptionsList.stream().forEach((kindOption) -> {
            kindOptionMap.put(kindOption, optionsParam.get(kindOption.name()));
        });
    }

    public String get(KindOption kindOptions) {
        return String.class.cast(getRaw(kindOptions));
    }

    public <T> T get(KindOption kindOptions, Class<T> clazz) {
        return clazz.cast(getRaw(kindOptions));
    }

    public Object getRaw(KindOption kindOptions) {
        return kindOptions.toObjFunc.apply(this, kindOptionMap.get(kindOptions));
    }

    public boolean getBool(KindOption kindOptions) {
        return get(kindOptions, Boolean.class);
    }

    public List<Kind> getByKindList(KindOption kindOptions) {
        return get(kindOptions, List.class);
    }

    public static Optional<Kind> optionalKindFromClassName(Object object) {
        String kindCamelCase = object.getClass().getSimpleName()
                .replaceAll("(^Snapshot|^Diff|Consumer$)", "");
        String kindSnake
                = String.join("_", kindCamelCase.split("(?=[\\p{Upper}])"))
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

    public String timerUri() {
        return this.get(KindOption.polling);
    }

    public String commonDiffKey() {
        return this.get(KindOption.common_diff);
    }

    public boolean isSkipDiff() {
        return this.isEnable(KindOption.skip_diff)
                && this.getBool(KindOption.skip_diff);
    }

    public boolean isEnable(KindOption kindOptions) {
        return kindOptionMap.containsKey(kindOptions);
    }

    public boolean hasConsumer() {
        return !kindOptionMap.isEmpty();
    }

    public String fillField() {
        return this.get(KindOption.fill);
    }

    public List<Kind> affects() {
        return this.getByKindList(KindOption.affect);
    }

    public List<Kind> alwaysAffects() {
        return this.getByKindList(KindOption.always_affect);
    }

    public String preMessage() {
        return Optional.ofNullable(message).orElseGet(() -> {
            Map map = new LinkedHashMap<>();
            map.put("kind", this.name());
            return message = new Gson().toJson(map);
        });
    }

    public boolean isShowCompletion() {
        return isShowCompletion;
    }
}
