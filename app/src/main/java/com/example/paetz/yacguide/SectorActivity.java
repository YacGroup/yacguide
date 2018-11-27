package com.example.paetz.yacguide;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.example.paetz.yacguide.database.Region;
import com.example.paetz.yacguide.database.Sector;
import com.example.paetz.yacguide.network.SectorParser;
import com.example.paetz.yacguide.utils.IntentConstants;
import com.example.paetz.yacguide.utils.WidgetUtils;

public class SectorActivity extends TableActivity {

    private Region _region;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int regionId = getIntent().getIntExtra(IntentConstants.REGION_KEY, 0);
        super.initialize(R.layout.activity_sector);

        htmlParser = new SectorParser(db, regionId, this);
        _region = db.regionDao().getRegion(regionId);

        displayContent();
    }

    @Override
    protected void displayContent() {
        LinearLayout layout = findViewById(R.id.tableLayout);
        layout.removeAllViews();
        this.setTitle(_region.getName());
        for (final Sector sector : db.sectorDao().getAll(_region.getId())) {
            final String sectorName = sector.getName();
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SectorActivity.this, RockActivity.class);
                    intent.putExtra(IntentConstants.SECTOR_KEY, sector.getId());
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
        db.deleteSectors(_region.getId());
    }
}
