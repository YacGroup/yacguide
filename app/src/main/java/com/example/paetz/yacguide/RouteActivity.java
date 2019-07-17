package com.example.paetz.yacguide;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.database.Ascend;
import com.example.paetz.yacguide.database.Comment.RockComment;
import com.example.paetz.yacguide.database.Rock;
import com.example.paetz.yacguide.database.Route;
import com.example.paetz.yacguide.utils.AscendStyle;
import com.example.paetz.yacguide.utils.IntentConstants;
import com.example.paetz.yacguide.utils.WidgetUtils;

import java.util.HashSet;
import java.util.Set;

public class RouteActivity extends TableActivity {

    private Rock _rock;
    private int _resultUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int rockId = getIntent().getIntExtra(IntentConstants.ROCK_KEY, AppDatabase.INVALID_ID);
        super.initialize(R.layout.activity_route);

        _rock = db.rockDao().getRock(rockId);
        final char rockStatus = _rock.getStatus();
        if (rockStatus == Rock.statusProhibited) {
            ((TextView) findViewById(R.id.infoTextView)).setText("Achtung: Der Felsen ist komplett gesperrt.");
        } else if (rockStatus == Rock.statusTemporarilyProhibited) {
            ((TextView) findViewById(R.id.infoTextView)).setText("Achtung: Der Felsen ist zeitweise gesperrt.");
        } else if (rockStatus == Rock.statusPartlyProhibited) {
            ((TextView) findViewById(R.id.infoTextView)).setText("Achtung: Der Felsen ist teilweise gesperrt.");
        } else if (_rock.getType() == Rock.typeUnofficial) {
            ((TextView) findViewById(R.id.infoTextView)).setText("Achtung: Der Felsen ist nicht anerkannt.");
        }
        _resultUpdated = IntentConstants.RESULT_NO_UPDATE;

        displayContent();
    }

    @Override
    public void back(View v) {
        Intent resultIntent = new Intent();
        setResult(_resultUpdated, resultIntent);
        finish();
    }

    public void showMap(View v) {
        final Uri gmmIntentUri = Uri.parse("geo:" + _rock.getLatitude() + "," + _rock.getLongitude());
        final Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        if (mapIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, "Keine Karten-App verfügbar", Toast.LENGTH_SHORT).show();
        } else {
            startActivity(mapIntent);
        }
    }


    public void showComments(View v) {
        final Dialog dialog = prepareCommentDialog();

        LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.commentLayout);
        for (final RockComment comment : db.rockCommentDao().getAll(_rock.getId())) {
            final int qualityId = comment.getQualityId();
            final String text = comment.getText();

            layout.addView(WidgetUtils.INSTANCE.createHorizontalLine(this, 5));
            if (RockComment.Companion.getQUALITY_MAP().containsKey(qualityId)) {
                layout.addView(WidgetUtils.INSTANCE.createCommonRowLayout(this,
                        "Charakter:",
                        RockComment.Companion.getQUALITY_MAP().get(qualityId),
                        WidgetUtils.textFontSizeDp,
                        null,
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0));
            }
            layout.addView(WidgetUtils.INSTANCE.createCommonRowLayout(this,
                    text,
                    "",
                    WidgetUtils.textFontSizeDp,
                    null,
                    Color.WHITE,
                    Typeface.NORMAL,
                    10, 10, 10, 10));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            _resultUpdated = resultCode;
            displayContent();
        }
    }

    @Override
    protected void displayContent() {
        LinearLayout layout = findViewById(R.id.tableLayout);
        layout.removeAllViews();
        this.setTitle(_rock.getNr() + " " + _rock.getName());

        for (final Route route : db.routeDao().getAll(_rock.getId())) {
            final int commentCount = db.routeCommentDao().getCommentCount(route.getId());
            String commentCountAddon = "";
            if (commentCount > 0) {
                commentCountAddon = "   [" + commentCount + "]";
            }
            View.OnClickListener onCLickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RouteActivity.this, DescriptionActivity.class);
                    intent.putExtra(IntentConstants.ROUTE_KEY, route.getId());
                    startActivityForResult(intent, 0);
                }
            };
            int statusId = route.getStatusId();
            int typeface = Typeface.BOLD;
            int bgColor = Color.WHITE;
            if (statusId == 3) { // prohibited
                typeface = Typeface.ITALIC;
                bgColor = Color.LTGRAY;
            }

            final Ascend[] ascends = db.ascendDao().getAscendsForRoute(route.getId());
            if (ascends.length > 0) {
                Set<Integer> rowColors = new HashSet<>();
                for (final Ascend ascend : ascends) {
                    rowColors.add(AscendStyle.Companion.fromId(ascend.getStyleId()).getColor());
                }
                bgColor = AscendStyle.Companion.getPreferredColor(rowColors);
            }

            layout.addView(WidgetUtils.INSTANCE.createCommonRowLayout(this,
                    route.getName() + commentCountAddon,
                    route.getGrade(),
                    WidgetUtils.tableFontSizeDp,
                    onCLickListener,
                    bgColor,
                    typeface));
            layout.addView(WidgetUtils.INSTANCE.createHorizontalLine(this, 1));
        }
    }

    @Override
    protected void deleteContent() {}
}
