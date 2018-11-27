package com.example.paetz.yacguide;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.paetz.yacguide.database.Rock;
import com.example.paetz.yacguide.database.Route;
import com.example.paetz.yacguide.utils.IntentConstants;
import com.example.paetz.yacguide.utils.WidgetUtils;

public class RouteActivity extends TableActivity {

    private Rock _rock;
    private int _resultUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int rockId = getIntent().getIntExtra(IntentConstants.ROCK_KEY, db.INVALID_ID);
        super.initialize(R.layout.activity_route);

        _rock = db.rockDao().getRock(rockId);
        char rockStatus = _rock.getStatus();
        if (rockStatus == Rock.statusProhibited) {
            ((TextView) findViewById(R.id.infoTextView)).setText("Achtung: Der Felsen ist komplett gesperrt.");
        } else if (rockStatus == Rock.statusTemporarilyProhibited) {
            ((TextView) findViewById(R.id.infoTextView)).setText("Achtung: Der Felsen ist zeitweise gesperrt.");
        } else if (rockStatus == Rock.statusUnofficial) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            _resultUpdated = resultCode;
            displayContent();
        }
    }

    @Override
    protected void displayContent() {
        LinearLayout ll = findViewById(R.id.tableLayout);
        ll.removeAllViews();
        this.setTitle(_rock.getNr() + " " + _rock.getName());

        for (final Route route : db.routeDao().getAll(_rock.getId())) {
            int commentCount = db.commentDao().getCommentCount(route.getId());
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
            ll.addView(WidgetUtils.createCommonRowLayout(this,
                    route.getName() + commentCountAddon,
                    route.getGrade(),
                    16,
                    onCLickListener,
                    route.getAscendCount() > 0 ? Color.GREEN : Color.WHITE,
                    Typeface.BOLD));
            ll.addView(WidgetUtils.createHorizontalLine(this, 1));
        }
    }

    @Override
    protected void deleteContent() {}
}
