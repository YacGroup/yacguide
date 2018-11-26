package com.example.paetz.yacguide;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paetz.yacguide.database.Rock;
import com.example.paetz.yacguide.network.RockParser;
import com.example.paetz.yacguide.utils.IntentConstants;
import com.example.paetz.yacguide.utils.WidgetUtils;

public class RockActivity extends TableActivity implements ProgressListener {

    private int _sectorId;
    private String _sectorName;
    private boolean _onlySummits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _sectorId = getIntent().getIntExtra(IntentConstants.SECTOR_KEY, -1);
        _sectorName = getIntent().getStringExtra(IntentConstants.SECTOR_NAME);
        _onlySummits = false;
        super.initialize(R.layout.activity_rock);
        htmlParser = new RockParser(db, _sectorId, this, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            displayContent();
        }
    }

    // ProgressListener
    @Override
    public void onProgress(int sent, int received) {
        if (updateDialog != null && sent != 0) {
            int percent = 100 * received / sent;
            ((TextView) updateDialog.findViewById(R.id.dialogText)).setText(percent + "%");
        }
    }

    public void map(View v) {
        Toast.makeText(this, "Noch nicht implementiert", Toast.LENGTH_SHORT).show();
    }

    public void onlySummitsCheck(View v) {
        _onlySummits = ((CheckBox) findViewById(R.id.onlySummitsCheckBox)).isChecked();
        _displayInner(_onlySummits);
    }

    @Override
    protected void displayContent() {
        _displayInner(_onlySummits);
    }

    private void _displayInner(boolean onlySummits) {
        LinearLayout layout = findViewById(R.id.tableLayout);
        layout.removeAllViews();
        this.setTitle(_sectorName);
        for (final Rock rock : db.rockDao().getAll(_sectorId)) {
            int bgColor = rock.getAscended() ? Color.GREEN : Color.WHITE;
            final String rockName = rock.getName();
            final String type = rock.getType();
            String typeAdd = "";
            int typeFace = Typeface.NORMAL;
            if (!type.equals(Rock.typeSummit)) {
                if (onlySummits) {
                    continue;
                }
                typeAdd = "  (" + type + ")";
            } else {
                typeFace = Typeface.BOLD;
            }
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RockActivity.this, RouteActivity.class);
                    intent.putExtra(IntentConstants.ROCK_KEY, rock.getId());
                    intent.putExtra(IntentConstants.ROCK_NR, rock.getNr());
                    intent.putExtra(IntentConstants.ROCK_NAME, rockName);
                    intent.putExtra(IntentConstants.ROCK_STATUS, rock.getStatus());
                    startActivityForResult(intent, 0);
                }
            };
            char status = rock.getStatus();
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    rock.getNr() + "  " + rockName + typeAdd,
                    String.valueOf(status),
                    16,
                    onClickListener,
                    bgColor,
                    typeFace));
            layout.addView(WidgetUtils.createHorizontalLine(this, 1));
        }
    }

    @Override
    protected void deleteContent() {
        db.deleteRocks(_sectorId);
    }
}
