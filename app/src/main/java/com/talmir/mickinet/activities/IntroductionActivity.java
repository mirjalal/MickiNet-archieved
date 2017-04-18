package com.talmir.mickinet.activities;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.talmir.mickinet.R;
import com.talmir.mickinet.fragments.SlideFragment;

/**
 * @author mirjalal
 * @created 12/2/2016.
 */

public class IntroductionActivity extends AppIntro {

    private static Fragment currentFragment;

    /** ++++++++++++++++++++++++++++++++ Permissions ++++++++++++++++++++++++++++++++++++ */
    private static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA
    };
    private static final String[] STORAGE_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int INITIAL_REQUEST = 0x4e;
    private static final int CAMERA_REQUEST = INITIAL_REQUEST + 1;
    private static final int STORAGE_REQUEST = INITIAL_REQUEST + 2;

    private boolean canAccessCamera() {
        return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean canReadExternalStorage() {
        return (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE));
    }

    private boolean canWriteExternalStorage() {
        return (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE));
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(permission));
        return true;
    }
    /** -------------------------------- Permissions ------------------------------------ */


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showStatusBar(false);
        showSkipButton(false);

        addSlide(SlideFragment.newInstance(R.layout.slide1));
        addSlide(SlideFragment.newInstance(R.layout.slide2));
        addSlide(AppIntroFragment.newInstance("Storage permission required", "This permission required to receive and send files.", R.drawable.ic_storage_white_72dp, ContextCompat.getColor(this, R.color.colorSlide2)));
        addSlide(AppIntroFragment.newInstance("We need camera permission", "Give us permission to share your media instantly.", R.drawable.ic_photo_camera_white_72dp, ContextCompat.getColor(this, R.color.colorSlide3)));
        addSlide(SlideFragment.newInstance(R.layout.slide3));
        setDepthAnimation();
//        setFlowAnimation();
//        setSlideOverAnimation();
//        setZoomAnimation();
//        askForPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
//        askForPermissions(new String[]{Manifest.permission.CAMERA}, 4);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("loseVirginity?", Boolean.TRUE);
        edit.apply();
        finish();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        currentFragment = newFragment;
        if (newFragment != null) {
            if (newFragment.getTag().endsWith(":2"))
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                   if (!canReadExternalStorage() || !canWriteExternalStorage())
                       requestPermissions(STORAGE_PERMISSIONS, STORAGE_REQUEST);
            }
            if (newFragment.getTag().endsWith(":3"))
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    if (!canAccessCamera())
                        requestPermissions(CAMERA_PERMISSIONS, CAMERA_REQUEST);

            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    private void LoadFragments(boolean fromFirst)
    {

    }
}
