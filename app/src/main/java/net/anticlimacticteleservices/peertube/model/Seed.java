package net.anticlimacticteleservices.peertube.model;

import android.webkit.WebView;

import java.util.Date;

public class Seed {
    private Video video;
    private WebView webView;
    private Long addedAt;
    private int priority;
    public Seed(Video video,WebView webView){
        this.webView=webView;
        this.video=video;
        this.addedAt=new Date().getTime();
        this.priority=69;
    }
    public WebView getWebView() {
        return webView;
    }
    public void setWebView(WebView webView) {
        this.webView = webView;
    }
    public Long getAddedAt() {
        return addedAt;
    }
    public void setAddedAt(Long addedAt) {
        this.addedAt = addedAt;
    }
    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }
}
