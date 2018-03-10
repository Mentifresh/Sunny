package com.danielkilders.sunny;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(forecastURL)
                .build();

        Call call = client.newCall(request);
        
        // make sure it's asynchronous
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    Log.v(TAG, response.body().string());
                    if (response.isSuccessful()) {
                        
                    } else {
                        alertUserAboutError();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Exception caught: ", e );
                }
            }
        });


    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }
}
