package com.talmir.mickinet.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.View;

import com.talmir.mickinet.R;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.MessageButtonBehaviour;
import agency.tango.materialintroscreen.SlideFragmentBuilder;

public class IntroActivity extends MaterialIntroActivity {

    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private static final String WRITE_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String[] CAMERA_PERMISSIONS = {
            CAMERA_PERMISSION
    };
    private static final String[] STORAGE_PERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private boolean canAccessCamera() {
        return (hasPermission(CAMERA_PERMISSION));
    }
    private boolean canWriteExternalStorage() {
        return (hasPermission(WRITE_STORAGE_PERMISSION));
    }
    private boolean hasPermission(String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (PackageManager.PERMISSION_GRANTED == checkSelfPermission(permission));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (!prefs.getBoolean("firstTimeRun?", Boolean.TRUE)) {
//            if (canAccessCamera() && canWriteExternalStorage()) {
                super.onCreate(savedInstanceState);
                startActivity(new Intent(getApplicationContext(), SplashScreenActivity.class));
                super.onFinish();
//            } else {
//                super.onCreate(savedInstanceState);
//                enableLastSlideAlphaExitTransition(true);
//                getBackButtonTranslationWrapper().setEnterTranslation(View::setAlpha);
//                if (!canWriteExternalStorage()) {
//                    addSlide(new SlideFragmentBuilder()
//                                    .backgroundColor(R.color.colorAccentDarkRipple)
//                                    .buttonsColor(R.color.colorPrimary)
//                                    .neededPermissions(STORAGE_PERMISSION)
//                                    .title("We provide best tools")
//                                    .description("ever")
//                                    .build()
//                            /*new MessageButtonBehaviour(v -> showMessage("Try us!"), "Tools")*/);
//                }
//                if (!canAccessCamera()) {
//                    addSlide(new SlideFragmentBuilder()
//                                    .backgroundColor(R.color.colorAccentDarkRipple)
//                                    .buttonsColor(R.color.colorPrimary)
//                                    .neededPermissions(CAMERA_PERMISSIONS)
//                                    .title("We provide best tools")
//                                    .description("ever")
//                                    .build()
//                            /*new MessageButtonBehaviour(v -> showMessage("Try us!"), "Tools")*/);
//                }
//            }
        } else {
            super.onCreate(savedInstanceState);
            enableLastSlideAlphaExitTransition(true);
            getBackButtonTranslationWrapper().setEnterTranslation(View::setAlpha);

            addSlide(new SlideFragmentBuilder()
                    .backgroundColor(R.color.colorAccent)
                    .buttonsColor(R.color.colorPrimaryDark)
                    .title("Organize your time with us")
                    .description("Would you try?")
                    .build(),
                    new MessageButtonBehaviour(v -> showMessage("We provide solutions to make you love your work"), "Work with love"));

            addSlide(new SlideFragmentBuilder()
                    .backgroundColor(R.color.colorPrimary)
                    .buttonsColor(R.color.colorAccent)
                    .title("Want more?")
                    .description("Go on")
                    .build());

            addSlide(new SlideFragmentBuilder()
                    .backgroundColor(R.color.colorAccentDarkRipple)
                    .buttonsColor(R.color.colorPrimary)
                    .neededPermissions(CAMERA_PERMISSIONS)
                    .title("We provide best tools")
                    .description("ever")
                    .build()
                    /*new MessageButtonBehaviour(v -> showMessage("Try us!"), "Tools")*/);

            addSlide(new SlideFragmentBuilder()
                            .backgroundColor(R.color.colorAccentDarkRipple)
                            .buttonsColor(R.color.colorPrimary)
                            .neededPermissions(STORAGE_PERMISSION)
                            .title("We provide best tools")
                            .description("ever")
                            .build()
                    /*new MessageButtonBehaviour(v -> showMessage("Try us!"), "Tools")*/);

            addSlide(new SlideFragmentBuilder()
                    .backgroundColor(R.color.colorAccent)
                    .buttonsColor(R.color.colorPrimaryDark)
                    .title("That's it")
                    .description("Would you join us?")
                    .build());
        }
    }

    @Override
    public void onFinish() {
        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("firstTimeRun?", Boolean.FALSE);
        edit.apply();
        super.onFinish();
    }
}