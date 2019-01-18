package com.joydeep.solar.calculator.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static java.lang.Math.PI;
import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

public class MoonTimeCalculator {

    private double dayMs = 1000 * 60 * 60 * 24;
    private double J1970 = 2440588;
    private double J2000 = 2451545;
    private double rad = PI / 180;
    private double e = rad * 23.4397; // obliquity of the Earth

    public String moonTime(long timeInMillis, double lat, double lng, boolean getRise) {

        Calendar c = Calendar.getInstance();
        c.setTime(new Date(timeInMillis));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Date t = new Date(c.getTimeInMillis());

        double hc = 0.133 * rad;
        double h0 = new GetMoonPosition(t, lat, lng).altitude - hc;
        double h1, h2, rise, set, a, b, xe, ye, d, roots, x1, x2, dx;
        rise = set = x1 = x2 = Double.POSITIVE_INFINITY;

        // go in 2-hour chunks, each time seeing if a 3-point quadratic curve crosses zero (which means rise or set)
        for (int i = 1; i <= 24; i += 2) {
            h1 = new GetMoonPosition(hoursLater(t, i), lat, lng).altitude - hc;
            h2 = new GetMoonPosition(hoursLater(t, i + 1), lat, lng).altitude - hc;

            a = (h0 + h2) / 2 - h1;
            b = (h2 - h0) / 2;
            xe = -b / (2 * a);
            ye = (a * xe + b) * xe + h1;
            d = b * b - 4 * a * h1;
            roots = (double) 0;

            if (d >= 0) {
                dx = Math.sqrt(d) / (Math.abs(a) * 2);
                x1 = xe - dx;
                x2 = xe + dx;
                if (Math.abs(x1) <= 1) roots++;
                if (Math.abs(x2) <= 1) roots++;
                if (x1 < -1) x1 = x2;
            }

            if (roots == 1) {
                if (h0 < 0) rise = i + x1;
                else set = i + x1;

            } else if (roots == 2) {
                rise = i + (ye < 0 ? x2 : x1);
                set = i + (ye < 0 ? x1 : x2);
            }

            if (rise != Double.POSITIVE_INFINITY && set != Double.POSITIVE_INFINITY) break;

            h0 = h2;
        }

        String pattern = "hh:mm a";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("gmt"));

        if (getRise) {
            if (rise != Double.POSITIVE_INFINITY) {
                return simpleDateFormat.format(hoursLater(t, rise));
            }
            return "-";
        } else {
            if (set != Double.POSITIVE_INFINITY) {
                return simpleDateFormat.format(hoursLater(t, set));
            }
            return "-";
        }
    }

    private double azimuth(double H, double phi, double dec) {
        return Math.atan2(sin(H), cos(H) * sin(phi) - tan(dec) * cos(phi));
    }

    private double altitude(double H, double phi, double dec) {
        return asin(sin(phi) * sin(dec) + cos(phi) * cos(dec) * cos(H));
    }

    private double toDays(Date date) {
        return toJulian(date) - J2000;
    }

    private double toJulian(Date date) {
        return date.getTime() / dayMs - 0.5 + J1970;
    }

    private double rightAscension(double l, double b) {
        return Math.atan2(sin(l) * cos(e) - tan(b) * sin(e), cos(l));
    }

    private double declination(double l, double b) {
        return asin(sin(b) * cos(e) + cos(b) * sin(e) * sin(l));
    }

    private double siderealTime(double d, double lw) {
        return rad * (280.16 + 360.9856235 * d) - lw;
    }

    private double astroRefraction(double h) {
        if (h < 0) // the following formula works for positive altitudes only.
            h = 0; // if h = -0.08901179 a div/0 would occur.

        // formula 16.4 of "Astronomical Algorithms" 2nd edition by Jean Meeus (Willmann-Bell, Richmond) 1998.
        // 1.02 / tan(h + 10.26 / (h + 5.10)) h in degrees, result in arc minutes -> converted to rad:
        return 0.0002967 / Math.tan(h + 0.00312536 / (h + 0.08901179));
    }

    private Date hoursLater(Date date, double h) {
        return new Date((long) (date.getTime() + h * dayMs / 24));
    }

    class GetMoonPosition {

        double azimuth, altitude, distance, parallacticAngle;

        GetMoonPosition(Date date, double lat, double lng) {

            double lw = rad * -lng,
                    phi = rad * lat,
                    d = toDays(date);

            MoonCoords c = new MoonCoords(d);
            double H = siderealTime(d, lw) - c.ra,
                    h = altitude(H, phi, c.dec),
                    // formula 14.1 of "Astronomical Algorithms" 2nd edition by Jean Meeus (Willmann-Bell, Richmond) 1998.
                    pa = Math.atan2(sin(H), tan(phi) * cos(c.dec) - sin(c.dec) * cos(H));

            h = h + astroRefraction(h); // altitude correction for refraction
            azimuth = azimuth(H, phi, c.dec);
            altitude = h;
            distance = c.dist;
            parallacticAngle = pa;
        }
    }

    class MoonCoords { // geocentric ecliptic coordinates of the moon

        double ra, dec, dist;

        MoonCoords(double d) {
            double L = rad * (218.316 + 13.176396 * d), // ecliptic longitude
                    M = rad * (134.963 + 13.064993 * d), // mean anomaly
                    F = rad * (93.272 + 13.229350 * d),  // mean distance

                    l = L + rad * 6.289 * sin(M), // longitude
                    b = rad * 5.128 * sin(F),     // latitude
                    dt = 385001 - 20905 * cos(M);  // distance to the moon in km
            ra = rightAscension(l, b);
            dec = declination(l, b);
            dist = dt;
        }
    }
}
