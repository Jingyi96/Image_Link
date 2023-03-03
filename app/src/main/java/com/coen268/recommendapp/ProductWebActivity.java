package com.coen268.recommendapp;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class ProductWebActivity extends AppCompatActivity {
    protected WebView mWebView;

    public static final String Extra_PRODUCT_WEB_URL = "Extra_PRODUCT_WEB_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_product_web);
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(getIntent().getStringExtra(Extra_PRODUCT_WEB_URL));
//        mWebView.loadUrl("https://www.ebay.com/itm/Oversized-Round-Women-Fashion-Sunglasses-Big-Trendy-Ladies-Designer-Shades-UV400-/153455990175");
    }
}