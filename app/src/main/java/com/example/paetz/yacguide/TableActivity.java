package com.example.paetz.yacguide;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.network.HTMLParser;
import com.example.paetz.yacguide.network.NetworkUtils;

public abstract class TableActivity extends AppCompatActivity implements UpdateListener {

    protected AppDatabase db;
    protected HTMLParser htmlParser;
    protected boolean updateInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void initialize(int layoutNumber) {
        setContentView(layoutNumber);
        db = MainActivity.database;
        updateInProgress = false;
        displayContent();
    }

    // UpdateListener
    @Override
    public void onEvent(boolean state) {
        findViewById(R.id.updateButton).setEnabled(true);
        findViewById(R.id.deleteButton).setEnabled(true);
        String stateStr = "Aktualisierung erfolgreich";
        if (!state) {
            stateStr = "Fehler bei Aktualisierung";
        }
        Toast.makeText(this, stateStr, Toast.LENGTH_SHORT).show();
        updateInProgress = false;
        displayContent();
    }

    public void home(View v) {
        if (updateInProgress) {
            Toast.makeText(getApplicationContext(), "Aktualisierung in den Hintergrund verschoben", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void back(View v) {
        if (updateInProgress) {
            Toast.makeText(getApplicationContext(), "Aktualisierung in den Hintergrund verschoben", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    public void update(View v) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Keine Internetverbindung", Toast.LENGTH_LONG).show();
            return;
        }
        if (htmlParser != null) {
            updateInProgress = true;
            htmlParser.parse();
            findViewById(R.id.updateButton).setEnabled(false);
            findViewById(R.id.deleteButton).setEnabled(false);
            Toast.makeText(this, "Aktualisierung gestartet", Toast.LENGTH_SHORT).show();
        }
    }

    public void delete(View v) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog);
        ((TextView) dialog.findViewById(R.id.dialogText)).setText("Diesen Bereich löschen?");
        dialog.findViewById(R.id.yesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteContent();
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "Bereich gelöscht", Toast.LENGTH_SHORT).show();
                displayContent();
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

    protected abstract void displayContent();

    protected abstract void deleteContent();
}
