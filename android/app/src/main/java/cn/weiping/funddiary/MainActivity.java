package cn.weiping.funddiary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.*;
import android.widget.Toast;
import androidx.webkit.WebViewAssetLoader;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private static final String ORIGIN = "https://appassets.androidplatform.net";
    private static final int EXPORT = 41, IMPORT = 42;
    private WebView web;
    private ValueCallback<Uri[]> fileCallback;
    private String exportText;
    private boolean exporting;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        web = new WebView(this);
        android.widget.FrameLayout root = new android.widget.FrameLayout(this);
        root.setBackgroundColor(android.graphics.Color.rgb(135,42,48));
        root.addView(web,new android.widget.FrameLayout.LayoutParams(-1,-1));
        setContentView(root);
        // Target 35 draws edge-to-edge: keep controls clear of system bars and keyboard.
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true); // Selected JSON document only; required for file input.
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.addJavascriptInterface(new ExportBridge(), "FundDiaryAndroid");
        WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
        web.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                WebResourceResponse local = loader.shouldInterceptRequest(request.getUrl());
                return local != null ? local : new WebResourceResponse("text/plain", "utf-8", 403, "Blocked", null, new ByteArrayInputStream(new byte[0]));
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri url = request.getUrl();
                if (ORIGIN.equals(url.getScheme() + "://" + url.getHost()) && url.getPath().startsWith("/assets/www/")) return false;
                if ("https".equals(url.getScheme())) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, url)); } catch (Exception ignored) { notifyUser("无法打开浏览器"); }
                }
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*");
                try { startActivityForResult(intent, IMPORT); } catch (Exception e) { fileCallback.onReceiveValue(null); fileCallback = null; notifyUser("无法打开系统文件选择器"); }
                return true;
            }
        });
        web.loadUrl(ORIGIN + "/assets/www/index.html");
    }

    private class ExportBridge {
        @JavascriptInterface public void closeApp() { runOnUiThread(() -> finish()); }
        @JavascriptInterface public void saveFile(String name, String text, String mime) {
            runOnUiThread(() -> {
                if (exporting) { exportResult(false, "已有导出任务，请先完成保存"); return; }
                if (name == null || text == null || text.length() > 15000000) { exportResult(false, "导出内容过大或无效"); return; }
                exportText = text; exporting = true;
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
                    .setType(mime != null && mime.startsWith("text/") ? "text/plain" : "application/json")
                    .putExtra(Intent.EXTRA_TITLE, name.replaceAll("[/\\\\]", "_"));
                try { startActivityForResult(intent, EXPORT); }
                catch (Exception e) { exporting = false; exportText = null; exportResult(false, "无法打开保存窗口"); }
            });
        }
    }

    private void exportResult(boolean success, String message) {
        web.evaluateJavascript("window.dispatchEvent(new CustomEvent('fund-export-result',{detail:{success:" + success + ",message:" + JSONObject.quote(message) + "}}))", null);
    }
    private void notifyUser(String message) { Toast.makeText(this, message, Toast.LENGTH_LONG).show(); }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == IMPORT && fileCallback != null) {
            fileCallback.onReceiveValue(result == RESULT_OK && data != null && data.getData() != null ? new Uri[]{data.getData()} : null);
            fileCallback = null;
        }
        if (request == EXPORT) {
            if (result == RESULT_OK && data != null && data.getData() != null && exportText != null) {
                try (OutputStream out = getContentResolver().openOutputStream(data.getData(), "wt")) {
                    if (out == null) throw new Exception("无法写入");
                    out.write(exportText.getBytes(StandardCharsets.UTF_8));
                    exportResult(true, "文件已保存，请妥善保管备份");
                } catch (Exception e) { exportResult(false, "保存失败，请重试"); }
            } else { exportResult(false, "已取消保存，文件未导出"); }
            exportText = null; exporting = false;
        }
    }
    @Override public void onBackPressed() {
        web.evaluateJavascript("window.dispatchEvent(new Event('fund-native-back'))", null);
    }
    @Override protected void onDestroy() { if (fileCallback != null) fileCallback.onReceiveValue(null); web.removeJavascriptInterface("FundDiaryAndroid"); web.destroy(); super.onDestroy(); }
}
