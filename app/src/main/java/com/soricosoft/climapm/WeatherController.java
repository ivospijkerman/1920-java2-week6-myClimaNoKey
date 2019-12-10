package com.soricosoft.climapm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity {

    // Constants:
    /**
     * This is a value to differentiate a request, this may be any value, as long as different
     * requests have different values
     */
    final int REQUEST_CODE = 123;
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "create your weather api key (app_Id)";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    // if set to 0 then updates are determined bij the MIN_TIME timeout
    // we assume the same weather within 2 Km
    final float MIN_DISTANCE = 2000;

    public static final String PERMISSION_STRING
            = android.Manifest.permission.ACCESS_FINE_LOCATION;

    final String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;


    // Member Variables:
    TextView cityLabel;
    ImageView weatherImage;
    TextView temperatureLabel;

    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        cityLabel = findViewById(R.id.locationTV);
        weatherImage = findViewById(R.id.weatherSymbolIV);
        temperatureLabel = findViewById(R.id.tempTV);
        ImageButton changeCityButton = findViewById(R.id.changeCityButton);


        changeCityButton.setOnClickListener(v -> {
            Intent myIntent = new Intent(WeatherController.this, ChangeCityController.class);
            startActivity(myIntent);
        });

        setupLocationServices();
        getWeatherForCurrentLocation();
    }

    //
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MyClima", "onResume() called");
        String newCity = getIntent().getStringExtra("City");

        if (newCity == null) {
            Log.d("MyClima", "Get weather for current location");
            getWeatherForCurrentLocation();
        } else {
            Log.d("MyClima", "Get weather for a new City");
            getWeatherForNewCity(newCity);
        }
    }


    private void getWeatherForNewCity(String newCity) {
        RequestParams params = new RequestParams();
        params.add("q", newCity);
        params.add("units", "metric");
        params.add("appid", APP_ID);

        callOpenWeatherAPI(params);
    }

    // TODO 004: Request a LocationManager (LocationManager as a System Service)
    private void setupLocationServices() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // TODO 005: Defining the location listener
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());
                String locationText = String.format("current location is %s long - %s lat", longitude, latitude);

                Log.d("MyClima", "locationListener: OnLocationChanged()");
                Log.d("MyClima", locationText);
//                cityLabel.setText(locationText);

                RequestParams params = new RequestParams();
                params.add("lon", longitude);
                params.add("lat", latitude);
                params.add("units", "metric");
                params.add("appid", APP_ID);

                callOpenWeatherAPI(params);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Wordt aangeroepen indien je tijdelijk geen gps meer hebt.
                Log.d("MyClima", "locationListener: OnStatusChanged()");
                String statusTxt = "";
                switch (status) {
                    case LocationProvider.OUT_OF_SERVICE:
                        statusTxt = "OUT_OF_SERVICE";
                        break;
                    case LocationProvider.AVAILABLE:
                        statusTxt = "AVAILABLE";
                        break;
                    // setting the minTime when requesting location updates will cause the provider to set itself to TEMPORARILY_UNAVAILABLE for minTime milliseconds in order to conserve battery power.
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        statusTxt = "TEMPORARILY_UNAVAILABLE";
                        break;

                }
                Log.d("MyClima", "status: " + statusTxt);
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("MyClima", "locationListener: OnProviderEnabled()");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("MyClima", "locationListener: OnProviderDisabled()");
            }
        };


    }

    private void getWeatherForCurrentLocation() {
        // TODO 002: Ask permission from user
        if (ContextCompat.checkSelfPermission(this, PERMISSION_STRING) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        } else {
            // TODO 006: Register for location updates
            locationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
        }
    }

    @Override
    // TODO 003: Handle result from the permission result
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MyClima", "onRequestPermissionsResult granted!");
                // Note the recursive call here
                getWeatherForCurrentLocation();
            } else {
                Log.d("MyClima", "onRequestPermissionsResult NOT granted!");
            }
        }
    }

    private void showToast(@NonNull final String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void callOpenWeatherAPI(RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                showToast("call to open weather api successful. Return JSONArray");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                super.onSuccess(statusCode, headers, responseString);
                showToast("call to open weather api successful. Return String!?");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                showToast("call to open weather api successful");
                Log.d("MyClima", "Call to open weather api successful. JSON: " + response.toString());
                WeatherDataModel weatherDataModel = Objects.requireNonNull(WeatherDataModel.fromJson(response));
                updateUI(weatherDataModel);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d("MyClima", "callOpenWeatherAPI, failed: " + errorResponse.toString());
                Log.d("MyClima", "callOpenWeatherAPI, statusCode: " + statusCode);
                showToast("call to open weather api failed");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                showToast("call to open weather api failed");
            }
        });
    }


    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void updateUI(WeatherDataModel weatherDataModel) {
        temperatureLabel.setText(String.format("%.1f", weatherDataModel.getTemperature()) + (char) 0x00B0);
        cityLabel.setText(weatherDataModel.getCityName());
        int resourceID = getResources().getIdentifier(weatherDataModel.getIconName(), "drawable", getPackageName());

        weatherImage.setImageResource(resourceID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}
