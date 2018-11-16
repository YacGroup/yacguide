package com.example.paetz.yacguide;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.paetz.yacguide.database.Region;
import com.example.paetz.yacguide.network.RegionParser;
import com.example.paetz.yacguide.utils.IntentConstants;
import com.example.paetz.yacguide.utils.WidgetUtils;

public class RegionActivity extends TableActivity {

    private String _countryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _countryName = getIntent().getStringExtra(IntentConstants.COUNTRY_KEY);
        super.initialize(R.layout.activity_region);
        htmlParser = new RegionParser(db, _countryName, this);
    }

    protected void displayContent() {
        this.setTitle(_countryName);
        LinearLayout layout = findViewById(R.id.tableLayout);
        layout.removeAllViews();
        for (final Region region : db.regionDao().getAll(_countryName)) {
            final String regionName = region.getName();
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RegionActivity.this, SectorActivity.class);
                    intent.putExtra(IntentConstants.REGION_KEY, region.getId());
                    intent.putExtra(IntentConstants.REGION_NAME, regionName);
                    startActivity(intent);
                }
            };
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    regionName,
                    "",
                    18,
                    onClickListener,
                    Color.WHITE,
                    Typeface.BOLD));
            layout.addView(WidgetUtils.createHorizontalLine(this, 1));
        }
    }

    protected void deleteContent() {
        db.deleteRegions(_countryName);
    }
}
