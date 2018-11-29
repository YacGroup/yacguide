package com.example.paetz.yacguide.network;

import com.example.paetz.yacguide.UpdateListener;
import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.database.Comment;
import com.example.paetz.yacguide.utils.HtmlUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DescriptionParser extends HTMLParser implements NetworkListener {

    private int _routeId;

    public DescriptionParser(AppDatabase db, int routeId, UpdateListener listener) {
        super(db, listener);
        _routeId = routeId;
    }

    @Override
    public void parse() {
        new NetworkTask(this).execute(HtmlUtils.concat(baseUrl, "komment.php?wegid=" + _routeId));
    }

    @Override
    public void onNetworkTaskResolved(Document document) {
        _processDescription(HtmlUtils.getTableElements(document), document);
        listener.onEvent(true);
    }

    private void _processDescription(Elements elements, Document document) {
        db.commentDao().deleteAll(_routeId);
        final Elements accElements = document.body().getElementsContainingOwnText("Unfälle am Weg");
        final String marker = "###MARK###"; // We need this to remove accidents
        String desc = document.body().ownText();
        if (!accElements.isEmpty()) {
            final Element accidentsHeaderElement = accElements.get(0);
            accidentsHeaderElement.before(marker); // This changes the doc variable!
            desc = document.body().ownText().replaceAll(marker + ".*", "");
        }
        db.routeDao().updateDescription(desc, _routeId);
        for (final Element element : elements) {
            Comment c = new Comment();
            final Elements data = element.select("td");
            final Element link = data.get(2).select("a").get(0);
            final Pattern p = Pattern.compile("(.*)kommentid=([0-9]+)(.*)");
            final Matcher m = p.matcher(link.attr("href").toString());
            if (m.find()) {
                c.setId(Integer.parseInt(m.group(2)));
                c.setAssessment(data.get(1).select("font").get(0).text());
                c.setText(data.get(2).ownText());
                c.setParentId(_routeId);
                db.commentDao().insert(c);
            }
        }
    }
}
