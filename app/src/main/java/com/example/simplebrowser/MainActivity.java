package com.example.simplebrowser;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private EditText addressBar;
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildInterface();
        configureWebView();
        loadUrl("https://www.google.com");
    }

    private void buildInterface() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        LinearLayout addressRow = new LinearLayout(this);
        addressRow.setGravity(Gravity.CENTER_VERTICAL);
        addressRow.setPadding(10, 8, 10, 4);

        addressBar = new EditText(this);
        addressBar.setSingleLine(true);
        addressBar.setHint("輸入網址，例如 example.com");
        addressBar.setTextSize(16);
        addressBar.setImeOptions(EditorInfo.IME_ACTION_GO);
        addressRow.addView(addressBar, new LinearLayout.LayoutParams(0, 52, 1));

        Button goButton = new Button(this);
        goButton.setText("前往");
        goButton.setOnClickListener(v -> loadUrl(addressBar.getText().toString()));
        addressRow.addView(goButton, new LinearLayout.LayoutParams(76, 52));
        root.addView(addressRow);

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.setPadding(8, 0, 8, 2);
        addControl(controls, "‹", v -> { if (webView.canGoBack()) webView.goBack(); });
        addControl(controls, "›", v -> { if (webView.canGoForward()) webView.goForward(); });
        addControl(controls, "↻", v -> webView.reload());
        TextView title = new TextView(this);
        title.setText("  簡易瀏覽器");
        title.setTextSize(14);
        title.setTextColor(Color.DKGRAY);
        controls.addView(title, new LinearLayout.LayoutParams(0, 44, 1));
        root.addView(controls);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        root.addView(progressBar, new LinearLayout.LayoutParams(-1, 3));

        webView = new WebView(this);
        root.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        addressBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) { loadUrl(addressBar.getText().toString()); return true; }
            return false;
        });
    }

    private void addControl(LinearLayout parent, String text, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(20);
        button.setOnClickListener(listener);
        parent.addView(button, new LinearLayout.LayoutParams(56, 44));
    }

    private void configureWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) { return false; }
            @Override public void onPageFinished(WebView view, String url) { addressBar.setText(url); }
        });
    }

    private void loadUrl(String raw) {
        String url = raw == null ? "" : raw.trim();
        if (url.isEmpty()) return;
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "https://" + url;
        addressBar.setText(url);
        webView.loadUrl(url);
    }

    @Override public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
