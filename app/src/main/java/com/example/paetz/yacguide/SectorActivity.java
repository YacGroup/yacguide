package com.example.paetz.yacguide;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.database.Comment.RegionComment;
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

        final int regionId = getIntent().getIntExtra(IntentConstants.REGION_KEY, AppDatabase.INVALID_ID);
        super.initialize(R.layout.activity_sector);

        jsonParser = new SectorParser(db, this, regionId);
        _region = db.regionDao().getRegion(regionId);

        displayContent();
    }

    public void showComments(View v) {
        final Dialog dialog = prepareCommentDialog();

        LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.commentLayout);
        for (final RegionComment comment : db.regionCommentDao().getAll(_region.getId())) {
            final int qualityId = comment.getQualityId();
            final String text = comment.getText();

            layout.addView(WidgetUtils.INSTANCE.createHorizontalLine(this, 5));
            if (RegionComment.Companion.getQUALITY_MAP().containsKey(qualityId)) {
                layout.addView(WidgetUtils.INSTANCE.createCommonRowLayout(this,
                        "Bedeutung:",
                        RegionComment.Companion.getQUALITY_MAP().get(qualityId),
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
            layout.addView(WidgetUtils.INSTANCE.createCommonRowLayout(this,
                    sectorName,
                    "",
                    WidgetUtils.tableFontSizeDp,
                    onClickListener,
                    Color.WHITE,
                    Typeface.BOLD));
            layout.addView(WidgetUtils.INSTANCE.createHorizontalLine(this, 1));
        }
    }

    @Override
    protected void deleteContent() {
        db.deleteSectors(_region.getId());
    }
}
