package com.talmir.mickinet.helpers.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.talmir.mickinet.R;

/**
 * Created by pantrif on 28/4/2016.
 */
public class SplashScreen {

    private Activity mActivity;
    private ImageView logo_iv;
    private TextView header_tv;
    private TextView footer_tv;
    private TextView before_logo_tv;
    private TextView after_logo_tv;
    private RelativeLayout splash_wrapper_rl;
    private Bundle bundle = null;
    private View mView;
    private Class<? extends Activity> TargetActivity = null;
    private int SPLASH_TIME_OUT = 2000; //The time before launch target Activity - by default 2 seconds

    public SplashScreen(Activity activity) {
        this.mActivity = activity;
        LayoutInflater mInflater = LayoutInflater.from(activity);
        this.mView = mInflater.inflate(R.layout.splash, null);
        this.splash_wrapper_rl = mView.findViewById(R.id.splash_wrapper_rl);

    }

    public SplashScreen withFullScreen() {
        mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        return this;
    }

    public SplashScreen withTargetActivity(Class<? extends Activity> tAct) {
        this.TargetActivity = tAct;
        return this;
    }

    public SplashScreen withSplashTimeOut(int timeout) {
        this.SPLASH_TIME_OUT = timeout;
        return this;
    }

    public SplashScreen withBundleExtras(Bundle bundle) {
        this.bundle = bundle;
        return this;
    }

    public SplashScreen withBackgroundColor(int color) {
        splash_wrapper_rl.setBackgroundColor(color);
        return this;
    }

    public SplashScreen withBackgroundResource(int resource) {
        splash_wrapper_rl.setBackgroundResource(resource);
        return this;
    }

    public SplashScreen withLogo(int logo) {
        logo_iv = mView.findViewById(R.id.logo);
        logo_iv.setImageResource(logo);
        return this;
    }

    public SplashScreen withHeaderText(String text) {
        header_tv = mView.findViewById(R.id.header_tv);
        header_tv.setText(text);
        return this;
    }

    public SplashScreen withFooterText(String text) {
        footer_tv = mView.findViewById(R.id.footer_tv);
        footer_tv.setText(text);
        return this;
    }

    public SplashScreen withBeforeLogoText(String text) {
        before_logo_tv = mView.findViewById(R.id.before_logo_tv);
        before_logo_tv.setText(text);
        return this;
    }

    public SplashScreen withAfterLogoText(String text) {
        after_logo_tv = mView.findViewById(R.id.after_logo_tv);
        after_logo_tv.setText(text);
        return this;
    }

    public ImageView getLogo() {
        return logo_iv;
    }

    public TextView getBeforeLogoTextView() {
        return before_logo_tv;
    }

    public TextView getAfterLogoTextView() {
        return after_logo_tv;
    }

    public TextView getHeaderTextView() {
        return header_tv;
    }

    public TextView getFooterTextView() {
        return footer_tv;
    }

    public View create() {
        setUpHandler();
        return mView;
    }

    private void setUpHandler() {
        if (TargetActivity != null) {
            new Handler().postDelayed(() -> {
                Intent i = new Intent(mActivity, TargetActivity);
                if (bundle != null) {
                    i.putExtras(bundle);
                }
                mActivity.startActivity(i);
                // close splash
                mActivity.finish();
            }, SPLASH_TIME_OUT);
        }
    }
}