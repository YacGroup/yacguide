package com.example.paetz.yacguide;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.database.Ascend;
import com.example.paetz.yacguide.database.Partner;
import com.example.paetz.yacguide.utils.IntentConstants;
import com.example.paetz.yacguide.utils.WidgetUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PartnersActivity extends AppCompatActivity {

    private AppDatabase _db;
    private Map<Integer, CheckBox> _checkboxMap;
    private ArrayList<Integer> _selectedPartnerIds;
    private String _partnerNamePart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partners);

        _db = MainActivity.database;
        _checkboxMap = new HashMap<Integer, CheckBox>();
        _selectedPartnerIds = getIntent().getIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS);
        _partnerNamePart = "";

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
                _partnerNamePart = searchEditText.getText().toString();
                _displayContent();
            }
        });
        _displayContent();
    }

    public void addPartner(View v) {
        final Dialog dialog = new Dialog(this);
        dialog.setTitle("Kletterpartner hinzufügen");
        dialog.setContentView(R.layout.add_partner_dialog);
        Button okButton = (Button) dialog.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newName = ((EditText) dialog.findViewById(R.id.addPartnerEditText)).getText().toString().trim();
                _updatePartner(dialog, newName, null);
            }
        });
        Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
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
        for (final Map.Entry<Integer, CheckBox> entry : _checkboxMap.entrySet()) {
            final CheckBox cb = entry.getValue();
            if (cb.isChecked()) {
                selectedIds.add(entry.getKey());
            }
        }
        Intent resultIntent = new Intent();
        resultIntent.putIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS, selectedIds);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void _displayContent() {
        setTitle("Kletterpartner");
        LinearLayout layout = findViewById(R.id.tableLayout);
        layout.removeAllViews();
        _checkboxMap.clear();
        final Partner[] partners = _db.partnerDao().getAll();
        final Ascend[] ascends = _db.ascendDao().getAll();

        // We need to sort the partners according to the number of ascends you have done with them
        ArrayList<Integer> ascendPartnerIds = new ArrayList<Integer>();
        for (final Ascend ascend : ascends) {
            ascendPartnerIds.addAll(ascend.getPartnerIds());
        }
        ArrayList<Partner> sortedPartners = new ArrayList<Partner>();
        ArrayList<Integer> partnerIdOccurences = new ArrayList<Integer>();
        for (int i = 0; i < partners.length; i++) {
            final int occurenceCount = Collections.frequency(ascendPartnerIds, partners[i].getId());
            int index = i;
            for (int j = 0; j < i; j++) {
                if (occurenceCount > partnerIdOccurences.get(j)) {
                    index = j;
                    break;
                }
            }
            sortedPartners.add(index, partners[i]);
            partnerIdOccurences.add(index, occurenceCount);
        }

        for (final Partner partner : sortedPartners) {
            final String partnerName = partner.getName();
            if (!partnerName.toLowerCase().contains(_partnerNamePart.toLowerCase())) {
                continue;
            }
            RelativeLayout innerLayout = new RelativeLayout(this);
            innerLayout.setBackgroundColor(Color.WHITE);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(partnerName);
            if (_selectedPartnerIds.contains(partner.getId())) {
                checkBox.setChecked(true);
            }
            _checkboxMap.put(partner.getId(), checkBox);
            innerLayout.addView(checkBox);

            ImageButton editButton = new ImageButton(this);
            editButton.setId(View.generateViewId());
            editButton.setImageResource(android.R.drawable.ic_menu_edit);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(PartnersActivity.this);
                    dialog.setContentView(R.layout.add_partner_dialog);
                    ((EditText) dialog.findViewById(R.id.addPartnerEditText)).setText(partnerName);
                    Button okButton = (Button) dialog.findViewById(R.id.okButton);
                    okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final String newName = ((EditText) dialog.findViewById(R.id.addPartnerEditText)).getText().toString().trim();
                            _updatePartner(dialog, newName, partner);
                        }
                    });
                    Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
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
            });
            innerLayout.addView(editButton);

            ImageButton deleteButton = new ImageButton(this);
            deleteButton.setId(View.generateViewId());
            deleteButton.setImageResource(android.R.drawable.ic_menu_delete);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(PartnersActivity.this);
                    dialog.setContentView(R.layout.dialog);
                    ((TextView) dialog.findViewById(R.id.dialogText)).setText("Kletterpartner löschen?");
                    Button okButton = (Button) dialog.findViewById(R.id.yesButton);
                    okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            _db.partnerDao().delete(partner);
                            dialog.dismiss();
                            _displayContent();
                        }
                    });
                    Button cancelButton = (Button) dialog.findViewById(R.id.noButton);
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
            });
            innerLayout.addView(deleteButton);

            final int buttonWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
            RelativeLayout.LayoutParams paramsDelete = (RelativeLayout.LayoutParams) deleteButton.getLayoutParams();
            paramsDelete.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            paramsDelete.width = paramsDelete.height = buttonWidthPx;

            RelativeLayout.LayoutParams paramsEdit = (RelativeLayout.LayoutParams) editButton.getLayoutParams();
            paramsEdit.addRule(RelativeLayout.LEFT_OF, deleteButton.getId());
            paramsEdit.width = paramsEdit.height = buttonWidthPx;

            layout.addView(innerLayout);
            layout.addView(WidgetUtils.INSTANCE.createHorizontalLine(this, 1));
        }
    }

    private void _updatePartner(Dialog dialog, String newName, Partner partner) {
        if (newName.equals("")) {
            Toast.makeText(dialog.getContext(), "Kein Name eingegeben", Toast.LENGTH_SHORT).show();
        } else if (_db.partnerDao().getId(newName) > 0) {
            Toast.makeText(dialog.getContext(), "Name bereits vergeben", Toast.LENGTH_SHORT).show();
        } else {
            if (partner == null) {
                partner = new Partner();
            }
            partner.setName(newName);
            _db.partnerDao().insert(partner);
            dialog.dismiss();
            _displayContent();
        }
    }
}
