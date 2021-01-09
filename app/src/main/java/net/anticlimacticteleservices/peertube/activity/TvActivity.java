package net.anticlimacticteleservices.peertube.activity;

import android.app.Activity;
import android.os.Bundle;

import net.anticlimacticteleservices.peertube.R;

public class TvActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv);
    }
}