package com.bible.hive;

import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;
import java.io.*;

@DesignerComponent(version = 1,
    description = "SmartBibleHive: High-speed internal data engine for real-time Bible rendering.",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "https://gaplink.net/smartadmob.png")
@SimpleObject(external = true)
public class SmartBibleHive extends AndroidNonvisibleComponent {

    private WebView webView;

    public SmartBibleHive(ComponentContainer container) {
        super(container.$form());
    }

    @SimpleFunction(description = "Initializes the Hive Bridge. Call this on Screen.Initialize.")
    public void InitializeHive(WebViewer webViewer) {
        this.webView = (WebView) webViewer.getView();
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.addJavascriptInterface(new HiveInterface(), "SmartBibleHive");
    }

    public class HiveInterface {
        @JavascriptInterface
        public void getBook(final String fileName) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String assetPath = "assets/bible/" + fileName;
                        InputStream is = SmartBibleHive.class.getClassLoader().getResourceAsStream(assetPath);
                        
                        if (is == null) {
                            sendErrorToJS("File not found: " + fileName);
                            return;
                        }

                        ByteArrayOutputStream result = new ByteArrayOutputStream();
                        byte[] buffer = new byte[16384];
                        int length;
                        while ((length = is.read(buffer)) != -1) {
                            result.write(buffer, 0, length);
                        }
                        is.close();

                        final String encodedData = Base64.encodeToString(result.toByteArray(), Base64.NO_WRAP);

                        webView.post(new Runnable() {
                            @Override
                            public void run() {
                                webView.loadUrl("javascript:window.receiveBookPayload('" + encodedData + "')");
                            }
                        });

                    } catch (Exception e) {
                        sendErrorToJS(e.getMessage());
                    }
                }
            }).start();
        }

        private void sendErrorToJS(final String error) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:console.error('Hive Error: " + error + "')");
                }
            });
        }
    }
}
