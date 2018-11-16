package com.example.paetz.yacguide.utils;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class HtmlUtils {

    public static String htmlTrim(String text) {
        text.replace("&nbsp;", "");
        text.trim();
        return text;
    }

    public static String concat(String url1, String url2) {
        return url1 + "/" + url2;
    }

    public static Elements getTableElements(Document doc) {
        Elements tableElements = doc.select("table");
        if (tableElements.isEmpty()) {
            return tableElements;
        }
        return doc.select("table").get(0)
                .select("tr").get(0)
                .select("td").get(0)
                .select("table").get(0)
                .select("tr");
    }
}
