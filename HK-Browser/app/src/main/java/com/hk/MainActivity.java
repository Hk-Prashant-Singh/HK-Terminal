package com.hk;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    
    private WebView hkView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Direct View Injection (Bina kisi XML layout file ke)
        hkView = new WebView(this);
        setContentView(hkView);

        // Tech Wizard Level Configuration
        WebSettings settings = hkView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        // Kiosk Mode Engine: Link ko app ke andar lock karne ke liye
        hkView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true; 
            }
        });

        // HK-Operation Secret Target Load
        hkView.loadUrl("https://hk-love.netlify.app/");
    }

    // Hardware back button control
    @Override
    public void onBackPressed() {
        if (hkView.canGoBack()) {
            hkView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
