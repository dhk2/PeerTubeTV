package net.anticlimacticteleservices.peertube.activity;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import net.anticlimacticteleservices.peertube.R;

public class TvActivity extends Activity {
    String TAG = "TvActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean forceTV = sharedPref.getBoolean(getString(R.string.pref_force_tv_key), false);

        UiModeManager uiMode = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if(uiMode.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            Log.e(TAG, "tv verified");
        }
         else if (forceTV) {
            Log.e(TAG, "tv forced");
        }
         else {
            Log.e(TAG, "not on tv");
            //then start phone
            Intent intentSettings = new Intent(this, VideoListActivity.class);
            this.startActivity(intentSettings);
            this.releaseInstance();
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv);
    }
}