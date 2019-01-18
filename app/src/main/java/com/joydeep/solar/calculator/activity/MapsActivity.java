package com.joydeep.solar.calculator.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.joydeep.solar.calculator.R;
import com.joydeep.solar.calculator.model.CustomDate;
import com.joydeep.solar.calculator.util.PermissionHelper;
import com.joydeep.solar.calculator.util.PhaseTimeCalculator;
import com.joydeep.solar.calculator.util.UIUtils;

import java.util.List;

import static com.joydeep.solar.calculator.util.PermissionHelper.LOCATION_REQ_CODE;

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
    private LatLng currentLatLng = new LatLng(0, 0);

    private SupportMapFragment mapFragment;
    private EditText searchEditText;
    private ImageView mapGPSIcon, dateNextImage, datePreviousImage, dateResetImage;
    private ImageView mapMarkerImage;
    private TextView dateTV, sunriseTV, sunsetTV, moonriseTV, moonsetTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        searchEditText = findViewById(R.id.search_edit_text);
        mapMarkerImage = findViewById(R.id.image_map_marker);
        mapGPSIcon = findViewById(R.id.image_gps);
        dateTV = findViewById(R.id.tv_date);
        dateNextImage = findViewById(R.id.image_next_date);
        datePreviousImage = findViewById(R.id.image_previous_date);
        dateResetImage = findViewById(R.id.image_reset_date);
        sunriseTV = findViewById(R.id.tv_sunrise);
        sunsetTV = findViewById(R.id.tv_sunset);
        moonriseTV = findViewById(R.id.tv_moon_rise);
        moonsetTV = findViewById(R.id.tv_moon_set);

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

    private void setUpViews() {
        customDate = new CustomDate(this);

        UIUtils.setupClearButtonWithAction(searchEditText);

        searchEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onMapSearch(v.getText().toString());
                    UIUtils.hideKeyboard(getApplicationContext(), searchEditText);
                    return true;
                }
                return false;
            }
        });

        mapGPSIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new PermissionHelper().checkAndRequestPermissions(MapsActivity.this)) {
                    getCurrentLocation();
                }
            }
        });

        dateTV.setText(customDate.toString());

        dateNextImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateTV.setText(customDate.nextDay());
            }
        });

        datePreviousImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateTV.setText(customDate.previousDay());
            }
        });

        dateResetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateTV.setText(customDate.reset());
            }
        });
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed)
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
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
        currentLatLng = mMap.getCameraPosition().target;
        Log.d(TAG, currentLatLng.toString());
        PhaseTimeCalculator phaseTimeCalculator = new PhaseTimeCalculator();
        String sunriseTime = phaseTimeCalculator.phaseTimeCalculator(customDate.getTimeInMillis(), currentLatLng.latitude,
                currentLatLng.longitude, true);
        sunriseTV.setText(sunriseTime);
        String sunsetTime = phaseTimeCalculator.phaseTimeCalculator(customDate.getTimeInMillis(), currentLatLng.latitude,
                currentLatLng.longitude, false);
        sunsetTV.setText(sunsetTime);
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

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getCurrentLocation();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapsActivity.this,
                                REQUEST_LOCATION_CODE);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore this error.
                    }
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations, this can be null.
                        if (location != null) {
                            handleNewLocation(
                                    new LatLng(location.getLatitude(), location.getLongitude()));
                        } else {
                            turnGPSOn();
                        }
                    }
                });
    }


}