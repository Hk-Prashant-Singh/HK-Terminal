package com.hk;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    
    private WebView hkView;
    private LinearLayout loaderLayout;
    private TextView mainErrorText, waitingText, systemErrorText, statusAlertText;
    private ImageView brokenChainIcon;
    private TextView copyButton; // New Copy Button Inject
    private final String TARGET_URL = "https://hk-love.netlify.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Pitch Black Background Setup
        RelativeLayout layout = new RelativeLayout(this);
        layout.setBackgroundColor(Color.parseColor("#050505")); 
        setContentView(layout);

        // WebView Setup (Layer adjusted for custom visuals)
        hkView = new WebView(this);
        hkView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        layout.addView(hkView, new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, 
            RelativeLayout.LayoutParams.MATCH_PARENT
        ));

        // HK-Operation: Custom Professional Error Layout
        loaderLayout = new LinearLayout(this);
        loaderLayout.setOrientation(LinearLayout.VERTICAL);
        loaderLayout.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        loaderLayout.setPadding(0, 100, 0, 0); // Spacing for professional look
        RelativeLayout.LayoutParams loaderParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, 
            RelativeLayout.LayoutParams.MATCH_PARENT
        );
        loaderLayout.setLayoutParams(loaderParams);

        // Android Icon - Static Professional Top placement
        ImageView androidIcon = new ImageView(this);
        // androidIcon.setImageResource(R.drawable.android_head_neon_green); // Target Visual Asset Reference
        // androidIcon.setPadding(0, 50, 0, 0); // Large spacing for layout (Adjust based on asset)
        loaderLayout.addView(androidIcon);

        // Target Visuals implementation based on provided image structure
        
        // 1. Large Bold Status Text (Primary Focus)
        mainErrorText = new TextView(this);
        mainErrorText.setTextSize(36); // LARGE AS REQUESTED
        mainErrorText.setTextColor(Color.parseColor("#00f2fe")); // Neon Cyan
        mainErrorText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        mainErrorText.setPadding(0, 80, 0, 0); // Spacing for prominence
        mainErrorText.setGravity(Gravity.CENTER);
        loaderLayout.addView(mainErrorText);

        // 2. Waiting Text (Subtitle Pulse reference)
        waitingText = new TextView(this);
        waitingText.setTextColor(Color.parseColor("#00f2fe")); // Neon Cyan pulse initial
        waitingText.setTextSize(18);
        waitingText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        waitingText.setPadding(0, 20, 0, 0);
        waitingText.setGravity(Gravity.CENTER);
        loaderLayout.addView(waitingText);

        // 3. Broken Connection Chain Icon - Animation Reference Target Visual
        brokenChainIcon = new ImageView(this);
        // brokenChainIcon.setImageResource(R.drawable.broken_neon_chain_animation); // Targeted Visual Asset
        brokenChainIcon.setPadding(0, 100, 0, 0); // Large professional spacing
        loaderLayout.addView(brokenChainIcon);

        // 4. Description Text (Static Target logic structure)
        TextView descriptiveText = new TextView(this);
        descriptiveText.setTextColor(Color.WHITE);
        descriptiveText.setTextSize(16);
        descriptiveText.setTypeface(Typeface.MONOSPACE);
        descriptiveText.setGravity(Gravity.CENTER);
        descriptiveText.setPadding(60, 100, 60, 0); // Large horizontal padding for visual balance
        descriptiveText.setText("The web page at https://hk-love.netlify.app/ could not be loaded.");
        loaderLayout.addView(descriptiveText);

        // 5. System Error Text (Structure from provided image)
        systemErrorText = new TextView(this);
        systemErrorText.setTextColor(Color.parseColor("#ff003c")); // Red Alert
        systemErrorText.setTextSize(16);
        systemErrorText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        systemErrorText.setGravity(Gravity.CENTER);
        systemErrorText.setPadding(0, 50, 0, 0);
        loaderLayout.addView(systemErrorText);

        // 6. Status Alert Text (Red Alert block structure)
        statusAlertText = new TextView(this);
        statusAlertText.setTextColor(Color.parseColor("#ff003c")); // Red Alert
        statusAlertText.setTextSize(16);
        statusAlertText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        statusAlertText.setGravity(Gravity.CENTER);
        statusAlertText.setPadding(0, 20, 0, 20);
        loaderLayout.addView(statusAlertText);

        // 7. NEW COPY BUTTON INJECTION (Stylish & Professional)
        copyButton = new TextView(this);
        copyButton.setText("[ COPY SYSTEM LOG ]");
        copyButton.setTextColor(Color.parseColor("#050505")); // Dark Text for contrast
        copyButton.setTextSize(14);
        copyButton.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        copyButton.setGravity(Gravity.CENTER);
        copyButton.setPadding(50, 25, 50, 25);

        // Elite Neon Cyan Background Shape
        GradientDrawable btnShape = new GradientDrawable();
        btnShape.setShape(GradientDrawable.RECTANGLE);
        btnShape.setColor(Color.parseColor("#00f2fe")); 
        btnShape.setCornerRadius(12f);
        copyButton.setBackground(btnShape);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnParams.setMargins(0, 80, 0, 0); // Spacing below alerts
        btnParams.gravity = Gravity.CENTER;
        copyButton.setLayoutParams(btnParams);
        
        copyButton.setVisibility(View.GONE); // Hide initially

        // Copy Engine Logic
        copyButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("HK_LOG", "Target: " + TARGET_URL + "\nError: net::ERR_INTERNET_DISCONNECTED\nStatus: OFFLINE");
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "SYSTEM LOG COPIED TO CLIPBOARD", Toast.LENGTH_SHORT).show();
            }
        });
        
        loaderLayout.addView(copyButton);

        layout.addView(loaderLayout);

        // Pulse Animation for specific texts/icons
        Animation pulseAnim = new AlphaAnimation(1.0f, 0.5f);
        pulseAnim.setDuration(1000); // 1-second pulse
        pulseAnim.setStartOffset(20);
        pulseAnim.setRepeatMode(Animation.REVERSE);
        pulseAnim.setRepeatCount(Animation.INFINITE);

        // Apply pulse animations to target references based on visualization provided
        waitingText.startAnimation(pulseAnim);
        // brokenChainIcon.startAnimation(pulseAnim); // Uncomment when asset is added
        
        // Targeted text visibility and content logic injection onReceivedError
        // (Visual reference of image_2.png replicated in structure)

        // Elite Web Settings (UNTOUCHED)
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
                
                // Clear error specific text/asset visuals on load start
                // mainErrorText.setText("");
                // waitingText.setText("");
                // systemErrorText.setText("");
                // statusAlertText.setText("");
                // brokenChainIcon.setVisibility(View.GONE);
                copyButton.setVisibility(View.GONE); // Hide copy button while loading

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
                    
                    // Specific Target Hit on Alert Text Block structure reference from image_2.png
                    mainErrorText.setText("NO INTERNET CONNECTION");
                    waitingText.setText("WAITING FOR NETWORK...");
                    systemErrorText.setText("SYSTEM ERROR: net::ERR_INTERNET_DISCONNECTED");
                    statusAlertText.setText("STATUS: OFFLINE");
                    copyButton.setVisibility(View.VISIBLE); // Trigger Copy Button Activation
                    
                    // Specific asset visual activation (broken neon chain visual target visual)
                    // brokenChainIcon.setVisibility(View.VISIBLE);

                }
            }
        });

        // 🚀 THE MAGIC: Auto-Detect Network Engine (UNTOUCHED)
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    runOnUiThread(() -> {
                        // Data ON hote hi ye trigger hoga
                        // statusText.setText("NETWORK DETECTED! RELOADING...");
                        // statusText.setTextColor(Color.parseColor("#00ff00")); // Hacker Green
                        // hkView.loadUrl(TARGET_URL); // Auto Reload
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
}
            
