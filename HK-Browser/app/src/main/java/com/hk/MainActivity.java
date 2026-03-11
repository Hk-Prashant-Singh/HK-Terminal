package com.hk;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    
    private WebView hkView;
    // Tera secret target lock kar diya gaya hai
    private final String TARGET_URL = "https://hk-love.netlify.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        hkView = new WebView(this);
        setContentView(hkView);

        WebSettings settings = hkView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        hkView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true; 
            }

            // HK-Operation: Offline Shield Injection (URL Hider)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                // Sirf main page ke error par ye shield activate hogi
                if (request.isForMainFrame()) {
                    // Custom Hacker-Style UI using embedded HTML/CSS
                    String offlineHtml = "<html><body style='background-color:#050505; color:#00ff00; text-align:center; font-family:\"Courier New\", Courier, monospace; display:flex; flex-direction:column; justify-content:center; height:90vh; margin:0;'>" +
                            "<div style='font-size:120px; margin-bottom:10px;'>🤖</div>" +
                            "<h1 style='text-transform:uppercase; letter-spacing:2px; font-size:22px; color:#ff003c; text-shadow: 0 0 10px #ff003c;'>No Internet Connection</h1>" +
                            "<p style='color:#444; font-size:12px; margin-top:5px; text-transform:uppercase;'>System Offline | URL Secured</p>" +
                            "<div style='margin-top:60px; border-top:1px solid #222; padding-top:20px;'>" +
                            "<h2 style='color:#00ff00; letter-spacing:3px; font-size:20px; text-shadow: 0 0 15px #00ff00; margin:0;'>HK PRASHANT SINGH</h2>" +
                            "<p style='color:#005500; font-size:12px; margin-top:5px; font-weight:bold;'>TECH WIZARD</p>" +
                            "</div>" +
                            "</body></html>";
                    
                    // Default error page ko hatakar apna shield load kar do
                    view.loadDataWithBaseURL(null, offlineHtml, "text/html", "UTF-8", null);
                }
            }
        });

        // Direct Load Payload
        hkView.loadUrl(TARGET_URL);
    }

    @Override
    public void onBackPressed() {
        if (hkView.canGoBack()) {
            hkView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}

