package com.joydeep.solar.calculator.util;

import android.text.format.DateFormat;
import android.util.Log;

import java.util.Date;

import static java.lang.Math.floor;

public class SunTimeCalculator {

    private final String TAG = "SunTimeCalculator";
    private double degToRadFactor = Math.PI / 180;
    private double radToDegFactor = 180 / Math.PI;
    private double zenith = degToRadFactor * (90 + (float) 50 / 60); // Using official zenith

    public double phaseTimeCalculator(long timeInMillis, double latitude, double longitude,
                                      boolean isRisingTime) {

        // 1. Calculate the day of the year
        Date date = new Date(timeInMillis);
        double day = Double.parseDouble(DateFormat.format("dd", date).toString()); // 17
        double month = Double.parseDouble((String) DateFormat.format("MM", date)); // 01
        double year = Double.parseDouble((String) DateFormat.format("yyyy", date)); // 2019
        double N = getDayOfYear(day, month, year);
        Log.d(TAG, "Day of the year: " + N);

        // 2. Convert the longitude to hour value and calculate an approximate time
        double t = getTime(N, longitude, isRisingTime);

        // 3. Calculate the Sun's mean anomaly
        double M = sunMeanAnomaly(t);

        // 4. Calculate the Sun's true longitude
        double L = sunTrueLongitude(M);

        // 5a. Calculate the Sun's right ascension
        double RA = sunRightAscension(L);

        // 5b. Right ascension value needs to be in the same quadrant as L
        double Lquadrant = (floor(L / 90)) * 90;
        double RAquadrant = (floor(RA / 90)) * 90;
        RA = RA + (Lquadrant - RAquadrant);

        // 5c. Right ascension value needs to be converted into hours
        RA = RA / 15;

        // 6. Calculate the Sun's declination
        double sinDec = 0.39782 * Math.sin(degToRadFactor * L);
        double cosDec = Math.cos(Math.asin(sinDec));

        // 7a. Calculate the Sun's local hour angle
        double cosH = sunLocalHourAngle(sinDec, latitude, cosDec);
        if (cosH > 1 && isRisingTime) {
            // The sun never rises on this location(on the specified date)
            return Double.POSITIVE_INFINITY;
        } else if (cosH < -1 && !isRisingTime) {
            // The sun never sets on this location(on the specified date)
            return Double.POSITIVE_INFINITY;
        }

        // 7b. Finish calculating H and convert into hours
        double H;
        if (isRisingTime) {
            H = 360 - radToDegFactor * Math.acos(cosH);
        } else {
            H = radToDegFactor * Math.acos(cosH);
        }
        H = H / 15;

        // 8. Calculate local mean time of rising/setting
        double T = H + RA - (0.06571 * t) - 6.622;

        // 9. Adjust back to UTC
        double UT = T - longitude / 15;
        // NOTE: UT potentially needs to be adjusted into the range [0,24) by adding/subtracting 24
        if (UT >= 24) {
            UT -= 24;
        } else if (UT < 0) {
            UT += 24;
        }
        Log.d(TAG, "UT = " + UT);

        return UT;
    }

    private double getDayOfYear(double day, double month, double year) {
        double N1 = floor(275 * month / 9);
        double N2 = floor((month + 9) / 12);
        double N3 = (1 + floor((year - 4 * floor(year / 4) + 2) / 3));
        return N1 - (N2 * N3) + day - 30;
    }

    private double getTime(double N, double longitude, boolean isRisingTime) {
        double lngHour = longitude / 15;
        double t;
        if (isRisingTime) {
            t = N + ((6 - lngHour) / 24);
        } else {
            t = N + ((18 - lngHour) / 24);
        }
        return t;
    }

    private double sunMeanAnomaly(double t) {
        return (0.9856 * t) - 3.289;
    }

    private double sunTrueLongitude(double M) {
        double L = M + (1.916 * Math.sin(degToRadFactor * M)) +
                (0.020 * Math.sin(degToRadFactor * 2 * M)) + 282.634;
        // NOTE: L potentially needs to be adjusted into the range [0,360) by adding/subtracting 360
        L = adjustDegreeRange(L);
        return L;
    }

    private double sunRightAscension(double L) {
        double RA = radToDegFactor * Math.atan(0.91764 * Math.tan(degToRadFactor * L));
        // NOTE: RA potentially needs to be adjusted into the range [0,360) by adding/subtracting 360
        RA = adjustDegreeRange(RA);
        return RA;
    }

    private double sunLocalHourAngle(double sinDec, double latitude, double cosDec) {
        return (Math.cos(zenith) - (sinDec * Math.sin(degToRadFactor * latitude)))
                / (cosDec * Math.cos(degToRadFactor * latitude));
    }

    private double adjustDegreeRange(double degree) {
        if (degree < 0) {
            degree += 360;
        } else if (degree >= 360) {
            degree -= 360;
        }
        return degree;
    }
}
