package com.example.paetz.yacguide.network;

import com.example.paetz.yacguide.UpdateListener;
import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.database.Country;
import com.example.paetz.yacguide.utils.HtmlUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CountryParser extends HTMLParser implements NetworkListener {

    public CountryParser(AppDatabase db, UpdateListener listener) {
        super(db, listener);
    }

    @Override
    public void parse() {
        new NetworkTask(this).execute(HtmlUtils.concat(baseUrl, "adr.php"));
    }

    @Override
    public void onNetworkTaskResolved(Document document) {
        _processCountries(document.select("ul").get(0).select("li"));
        listener.onEvent(true);
    }

    private void _processCountries(Elements elements) {
        db.countryDao().deleteAll();
        for (Element e : elements) {
            Country c = new Country();
            c.setName(e.text());
            db.countryDao().insert(c);
        }
    }
}
