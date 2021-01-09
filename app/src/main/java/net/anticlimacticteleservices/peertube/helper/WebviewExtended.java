package net.anticlimacticteleservices.peertube.helper;

import android.content.Context;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;

public class WebviewExtended extends WebView {
    public WebviewExtended(Context context) {
        super(context);
    }

    public void WebViewExtended(Context context) {

    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        // This line fixes some issues but introduces others, YMMV.
        // super.onCreateInputConnection(outAttrs);

        return new BaseInputConnection(this, false);
    }
}