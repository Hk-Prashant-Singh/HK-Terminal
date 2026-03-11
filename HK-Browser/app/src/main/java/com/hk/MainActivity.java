package com.hk;

import android.app.Activity;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity { // Ensure AppCompatActivity is used for compatibility
    private WebView hkView;
    private LinearLayout loaderLayout;
    private TextView statusText;
    private final String TARGET_URL = "https://hk-love.netlify.app/";
    private AlertDialog noInternetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Call super class onCreate

        // Stylish Background Setup (Pitch Black)
        RelativeLayout layout = new RelativeLayout(this);
        layout.setBackgroundColor(Color.parseColor("#050505"));
        setContentView(layout);

        // WebView Setup
        hkView = new WebView(this);
        hkView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        layout.addView(hkView, new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        ));

        // HK-Operation: Custom Stylish Loader Container
        loaderLayout = new LinearLayout(this);
        loaderLayout.setOrientation(LinearLayout.VERTICAL);
        loaderLayout.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams loaderParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        loaderParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        loaderLayout.setLayoutParams(loaderParams);

        // Loading Spinner
        ProgressBar spinner = new ProgressBar(this);
        loaderLayout.addView(spinner);

        // Dynamic Status Text (Below Spinner)
        statusText = new TextView(this);
        statusText.setTextColor(Color.parseColor("#00f2fe")); // Neon Cyan
        statusText.setTextSize(14);
        statusText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        statusText.setPadding(0, 30, 0, 0);
        statusText.setGravity(Gravity.CENTER);
        loaderLayout.addView(statusText);

        layout.addView(loaderLayout);

        // WebSettings Configuration
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
                hkView.setVisibility(View.GONE); // Hide website while loading
                loaderLayout.setVisibility(View.VISIBLE);
                statusText.setText("ESTABLISHING CONNECTION...");
                statusText.setTextColor(Color.parseColor("#00f2fe")); // Cyan
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loaderLayout.setVisibility(View.GONE);
                hkView.setVisibility(View.VISIBLE); // Show website
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    hkView.setVisibility(View.GONE);
                    loaderLayout.setVisibility(View.VISIBLE);
                    // Offline Text Target Hit
                    statusText.setText("NO INTERNET CONNECTION\n\nWaiting for Network...");
                    statusText.setTextColor(Color.parseColor("#ff003c")); // Red Alert
                    showNoInternetDialog();  // Show the no internet dialog
                }
            }
        });

        // 🚀 Auto-Detect Network Engine
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    runOnUiThread(() -> {
                        // Data ON, reload the website
                        statusText.setText("NETWORK DETECTED! RELOADING...");
                        statusText.setTextColor(Color.parseColor("#00ff00")); // Hacker Green
                        hkView.loadUrl(TARGET_URL); // Auto Reload
                    });
                }
            });
        }

        // Fire Initial Payload
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

    private void showNoInternetDialog() {
        if (noInternetDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("No internet connection detected. Please check your connection.")
                .setCancelable(false)
                .setPositiveButton("Retry", (dialog, id) -> {
                    hkView.loadUrl(TARGET_URL); // Retry loading the URL
                    dialog.dismiss();
                })
                .setNegativeButton("Close", (dialog, id) -> dialog.dismiss());

            noInternetDialog = builder.create();
        }
        noInternetDialog.show();
    }
}
