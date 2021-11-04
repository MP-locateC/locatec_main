package com.example.locatec;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;

public class ReportThirdPage extends Fragment {
    LottieAnimationView animationView;
    Button goHome, goFirst;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  (ViewGroup) inflater.inflate(
                R.layout.report_thirdpage, container, false);

        animationView = (LottieAnimationView) v.findViewById(R.id.lottie);
        animationView.setAnimation("completion.json");
        animationView.playAnimation();
        animationView.setRepeatCount(2);

        goHome = (Button) v.findViewById(R.id.goHome);
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ScreenSlidePagerActivity)ScreenSlidePagerActivity.viewPagerContext).goHome();
            }
        });
        goFirst = (Button) v.findViewById(R.id.goFirst);
        goFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ScreenSlidePagerActivity)ScreenSlidePagerActivity.viewPagerContext).goFirst();
            }
        });
        return v;
    }
}
