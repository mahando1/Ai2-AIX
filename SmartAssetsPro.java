package com.assets.pro;

import android.util.Base64;
import android.webkit.WebView;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;
import java.io.*;
import java.io.ByteArrayOutputStream;

@DesignerComponent(version = 6,
    description = "SmartAssetsPro: Final Optimized Version. Fixes JSON spacing and Watermark Icon loading.",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "https://gaplink.net/smartadmob.png")
@SimpleObject(external = true)
public class SmartAssetsPro extends AndroidNonvisibleComponent {

    private WebView targetWebView;

    public SmartAssetsPro(ComponentContainer container) {
        super(container.$form());
    }

    @SimpleFunction(description = "Injects all data. JSON keeps spaces; Icons are cleaned for watermarks.")
    public void InitializeData(WebViewer webViewer) {
        this.targetWebView = (WebView) webViewer.getView();
        try {
            // 1. Read JSON - We keep internal spaces for the verses
            String dailyDb = readRawAsset("assets/internal/daily_db.json").trim();
            
            // 2. Read Icons - We strip ALL whitespace so Base64 works for the Canvas Watermark
            String appIcon = readRawAsset("assets/internal/app_icon.txt").replaceAll("\\s+", "");
            String bookIcon = readRawAsset("assets/internal/book_icon.txt").replaceAll("\\s+", "");

            String payload = "{\"json\":" + dailyDb + 
                             ",\"appIcon\":\"" + appIcon + "\"" + 
                             ",\"bookIcon\":\"" + bookIcon + "\"}";

            final String encodedPayload = Base64.encodeToString(payload.getBytes("UTF-8"), Base64.NO_WRAP);

            if (targetWebView != null) {
                targetWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        targetWebView.loadUrl("javascript:window.processBase64Data('" + encodedPayload + "')");
                    }
                });
            }
        } catch (Exception e) {
            System.out.println("SmartAssetsPro Error: " + e.getMessage());
        }
    }

    private String readRawAsset(String path) throws IOException {
        InputStream is = SmartAssetsPro.class.getClassLoader().getResourceAsStream(path);
        if (is == null) return "";
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int length;
            while ((length = is.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString("UTF-8");
        } finally {
            if (is != null) is.close();
        }
    }
}