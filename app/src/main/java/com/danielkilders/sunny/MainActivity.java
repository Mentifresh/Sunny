package com.danielkilders.sunny;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY = "8c490071fb00c1ea4c867e0b58cbefac";

    private double latitude =  37.8267;
    private double longitude = -122.4233;
    private String forecastURL = "https://api.darksky.net/forecast/" + KEY + "/" + latitude + "," + longitude;

    private CurrentWeather mCurrentWeather;

    //Views
    private TextView mTemperatureLabel, mTimeLabel, mHumidityLabel, mPrecipLabel, mSummaryLabel, mLocationLabel;
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

        mProgressBar.setVisibility(View.INVISIBLE);

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getForecast();
            }
        });

        getForecast();
    }

    /*
     * Connect to dark sky api, retrieve data, updateUI
     */
    private void getForecast() {
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
                            mCurrentWeather = getCurrentDetails(jsonData);

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
     * Use CurrentWeather object to update UI with most up-to-date values
     */
    private void updateDisplay() {
        mTemperatureLabel.setText(mCurrentWeather.getTemperature() + "");
        mTimeLabel.setText("At " + mCurrentWeather.getFormattedTime() + " it will be");
        mHumidityLabel.setText(mCurrentWeather.getHumidity() + "%");
        mPrecipLabel.setText(mCurrentWeather.getPrecipChance() + "%");
        mSummaryLabel.setText(mCurrentWeather.getSummary());

        Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(drawable);

    }

    /*
     * Use JSON to create a CurrentWeather object, fill it with required information and return it
     */
    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject currently = forecast.getJSONObject("currently");

        mCurrentWeather = new CurrentWeather();
        mCurrentWeather.setHumidity(currently.getDouble("humidity"));
        mCurrentWeather.setTime(currently.getLong("time"));
        mCurrentWeather.setIcon(currently.getString("icon"));
        mCurrentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        mCurrentWeather.setSummary(currently.getString("summary"));
        mCurrentWeather.setTemperature(currently.getDouble("temperature"));
        mCurrentWeather.setTimeZone(timezone);

        return mCurrentWeather;
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
