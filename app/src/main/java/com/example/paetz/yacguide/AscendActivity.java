package com.example.paetz.yacguide;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.database.Ascend;
import com.example.paetz.yacguide.database.Partner;
import com.example.paetz.yacguide.database.Route;
import com.example.paetz.yacguide.utils.AscendStyle;
import com.example.paetz.yacguide.utils.IntentConstants;

import java.util.ArrayList;
import java.util.Calendar;

public class AscendActivity extends AppCompatActivity {

    private AppDatabase _db;
    private Ascend _ascend;
    private Route _route;
    private ArrayList<Integer> _partnerIds;
    private int _resultUpdated;
    private int _styleId;
    private int _year;
    private int _month;
    private int _day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ascend);

        _db = MainActivity.database;
        _resultUpdated = IntentConstants.RESULT_NO_UPDATE;
        _ascend = _db.ascendDao().getAscend(getIntent().getIntExtra(IntentConstants.ASCEND_KEY, AppDatabase.INVALID_ID));
        final int routeId = getIntent().getIntExtra(IntentConstants.ROUTE_KEY, AppDatabase.INVALID_ID);
        _route = _db.routeDao().getRoute(routeId == AppDatabase.INVALID_ID ? _ascend.getRouteId() : routeId);
        // Beware: _route may still be null (if the route of this ascend has been deleted meanwhile)
        _partnerIds = getIntent().getIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS);
        if (_partnerIds == null) {
            _partnerIds = (_ascend == null) ? new ArrayList<Integer>() : _ascend.getPartnerIds();
        }
        _styleId = _year = _month = _day = 0;

        findViewById(R.id.notesEditText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                final InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        _displayContent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            _partnerIds = data.getIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS);
            _displayContent();
        }
    }

    public void enter(View v) {
        Ascend ascend = new Ascend();
        if (_ascend != null) {
            ascend.setId(_ascend.getId());
        } else {
            _db.routeDao().updateAscendCount(_db.routeDao().getAscendCount(_route.getId()) + 1, _route.getId());
            _db.rockDao().updateAscended(true, _db.routeDao().getRoute(_route.getId()).getParentId());
            _resultUpdated = IntentConstants.RESULT_UPDATED;
        }
        ascend.setRouteId(_route == null ? _ascend.getRouteId() : _route.getId());
        ascend.setStyleId(_styleId);
        ascend.setYear(_year);
        ascend.setMonth(_month);
        ascend.setDay(_day);
        ascend.setPartnerIds(_partnerIds);
        ascend.setNotes(((EditText) findViewById(R.id.notesEditText)).getText().toString());
        _db.ascendDao().insert(ascend);
        Intent resultIntent = new Intent();
        setResult(_resultUpdated, resultIntent);
        finish();
    }

    public void cancel(View v) {
        Intent resultIntent = new Intent();
        setResult(IntentConstants.RESULT_NO_UPDATE, resultIntent);
        finish();
    }

    public void enterDate(View v) {
        final Calendar calendar = Calendar.getInstance();
        final int yy = calendar.get(Calendar.YEAR);
        final int mm = calendar.get(Calendar.MONTH);
        final int dd = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePicker = new DatePickerDialog(AscendActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                _year = year;
                _month = monthOfYear + 1;
                _day = dayOfMonth;
                ((EditText) findViewById(R.id.dateEditText)).setText(_day + "." + _month + "." + _year);
            }
        }, yy, mm, dd);
        datePicker.show();
    }

    public void selectPartners(View v) {
        final String[] partners = ((EditText) findViewById(R.id.partnersEditText)).getText().toString().split(", ");
        ArrayList<Integer> partnerIds = new ArrayList<Integer>();
        for (int i = 0; i < partners.length; i++) {
            partnerIds.add(_db.partnerDao().getId(partners[i]));
        }
        Intent intent = new Intent(AscendActivity.this, PartnersActivity.class);
        intent.putIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS, partnerIds);
        startActivityForResult(intent, 0);
    }

    private void _displayContent() {
        setTitle(_route != null ? _route.getName() + "   " + _route.getGrade() : AppDatabase.UNKNOWN_NAME);


        Spinner spinner = findViewById(R.id.styleSpinner);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_list_item_1, AscendStyle.Companion.getNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                _styleId = AscendStyle.Companion.fromName(parent.getItemAtPosition(position).toString()).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                _styleId = 0;
            }
        });

        if (_ascend != null) {
            ((EditText) findViewById(R.id.dateEditText)).setText((_day =_ascend.getDay()) + "." + (_month = _ascend.getMonth()) + "." + (_year = _ascend.getYear()));
            ((EditText) findViewById(R.id.notesEditText)).setText(_ascend.getNotes());
            spinner.setSelection(adapter.getPosition(AscendStyle.Companion.fromId(_styleId == 0 ? _ascend.getStyleId() : _styleId).getStyleName()));
        } else {
            spinner.setSelection(_styleId != 0 ? adapter.getPosition(AscendStyle.Companion.fromId(_styleId).getStyleName()) : 0);
        }

        ArrayList<String> partners = new ArrayList<String>();
        for (final Integer id : _partnerIds) {
            final Partner partner = _db.partnerDao().getPartner(id);
            partners.add(partner == null ? AppDatabase.UNKNOWN_NAME : partner.getName());
        }
        ((EditText) findViewById(R.id.partnersEditText)).setText(TextUtils.join(", ", partners));
    }
}
