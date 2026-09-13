package com.tradelab.mobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String APP_URL =
            "https://trading-simulator-t736.onrender.com/?hl=en-IN";

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();

        // Keep content inside the Android system-bar safe area.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true);
        }

        // Dark Android system bars.
        window.setStatusBarColor(Color.rgb(5, 7, 10));
        window.setNavigationBarColor(Color.rgb(5, 7, 10));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(false);
            window.setStatusBarContrastEnforced(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = window.getInsetsController();

            if (controller != null) {
                controller.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                                | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }

        // Root container.
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(5, 7, 10));

        // WebView.
        webView = new WebView(this);

        FrameLayout.LayoutParams webParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );

        root.addView(webView, webParams);
        setContentView(root);

        /*
         * Explicitly move the WebView below the status bar
         * and above the navigation/gesture bar.
         */
        root.setOnApplyWindowInsetsListener((view, insets) -> {

            int topInset = 0;
            int bottomInset = 0;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                android.graphics.Insets bars =
                        insets.getInsets(
                                WindowInsets.Type.systemBars()
                        );

                topInset = bars.top;
                bottomInset = bars.bottom;

            } else {

                topInset = insets.getSystemWindowInsetTop();
                bottomInset = insets.getSystemWindowInsetBottom();
            }

            FrameLayout.LayoutParams params =
                    (FrameLayout.LayoutParams) webView.getLayoutParams();

            params.leftMargin = 0;
            params.rightMargin = 0;
            params.topMargin = topInset;
            params.bottomMargin = bottomInset;

            webView.setLayoutParams(params);

            return insets;
        });

        WebSettings s = webView.getSettings();

        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);

        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);

        s.setLoadsImagesAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        s.setUserAgentString(
                s.getUserAgentString() + " TradeLabAndroid/1.2"
        );

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance()
                .setAcceptThirdPartyCookies(webView, true);

        webView.setBackgroundColor(Color.rgb(5, 7, 10));

        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request) {

                Uri u = request.getUrl();
                String scheme = u.getScheme();

                if (scheme != null &&
                        (scheme.equals("http")
                                || scheme.equals("https"))) {
                    return false;
                }

                try {
                    startActivity(
                            new Intent(Intent.ACTION_VIEW, u)
                    );
                } catch (ActivityNotFoundException ignored) {
                }

                return true;
            }
        });

        webView.setDownloadListener(
                new DownloadListener() {

                    @Override
                    public void onDownloadStart(
                            String url,
                            String userAgent,
                            String contentDisposition,
                            String mimetype,
                            long contentLength) {

                        try {

                            Intent i = new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(url)
                            );

                            startActivity(i);

                        } catch (Exception e) {

                            Toast.makeText(
                                    MainActivity.this,
                                    "Download link opened in browser",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                }
        );

        webView.loadUrl(APP_URL);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onPause() {

        if (webView != null) {
            webView.onPause();
        }

        super.onPause();
    }

    @Override
    public void onBackPressed() {

        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {

        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }

        super.onDestroy();
    }
}
