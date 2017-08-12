package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

public class SettingsWebViewActivity extends AppCompatActivity {
    private final String TAG = "SettingsWebViewActivity";

    private WebView mWebView;

    private static String mUrl;

    public static Intent newIntent(Context packageContext, String url) {
        Intent i = new Intent(packageContext,SettingsWebViewActivity.class);
        i.putExtra(mUrl, url);
        return i;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_web_view);


        mWebView = (WebView) findViewById(R.id.webViewA_webview);
        String url = getIntent().getStringExtra(mUrl);
        mWebView.loadUrl(url);

        //setContentView(mWebView);
    }
}
