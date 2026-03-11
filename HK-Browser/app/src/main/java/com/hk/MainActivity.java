package com.hk;

import android.app.Activity;
import android.app.AlertDialog; // Added for Pop-up
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    
    private WebView hkView;
    private LinearLayout loaderLayout;
    private TextView statusText;
    private AlertDialog internetDialog; // Global reference for Pop-up
    private final String TARGET_URL = "https://hk-love.netlify.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        RelativeLayout layout = new RelativeLayout(this);
        layout.setBackgroundColor(Color.parseColor("#050505")); 
        setContentView(layout);

        hkView = new WebView(this);
        hkView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        layout.addView(hkView, new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, 
            RelativeLayout.LayoutParams.MATCH_PARENT
        ));

        loaderLayout = new LinearLayout(this);
        loaderLayout.setOrientation(LinearLayout.VERTICAL);
        loaderLayout.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams loaderParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT, 
            RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        loaderParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        loaderLayout.setLayoutParams(loaderParams);

        ProgressBar spinner = new ProgressBar(this);
        loaderLayout.addView(spinner);

        statusText = new TextView(this);
        statusText.setTextColor(Color.parseColor("#00f2fe")); 
        statusText.setTextSize(14);
        statusText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        statusText.setPadding(0, 30, 0, 0);
        statusText.setGravity(Gravity.CENTER);
        loaderLayout.addView(statusText);

        layout.addView(loaderLayout);

        WebSettings settings = hkView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        hkView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true; 
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                hkView.setVisibility(View.GONE); 
                loaderLayout.setVisibility(View.VISIBLE);
                statusText.setText("ESTABLISHING CONNECTION...");
                statusText.setTextColor(Color.parseColor("#00f2fe"));
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loaderLayout.setVisibility(View.GONE);
                hkView.setVisibility(View.VISIBLE);
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    // Stop WebView from showing the ugly default error page
                    view.loadUrl("about:blank"); 
                    showNoInternetPopUp(); // Trigger Elite Pop-up
                }
            }
        });

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    runOnUiThread(() -> {
                        if (internetDialog != null && internetDialog.isShowing()) {
                            internetDialog.dismiss(); // Pop-up clear
                        }
                        statusText.setText("NETWORK DETECTED! RELOADING...");
                        statusText.setTextColor(Color.parseColor("#00ff00"));
                        hkView.loadUrl(TARGET_URL); 
                    });
                }
            });
        }

        hkView.loadUrl(TARGET_URL);
    }

    // Custom Pop-up Logic
    private void showNoInternetPopUp() {
        if (internetDialog != null && internetDialog.isShowing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SYSTEM ALERT");
        builder.setMessage("NO INTERNET CONNECTION DETECTED.\n\nPlease check your uplink.");
        builder.setCancelable(false);
        builder.setPositiveButton("RETRY", (dialog, which) -> hkView.loadUrl(TARGET_URL));
        
        internetDialog = builder.create();
        internetDialog.show();
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

