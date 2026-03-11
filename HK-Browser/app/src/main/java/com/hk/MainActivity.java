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

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    // HK STYLE: Transparent Blur & Stylish Animations
                    String offlineHtml = "<html><head><style>" +
                            "body { background: #000; margin: 0; display: flex; justify-content: center; align-items: center; height: 100vh; font-family: 'Segoe UI', sans-serif; overflow: hidden; }" +
                            ".container { width: 85%; padding: 30px; background: rgba(255, 255, 255, 0.05); backdrop-filter: blur(15px); -webkit-backdrop-filter: blur(15px); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 25px; text-align: center; box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.8); animation: fadeIn 0.8s ease-out; }" +
                            "@keyframes fadeIn { from { opacity: 0; transform: scale(0.9); } to { opacity: 1; transform: scale(1); } }" +
                            ".robot { font-size: 80px; margin-bottom: 10px; filter: drop-shadow(0 0 15px #00f2fe); animation: float 3s ease-in-out infinite; }" +
                            "@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-15px); } }" +
                            "h1 { color: #fff; font-size: 24px; margin: 10px 0; text-transform: uppercase; letter-spacing: 2px; }" +
                            "p { color: #bbb; font-size: 14px; margin-bottom: 25px; }" +
                            ".reload-btn { background: linear-gradient(45deg, #00f2fe, #4facfe); border: none; padding: 12px 35px; color: white; border-radius: 50px; font-weight: bold; text-transform: uppercase; cursor: pointer; box-shadow: 0 4px 15px rgba(0, 242, 254, 0.4); transition: 0.3s; }" +
                            ".tag-section { margin-top: 30px; border-top: 1px solid rgba(255,255,255,0.1); padding-top: 20px; }" +
                            ".hk-tag { color: #00ff00; font-family: monospace; font-size: 18px; text-shadow: 0 0 10px #00ff00; margin: 0; }" +
                            ".wizard { color: #fff; font-size: 12px; opacity: 0.6; letter-spacing: 3px; }" +
                            "</style></head><body>" +
                            "<div class='container'>" +
                            "<div class='robot'>🤖</div>" +
                            "<h1>No Internet</h1>" +
                            "<p>Please check your connection</p>" +
                            "<button class='reload-btn' onclick='window.location.reload()'>Reload</button>" +
                            "<div class='tag-section'>" +
                            "<p class='hk-tag'>HK PRASHANT BHAI</p>" +
                            "<p class='wizard'>TECH WIZARD</p>" +
                            "</div>" +
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

