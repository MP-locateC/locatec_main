package com.example.locatec;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SplashActivity extends AppCompatActivity {
    private static final String Tag = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startLoading();
    }

    // 초기 로딩 화면에서 서버로부터 데이터 받아와서, main activity로 넘기기
    private void startLoading() {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(getString(R.string.server_url) + "/product/find/registered").build();
            client.newCall(request).enqueue(new Callback() {
                //비동기 처리를 위해 Callback 구현
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("에러", e.toString());
                    startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                }

                // 성공시
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Intent mainActivity = new Intent(getApplicationContext(), MapsActivity.class);
                    mainActivity.putExtra("markerDatas", response.body().string());
                    startActivity(mainActivity);
                }
            });
        }).start();
    }
}
