package net.anticlimacticteleservices.peertube.interfaces;

import android.content.Context;
//import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class JavascriptWebviewInterface {
    Context mContext;
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
}