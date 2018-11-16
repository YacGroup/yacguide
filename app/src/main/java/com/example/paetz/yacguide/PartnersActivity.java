package com.example.paetz.yacguide;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.database.Partner;
import com.example.paetz.yacguide.utils.IntentConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PartnersActivity extends AppCompatActivity {

    private AppDatabase _db;
    private Map<Integer, CheckBox> _checkboxMap;
    private ArrayList<Integer> _selectedPartnerIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partners);

        _db = MainActivity.database;
        _checkboxMap = new HashMap<Integer, CheckBox>();

        _selectedPartnerIds = getIntent().getIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS);
        _displayContent();
    }

    public void addPartner(View v) {
        final Dialog dialog = new Dialog(this);
        dialog.setTitle("Kletterpartner hinzufügen");
        dialog.setContentView(R.layout.dialog_add_partner);
        Button okButton = dialog.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = ((EditText) dialog.findViewById(R.id.addPartnerEditText)).getText().toString();
                if (name.equals("")) {
                    Toast.makeText(dialog.getContext(), "Kein Name eingegeben", Toast.LENGTH_SHORT).show();
                } else {
                    Partner partner = new Partner();
                    partner.setName(name);
                    _db.partnerDao().insert(partner);
                }
                dialog.dismiss();
                _displayContent();
            }
        });
        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void enter(View v) {
        ArrayList<Integer> selectedIds = new ArrayList<Integer>();
        for (Map.Entry<Integer, CheckBox> entry : _checkboxMap.entrySet()) {
            CheckBox cb = entry.getValue();
            if (cb.isChecked()) {
                selectedIds.add(entry.getKey());
            }
        }
        Intent resultIntent = new Intent();
        resultIntent.putIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS, selectedIds);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    public void cancel(View v) {
        setResult(Activity.RESULT_CANCELED, null);
        finish();
    }

    private void _displayContent() {
        setTitle("Kletterpartner");
        LinearLayout layout = findViewById(R.id.tableLayout);
        layout.removeAllViews();
        _checkboxMap.clear();
        Partner[] partners = _db.partnerDao().getAll();
        for (Partner partner : partners) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(partner.getName());
            if (_selectedPartnerIds.contains(partner.getId())) {
                checkBox.setChecked(true);
            }
            _checkboxMap.put(partner.getId(), checkBox);
            layout.addView(checkBox);
        }
    }
}
