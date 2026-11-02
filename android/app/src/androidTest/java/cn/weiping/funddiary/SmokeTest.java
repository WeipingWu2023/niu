package cn.weiping.funddiary;

import android.app.Instrumentation;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.view.ViewGroup;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.io.File;
import java.io.FileOutputStream;
import android.graphics.Bitmap;

/** Runs only in the disposable CI emulator, with synthetic accounts. */
public class SmokeTest extends Instrumentation {
    private WebView web;
    @Override public void onCreate(Bundle args) { super.onCreate(args); start(); }
    private String js(String script) throws Exception {
        CountDownLatch done=new CountDownLatch(1); AtomicReference<String> value=new AtomicReference<>();
        runOnMainSync(() -> web.evaluateJavascript(script, result -> {value.set(result);done.countDown();}));
        if(!done.await(10,TimeUnit.SECONDS)) throw new Exception("JavaScript timed out");
        return value.get();
    }
    private void await(String expression) throws Exception {
        for(int i=0;i<150;i++){if("true".equals(js(expression))) return; Thread.sleep(200);}
        throw new Exception("Timeout: "+expression+"; status="+js("window.__qaResult || document.body.innerText"));
    }
    @Override public void onStart() {
        Bundle result=new Bundle();
        try {
            Activity activity=startActivitySync(new Intent().setClassName(getTargetContext(),MainActivity.class.getName()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            runOnMainSync(() -> web=(WebView)((ViewGroup)((ViewGroup)activity.findViewById(android.R.id.content)).getChildAt(0)).getChildAt(0));
            await("!!document.querySelector('[data-action=fund]')");
            String script=new String(getContext().getAssets().open("smoke.js").readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
            js(script); await("window.__qaResult === 'PASS'");
            runOnMainSync(() -> web.reload());
            await("document.body.innerText.includes('1710.00') && document.body.innerText.includes('-290.00')");
            if(!"true".equals(js("document.documentElement.scrollWidth <= window.innerWidth"))) throw new Exception("Mobile horizontal overflow: "+js("JSON.stringify({viewport:innerWidth,document:document.documentElement.scrollWidth,offenders:[...document.querySelectorAll('body *')].filter(e=>e.getBoundingClientRect().right>innerWidth+0.5).slice(0,12).map(e=>({tag:e.tagName,class:e.className,width:e.getBoundingClientRect().width,right:e.getBoundingClientRect().right,text:e.innerText?.slice(0,50)}))})"));
            // Use the actual Android content provider, with only the picker selection mocked.
            android.content.ContentValues values=new android.content.ContentValues();
            values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME,"niu-ci-backup.json");
            values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE,"application/json");
            android.net.Uri backupUri=getTargetContext().getContentResolver().insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,values);
            if(backupUri==null) throw new Exception("Cannot create test backup document");
            android.content.IntentFilter filter=new android.content.IntentFilter(Intent.ACTION_CREATE_DOCUMENT);
            filter.addCategory(Intent.CATEGORY_OPENABLE);filter.addDataType("application/json");
            ActivityMonitor monitor=addMonitor(filter,new ActivityResult(Activity.RESULT_OK,new Intent().setData(backupUri)),true);
            js("document.querySelector('[data-action=backup]').click()");
            await("document.getElementById('toast').innerText.includes('备份已保存')");
            removeMonitor(monitor);
            try(java.io.InputStream in=getTargetContext().getContentResolver().openInputStream(backupUri)){
                org.json.JSONObject saved=new org.json.JSONObject(new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8));
                if(saved.getJSONObject("data").getJSONArray("transactions").length()!=2) throw new Exception("Native exported JSON lost transactions");
            }
            getTargetContext().getContentResolver().delete(backupUri,null,null);
            js("document.getElementById('toast').style.display='none'");
            File screenshots=new File(getTargetContext().getExternalFilesDir(null),"screenshots");screenshots.mkdirs();
            try(FileOutputStream out=new FileOutputStream(new File(screenshots,"niu-android.png"))){getUiAutomation().takeScreenshot().compress(Bitmap.CompressFormat.PNG,100,out);}
            result.putString("stream","NIU_SMOKE_PASS: pending/confirmed ledger, NAV, simulator, JSON replay, mobile width, refresh persistence, native JSON export\n");
            finish(Activity.RESULT_OK,result);
        } catch(Exception e){result.putString("stream","NIU_SMOKE_FAIL: "+e);finish(Activity.RESULT_CANCELED,result);}
    }
}
