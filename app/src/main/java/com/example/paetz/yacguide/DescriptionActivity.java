package com.example.paetz.yacguide;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paetz.yacguide.database.Ascend;
import com.example.paetz.yacguide.database.Comment;
import com.example.paetz.yacguide.utils.IntentConstants;
import com.example.paetz.yacguide.utils.WidgetUtils;

import java.util.ArrayList;

public class DescriptionActivity extends TableActivity {

    private int _routeId;
    private String _routeName;
    private String _routeGrade;
    private String _routeDescription;
    private int _ascendCount;
    private int _resultUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _routeId = getIntent().getIntExtra(IntentConstants.ROUTE_KEY, -1);
        _routeName = getIntent().getStringExtra(IntentConstants.ROUTE_NAME);
        _routeGrade = getIntent().getStringExtra(IntentConstants.ROUTE_GRADE);
        _routeDescription = getIntent().getStringExtra(IntentConstants.ROUTE_DESCRIPTION);
        _ascendCount = getIntent().getIntExtra(IntentConstants.ASCEND_COUNT, 0);
        _resultUpdated = IntentConstants.RESULT_NO_UPDATE;
        super.initialize(R.layout.activity_description);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Note: Once reset, _resultUpdated may not be set back to RESULT_NO_UPDATE again!
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            _resultUpdated = resultCode;
            _ascendCount = db.ascendDao().getAscendsForRoute(_routeId).length;
            Button ascendsButton = findViewById(R.id.ascendsButton);
            ascendsButton.setText(_ascendCount + " Begehung(en)");
            ascendsButton.setVisibility(_ascendCount > 0 ? View.VISIBLE : View.INVISIBLE);
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
        intent.putExtra(IntentConstants.ROUTE_KEY, _routeId);
        startActivityForResult(intent, 0);
    }

    public void goToAscends(View v) {
        Intent intent = new Intent(DescriptionActivity.this, TourbookAscendActivity.class);
        intent.putExtra(IntentConstants.ROUTE_KEY, _routeId);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void displayContent() {
        Button ascendsButton = findViewById(R.id.ascendsButton);
        ascendsButton.setText(_ascendCount + " Begehung(en)");
        ascendsButton.setVisibility(_ascendCount > 0 ? View.VISIBLE : View.INVISIBLE);

        LinearLayout layout = findViewById(R.id.tableLayout);
        layout.removeAllViews();
        this.setTitle(_routeName + "   " + _routeGrade);
        TextView descView = new TextView(this);
        descView.setText(_routeDescription);
        descView.setTypeface(null, Typeface.BOLD);
        descView.setBackgroundColor(Color.WHITE);
        descView.setPadding(20,20,20,20);
        layout.addView(descView);

        for (final Comment comment : db.commentDao().getAll(_routeId)) {
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
