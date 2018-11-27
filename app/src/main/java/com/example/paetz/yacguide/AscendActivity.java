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
import com.example.paetz.yacguide.database.Route;
import com.example.paetz.yacguide.utils.IntentConstants;

import java.util.ArrayList;
import java.util.Calendar;

public class AscendActivity extends AppCompatActivity {

    private AppDatabase _db;
    private int _resultUpdated;
    private int _ascendId;
    private int _routeId;
    private int _styleId;
    private int _year;
    private int _month;
    private int _day;
    private ArrayList<Integer> _partnerIds;
    private String _notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ascend);
        findViewById(R.id.notesEditText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        _db = MainActivity.database;
        _resultUpdated = IntentConstants.RESULT_NO_UPDATE;
        _ascendId = getIntent().getIntExtra(IntentConstants.ASCEND_KEY, _db.INVALID_ID);
        _routeId = getIntent().getIntExtra(IntentConstants.ROUTE_KEY, _db.INVALID_ID);
        _styleId = getIntent().getIntExtra(IntentConstants.ASCEND_STYLE_KEY, 0);
        _year = getIntent().getIntExtra(IntentConstants.ASCEND_YEAR, 0);
        _month = getIntent().getIntExtra(IntentConstants.ASCEND_MONTH, 0);
        _day = getIntent().getIntExtra(IntentConstants.ASCEND_DAY, 0);
        _partnerIds = getPartnerIds(getIntent());
        _notes = getIntent().getStringExtra(IntentConstants.ASCEND_NOTES);
        _displayContent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            _partnerIds = getPartnerIds(data);
            _displayContent();
        }
    }

    public void enter(View v) {
        Ascend ascend = new Ascend();
        if (_ascendId != _db.INVALID_ID) {
            ascend.setId(_ascendId);
        } else {
            _db.routeDao().updateAscendCount(_db.routeDao().getAscendCount(_routeId) + 1, _routeId);
            _db.rockDao().updateAscended(true, _db.routeDao().getRoute(_routeId).getParentId());
            _resultUpdated = IntentConstants.RESULT_UPDATED;
        }
        ascend.setRouteId(_routeId);
        int styleId = _db.CLIMBING_STYLES.inverse().get(((Spinner) findViewById(R.id.styleSpinner)).getSelectedItem().toString());
        ascend.setStyleId(styleId);
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
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
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
        String[] partners = ((EditText) findViewById(R.id.partnersEditText)).getText().toString().split(", ");
        ArrayList<Integer> partnerIds = new ArrayList<Integer>();
        for (int i = 0; i < partners.length; i++) {
            partnerIds.add(_db.partnerDao().getId(partners[i]));
        }
        Intent intent = new Intent(AscendActivity.this, PartnersActivity.class);
        intent.putIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS, partnerIds);
        startActivityForResult(intent, 0);
    }

    private void _displayContent() {
        final Route route = _db.routeDao().getRoute(_routeId);
        setTitle(route != null ? route.getName() + "   " + route.getGrade() : "???");

        Spinner spinner = findViewById(R.id.styleSpinner);
        String[] climbing_styles = _db.CLIMBING_STYLES.values().toArray(new String[0]);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_list_item_1, climbing_styles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(_styleId != 0 ? adapter.getPosition(_db.CLIMBING_STYLES.get(_styleId)) : 0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                _styleId = _db.CLIMBING_STYLES.inverse().get(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                _styleId = 0;
            }
        });

        ((EditText) findViewById(R.id.dateEditText)).setText(_day + "." + _month + "." + _year);

        if (_partnerIds != null) {
            ArrayList<String> partners = new ArrayList<String>();
            for (Integer id : _partnerIds) {
                String name = _db.partnerDao().getName(id);
                partners.add(name == null ? "???" : name);
            }
            ((EditText) findViewById(R.id.partnersEditText)).setText(TextUtils.join(", ", partners));
        } else {
            ((EditText) findViewById(R.id.partnersEditText)).setText("");
        }

        ((EditText) findViewById(R.id.notesEditText)).setText(_notes);
    }

    private ArrayList<Integer> getPartnerIds(Intent intent) {
        _partnerIds = intent.getIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS);
        return _partnerIds == null ? new ArrayList<Integer>() : _partnerIds;
    }
}
