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
package net.anticlimacticteleservices.peertube.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;

import net.anticlimacticteleservices.peertube.R;
import net.anticlimacticteleservices.peertube.application.AppApplication;
import net.anticlimacticteleservices.peertube.helper.APIUrlHelper;
import net.anticlimacticteleservices.peertube.helper.ErrorHelper;
import net.anticlimacticteleservices.peertube.interfaces.JavascriptWebviewInterface;
import net.anticlimacticteleservices.peertube.model.Seed;
import net.anticlimacticteleservices.peertube.model.Video;
import net.anticlimacticteleservices.peertube.network.GetVideoDataService;
import net.anticlimacticteleservices.peertube.network.RetrofitInstance;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebviewFragment extends Fragment {

    private String TAG = "web";
    private static String mVideoUuid;
   private float aspectRatio;
    private Boolean isFullscreen = false;
    private static WebView webView;
    private static Video video;
    public static Button getMoreButton() { return moreButton; }

    private static Button moreButton;
    private AspectRatioFrameLayout.AspectRatioListener aspectRatioListerner = new AspectRatioFrameLayout.AspectRatioListener()
    {
        @Override
        public void onAspectRatioUpdated( float targetAspectRatio, float naturalAspectRatio, boolean aspectRatioMismatch )
        {
            aspectRatio = targetAspectRatio;
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        Log.v(TAG,"created the bundle "+mVideoUuid);
        return inflater.inflate(R.layout.fragment_webview, container, false);
    }


    public void start(String videoUuid) {

        // start service
        Context context = getContext();
        Activity activity = getActivity();
        Log.e(TAG,mVideoUuid+"   "+videoUuid);
        if (mVideoUuid != null){
            Log.e(TAG,"need to save webview here");

        }
        mVideoUuid = videoUuid;
        // get video details from api
        String apiBaseURL = APIUrlHelper.getUrlWithVersion(context);
        GetVideoDataService service = RetrofitInstance.getRetrofitInstance(apiBaseURL).create(GetVideoDataService.class);
        Log.e(TAG,"requesting video information for "+mVideoUuid);
        Call<Video> call = service.getVideoData(mVideoUuid);

        call.enqueue(new Callback<Video>() {
            @Override
            public void onResponse(@NonNull Call<Video> call, @NonNull Response<Video> response) {
                Log.e(TAG,"information retrieved for video");
                video = response.body();

                if (video == null) {
                    Toast.makeText(context, "Unable to retrieve video information, try again later.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.v(TAG,video.getName());
                // video Meta fragment
                VideoMetaDataFragment videoMetaDataFragment = (VideoMetaDataFragment)
                        requireActivity().getSupportFragmentManager().findFragmentById(R.id.video_meta_data_fragment);
                Log.v(TAG,"setup meta frag");
                assert videoMetaDataFragment != null;
                Log.v(TAG,"https://"+video.getChannel().getHost()+video.getEmbedPath());
                videoMetaDataFragment.updateVideoMeta(video,null);
                String playerURL=video.getChannel().getHost()+video.getEmbedPath();
                Log.e(TAG,playerURL);





                moreButton=activity.findViewById((R.id.moreButton));
                moreButton.setOnClickListener(v -> {

                    PopupMenu popup = new PopupMenu(context, v);
                    popup.setOnMenuItemClickListener(menuItem -> {
                        switch (menuItem.getItemId()) {
                            case R.id.halfspeed:
                                webView.loadUrl("javascript:videojsPlayer.playbackRate(.5)");
                                webView.loadUrl("javascript:videojsPlayer.play()");
                                return true;
                            case R.id.normalspeed:
                                webView.loadUrl("javascript:videojsPlayer.playbackRate(1)");
                                webView.loadUrl("javascript:videojsPlayer.play()");
                                return true;
                            case R.id.doublespeed:
                                webView.loadUrl("javascript:videojsPlayer.playbackRate(2)");
                                webView.loadUrl("javascript:videojsPlayer.play()");
                                return true;
                            case R.id.dumb:
                                webView.evaluateJavascript("videojsPlayer.networkState() ", new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String s) {
                                        Log.d("WTF", s); // Print "test"
                                        // data = s; // The value that I would like to return
                                    }
                                });
                                return true;
                            case R.id.video_details:
                                webView.evaluateJavascript("Android.debug('test')", new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String s) {
                                        Log.d("WTF", s); // Print "test"
                                        // data = s; // The value that I would like to return
                                    }
                                });
                                return true;
                            case R.id.cancel:
                                Log.e("WTF","figure out cancel");
                                return true;
                            default:
                                return false;
                        }
                    });
                    popup.inflate(R.menu.play_menu);
                    popup.show();

                });
                webView = activity.findViewById(R.id.playerWebview);
                Seed testSeed=AppApplication.getMatch(mVideoUuid);

                WebViewClient webViewClient = new WebViewClient();
                WebSettings webSettings = webView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setAllowContentAccess(true);
                webSettings.setAllowFileAccess(true);
                webSettings.setDomStorageEnabled(true);
                webSettings.setMediaPlaybackRequiresUserGesture(false);
                webSettings.setMixedContentMode(1);
                webSettings.setAllowUniversalAccessFromFileURLs(true);
                webSettings.setAllowFileAccessFromFileURLs(true);
                WebView.setWebContentsDebuggingEnabled(true);
                webView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                        Log.e("Javascript", consoleMessage.message() + " -- From line "
                                + consoleMessage.lineNumber() + " of "
                                + consoleMessage.sourceId());
                        return super.onConsoleMessage(consoleMessage);
                    }

                    public void onProgressChanged(WebView view, int progress) {
                        Log.e("WTF", "progress " + progress);
                        webView.requestFocus();
                        if (progress == 100) {
                            Log.e("WTF", "progress " + progress);
                            webView.requestFocus();
                            webView.evaluateJavascript("videojsPlayer.play() ", new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String s) {
                                    Log.d("WTF", s); // Print "test"
                                    // data = s; // The value that I would like to return
                                }
                            });
                        }
                    }
                });

                webView.loadUrl("https://" + playerURL);

                // webView.loadUrl("file:///android_asset/downloader.html");
                webView.addJavascriptInterface(new JavascriptWebviewInterface(getContext()), "Android");

            }

            @Override
            public void onFailure(@NonNull Call<Video> call, @NonNull Throwable t) {
                Log.wtf(TAG, t.fillInStackTrace());
                ErrorHelper.showToastFromCommunicationError( getActivity(), t );
            }
        });





    }

    public static String getVideoUuid() {
        return mVideoUuid;
    }
    public static WebView getWebView() {return webView;}
    public static Seed getSeed(){
        return new Seed(video,webView);
    }
}
