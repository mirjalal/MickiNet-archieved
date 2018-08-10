package com.talmir.mickinet.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.ui.SplashScreen;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SplashScreen splashScreenConfig = new SplashScreen(SplashScreenActivity.this)
                .withFullScreen()
                .withTargetActivity(HomeActivity.class)
//                .withSplashTimeOut(2000)
                .withBackgroundColor(getResources().getColor(R.color.white));
//                .withAfterLogoText(getResources().getString(R.string.app_name));

//        splashScreenConfig.getAfterLogoTextView().setTextColor(getResources().getColor(R.color.white));
//        splashScreenConfig.getAfterLogoTextView().setTextSize(20.0f);

        setContentView(splashScreenConfig.create());
    }
}
