package com.example.paetz.yacguide.database;

import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class TourbookExporter {

    private final String routeIdKey = "routeId";
    private final String styleIdKey = "styleId";
    private final String yearkey = "year";
    private final String monthkey = "month";
    private final String dayKey = "day";
    private final String partnersKey = "partners";
    private final String notesKey = "notes";

    private AppDatabase _db;

    public TourbookExporter(AppDatabase db) {
        _db = db;
    }

    public void exportTourbook(String filename) throws JSONException {
        final Ascend[] ascends = _db.ascendDao().getAll();
        JSONArray jsonAscends = new JSONArray();
        for (Ascend ascend : ascends) {
            jsonAscends.put(ascend2Json(ascend));
        }
        System.out.println(jsonAscends);
        /*File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath(), filename);
        try {
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(jsonAscends.toString().getBytes(StandardCharsets.UTF_8));
            outStream.close();
        } catch (Exception e){
            throw new JSONException("");
        }*/
    }

    private JSONObject ascend2Json(Ascend ascend) throws JSONException {
        JSONObject jsonAscend = new JSONObject();
        jsonAscend.put(routeIdKey, String.valueOf(ascend.getRouteId()));
        jsonAscend.put(styleIdKey, String.valueOf(ascend.getStyleId()));
        jsonAscend.put(yearkey, String.valueOf(ascend.getYear()));
        jsonAscend.put(monthkey, String.valueOf(ascend.getMonth()));
        jsonAscend.put(dayKey, String.valueOf(ascend.getDay()));
        JSONArray partnerList = new JSONArray();
        for (final int id : ascend.getPartnerIds()) {
            partnerList.put(_db.partnerDao().getPartner(id).getName());
        }
        jsonAscend.put(partnersKey, partnerList);
        jsonAscend.put(notesKey, ascend.getNotes());

        return jsonAscend;
    }
}
