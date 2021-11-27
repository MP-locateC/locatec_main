package com.example.locatec;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ScreenSlidePagerActivity extends FragmentActivity {
    public static Context viewPagerContext;
    private static final int NUM_PAGES = 3;
    public ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    ReportFirstPage firstPage;
    ReportSecondPage secondPage;
    ReportThirdPage thirdPage;
    Button goBack;
    FrameLayout loadingOvarlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_page);
        loadingOvarlay = (FrameLayout) findViewById(R.id.loading_overlay);
        loadingOvarlay.bringToFront();

        goBack = (Button) findViewById(R.id.goBack);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setUserInputEnabled(false);
        viewPagerContext = this;
    }

    // 다음 스크린으로 이동
    public void goNext() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }
    // 첫 스크린으로 이동
    public void goFirst() {
        viewPager.setCurrentItem(0);
        secondPage.userAddImage.setImageResource(android.R.color.transparent);
        secondPage.isAddingImage = false;
        secondPage.imageRadioGroup.check(R.id.removeImageButton);
    }
    // 홈페이지로 이동
    public void goHome() {
        finish();
    }

    // set loading visible
    public void setLoadingVisible() {
        loadingOvarlay.setVisibility(View.VISIBLE);
    }
    // set loading invisible
    public void setLoadingInvisible() {
        loadingOvarlay.setVisibility(View.INVISIBLE);
    }

    // 서버로 제출
    public void submit() {
        setLoadingVisible();
        new Thread(() -> {
            try {

                JSONObject body = new JSONObject();
                String type = secondPage.getMenuText();
                if(type == "흡연구역") {
                    body.put("type", "smoking");
                } else {
                    body.put("type", "trash");
                }
                body.put("latitude", firstPage.pickCoord.latitude);
                body.put("longitude", firstPage.pickCoord.longitude);

                // 이미지가 제대로 들어있으면 base64로 인코딩해서 body에 추가
                if(secondPage.isAddingImage) {
                    secondPage.userAddImage.buildDrawingCache();
                    Bitmap bmap = secondPage.userAddImage.getDrawingCache();
                    body.put("image", toBase64(bmap));
                } else {
                    body.put("image", null);
                }

                // 요청을 위한 request 생성
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                Request request = new Request.Builder().url(getString(R.string.server_url) + "/product/register/request")
                        .post(RequestBody.create(JSON, body.toString())).build();

                Handler handler = new Handler(Looper.getMainLooper());
                client.newCall(request).enqueue(new Callback() {
                    //비동기 처리를 위해 Callback 구현
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("error : ",  e.toString());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run()
                            {
                                setLoadingInvisible();
                                Toast.makeText(getApplicationContext(), "네트워크에러로 요청에 실패했습니다.\n잠시후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                            }
                        }, 0);
                    }

                    // 성공 시 
                    @Override
                    public void onResponse(Call call, Response response)  throws IOException {
                        try {
                            JSONObject jsonObj = new JSONObject(response.body().string());
                            if (jsonObj.getInt("status") != 200) {
                                throw new Exception("failed with status over 400");
                            }
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    goNext();
                                    setLoadingInvisible();
                                }
                            }, 0);
                        } catch (Exception e) {
                            if (e.getMessage() == "failed with status over 400") {
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        setLoadingInvisible();
                                        Toast.makeText(ScreenSlidePagerActivity.this, "어플리케이션 오류로 요청에 실패했습니다.\n관리자에게 수정을 요청하세요.", Toast.LENGTH_SHORT).show();
                                    }
                                }, 0);
                            } else {

                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        goNext();
                                        setLoadingInvisible();
                                    }
                                }, 0);
                            }
                        }
                    }
                });
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(), "실패했습니다.", Toast.LENGTH_SHORT).show();
                System.err.println(e);
            }
        }).start();
    }

    // bitmap 이미지를 base64로 바꿔줌.
    public String toBase64(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();

        return Base64.encodeToString(b, Base64.NO_WRAP);
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    firstPage = new ReportFirstPage();
                    return firstPage;
                case 1:
                    secondPage = new ReportSecondPage();
                    return secondPage;
                case 2:
                    thirdPage = new ReportThirdPage();
                    return thirdPage;
            }
            return new ReportSecondPage();
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
}