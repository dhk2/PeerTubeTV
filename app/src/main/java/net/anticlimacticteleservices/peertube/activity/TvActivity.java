package net.anticlimacticteleservices.peertube.activity;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import net.anticlimacticteleservices.peertube.R;

public class TvActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        UiModeManager uiMode = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if(uiMode.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            Log.e("wtf", "tv verified");
        }
         else {
            Log.e("wtf", "not on tv");
             //then start phone
            Intent intentSettings = new Intent(this, VideoListActivity.class);
            this.startActivity(intentSettings);
            this.releaseInstance();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv);
    }
}