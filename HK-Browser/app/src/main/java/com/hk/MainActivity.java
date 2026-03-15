package com.hk;

import android.app.Activity;
import android.app.AlertDialog;
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
    private AlertDialog internetDialog; 
    private final String TARGET_URL = "https://hk-mall-16bb9.web.app/";

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
                    view.loadUrl("about:blank"); 
                    showNoInternetPopUp(); 
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
                            internetDialog.dismiss(); 
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

    private void showNoInternetPopUp() {
        if (internetDialog != null && internetDialog.isShowing()) return;

        // The Big Green Panel (Replacing the white one)
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setBackgroundColor(Color.parseColor("#004D00")); // Solid Deep Green
        dialogLayout.setPadding(60, 80, 60, 80);
        dialogLayout.setGravity(Gravity.CENTER);

        // Alert Text
        TextView title = new TextView(this);
        title.setText("NETWORK ERROR");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        dialogLayout.addView(title);

        TextView msg = new TextView(this);
        msg.setText("\nConnection Lost.\nCheck your uplink, Prashant bhai.\n");
        msg.setTextColor(Color.parseColor("#A3FFA3")); 
        msg.setTextSize(13);
        msg.setTypeface(Typeface.MONOSPACE);
        msg.setGravity(Gravity.CENTER);
        dialogLayout.addView(msg);

        // Aggressive Red Retry Button
        TextView retryBtn = new TextView(this);
        retryBtn.setText(" RETRY ");
        retryBtn.setBackgroundColor(Color.parseColor("#FF0000")); // RED
        retryBtn.setTextColor(Color.WHITE);
        retryBtn.setTextSize(16);
        retryBtn.setPadding(0, 35, 0, 35);
        retryBtn.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        retryBtn.setGravity(Gravity.CENTER);
        
        retryBtn.setOnClickListener(v -> {
            hkView.loadUrl(TARGET_URL);
            if (internetDialog != null) internetDialog.dismiss();
        });

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnParams.setMargins(0, 50, 0, 0);
        retryBtn.setLayoutParams(btnParams);
        dialogLayout.addView(retryBtn);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogLayout);
        builder.setCancelable(false);
        
        internetDialog = builder.create();
        
        // Final Polish: Kill the default Android white background window
        if (internetDialog.getWindow() != null) {
            internetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
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
                                        
