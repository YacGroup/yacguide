package com.example.paetz.yacguide;

public interface UpdateListener {

    // Convention: If eventMessage is empty, the update process is finished
    void onEvent(boolean success, String eventMessage);
}
