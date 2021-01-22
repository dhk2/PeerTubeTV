/*
 * Copyright 2018 Stefan Schüller <sschueller@techdroid.com>
 *
 * License: GPL-3.0+
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package net.anticlimacticteleservices.peertube.activity;


import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;

import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.util.Rational;
import android.util.TypedValue;

import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.widget.FrameLayout;

import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import net.anticlimacticteleservices.peertube.R;
import net.anticlimacticteleservices.peertube.application.AppApplication;
import net.anticlimacticteleservices.peertube.fragment.VideoMetaDataFragment;
import net.anticlimacticteleservices.peertube.fragment.VideoPlayerFragment;
import net.anticlimacticteleservices.peertube.fragment.WebviewFragment;
import net.anticlimacticteleservices.peertube.service.VideoPlayerService;


import java.util.ArrayList;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import static com.google.android.exoplayer2.ui.PlayerNotificationManager.ACTION_PAUSE;
import static com.google.android.exoplayer2.ui.PlayerNotificationManager.ACTION_PLAY;
import static com.google.android.exoplayer2.ui.PlayerNotificationManager.ACTION_STOP;
import static net.anticlimacticteleservices.peertube.application.AppApplication.getContext;
import static net.anticlimacticteleservices.peertube.helper.VideoHelper.canEnterPipMode;

public class VideoPlayActivity extends AppCompatActivity implements  PopupMenu.OnMenuItemClickListener {

    private static final String TAG = "VideoPlayActivity";
    WebviewFragment webviewFragment;
    static boolean floatMode = false;

    private static final int REQUEST_CODE = 101;
    private BroadcastReceiver receiver;
    boolean test=true;
    boolean playing =false;
    boolean menu=false;
    String speed="1";
    VideoPlayerFragment videoPlayerFragment;
    VideoMetaDataFragment videoMetaDataFragment;
    //This can only be called when in entering pip mode which can't happen if the device doesn't support pip mode.
    @SuppressLint("NewApi")
    public void makePipControls() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        videoPlayerFragment = (VideoPlayerFragment) fragmentManager.findFragmentById(R.id.video_player_fragment);

        ArrayList<RemoteAction> actions = new ArrayList<>();

        Intent actionIntent = new Intent(getString(R.string.app_background_audio));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_CODE, actionIntent, 0);
        @SuppressLint({"NewApi", "LocalSuppress"}) Icon icon = Icon.createWithResource(getApplicationContext(), android.R.drawable.stat_sys_speakerphone);
        @SuppressLint({"NewApi", "LocalSuppress"}) RemoteAction remoteAction = new RemoteAction(icon, "close pip", "from pip window custom command", pendingIntent);
        actions.add(remoteAction);

        actionIntent = new Intent(ACTION_STOP);
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_CODE, actionIntent, 0);
        icon = Icon.createWithResource(getApplicationContext(), com.google.android.exoplayer2.ui.R.drawable.exo_notification_stop);
        remoteAction = new RemoteAction(icon, "play", "stop the media", pendingIntent);
        actions.add(remoteAction);

        assert videoPlayerFragment != null;
        if (videoPlayerFragment.isPaused()) {
            Log.e(TAG, "setting actions with play button");
            actionIntent = new Intent(ACTION_PLAY);
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_CODE, actionIntent, 0);
            icon = Icon.createWithResource(getApplicationContext(), com.google.android.exoplayer2.ui.R.drawable.exo_notification_play);
            remoteAction = new RemoteAction(icon, "play", "play the media", pendingIntent);
        } else {
            Log.e(TAG, "setting actions with pause button");
            actionIntent = new Intent(ACTION_PAUSE);
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_CODE, actionIntent, 0);
            icon = Icon.createWithResource(getApplicationContext(), com.google.android.exoplayer2.ui.R.drawable.exo_notification_pause);
            remoteAction = new RemoteAction(icon, "pause", "pause the media", pendingIntent);
        }
        actions.add(remoteAction);


        //add custom actions to pip window
        PictureInPictureParams params =
                new PictureInPictureParams.Builder()
                        .setActions(actions)
                        .build();
        setPictureInPictureParams(params);
    }

    public void changedToPipMode() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        videoPlayerFragment = (VideoPlayerFragment) fragmentManager.findFragmentById(R.id.video_player_fragment);

        assert videoPlayerFragment != null;
        videoPlayerFragment.showControls(false);
        //create custom actions
        makePipControls();

        //setup receiver to handle customer actions
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STOP);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_PLAY);
        filter.addAction((getString(R.string.app_background_audio)));
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                assert action != null;
                if (action.equals(ACTION_PAUSE)) {
                    videoPlayerFragment.pauseVideo();
                    makePipControls();
                }
                if (action.equals(ACTION_PLAY)) {
                    videoPlayerFragment.unPauseVideo();
                    makePipControls();
                }

                if (action.equals(getString(R.string.app_background_audio))) {
                    unregisterReceiver(receiver);
                    finish();
                }
                if (action.equals(ACTION_STOP)) {
                    unregisterReceiver(receiver);
                    finishAndRemoveTask();
                }
            }
        };
        registerReceiver(receiver, filter);

        Log.v(TAG, "switched to pip ");
        floatMode = true;
        videoPlayerFragment.showControls(false);
    }

    public void changedToNormalMode() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        videoPlayerFragment = (VideoPlayerFragment) fragmentManager.findFragmentById(R.id.video_player_fragment);

        assert videoPlayerFragment != null;
        videoPlayerFragment.showControls(true);
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        Log.v(TAG, "switched to normal");
        floatMode = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set theme
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(getResources().getIdentifier(
                sharedPref.getString(
                        getString(R.string.pref_theme_key),
                        getString(R.string.app_default_theme)
                ),
                "style",
                getPackageName())
        );

        if (sharedPref.getBoolean(getString(R.string.pref_webview_player_key),false)){
            setContentView(R.layout.activity_video_play_webview);
            Log.e("WTF","using full screen webview");
        }
        else {
            setContentView(R.layout.activity_video_play);
            Log.e("WTF","using normal activiy view ");
        }
        // get video ID
        Intent intent = getIntent();
        String videoUuid = intent.getStringExtra(VideoListActivity.EXTRA_VIDEOID);
        videoPlayerFragment=null;
        WebviewFragment webviewFragment=null;
        if (sharedPref.getBoolean(getString(R.string.pref_webview_player_key),false)){
            webviewFragment = (WebviewFragment) getSupportFragmentManager().findFragmentById(R.id.webview_fragment);
            assert webviewFragment !=null;
        }
        else {
            videoPlayerFragment = (VideoPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.video_player_fragment);
            assert videoPlayerFragment != null;
        }
        String playingVideo;
        Log.v(TAG,"attempting to play "+videoUuid);
        if (sharedPref.getBoolean(getString(R.string.pref_webview_player_key),false)){
            playingVideo = WebviewFragment.getVideoUuid();
        } else {
            playingVideo = videoPlayerFragment.getVideoUuid();
        }
        Log.v(TAG, "oncreate click: " + videoUuid + " is trying to replace: " + playingVideo);
        if (sharedPref.getBoolean(getString(R.string.pref_webview_player_key),false)){
            webviewFragment.start(videoUuid);
        }
        else {
            if (TextUtils.isEmpty(playingVideo)) {
                Log.v(TAG, "oncreate no video currently playing");
                videoPlayerFragment.start(videoUuid);
            } else if (!playingVideo.equals(videoUuid)) {
                Log.v(TAG, "oncreate different video playing currently");
                videoPlayerFragment.stopVideo();
                videoPlayerFragment.start(videoUuid);
            } else {
                Log.v(TAG, "oncreate same video playing currently");
            }
        }
        // if we are in landscape set the video to fullscreen
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setOrientation(true);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        videoPlayerFragment = null;
        webviewFragment =null;
        String playingVideo="";
        String videoUuid="";
        videoUuid = intent.getStringExtra(VideoListActivity.EXTRA_VIDEOID);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (sharedPref.getBoolean(getString(R.string.pref_webview_player_key),false)){
            webviewFragment = (WebviewFragment) getSupportFragmentManager().findFragmentById(R.id.webview_fragment);
            assert webviewFragment != null;
            playingVideo = webviewFragment.getVideoUuid();
        }
        else {
            videoPlayerFragment = (VideoPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.video_player_fragment);
            assert videoPlayerFragment != null;
            playingVideo = videoPlayerFragment.getVideoUuid();
        }
        Log.v(TAG, "new intent click: " + videoUuid + " is trying to replace: " + playingVideo);


        if (sharedPref.getBoolean(getString(R.string.pref_webview_player_key),false)){
                webviewFragment.start(videoUuid);
        }
        else {
            if (TextUtils.isEmpty(playingVideo)) {
                Log.v(TAG, "new intent no video currently playing");
                videoPlayerFragment.start(videoUuid);
            } else if (!playingVideo.equals(videoUuid)) {
                Log.v(TAG, "new intent different video playing currently");
                videoPlayerFragment.stopVideo();
                videoPlayerFragment.start(videoUuid);
            } else {
                Log.v(TAG, "new intent same video playing currently");
            }
        }

        // if we are in landscape set the video to fullscreen
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setOrientation(true);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        Log.v(TAG, "onConfigurationChanged()...");

        super.onConfigurationChanged(newConfig);

        // Checking the orientation changes of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setOrientation(true);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setOrientation(false);
        }
    }

    private void setOrientation(Boolean isLandscape) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        RelativeLayout.LayoutParams params;

        if (sharedPref.getBoolean(getString(R.string.pref_webview_player_key),false)){
            webviewFragment = (WebviewFragment) fragmentManager.findFragmentById(R.id.webview_fragment);
            assert webviewFragment !=null;
            params = (RelativeLayout.LayoutParams) webviewFragment.requireView().getLayoutParams();
        } else {
            videoPlayerFragment = (VideoPlayerFragment) fragmentManager.findFragmentById(R.id.video_player_fragment);
            assert videoPlayerFragment != null;
            params = (RelativeLayout.LayoutParams) videoPlayerFragment.requireView().getLayoutParams();

        }
        params.width = FrameLayout.LayoutParams.MATCH_PARENT;
        params.height = isLandscape ? FrameLayout.LayoutParams.MATCH_PARENT : (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, getResources().getDisplayMetrics());

        if (sharedPref.getBoolean(getString(R.string.pref_webview_player_key),false)){
            webviewFragment.requireView().setLayoutParams(params);
        } else {
            videoPlayerFragment.requireView().setLayoutParams(params);
        }

        if (videoMetaDataFragment != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

            if (isLandscape) {
                transaction.hide(videoMetaDataFragment);
            } else {
                transaction.show(videoMetaDataFragment);
            }

            transaction.commit();
        }

    }

    @Override
    protected void onDestroy() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (sharedPref.getBoolean(getString(R.string.pref_webview_player_key),false)){
            Log.v(TAG,"destroy webview here");
        }
        else {
            videoPlayerFragment = (VideoPlayerFragment)
                    getSupportFragmentManager().findFragmentById(R.id.video_player_fragment);

            assert videoPlayerFragment != null;
            videoPlayerFragment.destroyVideo();
        }
        super.onDestroy();
        Log.v(TAG, "onDestroy...");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()...");
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (sharedPref.getBoolean(getString(R.string.pref_webview_player_key),false)){
            Log.v(TAG,"should stop webview");

          //  AppApplication.addSeed(webviewFragment.getSeed());
        }
        else {
            videoPlayerFragment = (VideoPlayerFragment)
                    getSupportFragmentManager().findFragmentById(R.id.video_player_fragment);

            assert videoPlayerFragment != null;
            videoPlayerFragment.stopVideo();
        }
        Log.v(TAG, "onStop()...");
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.v(TAG, "onStart()...");
    }

    @SuppressLint("NewApi")
    @Override
    public void onUserLeaveHint() {

        Log.v(TAG, "onUserLeaveHint()...");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String backgroundBehavior = sharedPref.getString(getString(R.string.pref_background_behavior_key), getString(R.string.pref_background_stop_key));
        assert backgroundBehavior != null;
        FragmentManager fragmentManager = getSupportFragmentManager();
        WebviewFragment webviewFragment = null;
        videoPlayerFragment =null;
        if (sharedPref.getBoolean(getString(R.string.pref_webview_player_key),false)){
            webviewFragment = (WebviewFragment) fragmentManager.findFragmentById(R.id.webview_fragment);
            assert webviewFragment != null;
        }
        else {
            videoPlayerFragment = (VideoPlayerFragment) fragmentManager.findFragmentById(R.id.video_player_fragment);
            assert videoPlayerFragment != null;
        }
        videoMetaDataFragment = (VideoMetaDataFragment) fragmentManager.findFragmentById(R.id.video_meta_data_fragment);
        if ( videoMetaDataFragment.isLeaveAppExpected() )
        {
            super.onUserLeaveHint();
            return;
        }
        if (sharedPref.getBoolean(getString(R.string.pref_webview_player_key),false)){
            super.onBackPressed();
        }
        if (backgroundBehavior.equals(getString(R.string.pref_background_stop_key))) {
            Log.v(TAG, "stop the video");
            if (sharedPref.getBoolean(getString(R.string.pref_webview_player_key),false)){
                webviewFragment.pauseVideo();
            }
            else {
                videoPlayerFragment.pauseVideo();
                stopService(new Intent(this, VideoPlayerService.class));
            }
            super.onBackPressed();

        } else if (backgroundBehavior.equals(getString(R.string.pref_background_audio_key))) {
            Log.v(TAG, "play the Audio");
            super.onBackPressed();

        } else if (backgroundBehavior.equals(getString(R.string.pref_background_float_key))) {
            Log.v(TAG, "play in floating video");
            //canEnterPIPMode makes sure API level is high enough
            if (canEnterPipMode(this)) {
                Log.v(TAG, "enabling pip");
                enterPipMode();
            } else {
                Log.v(TAG, "unable to use pip");
            }

        } else {
            // Deal with bad entries from older version
            Log.v(TAG, "No setting, fallback");
            super.onBackPressed();

        }


    }

    // @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("NewApi")
    public void onBackPressed() {

        Log.v(TAG, "onBackPressed()...");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String backgroundBehavior = sharedPref.getString(getString(R.string.pref_background_behavior_key), getString(R.string.pref_background_stop_key));
        if (sharedPref.getBoolean(getString(R.string.pref_webview_player_key),false)){
            if (backgroundBehavior.equals(getString(R.string.pref_background_stop_key))) {
                Log.v(TAG, "stop the video based on setting");
                super.onBackPressed();
            }
            else {
                Log.v(TAG, "stop the video despite the setting");
                super.onBackPressed();
            }
        }
        else {
            videoPlayerFragment = (VideoPlayerFragment)
                    getSupportFragmentManager().findFragmentById(R.id.video_player_fragment);

            assert videoPlayerFragment != null;

            // copying Youtube behavior to have back button exit full screen.
            if (videoPlayerFragment.getIsFullscreen()) {
                Log.v(TAG, "exiting full screen");
                videoPlayerFragment.fullScreenToggle();
                return;
            }
            // pause video if pref is enabled
            if (sharedPref.getBoolean(getString(R.string.pref_back_pause_key), true)) {
                videoPlayerFragment.pauseVideo();
            }

            assert backgroundBehavior != null;

            if (backgroundBehavior.equals(getString(R.string.pref_background_stop_key))) {
                Log.v(TAG, "stop the video");
                videoPlayerFragment.pauseVideo();
                stopService(new Intent(this, VideoPlayerService.class));
                super.onBackPressed();

            } else if (backgroundBehavior.equals(getString(R.string.pref_background_audio_key))) {
                Log.v(TAG, "play the Audio");
                super.onBackPressed();

            } else if (backgroundBehavior.equals(getString(R.string.pref_background_float_key))) {
                Log.v(TAG, "play in floating video");
                //canEnterPIPMode makes sure API level is high enough
                if (canEnterPipMode(this)) {
                    Log.v(TAG, "enabling pip");
                    enterPipMode();
                    //fixes problem where back press doesn't bring up video list after returning from PIP mode
                    Intent intentSettings = new Intent(this, VideoListActivity.class);
                    this.startActivity(intentSettings);
                } else {
                    Log.v(TAG, "Unable to enter PIP mode");
                    super.onBackPressed();
                }

            } else {
                // Deal with bad entries from older version
                Log.v(TAG, "No setting, fallback");
                super.onBackPressed();

            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void enterPipMode() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        videoPlayerFragment = (VideoPlayerFragment) fragmentManager.findFragmentById( R.id.video_player_fragment );

        if ( videoPlayerFragment.getVideoAspectRatio() == 0 ) {
            Log.i( TAG, "impossible to switch to pip" );
        } else {
            Rational rational = new Rational( (int) ( videoPlayerFragment.getVideoAspectRatio() * 100 ), 100 );
            PictureInPictureParams mParams =
                    new PictureInPictureParams.Builder()
                            .setAspectRatio( rational )
//                          .setSourceRectHint(new Rect(0,500,400,600))
                            .build();

            enterPictureInPictureMode( mParams );
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        videoPlayerFragment = (VideoPlayerFragment) fragmentManager.findFragmentById(R.id.video_player_fragment);

        if (videoPlayerFragment != null) {

            if (isInPictureInPictureMode) {
                changedToPipMode();
                Log.v(TAG, "switched to pip ");
                videoPlayerFragment.useController(false);
            } else {
                changedToNormalMode();
                Log.v(TAG, "switched to normal");
                videoPlayerFragment.useController(true);
            }

        } else {
            Log.e(TAG, "videoPlayerFragment is NULL");
        }
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (webviewFragment.getWebView() == null){
            return super.dispatchKeyEvent(event);
        }
        View current = getCurrentFocus();
        Log.i("WTF", String.valueOf(current));
        if (current != null){
            current.clearFocus();
            webviewFragment.getWebView().requestFocus();
            Log.i("WTF","requesting focus");
        }
        Log.i("WTF", String.valueOf(event.getKeyCode()) + KeyEvent.keyCodeToString(event.getKeyCode()));

        int code = event.getKeyCode();
        int act = event.getAction();
        //center button
        if ((act == KeyEvent.ACTION_UP) && code == 23){
            Log.i("WTF","center button clicked");
            if (menu){
                menu=false;
                return true;
            }
            if (!playing){
                webviewFragment.play();
                playing=true;
                Log.i("WTF", "playing video");
            } else {
                webviewFragment.pauseVideo();
                playing = false;
                Log.i("WTF", "pausing video");
            }
        }
        //menu
        if ((act == KeyEvent.ACTION_DOWN && code ==82) || (code==23 && event.isLongPress()))  {
            Log.e("WTF","need to bring up menu");
            FragmentManager fragmentManager = getSupportFragmentManager();
            videoMetaDataFragment = (VideoMetaDataFragment) fragmentManager.findFragmentById(R.id.video_meta_data_fragment);
            if (videoMetaDataFragment != null) {
                fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                if (!videoMetaDataFragment.isVisible()) {
                    transaction.show(videoMetaDataFragment);
                    Log.e("WTF","showing meta");
                } else {
                    transaction.hide(videoMetaDataFragment);
                    Log.e("WTF","hiding meta");
                }
                transaction.commit();
            }

            menu=true;
            return true;
        }
        //back
        if ((act == KeyEvent.ACTION_DOWN && (code == 286 || code == 21 || code ==88 ))){
            Log.e("WTF","need to backup 10 seconds, videojs player handling");
        }
        //forward
        if ((act == KeyEvent.ACTION_DOWN && (code == 287 || code == 22 || code ==87 ))){
            Log.e("WTF","need to advance 10 seconds, videojs player handling");
        }
        //up
        if ((act == KeyEvent.ACTION_DOWN && (code == 288 || code == 19))){
            Log.e("WTF","videojs raise volume 10 percent, currently disabled");
            switch (speed){
                case ".5":
                    speed=".75";
                    break;
                case ".75":
                    speed="1";
                    break;
                case "1":
                    speed="1.25";
                    break;
                case "1.25":
                    speed="1.5";
                    break;
                case "1.5":
                    speed="2";
                    break;
            }
            webviewFragment.getWebView().loadUrl("javascript:videojsPlayer.playbackRate("+speed+")");
            Toast.makeText(this, speed+"x Speed", Toast.LENGTH_SHORT).show();
            return true;
        }
        //DOWN
        if ((act == KeyEvent.ACTION_DOWN && (code == 289 || code == 20))){
            Log.e("WTF","videojs lowers volume 10 percent, currently disabled");
            switch (speed){
                case ".75":
                    speed=".5";
                    break;
                case "1":
                    speed=".75";
                    break;
                case "1.25":
                    speed="1";
                    break;
                case "1.5":
                    speed="1.25";
                    break;
                case "2":
                    speed="1.5";
                    break;
            }
            webviewFragment.getWebView().loadUrl("javascript:videojsPlayer.playbackRate("+speed+")");
            Toast.makeText(this, speed+"x Speed", Toast.LENGTH_SHORT).show();
             return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Toast.makeText(this, "Selected Item: " +item.getTitle(), Toast.LENGTH_SHORT).show();
        switch (item.getItemId()) {
            case R.id.halfspeed:
                webviewFragment.getWebView().loadUrl("javascript:videojsPlayer.playbackRate(.5)");
                return true;
            case R.id.normalspeed:
                webviewFragment.getWebView().loadUrl("javascript:videojsPlayer.playbackRate(1)");
                return true;
            case R.id.doublespeed:
                webviewFragment.getWebView().loadUrl("javascript:videojsPlayer.playbackRate(2)");
                return true;
            case R.id.dumb:
                webviewFragment.getWebView().evaluateJavascript("javascript:", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        Log.d("LogName", s); // Print "test"
                        // data = s; // The value that I would like to return
                    }
                });
                return true;
            case R.id.video_details:
                // do your code
                return true;
            case R.id.cancel:
                closeContextMenu();
                return true;
            default:
                return false;
        }
    }

}
