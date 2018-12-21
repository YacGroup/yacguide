package com.example.paetz.yacguide;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.paetz.yacguide.database.Comment.SectorComment;
import com.example.paetz.yacguide.database.Rock;
import com.example.paetz.yacguide.database.Sector;
import com.example.paetz.yacguide.network.RockParser;
import com.example.paetz.yacguide.utils.IntentConstants;
import com.example.paetz.yacguide.utils.WidgetUtils;

public class RockActivity extends TableActivity {

    private Sector _sector;
    private boolean _onlySummits;
    private String _rockNamePrefix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int sectorId = getIntent().getIntExtra(IntentConstants.SECTOR_KEY, db.INVALID_ID);
        super.initialize(R.layout.activity_rock);

        jsonParser = new RockParser(db, this, sectorId);
        _sector = db.sectorDao().getSector(sectorId);
        _onlySummits = false;
        _rockNamePrefix = "";

        final EditText searchEditText = findViewById(R.id.searchEditText);
        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                final InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                _rockNamePrefix = searchEditText.getText().toString();
                displayContent();
            }
        });

        displayContent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            displayContent();
        }
    }

    public void showComments(View v) {
        final Dialog dialog = prepareCommentDialog();

        LinearLayout layout = dialog.findViewById(R.id.commentLayout);
        for (final SectorComment comment : db.sectorCommentDao().getAll(_sector.getId())) {
            final int qualityId = comment.getQualityId();
            final String text = comment.getText();

            layout.addView(WidgetUtils.createHorizontalLine(this, 5));
            if (SectorComment.QUALITY_MAP.containsKey(qualityId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        "Bedeutung:",
                        SectorComment.QUALITY_MAP.get(qualityId),
                        WidgetUtils.textFontSizeDp,
                        null,
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0));
            }
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    text,
                    "",
                    WidgetUtils.textFontSizeDp,
                    null,
                    Color.WHITE,
                    Typeface.NORMAL,
                    10, 10, 10, 10));
        }
    }

    public void onlySummitsCheck(View v) {
        _onlySummits = ((CheckBox) findViewById(R.id.onlySummitsCheckBox)).isChecked();
        displayContent();
    }

    @Override
    protected void displayContent() {
        LinearLayout layout = findViewById(R.id.tableLayout);
        layout.removeAllViews();
        this.setTitle(_sector.getName());
        for (final Rock rock : db.rockDao().getAll(_sector.getId())) {
            final String rockName = rock.getName();
            if (!rockName.toLowerCase().contains(_rockNamePrefix.toLowerCase())) {
                continue;
            }
            final char type = rock.getType();
            final char status = rock.getStatus();
            if (_onlySummits && !rockIsAnOfficialSummit(rock)) {
                continue;
            }
            final int bgColor = rock.getAscended() ? Color.GREEN : Color.WHITE;
            String typeAdd = "";
            int typeFace = Typeface.NORMAL;
            if (type != Rock.typeSummit) {
                typeAdd = "  (" + type + ")";
            } else {
                typeFace = Typeface.BOLD;
            }
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RockActivity.this, RouteActivity.class);
                    intent.putExtra(IntentConstants.ROCK_KEY, rock.getId());
                    startActivityForResult(intent, 0);
                }
            };
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    rock.getNr() + "  " + rockName + typeAdd,
                    String.valueOf(rock.getStatus()),
                    WidgetUtils.tableFontSizeDp,
                    onClickListener,
                    bgColor,
                    typeFace));
            layout.addView(WidgetUtils.createHorizontalLine(this, 1));
        }
    }

    @Override
    protected void deleteContent() {
        db.deleteRocks(_sector.getId());
    }

    private boolean rockIsAnOfficialSummit(Rock rock) {
        return (rock.getType() == Rock.typeSummit || rock.getType() == Rock.typeAlpine)
                && rock.getStatus() != Rock.statusProhibited;
    }
}
