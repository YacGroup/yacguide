package com.example.paetz.yacguide.network;

import com.example.paetz.yacguide.ProgressListener;
import com.example.paetz.yacguide.UpdateListener;
import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.database.Rock;
import com.example.paetz.yacguide.utils.HtmlUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RockParser extends HTMLParser implements NetworkListener, UpdateListener {

    private int _sectorId;
    private int _rockCount;
    private int _resolvedRockCount;

    private ProgressListener _progressListener;

    public RockParser(AppDatabase db, int sectorId, UpdateListener listener, ProgressListener progressListener) {
        super(db, listener);
        _sectorId = sectorId;
        _progressListener = progressListener;
    }

    @Override
    public void parse() {
        _resolvedRockCount = 0;
        new NetworkTask(this).execute(HtmlUtils.concat(baseUrl, "gipfel.php?sektorid=" + _sectorId + "&typen=alle"));
    }

    // NetworkListener
    @Override
    public void onNetworkTaskResolved(Document document) {
        Elements tableElements = HtmlUtils.getTableElements(document);
        _rockCount = tableElements.size() - 1; // mind one heading element
        _processRocks(tableElements);
        if (_rockCount <= 0) {
            // No rocks here
            _progressListener.onProgress(1, 1);
            listener.onEvent(false);
        } else {
            _progressListener.onProgress(_rockCount, _resolvedRockCount);
            // If there are some rocks, we don't tell the listener yet since we need to wait for the sub tasks
        }
    }

    // UpdateListener
    @Override
    public void onEvent(boolean state) {
        _progressListener.onProgress(_rockCount, ++_resolvedRockCount);
        if (_resolvedRockCount == _rockCount) {
            listener.onEvent(true);
        }
    }

    private void _processRocks(Elements elements) {
        db.rockDao().deleteAll(_sectorId);
        for (int i = 1; i < elements.size(); i++) { // index 0 is the table header
            Rock r = new Rock();
            Elements data = elements.get(i).select("td");
            Element link = data.get(1).select("a").get(0);
            Pattern p = Pattern.compile("(.*)gipfelid=([0-9]+)(.*)");
            Matcher m = p.matcher(link.attr("href").toString());
            if (m.find()) {
                r.setId(Integer.parseInt(m.group(2)));
                Pattern pNr = Pattern.compile("([0-9]+\\.?[0-9]*)([GMBH])([ZXNT])?");
                Matcher mNr = pNr.matcher(data.get(0).select("font").get(0).text());
                if (mNr.find()) {
                    r.setNr(Float.parseFloat(mNr.group(1)));
                    r.setType(mNr.group(2));
                    r.setStatus(mNr.group(3) != null ? mNr.group(3).charAt(0) : Character.MIN_VALUE);
                    r.setName(link.text().split("/")[0].trim());
                    r.setAscended(false); // We may update this after adding the routes; see RouteParser
                    r.setParentId(_sectorId);
                    db.rockDao().insert(r);
                    (new RouteParser(db, r.getId(), this)).parse();
                } else {
                    // This is something else like a pub or parking area
                    onEvent(true);
                }
            }

        }
    }
}
