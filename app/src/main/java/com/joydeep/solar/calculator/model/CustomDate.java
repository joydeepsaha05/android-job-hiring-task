package com.joydeep.solar.calculator.model;

import android.content.Context;

import java.text.DateFormat;
import java.util.Date;

public class CustomDate {

    private Context context;

    private long timeInMillis;

    public CustomDate(Context context) {
        this.context = context;
        timeInMillis = System.currentTimeMillis();
    }

    public CustomDate(Context context, long timeInMillis) {
        this.context = context;
        this.timeInMillis = timeInMillis;
    }

    @Override
    public String toString() {
        Date date = new Date(timeInMillis);
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL);
        return dateFormat.format(date);
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public String nextDay() {
        timeInMillis += (1000 * 60 * 60 * 24);
        return this.toString();
    }

    public String previousDay() {
        timeInMillis -= (1000 * 60 * 60 * 24);
        return this.toString();
    }

    public String reset() {
        timeInMillis = System.currentTimeMillis();
        return this.toString();
    }
}
