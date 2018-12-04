package com.example.paetz.yacguide;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.network.JSONWebParser;
import com.example.paetz.yacguide.network.NetworkUtils;

public abstract class TableActivity extends AppCompatActivity implements UpdateListener {

    protected AppDatabase db;
    protected JSONWebParser jsonParser;
    protected Dialog updateDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void initialize(int layoutNumber) {
        setContentView(layoutNumber);
        db = MainActivity.database;
    }

    // UpdateListener
    @Override
    public void onEvent(boolean success) {
        if (updateDialog != null) {
            updateDialog.dismiss();
        }
        if (success) {
            Toast.makeText(this, "Bereich aktualisiert", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Fehler bei Aktualisierung", Toast.LENGTH_SHORT).show();
        }
        displayContent();
    }

    public void home(View v) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void back(View v) {
        finish();
    }

    public void update(View v) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Keine Internetverbindung", Toast.LENGTH_LONG).show();
            return;
        }
        if (jsonParser != null) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog);
            ((TextView) dialog.findViewById(R.id.dialogText)).setText("Diesen Bereich aktualisieren?");
            dialog.findViewById(R.id.yesButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    jsonParser.fetchData();
                    showUpdateDialog();
                    dialog.dismiss();
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

    protected void showUpdateDialog() {
        updateDialog = new Dialog(this);
        updateDialog.setContentView(R.layout.info_dialog);
        updateDialog.setCancelable(false);
        updateDialog.setCanceledOnTouchOutside(false);
        updateDialog.show();
    }

    protected abstract void displayContent();

    protected abstract void deleteContent();
}
