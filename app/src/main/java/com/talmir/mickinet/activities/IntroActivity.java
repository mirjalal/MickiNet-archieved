package com.talmir.mickinet.activities;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.talmir.mickinet.R;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.MessageButtonBehaviour;
import agency.tango.materialintroscreen.SlideFragmentBuilder;

public class IntroActivity extends MaterialIntroActivity {

    private static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA
    };
    private static final String[] STORAGE_PERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected synchronized void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableLastSlideAlphaExitTransition(true);
        getBackButtonTranslationWrapper().setEnterTranslation(View::setAlpha);

        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.colorAccent)
                        .buttonsColor(R.color.colorPrimaryDark)
                        .title(getString(R.string.slide1_title))
                        .description(getString(R.string.slide1_description))
                        .build(),
                new MessageButtonBehaviour(v -> showMessage(getString(R.string.slide1_snack_message)), getString(R.string.slide1_button_text)));

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.colorPrimaryRipple)
                .buttonsColor(R.color.colorAccent)
                .title(getString(R.string.slide2_title))
                .description(getString(R.string.slide2_description))
                .build());

        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.colorAccentDarkRipple)
                        .buttonsColor(R.color.colorPrimary)
                        .neededPermissions(STORAGE_PERMISSION)
//                        .title("We provide best tools")
//                        .description("EVER")
                        .title(getString(R.string.slide3_title))
                        .description(getString(R.string.slide3_description))
                        .build());

        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.colorAccentRipple)
                        .buttonsColor(R.color.colorPrimary)
                        .neededPermissions(CAMERA_PERMISSIONS)
                        .title(getString(R.string.slide4_title))
                        .description(getString(R.string.slide4_description))
                        .build());

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.colorAccent)
                .buttonsColor(R.color.colorPrimaryDark)
                .title(getString(R.string.slide5_title))
                .description(getString(R.string.slide5_description))
                .build());
    }

    @Override
    public void onFinish() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("firstTimeRun?", Boolean.FALSE);
        edit.apply();
        super.onFinish();
    }

    @Override
    public void showMessage(String message) {
        Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
            }
        }).show();
    }
}