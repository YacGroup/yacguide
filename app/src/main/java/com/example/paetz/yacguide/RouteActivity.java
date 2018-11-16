package com.example.paetz.yacguide;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paetz.yacguide.database.Rock;
import com.example.paetz.yacguide.database.Route;
import com.example.paetz.yacguide.utils.IntentConstants;
import com.example.paetz.yacguide.utils.WidgetUtils;

public class RouteActivity extends TableActivity {

    private int _rockId;
    private float _rockNr;
    private String _rockName;
    private int _resultUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _rockId = getIntent().getIntExtra(IntentConstants.ROCK_KEY, -1);
        _rockNr = getIntent().getFloatExtra(IntentConstants.ROCK_NR, 0);
        _rockName = getIntent().getStringExtra(IntentConstants.ROCK_NAME);
        char rockStatus = getIntent().getCharExtra(IntentConstants.ROCK_STATUS, Character.MIN_VALUE);
        _resultUpdated = IntentConstants.RESULT_NO_UPDATE;
        super.initialize(R.layout.activity_route);
        if (rockStatus == Rock.statusProhibited) {
            ((TextView) findViewById(R.id.infoTextView)).setText("Achtung: Der Felsen ist komplett gesperrt.");
        } else if (rockStatus == Rock.statusTemporarilyProhibited) {
            ((TextView) findViewById(R.id.infoTextView)).setText("Achtung: Der Felsen ist zeitweise gesperrt.");
        } else if (rockStatus == Rock.statusUnofficial) {
            ((TextView) findViewById(R.id.infoTextView)).setText("Achtung: Der Felsen ist nicht anerkannt.");
        }
    }

    @Override
    public void back(View v) {
        if (updateInProgress) {
            Toast.makeText(getApplicationContext(), "Aktualisierung in den Hintergrund verschoben", Toast.LENGTH_SHORT).show();
        }
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
        this.setTitle(_rockNr + " " + _rockName);

        for (final Route route : db.routeDao().getAll(_rockId)) {
            int commentCount = db.commentDao().getCommentCount(route.getId());
            String commentCountAddon = "";
            if (commentCount > 0) {
                commentCountAddon = "   [" + commentCount + "]";
            }
            final int ascendCount = route.getAscendCount();
            View.OnClickListener onCLickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RouteActivity.this, DescriptionActivity.class);
                    intent.putExtra(IntentConstants.ROUTE_KEY, route.getId());
                    intent.putExtra(IntentConstants.ROUTE_NAME, route.getName());
                    intent.putExtra(IntentConstants.ROUTE_GRADE, route.getGrade());
                    intent.putExtra(IntentConstants.ROUTE_DESCRIPTION, route.getDescription());
                    intent.putExtra(IntentConstants.ASCEND_COUNT, ascendCount);
                    startActivityForResult(intent, 0);
                }
            };
            ll.addView(WidgetUtils.createCommonRowLayout(this,
                    route.getName() + commentCountAddon,
                    route.getGrade(),
                    18,
                    onCLickListener,
                    ascendCount > 0 ? Color.GREEN : Color.WHITE,
                    Typeface.BOLD));
            ll.addView(WidgetUtils.createHorizontalLine(this, 1));
        }
    }

    @Override
    protected void deleteContent() {}
}
