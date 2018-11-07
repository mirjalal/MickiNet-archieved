package com.talmir.mickinet.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.PermissionUtils;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.MessageButtonBehaviour;
import agency.tango.materialintroscreen.SlideFragmentBuilder;

public class IntroActivity extends MaterialIntroActivity {
    @Override
    protected synchronized void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setTheme(R.style.AppTheme1); // fixs bugs in first time run
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
                        .neededPermissions(PermissionUtils.STORAGE_PERMISSION)
//                        .title("We provide best tools")
//                        .description("EVER")
                        .title(getString(R.string.slide3_title))
                        .description(getString(R.string.slide3_description))
                        .build());

        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.colorAccentRipple)
                        .buttonsColor(R.color.colorPrimary)
                        .neededPermissions(PermissionUtils.CAMERA_PERMISSIONS)
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