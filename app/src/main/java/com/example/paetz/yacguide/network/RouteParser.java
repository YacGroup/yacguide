package com.example.paetz.yacguide.network;

import com.example.paetz.yacguide.UpdateListener;
import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.database.Route;
import com.example.paetz.yacguide.utils.HtmlUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouteParser extends HTMLParser implements NetworkListener, UpdateListener {

    private int _rockId;
    private int _routeCount;
    private int _resolvedRouteCount;

    public RouteParser(AppDatabase db, int rockId, UpdateListener listener) {
        super(db, listener);
        _rockId = rockId;
    }

    @Override
    public void parse() {
        _resolvedRouteCount = 0;
        new NetworkTask(this).execute(HtmlUtils.concat(baseUrl, "weg.php?gipfelid=" + _rockId));
    }

    @Override
    public void onNetworkTaskResolved(Document document) {
        Elements tableElements = HtmlUtils.getTableElements(document);
        _routeCount = tableElements.size() - 1; // mind one heading element
        _processRoutes(tableElements);
        if (_routeCount <= 0) {
            // No routes here
            listener.onEvent(false);
        }
    }

    @Override
    public void onEvent(boolean state) {
        if (_routeCount == ++_resolvedRouteCount) {
            listener.onEvent(true);
        }
    }

    private void _processRoutes(Elements elements) {
        db.routeDao().deleteAll(_rockId);
        boolean ascended = false;
        for (int i = 1; i < elements.size(); i++) { // index 0 is the table header
            Route r = new Route();
            Elements data = elements.get(i).select("td");
            Element link = data.get(1).select("a").get(0);
            Pattern p = Pattern.compile("(.*)wegid=([0-9]+)(.*)");
            Matcher m = p.matcher(link.attr("href").toString());
            if (m.find()) {
                int routeId = Integer.parseInt(m.group(2));
                r.setId(routeId);
                r.setNr(Float.parseFloat(HtmlUtils.htmlTrim(data.get(0).text())));
                r.setName(link.text());
                r.setGrade(HtmlUtils.htmlTrim(data.get(3).text()));
                int currentAscendCount = db.ascendDao().getAscendsForRoute(routeId).length;
                r.setAscendCount(currentAscendCount); // if we re-add a route that has already been marked as ascended in the past
                ascended |= (currentAscendCount > 0);
                r.setParentId(_rockId);
                db.routeDao().insert(r);
                (new DescriptionParser(db, r.getId(), this)).parse();
            }
        }
        db.rockDao().updateAscended(ascended, _rockId);
    }
}
