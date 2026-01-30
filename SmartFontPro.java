package com/font/pro;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.Form;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

@DesignerComponent(version = 1,
    description = "Extension to load custom fonts from its internal assets into a WebViewer.",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png") // Path relative to the AIX root
@SimpleObject(external = true)
public class SmartFontPro extends AndroidNonvisibleComponent {

    private Form form;
    private WebViewer webViewer;
    private static final String FONT_ASSET_PREFIX = "assets/fonts/";

    public SmartFontPro(ComponentContainer container) {
        super(container.());
        this.form = container.();
    }

    @SimpleFunction(description = "Sets the WebViewer component for font injection.")
    public void SetWebViewer(WebViewer targetWebViewer) {
        this.webViewer = targetWebViewer;
        if (this.webViewer != null) {
            WebView androidWebView = (WebView) targetWebViewer.getView();
            androidWebView.setWebViewClient(new FontLoadingWebViewClient());
            androidWebView.getSettings().setAllowFileAccess(true);
        }
    }

    private class FontLoadingWebViewClient extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (url.contains("/fonts/") && url.endsWith(".woff2")) {
                String fileName = url.substring(url.lastIndexOf("/") + 1);
                String assetPath = FONT_ASSET_PREFIX + fileName;
                try {
                    InputStream is = SmartFontPro.class.getClassLoader().getResourceAsStream(assetPath);
                    if (is != null) {
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        int nRead;
                        byte[] data = new byte[1024];
                        while ((nRead = is.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }
                        buffer.flush();
                        byte[] fontBytes = buffer.toByteArray();
                        is.close();
                        // You can uncomment the line below for debugging output in Logcat if needed
                        // android.util.Log.d("smartfontpro", "Serving font: " + fileName);
                        return new WebResourceResponse("font/woff2", "UTF-8", new ByteArrayInputStream(fontBytes));
                    } else {
                        // android.util.Log.w("smartfontpro", "Font not found in internal assets: " + assetPath);
                    }
                } catch (IOException e) {
                    // android.util.Log.e("smartfontpro", "Error loading font " + assetPath + ": " + e.getMessage());
                }
            }
            return super.shouldInterceptRequest(view, request);
        }
    }

    // You can add a dummy function or event if you need to "use" the extension in blocks
    // @SimpleFunction(description = "Returns the path to the bundled fonts.")
    // public String GetFontAssetPath() {
    //     return FONT_ASSET_PREFIX;
    // }
}
