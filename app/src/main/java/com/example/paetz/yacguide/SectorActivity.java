package com.example.paetz.yacguide;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.example.paetz.yacguide.database.Sector;
import com.example.paetz.yacguide.network.SectorParser;
import com.example.paetz.yacguide.utils.IntentConstants;
import com.example.paetz.yacguide.utils.WidgetUtils;

public class SectorActivity extends TableActivity {

    private int _regionId;
    private String _regionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _regionId = getIntent().getIntExtra(IntentConstants.REGION_KEY, -1);
        _regionName = getIntent().getStringExtra(IntentConstants.REGION_NAME);
        super.initialize(R.layout.activity_sector);
        htmlParser = new SectorParser(db, _regionId, this);
    }

    @Override
    protected void displayContent() {
        LinearLayout layout = findViewById(R.id.tableLayout);
        layout.removeAllViews();
        this.setTitle(_regionName);
        for (final Sector sector : db.sectorDao().getAll(_regionId)) {
            final String sectorName = sector.getName();
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SectorActivity.this, RockActivity.class);
                    intent.putExtra(IntentConstants.SECTOR_KEY, sector.getId());
                    intent.putExtra(IntentConstants.SECTOR_NAME, sectorName);
                    startActivity(intent);
                }
            };
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    sectorName,
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
        db.deleteSectors(_regionId);
    }
}
