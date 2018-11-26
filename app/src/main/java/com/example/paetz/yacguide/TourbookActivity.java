package com.example.paetz.yacguide;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.database.Ascend;
import com.example.paetz.yacguide.database.Region;
import com.example.paetz.yacguide.database.Rock;
import com.example.paetz.yacguide.database.Route;
import com.example.paetz.yacguide.database.Sector;
import com.example.paetz.yacguide.utils.IntentConstants;
import com.example.paetz.yacguide.utils.WidgetUtils;

import java.util.Arrays;


public class TourbookActivity extends AppCompatActivity {

    private AppDatabase _db;
    private int[] _availableYears;
    private int _currentYearIdx;
    private int _maxYearIdx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tourbook);
        this.setTitle("Tourenbuch");

        _db = MainActivity.database;
        _availableYears = _db.ascendDao().getYears();
        Arrays.sort(_availableYears);
        _currentYearIdx = _maxYearIdx = _availableYears.length - 1;
        if (_currentYearIdx >= 0) {
            findViewById(R.id.prevYearButton).setVisibility(_availableYears.length > 1 ? View.VISIBLE : View.INVISIBLE);
            _displayContent(_availableYears[_currentYearIdx]);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            Toast.makeText(this, "Begehung gelöscht", Toast.LENGTH_SHORT).show();
            _displayContent(_availableYears[_currentYearIdx]);
        }
    }

    public void home(View v) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void goToNextYear(View v) {
        if (++_currentYearIdx <= _maxYearIdx) {
            findViewById(R.id.prevYearButton).setVisibility(View.VISIBLE);
            findViewById(R.id.nextYearButton).setVisibility(_currentYearIdx == _maxYearIdx ? View.INVISIBLE : View.VISIBLE);
            _displayContent(_availableYears[_currentYearIdx]);
        }
    }

    public void goToPreviousYear(View v) {
        if (--_currentYearIdx >= 0) {
            findViewById(R.id.nextYearButton).setVisibility(View.VISIBLE);
            findViewById(R.id.prevYearButton).setVisibility(_currentYearIdx == 0 ? View.INVISIBLE : View.VISIBLE);
            _displayContent(_availableYears[_currentYearIdx]);
        }
    }

    private void _displayContent(int year) {
        Ascend[] ascends = _db.ascendDao().getAll(year);
        LinearLayout layout = findViewById(R.id.tableLayout);
        layout.removeAllViews();
        ((TextView) findViewById(R.id.yearTextView)).setText(String.valueOf(year));

        int currentMonth, currentDay, currentRegionId;
        currentMonth = currentDay = currentRegionId = -1;
        for (final Ascend ascend : ascends) {
            int month = ascend.getMonth();
            int day = ascend.getDay();

            Route route = _db.routeDao().getRoute(ascend.getRouteId());
            Rock rock; Sector sector; Region region;
            if (route == null) {
                // The database entry has been deleted
                route = _db.createUnknownRoute();
                rock = _db.createUnknownRock();
                sector = _db.createUnknownSector();
                region = _db.createUnknownRegion();
            } else {
                rock = _db.rockDao().getRock(route.getParentId());
                sector = _db.sectorDao().getSector(rock.getParentId());
                region = _db.regionDao().getRegion(sector.getParentId());
            }

            if (month != currentMonth || day != currentDay || region.getId() != currentRegionId) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        day + "." + month + "." + year,
                        region.getName(),
                        14,
                        null,
                        0xFFBBBBBB,
                        Typeface.BOLD,
                        5, 10, 5, 0));
                layout.addView(WidgetUtils.createHorizontalLine(this, 5));
                currentMonth = month;
                currentDay = day;
                currentRegionId = region.getId();
            }
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TourbookActivity.this, TourbookAscendActivity.class);
                    intent.putExtra(IntentConstants.ASCEND_KEY, ascend.getId());
                    startActivityForResult(intent, 0);
                }
            };
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    rock.getName() + " - " + route.getName(),
                    route.getGrade(),
                    16,
                    onClickListener,
                    Color.WHITE,
                    Typeface.NORMAL));
            layout.addView(WidgetUtils.createHorizontalLine(this, 1));
        }
    }

}
