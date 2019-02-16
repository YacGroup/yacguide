package com.example.paetz.yacguide.database;

import com.example.paetz.yacguide.utils.ParserUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TourbookExporter {

    private final String _routeIdKey = "routeId";
    private final String _styleIdKey = "styleId";
    private final String _yearkey = "year";
    private final String _monthkey = "month";
    private final String _dayKey = "day";
    private final String _partnersKey = "partners";
    private final String _notesKey = "notes";

    private AppDatabase _db;

    public TourbookExporter(AppDatabase db) {
        _db = db;
    }

    public void exportTourbook(String filePath) throws JSONException {
        final Ascend[] ascends = _db.ascendDao().getAll();
        JSONArray jsonAscends = new JSONArray();
        for (Ascend ascend : ascends) {
            jsonAscends.put(ascend2Json(ascend));
        }
        File file = new File(filePath);
        try {
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(jsonAscends.toString().getBytes(StandardCharsets.UTF_8));
            outStream.close();
        } catch (Exception e){
            throw new JSONException("");
        }
    }

    public void importTourbook(String filePath) throws JSONException {
        File file = new File(filePath);
        String jsonString = "";
        try {
            FileInputStream inStream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.ISO_8859_1));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            jsonString = sb.toString();
            reader.close();
            inStream.close();
        } catch (Exception e) {
            throw new JSONException("");
        }
        _writeJsonStringToDatabase(jsonString);
    }

    private JSONObject ascend2Json(Ascend ascend) throws JSONException {
        JSONObject jsonAscend = new JSONObject();
        jsonAscend.put(_routeIdKey, String.valueOf(ascend.getRouteId()));
        jsonAscend.put(_styleIdKey, String.valueOf(ascend.getStyleId()));
        jsonAscend.put(_yearkey, String.valueOf(ascend.getYear()));
        jsonAscend.put(_monthkey, String.valueOf(ascend.getMonth()));
        jsonAscend.put(_dayKey, String.valueOf(ascend.getDay()));
        JSONArray partnerList = new JSONArray();
        for (final int id : ascend.getPartnerIds()) {
            partnerList.put(_db.partnerDao().getPartner(id).getName());
        }
        jsonAscend.put(_partnersKey, partnerList);
        jsonAscend.put(_notesKey, ascend.getNotes());

        return jsonAscend;
    }

    private void _writeJsonStringToDatabase(String jsonString) throws JSONException {
        final JSONArray jsonAscends = new JSONArray(jsonString);
        for (final Ascend ascend : _db.ascendDao().getAll()) {
            _db.deleteAscend(ascend);
        }
        _db.partnerDao().deleteAll();
        int ascendId = 1;
        int partnerId = 1;
        for (int i = 0; i < jsonAscends.length(); i++) {
            JSONObject jsonAscend = jsonAscends.getJSONObject(i);
            final int routeId = ParserUtils.jsonField2Int(jsonAscend, _routeIdKey);
            Ascend ascend = new Ascend();
            ascend.setId(ascendId++);
            ascend.setRouteId(routeId);
            ascend.setStyleId(ParserUtils.jsonField2Int(jsonAscend, _styleIdKey));
            ascend.setYear(ParserUtils.jsonField2Int(jsonAscend, _yearkey));
            ascend.setMonth(ParserUtils.jsonField2Int(jsonAscend, _monthkey));
            ascend.setDay(ParserUtils.jsonField2Int(jsonAscend, _dayKey));
            ascend.setNotes(jsonAscend.getString(_notesKey));
            JSONArray partnerNames = jsonAscend.getJSONArray(_partnersKey);
            ArrayList<Integer> partnerIds = new ArrayList<Integer>();
            for (int j = 0; j < partnerNames.length(); j++) {
                String name = partnerNames.getString(j).trim();
                boolean partnerAvailable = false;
                for (Partner partner : _db.partnerDao().getAll()) {
                    if (partner.getName().equals(name)) {
                        partnerIds.add(partner.getId());
                        partnerAvailable = true;
                        break;

                    }
                }
                if (!partnerAvailable) {
                    Partner newPartner = new Partner();
                    newPartner.setId(partnerId);
                    newPartner.setName(name);
                    _db.partnerDao().insert(newPartner);
                    partnerIds.add(partnerId++);
                }
            }
            ascend.setPartnerIds(partnerIds);
            _db.ascendDao().insert(ascend);
            if (_db.routeDao().getRoute(routeId) != null) {
                // route may not exist in database anymore
                _db.routeDao().updateAscendCount(_db.routeDao().getAscendCount(routeId) + 1, routeId);
                _db.rockDao().updateAscended(true, _db.routeDao().getRoute(routeId).getParentId());
            }
        }
    }
}
