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
    // Tera secret target lock
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

            // HK-Operation: Updated Offline Shield (No Extra Comments, Pure Aggression)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    String offlineHtml = "<html><body style='background-color:#050505; text-align:center; display:flex; flex-direction:column; justify-content:center; align-items:center; height:100vh; margin:0; overflow:hidden;'>" +
                            "" +
                            "<div style='font-size:110px; filter:drop-shadow(0px 0px 20px #00ff00); margin-bottom:20px;'>🤖</div>" +
                            "" +
                            "<h1 style='font-family:\"Arial Black\", Impact, sans-serif; font-size:38px; color:#ff3333; text-shadow:0 0 20px #ff0000; margin:10px 0; line-height:1.1;'>NO INTERNET<br>CONNECTION</h1>" +
                            "<div style='flex-grow:0.2;'></div>" +
                            "" +
                            "<div style='margin-top:40px; padding-top:20px; border-top:2px solid #222; width:85%;'>" +
                            "<div style='color:#888; font-size:14px; letter-spacing:2px; margin-bottom:10px; font-family:monospace;'>&gt;-&gt; H-K Terminal Alpha &lt;-&lt;</div>" +
                            "<h2 style='color:#e0e0e0; letter-spacing:1px; font-size:28px; text-transform:uppercase; margin:5px 0;'>HK PRASHANT SINGH</h2>" +
                            "<h3 style='color:#00ff00; font-family:cursive; font-size:26px; text-shadow:0 0 10px #00ff00; margin:5px 0; font-style:italic;'>Tech Wizard</h3>" +
                            "</div>" +
                            "</body></html>";
                    
                    view.loadDataWithBaseURL(null, offlineHtml, "text/html", "UTF-8", null);
                }
            }
        });

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

