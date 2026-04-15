package com.hk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.*;
import android.widget.*;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class MainActivity extends Activity {

    // --- HK-MALL CORE TARGETS ---
    private static final String TARGET_URL = "https://hk-mall-16bb9.web.app/";
    private static final String WEB_CLIENT_ID = "172778880682-t1ucts0ar6lqrl0klnkv2620nf46ukbv.apps.googleusercontent.com";
    private static final int RC_SIGN_IN = 9001;
    private static final int REQUEST_SELECT_FILE = 100;

    private WebView hkView;
    private LinearLayout loaderLayout;
    private View hkDot; 
    private AlertDialog internetDialog;
    private GoogleSignInClient mGoogleSignInClient;
    public ValueCallback<Uri[]> uploadMessage;
    
    // 🛡️ HK NATIVE VAULT
    private SharedPreferences hkElitePrefs;

    // 🛡️ THE ALPHA BRIDGE: Direct WebView to Native Hijack
    public class WebAppInterface {
        Context mContext;
        WebAppInterface(Context c) { mContext = c; }

        @JavascriptInterface
        public void triggerGoogleLogin() {
            // MANUAL TRIGGER ONLY - FORCE ACCOUNT PICKER
            if (mGoogleSignInClient != null) {
                mGoogleSignInClient.signOut(); // ⚡ System purane session ko kill karega taki har bar ID puche
            }
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            ((Activity)mContext).startActivityForResult(signInIntent, RC_SIGN_IN);
        }

        // ⚡ NATIVE LOGOUT DESTROYER
        @JavascriptInterface
        public void triggerGoogleLogout() {
            if (mGoogleSignInClient != null) {
                mGoogleSignInClient.signOut(); 
            }
            hkElitePrefs.edit().clear().apply(); 
            ((Activity)mContext).runOnUiThread(() -> {
                Toast.makeText(mContext, "HK-SYSTEM: Native Session Destroyed", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hkElitePrefs = getSharedPreferences("HK_ELITE_VAULT", MODE_PRIVATE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        RelativeLayout mainLayout = new RelativeLayout(this);
        mainLayout.setBackgroundColor(Color.parseColor("#050505")); 
        setContentView(mainLayout);

        hkView = new WebView(this);
        mainLayout.addView(hkView, new RelativeLayout.LayoutParams(-1, -1));

        setupLoader(mainLayout);
        setupGoogle();
        setupWebSettings();
        setupHandlers();
        setupNetwork();

        hkView.loadUrl(TARGET_URL);
    }

    // --- UI SETUP: Modified Loading Screen ---
    private void setupLoader(RelativeLayout layout) {
        loaderLayout = new LinearLayout(this);
        loaderLayout.setOrientation(LinearLayout.VERTICAL);
        loaderLayout.setGravity(Gravity.CENTER);

        // 1. ORIGINAL HK LOGO (Blinking)
        ImageView logo = new ImageView(this);
        int resId = getResources().getIdentifier("hk_logo", "drawable", getPackageName());
        if (resId != 0) logo.setImageResource(resId);
        logo.setLayoutParams(new LinearLayout.LayoutParams(450, 450));
        
        AlphaAnimation logoPulse = new AlphaAnimation(1.0f, 0.4f);
        logoPulse.setDuration(800);
        logoPulse.setRepeatMode(Animation.REVERSE);
        logoPulse.setRepeatCount(Animation.INFINITE);
        logo.startAnimation(logoPulse);

        // 2. SMALL ORANGE DOT (Blinking Below Logo)
        hkDot = new View(this);
        GradientDrawable dotShape = new GradientDrawable();
        dotShape.setShape(GradientDrawable.OVAL);
        dotShape.setColor(Color.parseColor("#FF8C00")); // Dark Orange Emoji Color
        hkDot.setBackground(dotShape);

        int dotSize = 40; 
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dotSize, dotSize);
        dotParams.topMargin = 40; // Space below logo
        hkDot.setLayoutParams(dotParams);

        AlphaAnimation dotPulse = new AlphaAnimation(1.0f, 0.3f); 
        dotPulse.setDuration(600); 
        dotPulse.setRepeatMode(Animation.REVERSE); 
        dotPulse.setRepeatCount(Animation.INFINITE); 
        hkDot.startAnimation(dotPulse);

        // 3. ⚡ [UPDATED] STABLE TEXT (Below Orange Dot)
        TextView stableText = new TextView(this);
        stableText.setText("Rs Mall"); // Text is replaced as per Alpha Command
        stableText.setTextColor(Color.WHITE); 
        stableText.setGravity(Gravity.CENTER);
        stableText.setTextSize(14f); 
        stableText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD); 

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.topMargin = 20; 
        stableText.setLayoutParams(textParams);

        // ADD ALL TO LOADER LAYOUT
        loaderLayout.addView(logo);
        loaderLayout.addView(hkDot);
        loaderLayout.addView(stableText); 
        
        layout.addView(loaderLayout, new RelativeLayout.LayoutParams(-1, -1));
    }

    private void setupGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .requestProfile() // System will explicitly ask for Profile info now
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupWebSettings() {
        WebSettings settings = hkView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setGeolocationEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36");
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        
        hkView.addJavascriptInterface(new WebAppInterface(this), "AndroidHost");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(hkView, true);
    }

    private void setupHandlers() {
        hkView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.substring(url.lastIndexOf("/") + 1));
            ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(r);
            Toast.makeText(this, "RIDDHI SIDDHI: Encrypted Download Started", Toast.LENGTH_SHORT).show();
        });

        hkView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                uploadMessage = filePathCallback;
                startActivityForResult(fileChooserParams.createIntent(), REQUEST_SELECT_FILE);
                return true;
            }
        });

        hkView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("intent://")) { return true; } 
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loaderLayout.setVisibility(View.VISIBLE);
                hkView.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loaderLayout.setVisibility(View.GONE);
                hkView.setVisibility(View.VISIBLE);
                CookieManager.getInstance().flush();
            }
            
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) { view.loadUrl("about:blank"); showNoInternetPopUp(); }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();
                String userEmail = account.getEmail();

                hkElitePrefs.edit().putString("SECURE_EMAIL", userEmail).putString("SECURE_TOKEN", idToken).apply();

                Toast.makeText(this, "RIDDHI SIDDHI: Decrypting Alpha Token...", Toast.LENGTH_SHORT).show();

                String js = "javascript:(function() { " +
                            "if(window.handleAndroidLogin) { window.handleAndroidLogin('" + idToken + "'); }" +
                            "else { alert('System Breach: Master Receiver Not Found'); }" +
                            "})()";
                hkView.evaluateJavascript(js, null);

                // FORCE AUTOMATIC REFRESH
                new Handler().postDelayed(() -> {
                    Toast.makeText(MainActivity.this, "Tech Wizard System Refreshing...", Toast.LENGTH_SHORT).show();
                    hkView.reload();
                }, 2500);

            } catch (Exception e) {
                Toast.makeText(this, "Auth Intercept Failed! Check SHA-1 Key.", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == REQUEST_SELECT_FILE && uploadMessage != null) {
            uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            uploadMessage = null;
        }
    }

    private void setupNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && Build.VERSION.SDK_INT >= 24) {
            cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    runOnUiThread(() -> {
                        if (internetDialog != null) internetDialog.dismiss();
                        hkView.reload();
                    });
                }
            });
        }
    }

    private void showNoInternetPopUp() {
        if (internetDialog != null && internetDialog.isShowing()) return;
        LinearLayout dLayout = new LinearLayout(this);
        dLayout.setOrientation(LinearLayout.VERTICAL);
        dLayout.setBackgroundColor(Color.parseColor("#002200")); 
        dLayout.setPadding(60, 80, 60, 80);
        dLayout.setGravity(Gravity.CENTER);
        
        TextView title = new TextView(this);
        title.setText("NETWORK BREACH");
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        
        TextView retryBtn = new TextView(this);
        retryBtn.setText(" FORCE RECONNECT ");
        retryBtn.setPadding(40, 35, 40, 35);
        retryBtn.setBackgroundColor(Color.parseColor("#FF0000")); 
        retryBtn.setTextColor(Color.WHITE);
        
        retryBtn.setOnClickListener(v -> { hkView.loadUrl(TARGET_URL); if (internetDialog != null) internetDialog.dismiss(); });
        
        dLayout.addView(title);
        dLayout.addView(retryBtn);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dLayout).setCancelable(false);
        internetDialog = builder.create();
        internetDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (hkView.canGoBack()) hkView.goBack(); else super.onBackPressed();
    }
}
