package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.concough.android.general.AlertClass;
import com.concough.android.vendor.progressHUD.KProgressHUD;

public class SettingsWebViewActivity extends BottomNavigationActivity {
    private final String TAG = "SettingsWebViewActivity";

    private WebView mWebView;
    private KProgressHUD loading;

    private static String mUrl = "URL";
    private static String mTitle = "TITLE";

    public static Intent newIntent(Context packageContext, String url, String title) {
        Intent i = new Intent(packageContext, SettingsWebViewActivity.class);
        i.putExtra(mUrl, url);
        i.putExtra(mTitle, title);
        return i;
    }


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_settings_web_view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setMenuSelectedIndex(3);
        super.onCreate(savedInstanceState);

        loading = AlertClass.showLoadingMessage(SettingsWebViewActivity.this);
        loading.show();

        mWebView = (WebView) findViewById(R.id.webViewA_webview);

        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.clearCache(true);
        mWebView.clearHistory();
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        String url = getIntent().getStringExtra(mUrl);
        mWebView.loadUrl(url);


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loading.dismiss();
            }
        },6000);

        actionBarSet();
    }


    private void actionBarSet() {
        super.clickEventInterface = new OnClickEventInterface() {
            @Override
            public void OnButtonClicked(int id) {
            }

            @Override
            public void OnBackClicked() {
                onBackPressed();
            }

            @Override
            public void OnTitleClicked() {

            }
        };

        String title = getIntent().getStringExtra(mTitle);
        super.createActionBar(title, true, null);
    }
}
