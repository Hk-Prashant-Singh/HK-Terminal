package com.hk;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {
    
    private WebView hkView;
    private ProgressBar loadingIndicator;
    private final String TARGET_URL = "https://hk-love.netlify.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        RelativeLayout layout = new RelativeLayout(this);
        setContentView(layout);

        // Initialize WebView
        hkView = new WebView(this);
        layout.addView(hkView);

        // Initialize loading spinner
        loadingIndicator = new ProgressBar(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT, 
            RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        loadingIndicator.setLayoutParams(params);
        layout.addView(loadingIndicator);

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

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                // Show loading spinner when page starts loading
                loadingIndicator.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // Hide loading spinner when page is finished loading
                loadingIndicator.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    // HK-STYLE: ALL-IN-ONE COMPACT OFFLINE MODULE
                    String offlineHtml = "<html><head><style>" +
                            "body { background: #000; margin: 0; display: flex; justify-content: center; align-items: center; height: 100vh; font-family: 'Segoe UI', sans-serif; overflow: hidden; }" +
                            ".container { width: 320px; padding: 25px; background: rgba(255, 255, 255, 0.05); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px); border: 1.5px solid rgba(255, 255, 255, 0.15); border-radius: 25px; text-align: center; box-shadow: 0 10px 40px rgba(0, 0, 0, 0.9); transform: scale(0.7); animation: bootUp 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275); }" +
                            "@keyframes bootUp { from { opacity: 0; transform: scale(0.4); } to { opacity: 1; transform: scale(0.7); } }" +
                            ".robot { font-size: 70px; margin-bottom: 10px; filter: drop-shadow(0 0 15px #00f2fe); animation: pulse 2.5s ease-in-out infinite; }" +
                            "@keyframes pulse { 0%, 100% { transform: translateY(0) scale(1); } 50% { transform: translateY(-12px) scale(1.05); } }" +
                            "h1 { color: #fff; font-size: 22px; margin: 5px 0; text-transform: uppercase; letter-spacing: 2px; font-weight: 800; }" +
                            "p { color: #aaa; font-size: 13px; margin-bottom: 30px; letter-spacing: 0.5px; }" +
                            ".reload-btn { background: linear-gradient(135deg, #00f2fe 0%, #4facfe 100%); border: none; padding: 12px 30px; color: white; border-radius: 50px; font-weight: bold; font-size: 13px; text-transform: uppercase; cursor: pointer; box-shadow: 0 5px 20px rgba(0, 242, 254, 0.5); transition: 0.3s; }" +
                            ".reload-btn:active { transform: scale(0.9); }" +
                            ".tag-section { margin-top: 35px; border-top: 1px solid rgba(255,255,255,0.1); padding-top: 20px; }" +
                            ".hk-tag { color: #00ff00; font-family: 'Courier New', monospace; font-size: 16px; font-weight: bold; text-shadow: 0 0 12px #00ff00; margin: 0; }" +
                            ".wizard { color: #fff; font-size: 11px; opacity: 0.5; letter-spacing: 4px; text-transform: uppercase; margin-top: 5px; }" +
                            "</style></head><body>" +
                            "<div class='container'>" +
                            "<div class='robot'>🤖</div>" +
                            "<h1>Offline</h1>" +
                            "<p>HK SECURITY PROTOCOL ACTIVE</p>" +
                            "<button class='reload-btn' onclick='window.location.reload()'>System Reload</button>" +
                            "<div class='tag-section'>" +
                            "<div class='hk-tag'>HK PRASHANT BHAI</div>" +
                            "<div class='wizard'>Tech Wizard</div>" +
                            "</div>" +
                            "</div>" +
                            "</body></html>";
                    
                    view.loadDataWithBaseURL(null, offlineHtml, "text/html", "UTF-8", null);
                }
            }
        });

        // Load the target URL
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
