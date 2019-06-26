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
import com.example.paetz.yacguide.database.Partner;
import com.example.paetz.yacguide.database.Region;
import com.example.paetz.yacguide.database.Rock;
import com.example.paetz.yacguide.database.Route;
import com.example.paetz.yacguide.database.Sector;
import com.example.paetz.yacguide.utils.AscendStyle;
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
        final int ascendId = getIntent().getIntExtra(IntentConstants.ASCEND_KEY, _db.INVALID_ID);
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
        final Ascend ascend = _ascends[_currentAscendIdx];
        intent.putExtra(IntentConstants.ASCEND_KEY, ascend.getId());
        startActivityForResult(intent, 0);
    }

    public void delete(View v) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog);
        ((TextView) dialog.findViewById(R.id.dialogText)).setText("Diese Begehung löschen?");
        dialog.findViewById(R.id.yesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _db.deleteAscend(_ascends[_currentAscendIdx]);
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
        for (final Integer id : partnerIds) {
            final Partner partner = _db.partnerDao().getPartner(id);
            partners.add(partner == null ? _db.UNKNOWN_NAME : partner.getName());
        }
        final String partnersString = TextUtils.join(", ",  partners);

        layout.addView(WidgetUtils.createCommonRowLayout(this,
                ascend.getDay() + "." + ascend.getMonth() + "." + ascend.getYear(),
                region.getName(),
                WidgetUtils.infoFontSizeDp,
                null,
                0xFFBBBBBB,
                Typeface.BOLD,
                10,10,10,10));
        layout.addView(WidgetUtils.createHorizontalLine(this, 1));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Teilgebiet",
                "",
                WidgetUtils.textFontSizeDp,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                sector.getName(),
                "",
                WidgetUtils.tableFontSizeDp,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10,10,10,10));
        layout.addView(WidgetUtils.createHorizontalLine(this, 1));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Felsen",
                "",
                WidgetUtils.textFontSizeDp,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                rock.getName(),
                "",
                WidgetUtils.tableFontSizeDp,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10,10,10,10));
        layout.addView(WidgetUtils.createHorizontalLine(this, 1));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Weg",
                "",
                WidgetUtils.textFontSizeDp,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                route.getName() + "   " + route.getGrade(),
                "",
                WidgetUtils.tableFontSizeDp,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10,10,10,10));
        layout.addView(WidgetUtils.createHorizontalLine(this, 1));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Stil",
                "",
                WidgetUtils.textFontSizeDp,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                AscendStyle.fromId(ascend.getStyleId()).name,
                "",
                WidgetUtils.tableFontSizeDp,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10,10,10,10));
        layout.addView(WidgetUtils.createHorizontalLine(this, 1));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Seilpartner",
                "",
                WidgetUtils.textFontSizeDp,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                partnersString.isEmpty() ? " - " : partnersString,
                "",
                WidgetUtils.tableFontSizeDp,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10,10,10,10));
        layout.addView(WidgetUtils.createHorizontalLine(this, 1));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Bemerkungen",
                "",
                WidgetUtils.textFontSizeDp,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0));
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                ascend.getNotes().isEmpty() ? " - " : ascend.getNotes(),
                "",
                WidgetUtils.tableFontSizeDp,
                null,
                Color.WHITE,
                Typeface.NORMAL,
                10,10,10,10));
        layout.addView(WidgetUtils.createHorizontalLine(this, 1));
    }

}
