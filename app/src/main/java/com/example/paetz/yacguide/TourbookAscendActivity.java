package com.example.paetz.yacguide;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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

import java.util.ArrayList;
import java.util.List;


public class TourbookAscendActivity extends AppCompatActivity {

    private AppDatabase _db;
    private Ascend[] _ascends;
    private int _currentAscendIdx;
    private int _maxAscendIdx;
    private int _routeId;
    private int _resultUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tourbook_ascend);
        this.setTitle("Tourenbuch");

        _db = MainActivity.database;
        _resultUpdated = IntentConstants.RESULT_NO_UPDATE;
        int ascendId = getIntent().getIntExtra(IntentConstants.ASCEND_KEY, _db.INVALID_ID);
        _routeId = getIntent().getIntExtra(IntentConstants.ROUTE_KEY, _db.INVALID_ID);
        _currentAscendIdx = 0;
        if (ascendId != _db.INVALID_ID) {
            _ascends = new Ascend[1];
            _ascends[0] = _db.ascendDao().getAscend(ascendId);
            _routeId = _ascends[0].getRouteId();
        } else if (_routeId != _db.INVALID_ID) {
            _ascends = _db.ascendDao().getAscendsForRoute(_routeId);
            findViewById(R.id.nextAscendButton).setVisibility(_ascends.length > 1 ? View.VISIBLE : View.INVISIBLE);
        }
        _maxAscendIdx = _ascends.length - 1;
        if (_ascends.length != 0) {
            _displayContent(_ascends[_currentAscendIdx]);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            _resultUpdated = IntentConstants.RESULT_UPDATED;
        }
        _displayContent(_ascends[_currentAscendIdx] = _db.ascendDao().getAscend(_ascends[_currentAscendIdx].getId()));
    }

    public void home(View v) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void back(View v) {
        Intent resultIntent = new Intent();
        setResult(_resultUpdated, resultIntent);
        finish();
    }

    public void goToNextAscend(View v) {
        if (++_currentAscendIdx <= _maxAscendIdx) {
            findViewById(R.id.prevAscendButton).setVisibility(View.VISIBLE);
            findViewById(R.id.nextAscendButton).setVisibility(_currentAscendIdx == _maxAscendIdx ? View.INVISIBLE : View.VISIBLE);
            _displayContent(_ascends[_currentAscendIdx]);
        }
    }

    public void goToPreviousAscend(View v) {
        if (--_currentAscendIdx >= 0) {
            findViewById(R.id.nextAscendButton).setVisibility(View.VISIBLE);
            findViewById(R.id.prevAscendButton).setVisibility(_currentAscendIdx == 0 ? View.INVISIBLE : View.VISIBLE);
            _displayContent(_ascends[_currentAscendIdx]);
        }
    }

    public void edit(View v) {
        Intent intent = new Intent(TourbookAscendActivity.this, AscendActivity.class);
        Ascend ascend = _ascends[_currentAscendIdx];
        intent.putExtra(IntentConstants.ASCEND_KEY, ascend.getId());
        intent.putExtra(IntentConstants.ROUTE_KEY, ascend.getRouteId());
        intent.putExtra(IntentConstants.ASCEND_STYLE_KEY, ascend.getStyleId());
        intent.putExtra(IntentConstants.ASCEND_YEAR, ascend.getYear());
        intent.putExtra(IntentConstants.ASCEND_MONTH, ascend.getMonth());
        intent.putExtra(IntentConstants.ASCEND_DAY, ascend.getDay());
        intent.putIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS, ascend.getPartnerIds());
        intent.putExtra(IntentConstants.ASCEND_NOTES, ascend.getNotes());
        startActivityForResult(intent, 0);
    }

    public void delete(View v) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog);
        ((TextView) dialog.findViewById(R.id.dialogText)).setText("Diese Begehung löschen?");
        dialog.findViewById(R.id.yesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _db.ascendDao().delete(_ascends[_currentAscendIdx]);
                int ascendedCount = _db.routeDao().getAscendCount(_routeId) - 1;
                _db.routeDao().updateAscendCount(ascendedCount, _routeId);
                if (ascendedCount == 0) {
                    _db.rockDao().updateAscended(false, _db.routeDao().getRoute(_routeId).getParentId());
                }
                dialog.dismiss();
                Intent resultIntent = new Intent();
                setResult(IntentConstants.RESULT_UPDATED, resultIntent);
                finish();
            }
        });
        dialog.findViewById(R.id.noButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void _displayContent(Ascend ascend) {
        LinearLayout layout = findViewById(R.id.tableLayout);
        layout.removeAllViews();

        Route route = _db.routeDao().getRoute(_routeId);
        Rock rock;
        Sector sector;
        Region region;
        if (route != null) {
            rock = _db.rockDao().getRock(route.getParentId());
            sector = _db.sectorDao().getSector(rock.getParentId());
            region = _db.regionDao().getRegion(sector.getParentId());
        } else {
            route = _db.createUnknownRoute();
            rock = _db.createUnknownRock();
            sector = _db.createUnknownSector();
            region = _db.createUnknownRegion();
            Toast.makeText(this, "Zugehöriger Weg nicht gefunden.\n" +
                    "Datenbankeintrag wurde scheinbar gelöscht.", Toast.LENGTH_LONG).show();
        }

        final List<Integer> partnerIds = ascend.getPartnerIds();
        ArrayList<String> partners = new ArrayList<String>();
        for (Integer id : partnerIds) {
            String name = _db.partnerDao().getName(id);
            partners.add(name == null ? "???" : name);
        }
        final String partnersString = TextUtils.join(", ",  partners);

        layout.addView(WidgetUtils.createCommonRowLayout(this,
                ascend.getDay() + "." + ascend.getMonth() + "." + ascend.getYear(),
                region.getName(),
                14,
                null,
                0xFFBBBBBB,
                Typeface.BOLD,
                10,10,10,10));
        layout.addView(WidgetUtils.createHorizontalLine(this, 1));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Teilgebiet",
                "",
                12,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                sector.getName(),
                "",
                16,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10,10,10,10));
        layout.addView(WidgetUtils.createHorizontalLine(this, 1));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Felsen",
                "",
                12,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                rock.getName(),
                "",
                16,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10,10,10,10));
        layout.addView(WidgetUtils.createHorizontalLine(this, 1));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Weg",
                "",
                12,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                route.getName() + "   " + route.getGrade(),
                "",
                16,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10,10,10,10));
        layout.addView(WidgetUtils.createHorizontalLine(this, 1));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Stil",
                "",
                12,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                _db.CLIMBING_STYLES.get(ascend.getStyleId()),
                "",
                16,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10,10,10,10));
        layout.addView(WidgetUtils.createHorizontalLine(this, 1));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Seilpartner",
                "",
                12,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                partnersString.isEmpty() ? " - " : partnersString,
                "",
                16,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10,10,10,10));
        layout.addView(WidgetUtils.createHorizontalLine(this, 1));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Bemerkungen",
                "",
                12,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                ascend.getNotes().isEmpty() ? " - " : ascend.getNotes(),
                "",
                16,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10,10,10,10));
        layout.addView(WidgetUtils.createHorizontalLine(this, 1));
    }

}
