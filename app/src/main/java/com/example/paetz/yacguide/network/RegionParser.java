package com.example.paetz.yacguide.network;

import com.example.paetz.yacguide.UpdateListener;
import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.database.Region;
import com.example.paetz.yacguide.utils.HtmlUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegionParser extends HTMLParser implements NetworkListener {

    private String _countryName;

    public RegionParser(AppDatabase db, String countryName, UpdateListener listener) {
        super(db, listener);
        _countryName = countryName;
    }

    @Override
    public void parse() {
        new NetworkTask(this).execute(HtmlUtils.concat(baseUrl, "boehmen.php?land=" + _countryName));
    }

    @Override
    public void onNetworkTaskResolved(Document document) {
        final Elements tableElements = HtmlUtils.getTableElements(document);
        _processRegions(tableElements);
        listener.onEvent(tableElements.size() > 1); // mind one heading element
    }

    private void _processRegions(Elements elements) {
        db.regionDao().deleteAll(_countryName);
        for (int i = 1; i < elements.size(); i++) { // index 0 is the table header
            Region r = new Region();
            final Elements data = elements.get(i).select("td");
            final Element link = data.get(0).select("a").get(0);
            final Pattern p = Pattern.compile("(.*)gebietid=([0-9]+)(.*)");
            final Matcher m = p.matcher(link.attr("href").toString());
            if (m.find()) {
                r.setId(Integer.parseInt(m.group(2)));
                r.setName(link.text());
                r.setCountry(_countryName);
                db.regionDao().insert(r);
            }
        }
    }
}
