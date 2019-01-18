package com.joydeep.solar.calculator.realm;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

import io.realm.RealmObject;

public class RealmLocation extends RealmObject {

    public double latitude;
    public double longitude;

    public LatLng getLocation() {
        return new LatLng(latitude, longitude);
    }

    public void setLocation(LatLng latLng) {
        latitude = latLng.latitude;
        longitude = latLng.longitude;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%.2f, %.2f", latitude, longitude);
    }
}
