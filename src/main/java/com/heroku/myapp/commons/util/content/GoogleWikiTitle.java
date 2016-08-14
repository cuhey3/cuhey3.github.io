package com.heroku.myapp.commons.util.content;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class GoogleWikiTitle {

    public Optional<Set<String>> google(String keyword) {
        try {
            keyword = URLEncoder.encode(keyword.replace("-", "－"), "UTF-8");
            Document doc = Jsoup.connect("https://www.google.co.jp/search"
                    + "?ie=utf-8&oe=utf-8&hl=ja"
                    + "&q=" + keyword + " wikipedia")
                    .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.130 Safari/537.36")
                    .timeout(Integer.MAX_VALUE).get();
            doc.select("table").remove();
            Elements select
                    = doc.select("h3 a[href^=https://ja.wikipedia.org/wiki/]");
            Set<String> collect = select.stream()
                    .map((e) -> e.attr("href")
                            .replace("https://ja.wikipedia.org/wiki/", "")
                            .replaceFirst("#.+", "")
                            .replace("_", " "))
                    .map((String t) -> {
                        try {
                            return URLDecoder.decode(t, "UTF-8");
                        } catch (UnsupportedEncodingException ex) {
                            return "";
                        }
                    }).collect(Collectors.toSet());
            return Optional.ofNullable(collect);
        } catch (Exception ex) {
            Set<String> set = new HashSet<>();
            set.add("google error");
            return Optional.ofNullable(set);
        }
    }
}
