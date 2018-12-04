package com.example.paetz.yacguide;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.example.paetz.yacguide.database.Country;
import com.example.paetz.yacguide.network.CountryParser;
import com.example.paetz.yacguide.utils.IntentConstants;
import com.example.paetz.yacguide.utils.WidgetUtils;


public class CountryActivity extends TableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        super.initialize(R.layout.activity_country);
        jsonParser = new CountryParser(db, this);

        displayContent();
    }

    @Override
    protected void displayContent() {
        this.setTitle("YACguide");
        LinearLayout layout = findViewById(R.id.tableLayout);
        layout.removeAllViews();
        for (final Country country : db.countryDao().getAll()) {
            final String countryName = country.getName();
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CountryActivity.this, RegionActivity.class);
                    intent.putExtra(IntentConstants.COUNTRY_KEY, countryName);
                    startActivity(intent);
                }
            };
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    countryName,
                    "",
                    16,
                    onClickListener,
                    Color.WHITE,
                    Typeface.BOLD));
            layout.addView(WidgetUtils.createHorizontalLine(this, 1));
        }
    }

    @Override
    protected void deleteContent() {
        db.deleteCountries();
    }
}
