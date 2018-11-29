package com.example.paetz.yacguide;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paetz.yacguide.database.Comment;
import com.example.paetz.yacguide.database.Route;
import com.example.paetz.yacguide.utils.IntentConstants;
import com.example.paetz.yacguide.utils.WidgetUtils;

public class DescriptionActivity extends TableActivity {

    private Route _route;
    private int _resultUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int routeId = getIntent().getIntExtra(IntentConstants.ROUTE_KEY, db.INVALID_ID);
        super.initialize(R.layout.activity_description);

        _route = db.routeDao().getRoute(routeId);
        _resultUpdated = IntentConstants.RESULT_NO_UPDATE;

        displayContent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Note: Once reset, _resultUpdated may not be set back to RESULT_NO_UPDATE again!
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            _resultUpdated = resultCode;
            _route = db.routeDao().getRoute(_route.getId());
            Button ascendsButton = findViewById(R.id.ascendsButton);
            ascendsButton.setText(_route.getAscendCount() + " Begehung(en)");
            ascendsButton.setVisibility(_route.getAscendCount() > 0 ? View.VISIBLE : View.INVISIBLE);
            Toast.makeText(this, "Begehungen aktualisiert", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void back(View v) {
        Intent resultIntent = new Intent();
        setResult(_resultUpdated, resultIntent);
        finish();
    }

    public void enterAscend(View v) {
        Intent intent = new Intent(DescriptionActivity.this, AscendActivity.class);
        intent.putExtra(IntentConstants.ROUTE_KEY, _route.getId());
        startActivityForResult(intent, 0);
    }

    public void goToAscends(View v) {
        Intent intent = new Intent(DescriptionActivity.this, TourbookAscendActivity.class);
        intent.putExtra(IntentConstants.ROUTE_KEY, _route.getId());
        startActivityForResult(intent, 0);
    }

    @Override
    protected void displayContent() {
        Button ascendsButton = findViewById(R.id.ascendsButton);
        ascendsButton.setText(_route.getAscendCount() + " Begehung(en)");
        ascendsButton.setVisibility(_route.getAscendCount() > 0 ? View.VISIBLE : View.INVISIBLE);

        LinearLayout layout = findViewById(R.id.tableLayout);
        layout.removeAllViews();
        this.setTitle(_route.getName() + "   " + _route.getGrade());
        TextView descView = new TextView(this);
        descView.setText(_route.getDescription());
        descView.setTypeface(null, Typeface.BOLD);
        descView.setBackgroundColor(Color.WHITE);
        descView.setPadding(20,20,20,20);
        layout.addView(descView);

        for (final Comment comment : db.commentDao().getAll(_route.getId())) {
            layout.addView(WidgetUtils.createHorizontalLine(this, 5));

            TextView assessView = new TextView(this);
            assessView.setText(comment.getAssessment());
            assessView.setTypeface(null, Typeface.ITALIC);
            assessView.setBackgroundColor(Color.WHITE);
            assessView.setPadding(10, 10, 10, 10);
            layout.addView(assessView);
            TextView commentView = new TextView(this);
            commentView.setText(comment.getText());
            commentView.setBackgroundColor(Color.WHITE);
            commentView.setPadding(10, 10, 10, 10);
            layout.addView(commentView);
        }
    }

    @Override
    protected void deleteContent() {}
}
