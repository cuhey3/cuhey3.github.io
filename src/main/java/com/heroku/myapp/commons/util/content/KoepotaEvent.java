package com.heroku.myapp.commons.util.content;

import org.jsoup.nodes.Element;

public class KoepotaEvent {

    private String url;
    private String d1;
    private String t1;
    private String l1;
    private String c1;
    private String d2;
    private String t2;
    private String d3;
    private String d4;
    private String l2;
    private String c2;
    private String p1;
    private String w1;

    public KoepotaEvent(String url, String d1, String t1, String l1, String c1, String d2) {
        this.url = url;
        this.d1 = d1;
        this.t1 = t1;
        this.l1 = l1;
        this.c1 = c1;
        this.d2 = d2;
    }

    public KoepotaEvent(org.bson.Document doc) {
        this.url = (String) doc.get("url");
        this.d1 = (String) doc.get("d1");
        this.t1 = (String) doc.get("t1");
        this.l1 = (String) doc.get("l1");
        this.c1 = (String) doc.get("c1");
        this.d2 = (String) doc.get("d2");
        this.t2 = (String) doc.get("t2");
        this.d3 = (String) doc.get("d3");
        this.d4 = (String) doc.get("d4");
        this.l2 = (String) doc.get("l2");
        this.c2 = (String) doc.get("c2");
        this.p1 = (String) doc.get("p1");
        this.w1 = (String) doc.get("w1");
    }

    public KoepotaEvent(Element el) {
        this(el.select("td.title a").attr("href")
                .replace("http://www.koepota.jp/eventschedule/", ""),
                el.select("td.day")
                .size() > 0 ? el.select("td.day").get(0).text() : "",
                el.select("td.title").text(),
                el.select("td.hall").text(),
                el.select("td.number").text(),
                el.select("td.day")
                .size() > 1 ? el.select("td.day").get(1).text() : "");
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getD1() {
        return d1;
    }

    public void setD1(String d1) {
        this.d1 = d1;
    }

    public String getT1() {
        return t1;
    }

    public void setT1(String t1) {
        this.t1 = t1;
    }

    public String getL1() {
        return l1;
    }

    public void setL1(String l1) {
        this.l1 = l1;
    }

    public String getC1() {
        return c1;
    }

    public void setC1(String c1) {
        this.c1 = c1;
    }

    public String getD2() {
        return d2;
    }

    public void setD2(String d2) {
        this.d2 = d2;
    }

    public String getT2() {
        return t2;
    }

    public void setT2(String t2) {
        this.t2 = t2;
    }

    public String getD3() {
        return d3;
    }

    public void setD3(String d3) {
        this.d3 = d3;
    }

    public String getD4() {
        return d4;
    }

    public void setD4(String d4) {
        this.d4 = d4;
    }

    public String getL2() {
        return l2;
    }

    public void setL2(String l2) {
        this.l2 = l2;
    }

    public String getC2() {
        return c2;
    }

    public void setC2(String c2) {
        this.c2 = c2;
    }

    public String getP1() {
        return p1;
    }

    public void setP1(String p1) {
        this.p1 = p1;
    }

    public String getW1() {
        return w1;
    }

    public void setW1(String w1) {
        this.w1 = w1;
    }

    public org.bson.Document getDocument() {
        org.bson.Document doc = new org.bson.Document();
        doc.put("url", this.url);
        doc.put("d1", this.d1);
        doc.put("t1", this.t1);
        doc.put("l1", this.l1);
        doc.put("c1", this.c1);
        doc.put("d2", this.d2);
        return doc;
    }
}
