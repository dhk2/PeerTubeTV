package net.anticlimacticteleservices.peertube.interfaces;

import android.content.Context;
//import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class JavascriptWebviewInterface {
    Context mContext;
    static int bob=0;
    // Instantiate the interface and set the context
    public JavascriptWebviewInterface(Context c) {
        mContext = c;
    }

    // Show a toast from the web page
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }
    @JavascriptInterface
    public void timeRemaining(Long timeRemaining, Long videoID){
        System.out.println("video "+ Long.toString(videoID)+" finished in:"+ Long.toString(timeRemaining));
    }
    @JavascriptInterface
    public void bytesDownloaded(Long bytes, Long videoID){
        System.out.println("video "+ Long.toString(videoID)+" so far downloaded:"+ Long.toString(bytes));
    }
    @JavascriptInterface
    public void bytesUploaded(Long bytes, Long videoID){
        System.out.println("video "+ Long.toString(videoID)+" so far uploaded:"+ Long.toString(bytes));
    }
    @JavascriptInterface
    public void peers(int peers, Long videoID){
        System.out.println("video "+ Long.toString(videoID)+" peers:"+ Long.toString(peers));
    }
    @JavascriptInterface
    public void path(String path, Long videoID){
        System.out.println("video "+ Long.toString(videoID)+" path:"+path);
    }
    @JavascriptInterface
    public void debug(String g){
        Log.d("wtf","Console:"+g);
    }


    @JavascriptInterface
    public String getMagnet() {
        bob++;
        switch (bob){
            case 1:         return "magnet:?xs=https%3A%2F%2Fbittube.video%2Fstatic%2Ftorrents%2F29667106-acbe-4b5a-b067-d504395e39b1-480.torrent&xt=urn:btih:24293d038813b0860d26f41776f371e4bf2e2624&dn=%F0%9F%95%B9+Wings+of+Vi+(Azurel)+Let's+Play!+%2366&tr=wss%3A%2F%2Fbittube.video%2Ftracker%2Fsocket&tr=https%3A%2F%2Fbittube.video%2Ftracker%2Fannounce&ws=https%3A%2F%2Fbittube.video%2Fstatic%2Fwebseed%2F29667106-acbe-4b5a-b067-d504395e39b1-480.mp4";
            case 2:         return "magnet:?xs=https%3A%2F%2Fpeervideo.club%2Fstatic%2Ftorrents%2F256e4d07-b908-471d-a66d-0384e84acc02-1080.torrent&xt=urn:btih:2505f80cdad7f2029b90cc3079d6d6acb002afd9&dn=Izrael+2016&tr=wss%3A%2F%2Fpeervideo.club%2Ftracker%2Fsocket&tr=https%3A%2F%2Fpeervideo.club%2Ftracker%2Fannounce&ws=https%3A%2F%2Fpeervideo.club%2Fstatic%2Fwebseed%2F256e4d07-b908-471d-a66d-0384e84acc02-1080.mp4";
            case 3:         return "magnet:?xs=https%3A%2F%2Fpeervideo.club%2Fstatic%2Ftorrents%2F0d12e2dc-f073-47bb-a59f-6d5c21abfe9b-240.torrent&xt=urn:btih:3656cf805d71ec0098847cffd0df8d0147763427&dn=do+prace+na+kole&tr=wss%3A%2F%2Fpeervideo.club%2Ftracker%2Fsocket&tr=https%3A%2F%2Fpeervideo.club%2Ftracker%2Fannounce&ws=https%3A%2F%2Fpeervideo.club%2Fstatic%2Fwebseed%2F0d12e2dc-f073-47bb-a59f-6d5c21abfe9b-240.mp4";
        }

        Log.e("javascriptinterface", String.valueOf(bob));
        return "magnet:?xs=https%3A%2F%2Fbittube.video%2Fstatic%2Ftorrents%2F29667106-acbe-4b5a-b067-d504395e39b1-480.torrent&xt=urn:btih:24293d038813b0860d26f41776f371e4bf2e2624&dn=%F0%9F%95%B9+Wings+of+Vi+(Azurel)+Let's+Play!+%2366&tr=wss%3A%2F%2Fbittube.video%2Ftracker%2Fsocket&tr=https%3A%2F%2Fbittube.video%2Ftracker%2Fannounce&ws=https%3A%2F%2Fbittube.video%2Fstatic%2Fwebseed%2F29667106-acbe-4b5a-b067-d504395e39b1-480.mp4";

    }
}