package com.adobe.adobepass.apps.demo;

import com.adobe.adobepass.accessenabler.models.Mvpd;

import java.util.ArrayList;

public class SerializedMVPDListSingleton {

    private final String LOG_TAG = "SerializedMVPDListSingleton";

    public static SerializedMVPDListSingleton instance = null;
    public ArrayList<Mvpd> mvpdsList = null;

    private SerializedMVPDListSingleton() {

    }

    public static SerializedMVPDListSingleton getInstance() {
        if (instance == null) {
            instance = new SerializedMVPDListSingleton();
        }
        return instance;
    }

    public void setMvpdsList(ArrayList<Mvpd> mvpdsListSerialized) {
        this.mvpdsList = mvpdsListSerialized;
    }

    public ArrayList<Mvpd> getMvpdsList() {
        return this.mvpdsList;
    }
}
