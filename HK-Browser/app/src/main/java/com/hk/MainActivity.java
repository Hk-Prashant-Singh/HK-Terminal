package com.hk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface; 
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView; 
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    
    private WebView hkView;
    private LinearLayout loaderLayout;
    private TextView statusText;
    private AlertDialog internetDialog; 
    private final String TARGET_URL = "https://hk-mall-16bb9.web.app/";
    
    private static final int REQUEST_CODE_EMAIL = 1001;
    private String systemUserEmail = "UNKNOWN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // --- 1. FULLSCREEN ALPHA UI SETUP (Pitch Black) ---
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        RelativeLayout layout = new RelativeLayout(this);
        layout.setBackgroundColor(Color.parseColor("#050505")); 
        setContentView(layout);
        
        hkView = new WebView(this);
        
        // HARDWARE ACCELERATION
        hkView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        hkView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY); 
        
        // 👉 HK-OPERATION: KEYBOARD FOCUS ENGINE
        hkView.setFocusable(true);
        hkView.setFocusableInTouchMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            hkView.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
        }

        // DIRECT INJECTION
        layout.addView(hkView, new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, 
            RelativeLayout.LayoutParams.MATCH_PARENT
        ));

        // --- 2. ELITE SPLASH LOADER (Stylish 'R' Logo + Neon Spinner) ---
        loaderLayout = new LinearLayout(this);
        loaderLayout.setOrientation(LinearLayout.VERTICAL);
        loaderLayout.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams loaderParams = new RelativeLayout.LayoutParams(-2, -2);
        loaderParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        loaderLayout.setLayoutParams(loaderParams);

        // Dynamic Custom Logo
        ImageView splashLogo = new ImageView(this);
        int logoId = getResources().getIdentifier("hk_logo", "drawable", getPackageName());
        if(logoId != 0) splashLogo.setImageResource(logoId);
        splashLogo.setLayoutParams(new LinearLayout.LayoutParams(400, 400)); 
        
        // Stylish Pulsing Animation
        AlphaAnimation pulse = new AlphaAnimation(1.0f, 0.7f);
        pulse.setDuration(800);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(Animation.INFINITE);
        splashLogo.startAnimation(pulse);
        loaderLayout.addView(splashLogo);

        // Stylish Colored Spinner (Neon Cyan)
        ProgressBar spinner = new ProgressBar(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            spinner.setIndeterminateTintList(ColorStateList.valueOf(Color.parseColor("#00f2fe")));
        }
        loaderLayout.addView(spinner);

        statusText = new TextView(this);
        statusText.setTextColor(Color.parseColor("#00f2fe")); 
        statusText.setTextSize(16); 
        statusText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        statusText.setPadding(0, 40, 0, 0); 
        statusText.setGravity(Gravity.CENTER);
        statusText.setText("PLEASE WAIT");
        loaderLayout.addView(statusText);

        layout.addView(loaderLayout);

        // --- 3. HYPER-SPEED REAL-TIME ENGINE & FIREBASE OAUTH SPOOFING ---
        WebSettings settings = hkView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true); // VERY IMPORTANT FOR FIREBASE AUTH
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT); 
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH); 
        settings.setSaveFormData(true);
        
        // 👉 FIREBASE BYPASS ENGINE: Removes "wv" tag. Makes Google think it's pure Chrome.
        String chromeAgent = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36";
        settings.setUserAgentString(chromeAgent);
        
        // Allow third-party cookies so Firebase can verify the Google session
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(hkView, true);
        
        // --- 4. HARDWARE PERMISSIONS ---
        hkView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }
        });

        // --- 5. WEBVIEW CLIENT (FIREBASE INTERNAL LOGIN HANDLER) ---
        hkView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Agar URL intent, mailto, ya whatsapp hai toh system ko de do
                if (url.startsWith("intent://") || url.startsWith("mailto:") || url.startsWith("whatsapp://")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    } catch (Exception e) {
                        return false; 
                    }
                }
                
                // 👉 HK-OPERATION: Google accounts.com ko ANDAR hi load hone do (return false)
                // Isse Firebase verification APK ke andar hi chalega bina white screen ke
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    return false; 
                }
                
                return false; 
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                hkView.setVisibility(View.GONE); 
                loaderLayout.setVisibility(View.VISIBLE);
                statusText.setText("SYNCING...");
                statusText.setTextColor(Color.parseColor("#39FF14")); // Green neon syncing
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loaderLayout.setVisibility(View.GONE);
                hkView.setVisibility(View.VISIBLE);
                statusText.setTextColor(Color.parseColor("#00f2fe")); // Cyan back
                CookieManager.getInstance().flush(); // Firebase Auth Token Saved to Device Memory
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

        // --- 6. AUTO-RECONNECT ENGINE ---
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    runOnUiThread(() -> {
                        if (internetDialog != null && internetDialog.isShowing()) {
                            internetDialog.dismiss(); 
                            statusText.setText("PLEASE WAIT...");
                            statusText.setTextColor(Color.parseColor("#00ff00"));
                            hkView.loadUrl(TARGET_URL); 
                        }
                    });
                }
            });
        }

        hkView.loadUrl(TARGET_URL);
    }

    // --- SECURE NO-INTERNET POPUP (GREEN PANEL / RED BUTTON) ---
    private void showNoInternetPopUp() {
        if (internetDialog != null && internetDialog.isShowing()) return;
        
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setBackgroundColor(Color.parseColor("#004D00")); 
        dialogLayout.setPadding(60, 80, 60, 80);
        dialogLayout.setGravity(Gravity.CENTER);
        
        TextView title = new TextView(this);
        title.setText("UPLINK DISCONNECTED");
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        dialogLayout.addView(title);
        
        TextView msg = new TextView(this);
        msg.setText("\nNetwork breach detected. Check connection.\n");
        msg.setTextColor(Color.parseColor("#A3FFA3"));
        msg.setTypeface(Typeface.MONOSPACE);
        msg.setGravity(Gravity.CENTER);
        dialogLayout.addView(msg);
        
        TextView retryBtn = new TextView(this);
        retryBtn.setText(" RETRY UPLINK ");
        retryBtn.setBackgroundColor(Color.parseColor("#FF0000")); 
        retryBtn.setTextColor(Color.WHITE);
        retryBtn.setPadding(30, 30, 30, 30);
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
        btnParams.setMargins(0, 40, 0, 0);
        retryBtn.setLayoutParams(btnParams);
        dialogLayout.addView(retryBtn);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogLayout);
        builder.setCancelable(false);
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
