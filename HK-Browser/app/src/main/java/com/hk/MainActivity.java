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
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
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

// ⚠️ NO PLAY SERVICES AUTH IMPORTS HERE (GHOST PROTOCOL ACTIVATED)

public class MainActivity extends Activity {

    // --- HK-OPERATION: CORE SYSTEM VARIABLES ---
    private WebView hkView;
    private LinearLayout loaderLayout;
    private TextView statusText;
    private AlertDialog internetDialog;
    private final String TARGET_URL = "https://hk-mall-16bb9.web.app/";

    // --- HK-OPERATION: FILE UPLOAD MATRIX ---
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- 1. FULLSCREEN ALPHA UI SETUP (Pitch Black Fortress) ---
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, 
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );
        RelativeLayout layout = new RelativeLayout(this);
        layout.setBackgroundColor(Color.parseColor("#050505"));
        setContentView(layout);

        hkView = new WebView(this);
        hkView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        hkView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        
        // FOCUS ENGINE (For Autofill & Keyboard Injection)
        hkView.setFocusable(true);
        hkView.setFocusableInTouchMode(true);
        hkView.requestFocus(View.FOCUS_DOWN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            hkView.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
        }

        layout.addView(hkView, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        ));

        // --- 2. ELITE SPLASH LOADER (Custom Logo + Neon Pulse Animation) ---
        loaderLayout = new LinearLayout(this);
        loaderLayout.setOrientation(LinearLayout.VERTICAL);
        loaderLayout.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams loaderParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT, 
            RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        loaderParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        loaderLayout.setLayoutParams(loaderParams);

        // Dynamic Logo Setup
        ImageView splashLogo = new ImageView(this);
        int logoId = getResources().getIdentifier("hk_logo", "drawable", getPackageName());
        if (logoId != 0) {
            splashLogo.setImageResource(logoId);
        }
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(400, 400);
        logoParams.setMargins(0, 0, 0, 50);
        splashLogo.setLayoutParams(logoParams);
        
        // Pulse Animation for Tech Wizard Feel
        AlphaAnimation pulse = new AlphaAnimation(1.0f, 0.6f);
        pulse.setDuration(900);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(Animation.INFINITE);
        splashLogo.startAnimation(pulse);
        loaderLayout.addView(splashLogo);

        // Neon Cyan Spinner
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
        statusText.setText("SYSTEM INITIALIZING...");
        loaderLayout.addView(statusText);

        layout.addView(loaderLayout);

        // --- 3. 👉 THE GHOST ENGINE (OAUTH BYPASS & PERFORMANCE MATRIX) ---
        WebSettings settings = hkView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true); // Critical for Firebase Tokens
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setSaveFormData(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setGeolocationEnabled(true); // For Delivery Location
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(false); 
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // 👉 HK-OPERATION: DEEP USER-AGENT SPOOFING (Chrome Pixel 8)
        // This is the lethal line that completely bypasses Google's WebView Block
        String ghostChromeAgent = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36";
        settings.setUserAgentString(ghostChromeAgent);

        // Session Memory Lock
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(hkView, true);

        // --- 4. HK-OPERATION: DOWNLOAD MANAGER ---
        hkView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimetype);
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading encrypted file...");
                request.setTitle(url.substring(url.lastIndexOf("/") + 1));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.substring(url.lastIndexOf("/") + 1));
                
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                if (dm != null) {
                    dm.enqueue(request);
                    Toast.makeText(getApplicationContext(), "Download Sequence Initiated...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // --- 5. PERMISSIONS & FILE UPLOADS (WEB CHROME CLIENT) ---
        hkView.setWebChromeClient(new WebChromeClient() {
            // Geolocation Bypass
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            // General Hardware Permissions
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }

            // File Upload Engine (Modern Android)
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (uploadMessage != null) { 
                    uploadMessage.onReceiveValue(null); 
                    uploadMessage = null; 
                }
                uploadMessage = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                try { 
                    startActivityForResult(intent, REQUEST_SELECT_FILE); 
                } catch (Exception e) { 
                    uploadMessage = null;
                    Toast.makeText(getApplicationContext(), "File System Locked", Toast.LENGTH_SHORT).show();
                    return false; 
                }
                return true;
            }

            // File Upload Engine (Legacy Android)
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select File"), FILECHOOSER_RESULTCODE);
            }
        });

        // --- 6. THE WEBVIEW CLIENT (ROUTING MATRIX) ---
        hkView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // External Triggers (WhatsApp, Phone, Email)
                if (url.startsWith("intent://") || url.startsWith("whatsapp://") || url.startsWith("mailto:") || url.startsWith("tel:")) {
                    try { 
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); 
                        return true; 
                    } catch (Exception e) { 
                        return false; 
                    }
                }
                
                // Allow EVERYTHING inside the WebView (Ghost Chrome handles the rest)
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                hkView.setVisibility(View.GONE);
                loaderLayout.setVisibility(View.VISIBLE);
                statusText.setText("SYNCING DATA...");
                statusText.setTextColor(Color.parseColor("#39FF14")); // Hacker Green
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loaderLayout.setVisibility(View.GONE);
                hkView.setVisibility(View.VISIBLE);
                statusText.setTextColor(Color.parseColor("#00f2fe")); // Cyan Reset
                
                // FLUSH MEMORY: Permanently locks the user session inside the app
                CookieManager.getInstance().flush(); 
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

        // --- 7. AUTO-RECONNECT ENGINE (NETWORK LISTENER) ---
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        runOnUiThread(() -> {
                            if (internetDialog != null && internetDialog.isShowing()) {
                                internetDialog.dismiss();
                                statusText.setText("UPLINK RESTORED...");
                                statusText.setTextColor(Color.parseColor("#00ff00"));
                                hkView.loadUrl(TARGET_URL);
                            }
                        });
                    }
                });
            }
        }
        
        // INITIAL FIRE
        hkView.loadUrl(TARGET_URL);
    }

    // --- 8. UPLOAD RESULT HANDLER ---
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null) return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) return;
            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else {
            Toast.makeText(this, "Upload Terminated", Toast.LENGTH_SHORT).show();
        }
    }

    // --- 9. SECURE NO-INTERNET POPUP (HACKER UI) ---
    private void showNoInternetPopUp() {
        if (internetDialog != null && internetDialog.isShowing()) return;
        
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setBackgroundColor(Color.parseColor("#002200")); // Dark hacker green bg
        dialogLayout.setPadding(60, 80, 60, 80);
        dialogLayout.setGravity(Gravity.CENTER);
        
        TextView title = new TextView(this);
        title.setText("NETWORK BREACH");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        dialogLayout.addView(title);
        
        TextView msg = new TextView(this);
        msg.setText("\nUplink lost. System waiting for secure connection...\n");
        msg.setTextColor(Color.parseColor("#39FF14")); // Neon green text
        msg.setTextSize(14);
        msg.setTypeface(Typeface.MONOSPACE);
        msg.setGravity(Gravity.CENTER);
        dialogLayout.addView(msg);
        
        TextView retryBtn = new TextView(this);
        retryBtn.setText(" FORCE RECONNECT ");
        retryBtn.setBackgroundColor(Color.parseColor("#FF0000")); // Aggressive Red
        retryBtn.setTextColor(Color.WHITE);
        retryBtn.setPadding(40, 35, 40, 35);
        retryBtn.setTextSize(16);
        retryBtn.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        
        retryBtn.setOnClickListener(v -> { 
            hkView.loadUrl(TARGET_URL); 
            if (internetDialog != null) internetDialog.dismiss(); 
        });
        
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnParams.setMargins(0, 40, 0, 0);
        retryBtn.setLayoutParams(btnParams);
        dialogLayout.addView(retryBtn);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogLayout);
        builder.setCancelable(false);
        internetDialog = builder.create();
        
        if (internetDialog.getWindow() != null) {
            internetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        internetDialog.show();
    }

    // --- 10. CUSTOM SECURE EXIT & GESTURE LOGIC ---
    @Override
    public void onBackPressed() {
        if (hkView.canGoBack()) {
            hkView.goBack(); 
        } else {
            super.onBackPressed();
        }
    }
}
