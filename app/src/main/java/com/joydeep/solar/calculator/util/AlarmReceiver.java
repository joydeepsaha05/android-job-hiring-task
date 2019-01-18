package com.joydeep.solar.calculator.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;

import com.joydeep.solar.calculator.R;
import com.joydeep.solar.calculator.activity.MapsActivity;
import com.joydeep.solar.calculator.helper.SharedPrefHelper;

public class AlarmReceiver extends BroadcastReceiver {

    public static final int REQUEST_CODE = 999;
    private final StyleSpan mBoldSpan = new StyleSpan(Typeface.BOLD);

    private SpannableString makeNotificationLine(String title, String text) {
        final SpannableString spannableString;
        if (title != null && title.length() > 0) {
            spannableString = new SpannableString(String.format("%s  %s", title, text));
            spannableString.setSpan(mBoldSpan, 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            spannableString = new SpannableString(text);
        }
        return spannableString;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d("AlarmReceiver", "onReceive");
        double latitude = Double.parseDouble(SharedPrefHelper.getSharedPreferenceString(
                context, SharedPrefHelper.LATITUDE_PREF_KEY, "-1000"));
        double longitude = Double.parseDouble(SharedPrefHelper.getSharedPreferenceString(
                context, SharedPrefHelper.LONGITUDE_PREF_KEY, "-1000"));

        if (latitude == -1000 && longitude == -1000) {
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        Intent notificationIntent = new Intent(context, MapsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MapsActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Golden Hour");
        Notification notification = builder.setContentTitle(context.getString(R.string.app_name))
                .setContentText(makeNotificationLine("Golden Hour", "has started"))
                .setTicker("Golden Hour has started!")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(bitmap)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(R.string.app_name, notification);
        }
    }
}