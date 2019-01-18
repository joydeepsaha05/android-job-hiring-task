package com.joydeep.solar.calculator.util;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.TimeZoneApi;

import java.util.TimeZone;

public class TimeZoneCalculator {

    private String apiKey;

    public TimeZoneCalculator(String apiKey) {
        this.apiKey = apiKey;
    }

    public void calculateTimeZone(LatLng latLng, PendingResult.Callback<TimeZone> callback) {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();

        com.google.maps.model.LatLng location =
                new com.google.maps.model.LatLng(latLng.latitude, latLng.longitude);
        PendingResult<TimeZone> timeZone = TimeZoneApi.getTimeZone(context, location);
        timeZone.setCallback(callback);
    }
}
