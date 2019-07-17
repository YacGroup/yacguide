package com.example.paetz.yacguide;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.utils.IntentConstants;

public class MainActivity extends AppCompatActivity {

    public static AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("YACguide");

        database = AppDatabase.Companion.getAppDatabase(this);
    }

    public void enterDatabase (View v) {
        Intent intent = new Intent(this, CountryActivity.class);
        startActivity(intent);
    }

    public void enterTourbook(View v) {
        Intent intent = new Intent(this, TourbookActivity.class);
        startActivity(intent);
    }
}
