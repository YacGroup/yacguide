package com.example.paetz.yacguide.network;

import com.example.paetz.yacguide.UpdateListener;
import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.database.Sector;
import com.example.paetz.yacguide.utils.HtmlUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SectorParser extends HTMLParser implements NetworkListener {

    private int _regionId;

    public SectorParser(AppDatabase db, int regionId, UpdateListener listener) {
        super(db, listener);
        _regionId = regionId;
    }

    @Override
    public void parse() {
        new NetworkTask(this).execute(HtmlUtils.concat(baseUrl, "gebiet.php?gebietid=" + _regionId));
    }

    @Override
    public void onNetworkTaskResolved(Document document) {
        final Elements tableElements = HtmlUtils.getTableElements(document);
        _processSectors(tableElements);
        listener.onEvent(tableElements.size() > 2); // mind one heading and one sum element
    }

    private void _processSectors(Elements elements) {
        db.sectorDao().deleteAll(_regionId);
        for (int i = 1; i < elements.size() - 1; i++) { // index 0 is the table header, last index is the sum row
            Sector s = new Sector();
            final Elements data = elements.get(i).select("td");
            final Element link = data.get(1).select("a").get(0);
            final Pattern p = Pattern.compile("(.*)sektorid=([0-9]+)(.*)");
            final Matcher m = p.matcher(link.attr("href").toString());
            if (m.find()) {
                s.setId(Integer.parseInt(m.group(2)));
                s.setName(link.text());
                s.setParentId(_regionId);
                db.sectorDao().insert(s);
            }
        }
    }
}
