package com.hk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
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
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.*;
import android.widget.*;

// 👉 HK-CORE: NATIVE GOOGLE AUTH IMPORTS
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class MainActivity extends Activity {

    // --- HK-MALL SYSTEM CONSTANTS ---
    private static final String TARGET_URL = "https://hk-mall-16bb9.web.app/";
    private static final String WEB_CLIENT_ID = "172778880682-t1ucts0ar6lqrl0klnkv2620nf46ukbv.apps.googleusercontent.com";
    private static final int RC_SIGN_IN = 9001;
    private static final int REQUEST_SELECT_FILE = 100;

    // --- SYSTEM VARIABLES ---
    private WebView hkView;
    private LinearLayout loaderLayout;
    private TextView statusText;
    private AlertDialog internetDialog;
    private GoogleSignInClient mGoogleSignInClient;
    public ValueCallback<Uri[]> uploadMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🛡️ FULLSCREEN MATRIX UI (Hardware Accelerated for Speed)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );

        RelativeLayout mainLayout = new RelativeLayout(this);
        mainLayout.setBackgroundColor(Color.parseColor("#050505")); // Deep Dark Stealth
        setContentView(mainLayout);

        hkView = new WebView(this);
        mainLayout.addView(hkView, new RelativeLayout.LayoutParams(-1, -1));

        // INITIALIZE ALL SYSTEMS
        setupLoader(mainLayout);
        setupGoogle();
        setupWebSettings();
        setupHandlers();
        setupNetwork();

        // EXECUTE TARGET
        hkView.loadUrl(TARGET_URL);
    }

    private void setupLoader(RelativeLayout layout) {
        loaderLayout = new LinearLayout(this);
        loaderLayout.setOrientation(LinearLayout.VERTICAL);
        loaderLayout.setGravity(Gravity.CENTER);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(-2, -2);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);

        // ⚡ PULSING LOGO
        ImageView logo = new ImageView(this);
        int resId = getResources().getIdentifier("hk_logo", "drawable", getPackageName());
        if (resId != 0) logo.setImageResource(resId);
        logo.setLayoutParams(new LinearLayout.LayoutParams(400, 400));
        
        AlphaAnimation pulse = new AlphaAnimation(1.0f, 0.4f);
        pulse.setDuration(900);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(Animation.INFINITE);
        logo.startAnimation(pulse);

        ProgressBar bar = new ProgressBar(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bar.setIndeterminateTintList(ColorStateList.valueOf(Color.parseColor("#00f2fe")));
        }

        statusText = new TextView(this);
        statusText.setTextColor(Color.parseColor("#00f2fe"));
        statusText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        statusText.setText("HK SYSTEM INITIALIZING...");
        statusText.setPadding(0, 30, 0, 0);
        statusText.setGravity(Gravity.CENTER);

        loaderLayout.addView(logo);
        loaderLayout.addView(bar);
        loaderLayout.addView(statusText);
        layout.addView(loaderLayout, lp);
    }

    private void setupGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
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
        settings.setGeolocationEnabled(true); // Added for location tracking
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // ELITE USER AGENT SPOOFING
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(hkView, true);
    }

    private void setupHandlers() {
        // 📥 SECURE DOWNLOAD MANAGER INTERCEPTOR
        hkView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setMimeType(mimetype);
            request.addRequestHeader("cookie", CookieManager.getInstance().getCookie(url));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.substring(url.lastIndexOf("/") + 1));
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (dm != null) { dm.enqueue(request); Toast.makeText(getApplicationContext(), "HK-MALL: Download Initiated...", Toast.LENGTH_SHORT).show(); }
        });

        hkView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false); // Auto-grant location to website
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                uploadMessage = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                startActivityForResult(intent, REQUEST_SELECT_FILE);
                return true;
            }
        });

        hkView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 👉 GOOGLE AUTH BYPASS TRIGGER
                if (url.contains("accounts.google.com") || url.contains("gsi/")) {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                    return true;
                }
                
                // 👉 EXTERNAL APP TRIGGER (WhatsApp, Call, Mail)
                if (url.startsWith("intent://") || url.startsWith("whatsapp://") || url.startsWith("mailto:") || url.startsWith("tel:")) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); return true; } catch (Exception e) { return false; }
                }

                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loaderLayout.setVisibility(View.VISIBLE);
                hkView.setVisibility(View.GONE);
                statusText.setText("SYNCING DATA...");
                statusText.setTextColor(Color.parseColor("#39FF14")); // Matrix Green
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loaderLayout.setVisibility(View.GONE);
                hkView.setVisibility(View.VISIBLE);
                CookieManager.getInstance().flush();
                // 🚩 FLAG FOR REACT/WEBSITE
                hkView.evaluateJavascript("window.isAndroidApp=true;", null);
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

        // 🛡️ NATIVE GOOGLE LOGIN HANDLER
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();

                // ALPHA BRANDING TOAST
                Toast.makeText(this, "HK-MALL: Access Granted. Tech Wizard Live!", Toast.LENGTH_LONG).show();

                // 👉 TERA PROFESSIONAL 'POSTMESSAGE' LOGIC
                String js = "window.postMessage({type:'ANDROID_LOGIN',token:'" + idToken + "'},'*');";
                hkView.evaluateJavascript(js, null);

            } catch (Exception e) {
                Toast.makeText(this, "HK-MALL: Auth Intercept Failed!", Toast.LENGTH_LONG).show();
            }
        }

        // 📂 FILE UPLOAD HANDLER
        if (requestCode == REQUEST_SELECT_FILE) {
            if (uploadMessage == null) return;
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
                        if (internetDialog != null && internetDialog.isShowing()) internetDialog.dismiss();
                        hkView.reload();
                    });
                }
            });
        }
    }

    // 🚨 ELITE NETWORK BREACH DIALOG (Tera purana professional dialog added back)
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
