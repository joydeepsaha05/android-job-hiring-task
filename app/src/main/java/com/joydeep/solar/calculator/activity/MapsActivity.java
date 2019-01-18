package com.joydeep.solar.calculator.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.maps.PendingResult;
import com.joydeep.solar.calculator.R;
import com.joydeep.solar.calculator.helper.PermissionHelper;
import com.joydeep.solar.calculator.helper.SharedPrefHelper;
import com.joydeep.solar.calculator.model.CustomDate;
import com.joydeep.solar.calculator.realm.RealmLocation;
import com.joydeep.solar.calculator.realm.RealmSingleton;
import com.joydeep.solar.calculator.util.AlarmReceiver;
import com.joydeep.solar.calculator.util.MoonTimeCalculator;
import com.joydeep.solar.calculator.util.SavedLocationsDialog;
import com.joydeep.solar.calculator.util.SunTimeCalculator;
import com.joydeep.solar.calculator.util.TimeZoneCalculator;
import com.joydeep.solar.calculator.util.UIUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

import static com.joydeep.solar.calculator.helper.PermissionHelper.LOCATION_REQ_CODE;

public class MapsActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnCameraIdleListener,
        OnMapReadyCallback {

    public static final String TAG = MapsActivity.class.getSimpleName();
    /*
     * Define a request code to send to Google Play services
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int REQUEST_LOCATION_CODE = 2;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private CustomDate customDate;

    private SupportMapFragment mapFragment;
    private EditText searchEditText;
    private ImageView mapGPSIcon, dateNextImage, datePreviousImage, dateResetImage,
            saveLocImage, showSavedLocImage;
    private TextView dateTV, sunriseTV, sunsetTV, moonriseTV, moonsetTV, timezoneTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        searchEditText = findViewById(R.id.search_edit_text);
        mapGPSIcon = findViewById(R.id.image_gps);
        dateTV = findViewById(R.id.tv_date);
        dateNextImage = findViewById(R.id.image_next_date);
        datePreviousImage = findViewById(R.id.image_previous_date);
        dateResetImage = findViewById(R.id.image_reset_date);
        sunriseTV = findViewById(R.id.tv_sunrise);
        sunsetTV = findViewById(R.id.tv_sunset);
        moonriseTV = findViewById(R.id.tv_moon_rise);
        moonsetTV = findViewById(R.id.tv_moon_set);
        saveLocImage = findViewById(R.id.image_pin);
        showSavedLocImage = findViewById(R.id.image_bookmark);
        timezoneTV = findViewById(R.id.tv_timezone);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(15 * 1000)
                .setFastestInterval(5 * 1000);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setUpViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed)
     */
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(this);
        }
    }

    private void handleNewLocation(LatLng latLng) {
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mMap != null) {
            if (new PermissionHelper().checkAndRequestPermissions(this)) {
                showMyLocationButton();
            }
            mMap.setOnCameraIdleListener(this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " +
                    connectionResult.getErrorCode());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (new PermissionHelper().checkAndRequestPermissions(this) && mGoogleApiClient.isConnected()) {
            showMyLocationButton();
        }
    }

    @Override
    public void onCameraIdle() {
        updateTimes();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQ_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mGoogleApiClient.isConnected() && mMap != null) {
                    showMyLocationButton();
                }
            } else {
                Toast.makeText(this, R.string.location_access_error,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        getCurrentLocation();
                    }
                }
        }
    }

    /**
     * Initializes views with data and sets onClickListeners where required
     */
    private void setUpViews() {
        customDate = new CustomDate(this);

        UIUtils.setupClearButtonWithAction(searchEditText);

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onMapSearch(v.getText().toString());
                UIUtils.hideKeyboard(getApplicationContext(), searchEditText);
                return true;
            }
            return false;
        });

        mapGPSIcon.setOnClickListener(v -> {
            if (new PermissionHelper().checkAndRequestPermissions(MapsActivity.this)) {
                getCurrentLocation();
            }
        });

        dateTV.setText(customDate.toString());

        dateNextImage.setOnClickListener(v -> {
            dateTV.setText(customDate.nextDay());
            updateTimes();
        });

        datePreviousImage.setOnClickListener(v -> {
            dateTV.setText(customDate.previousDay());
            updateTimes();
        });

        dateResetImage.setOnClickListener(v -> {
            dateTV.setText(customDate.reset());
            updateTimes();
        });

        saveLocImage.setOnClickListener(v -> {
            LatLng latLng = mMap.getCameraPosition().target;
            Realm realm = RealmSingleton.getInstance().getRealm();
            realm.executeTransactionAsync(realm1 -> {
                RealmLocation realmLocation = realm1.where(RealmLocation.class)
                        .equalTo("latitude", latLng.latitude)
                        .equalTo("longitude", latLng.longitude)
                        .findFirst();
                if (realmLocation == null) {
                    // Location does not exist in DB
                    realmLocation = realm1.createObject(RealmLocation.class);
                    realmLocation.setLocation(latLng);
                    Log.d(TAG, "Saved Location: " + latLng.latitude + ", " + latLng.longitude);
                } else {
                    Log.d(TAG, "Location already exists in DB, skipping");
                }
            }, () -> Toast.makeText(MapsActivity.this, R.string.loc_saved,
                    Toast.LENGTH_SHORT).show());
        });

        showSavedLocImage.setOnClickListener(
                v -> new SavedLocationsDialog().showDialog(this,
                        realmLocation -> handleNewLocation(realmLocation.getLocation())));
    }

    private void updateTimes() {
        LatLng currentLatLng = mMap.getCameraPosition().target;
        Log.d(TAG, currentLatLng.toString());
        SunTimeCalculator sunTimeCalculator = new SunTimeCalculator();
        double sunriseTime = sunTimeCalculator.phaseTimeCalculator(customDate.getTimeInMillis(),
                currentLatLng.latitude, currentLatLng.longitude, true);
        sunriseTV.setText(UIUtils.formatTime(sunriseTime));
        double sunsetTime = sunTimeCalculator.phaseTimeCalculator(customDate.getTimeInMillis(),
                currentLatLng.latitude, currentLatLng.longitude, false);
        sunsetTV.setText(UIUtils.formatTime(sunsetTime));

        MoonTimeCalculator moonTimeCalculator = new MoonTimeCalculator();
        double moonRiseTime = moonTimeCalculator.moonTime(customDate.getTimeInMillis(),
                currentLatLng.latitude, currentLatLng.longitude, true);
        moonriseTV.setText(UIUtils.formatTime(moonRiseTime));
        double moonSetTime = moonTimeCalculator.moonTime(customDate.getTimeInMillis(),
                currentLatLng.latitude, currentLatLng.longitude, false);
        moonsetTV.setText(UIUtils.formatTime(moonSetTime));

        timezoneTV.setText(getString(R.string.all_times_in_utc));
        setLocalTimeZone(currentLatLng, sunriseTime, sunsetTime, moonRiseTime, moonSetTime);
    }

    @SuppressLint("MissingPermission")
    private void showMyLocationButton() {
        mMap.setMyLocationEnabled(true);

        View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).
                getParent()).findViewById(Integer.parseInt("2"));
        locationButton.setVisibility(View.GONE);

        getCurrentLocation();
    }

    public void onMapSearch(String location) {
        List<Address> addressList;
        Geocoder geocoder = new Geocoder(this);
        try {
            addressList = geocoder.getFromLocationName(location, 1);
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            handleNewLocation(latLng);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            Toast.makeText(this, R.string.location_search_failure, Toast.LENGTH_SHORT).show();
        }
    }

    private void turnGPSOn() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> getCurrentLocation());

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MapsActivity.this,
                            REQUEST_LOCATION_CODE);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore this error.
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations, this can be null.
                    if (location != null) {
                        handleNewLocation(
                                new LatLng(location.getLatitude(), location.getLongitude()));
                        SharedPrefHelper.setSharedPreferenceString(this,
                                SharedPrefHelper.LATITUDE_PREF_KEY, String.valueOf(location.getLatitude()));
                        SharedPrefHelper.setSharedPreferenceString(this,
                                SharedPrefHelper.LONGITUDE_PREF_KEY, String.valueOf(location.getLongitude()));
                        setNotification();
                    } else {
                        turnGPSOn();
                    }
                });
    }

    private void setLocalTimeZone(LatLng latLng, final double sunrise, final double sunset,
                                  final double moonrise, final double moonset) {
        new TimeZoneCalculator(getString(R.string.GOOGLE_MAPS_KEY)).calculateTimeZone(latLng,
                new PendingResult.Callback<TimeZone>() {
                    @Override
                    public void onResult(TimeZone result) {
                        Log.d(TAG, "Location offset in millis: " + result.getRawOffset());
                        runOnUiThread(() -> {
                            if (mMap.getCameraPosition().target.latitude != latLng.latitude &&
                                    mMap.getCameraPosition().target.longitude != latLng.longitude) {
                                return;
                            }
                            long minutes = TimeUnit.MILLISECONDS.toMinutes(result.getRawOffset());
                            Log.d(TAG, "Location offset in mins: " + minutes);
                            double offset = (double) minutes / 60;
                            Log.d(TAG, "Location offset in hours: " + offset);

                            sunriseTV.setText(UIUtils.formatTime(sunrise + offset));
                            sunsetTV.setText(UIUtils.formatTime(sunset + offset));
                            moonriseTV.setText(UIUtils.formatTime(moonrise + offset));
                            moonsetTV.setText(UIUtils.formatTime(moonset + offset));

                            String timezone = "All times in GMT " + ((offset < 0) ? "" : "+") +
                                    minutes / 60 + ":" +
                                    (minutes % 60 < 10 ? "0" + minutes % 60 : minutes % 60);
                            timezoneTV.setText(timezone);
                        });
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.e(TAG, e.toString());
                    }
                });
    }

    public void setNotification() {
        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        notificationIntent.addCategory("android.intent.category.DEFAULT");
        PendingIntent broadcast = PendingIntent.getBroadcast(this,
                AlarmReceiver.REQUEST_CODE, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (alarmManager != null) {
            alarmManager.cancel(broadcast);
        }

        double latitude = Double.parseDouble(SharedPrefHelper.getSharedPreferenceString(
                this, SharedPrefHelper.LATITUDE_PREF_KEY, "-1000"));
        double longitude = Double.parseDouble(SharedPrefHelper.getSharedPreferenceString(
                this, SharedPrefHelper.LONGITUDE_PREF_KEY, "-1000"));

        double sunsetTime = new SunTimeCalculator().phaseTimeCalculator(System.currentTimeMillis(),
                latitude, longitude, false) - 1;

        Intent sendNotifIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        sendNotifIntent.addCategory("android.intent.category.DEFAULT");
        PendingIntent sendBroadcast = PendingIntent.getBroadcast(this,
                AlarmReceiver.REQUEST_CODE, sendNotifIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int GMTOffset = mTimeZone.getRawOffset();

        long minutes = TimeUnit.MILLISECONDS.toMinutes(GMTOffset);
        double offset = (double) minutes / 60;
        double timeInHours = sunsetTime + offset;
        if (timeInHours >= 24) {
            timeInHours -= 24;
        } else if (timeInHours < 0) {
            timeInHours += 24;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.set(Calendar.HOUR_OF_DAY, (int) timeInHours);
        c.set(Calendar.MINUTE, (int) ((timeInHours - ((int) timeInHours)) * 60));
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        if (alarmManager != null) {
            Log.d(TAG, "Golden Hour notification set for " + c.getTime().toString());
            alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sendBroadcast);
        }
    }
}