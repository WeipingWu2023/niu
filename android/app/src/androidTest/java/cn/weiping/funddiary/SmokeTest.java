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
            runOnMainSync(() -> web=(WebView)((ViewGroup)activity.findViewById(android.R.id.content)).getChildAt(0));
            await("!!document.querySelector('[data-action=fund]')");
            String script=new String(getContext().getAssets().open("smoke.js").readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
            js(script); await("window.__qaResult === 'PASS'");
            runOnMainSync(() -> web.reload());
            await("document.body.innerText.includes('1710.00') && document.body.innerText.includes('-290.00')");
            if(!"true".equals(js("document.documentElement.scrollWidth <= window.innerWidth"))) throw new Exception("Mobile horizontal overflow");
            File screenshots=new File(getTargetContext().getExternalFilesDir(null),"screenshots");screenshots.mkdirs();
            try(FileOutputStream out=new FileOutputStream(new File(screenshots,"niu-android.png"))){getUiAutomation().takeScreenshot().compress(Bitmap.CompressFormat.PNG,100,out);}
            result.putString("stream","NIU_SMOKE_PASS: pending/confirmed ledger, NAV, simulator, JSON replay, mobile width, refresh persistence\n");
            finish(Activity.RESULT_OK,result);
        } catch(Exception e){result.putString("stream","NIU_SMOKE_FAIL: "+e);finish(Activity.RESULT_CANCELED,result);}
    }
}
