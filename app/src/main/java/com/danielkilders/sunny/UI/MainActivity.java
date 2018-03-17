package com.danielkilders.sunny.UI;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.danielkilders.sunny.R;
import com.danielkilders.sunny.weather.Current;
import com.danielkilders.sunny.weather.Forecast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY = "8c490071fb00c1ea4c867e0b58cbefac";

    private double latitude;
    private double longitude;
    private String cityName = "At your location";
    private Boolean useCelsius = true;

    private Forecast mForecast;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;

    // TODO: make app prettier

    //Views
    private TextView mTemperatureLabel, mTimeLabel, mHumidityLabel, mPrecipLabel, mSummaryLabel, mLocationLabel, mToggleUnitButton;
    private ImageView mIconImageView, mRefreshImageView;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTemperatureLabel = findViewById(R.id.temperatureLabel);
        mTimeLabel = findViewById(R.id.timeLabel);
        mHumidityLabel = findViewById(R.id.humidityLabel);
        mPrecipLabel = findViewById(R.id.precipLabel);
        mSummaryLabel = findViewById(R.id.summaryLabel);
        mIconImageView = findViewById(R.id.iconImageView);
        mRefreshImageView = findViewById(R.id.refreshImageView);
        mProgressBar = findViewById(R.id.progressBar);
        mLocationLabel = findViewById(R.id.locationLabel);
        mToggleUnitButton = findViewById(R.id.toggleUnit);

        mProgressBar.setVisibility(View.INVISIBLE);

        boolean permissionsGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ;

        if (permissionsGranted) {
            getLocation();
            getForecast(latitude, longitude);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                }
            }
        });

        mToggleUnitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (useCelsius) {
                    mToggleUnitButton.setText("Use ºF");
                    updateDisplay();
                    useCelsius = false;
                } else {
                    mToggleUnitButton.setText("Use ºC");
                    updateDisplay();
                    useCelsius = true;
                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        getCity(latitude, longitude);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    /*
     * Get current location of the user
     */
    private void getLocation() {
        Log.v(TAG, "retrieving location");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
            return;
        }
        mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        getCity(latitude, longitude);

        Log.v(TAG, "longitude: " + longitude);
        Log.v(TAG, "latitude: " + latitude);


        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                0, this);
    }

    /*
     * Get current city of the user using latitude and longitude
     */
    private String getCity(double latitude, double longitude) {
        Log.v(TAG, "Retrieving city name...");
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            cityName = addresses.get(0).getAddressLine(1);

            // replace all digits with empty string -> originally returned string also return zip code
            cityName = cityName.replaceAll("\\d","");

            Log.v(TAG, "Retrieved city name: " + cityName);
        } catch (IOException e) {
            Log.v(TAG, "Error getting city name", e);
            cityName = "At your location";
        }

        return cityName;
    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(jsonData));

        return forecast;
    }


    /*
     * Connect to dark sky api, retrieve data, updateUI
     */
    private void getForecast(double latitude, double longitude) {
        String forecastURL = "https://api.darksky.net/forecast/" + KEY + "/" + latitude + "," + longitude;

        if (isNetworkAvailable()) {
            toggleRefresh();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastURL)
                    .build();

            Call call = client.newCall(request);

            // handle request asynchronously
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });

                    alertUserAboutError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });

                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);

                        if (response.isSuccessful()) {
                            mForecast = parseForecastDetails(jsonData);

                            // Run this in the main thread
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                    catch (JSONException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });

        } else {
            Toast.makeText(this, "Network unavailable :(", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Toggle visibility of progressbar and refresh button
     */
    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }

    }

    /*
     * Use Current object to update UI with most up-to-date values
     */
    private void updateDisplay() {
        Current current = mForecast.getCurrent();
        mTemperatureLabel.setText(current.getTemperature() + "");
        if (useCelsius) {
            mTemperatureLabel.setText(current.getCelsiusTemperature() + "");
        }
        mTimeLabel.setText("At " + current.getFormattedTime() + " it will be");
        mHumidityLabel.setText(current.getHumidity() + "%");
        mPrecipLabel.setText(current.getPrecipChance() + "%");
        mSummaryLabel.setText(current.getSummary());
        mLocationLabel.setText(cityName);

        Drawable drawable = getResources().getDrawable(current.getIconId());
        mIconImageView.setImageDrawable(drawable);

    }

    /*
     * Use JSON to create a Current object, fill it with required information and return it
     */
    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject currently = forecast.getJSONObject("currently");

        Current current = new Current();
        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setTimeZone(timezone);

        return current;
    }

    /*
     * Check if the phone has internet connection
     */
    private boolean isNetworkAvailable() {
        boolean isAvailable = false;

        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    /*
     * Inform user about connection issue
     */
    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }
}
