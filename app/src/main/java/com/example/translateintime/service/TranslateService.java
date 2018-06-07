package com.example.translateintime.service;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TranslateService extends AccessibilityService {
    public Context context;
    String old="&&*&*&*（（）&*（）";
    ClipboardManager clipManager;
    /**
     *  必须重写的方法：此方法用了接受系统发来的event。在你注册的event发生时被调用。在整个生命周期会被调用多次。
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    /**
     *  必须重写的方法：系统要中断此service返回的响应时会调用。在整个生命周期会被调用多次。
     */
    @Override
    public void onInterrupt() {
    }

    /**
     *  当系统连接上你的服务时被调用
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        context=getApplicationContext();
        Toast.makeText(getApplicationContext(),"服务开始",Toast.LENGTH_LONG);
         clipManager= (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        new Thread(new Runnable() {
            @Override
            public void run() {

                while (true){
                    try {
                        String contain=getTranslate(getSystemClipText());
                        if(contain!=null || !contain.equals("")) {
                            Message message = new Message();
                            message.obj =contain;
                            handler.sendMessage(message);
                        }
                        Thread.sleep(2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

    }


    /**
     *  在系统要关闭此service时调用。
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

//获取系统剪贴板的数据
    public String getSystemClipText(){

        //判断剪贴板里是否有内容
        if(!clipManager.hasPrimaryClip()) {
            return "没有获取到信息";
        }
        ClipData clip = clipManager.getPrimaryClip();
        //与之前相同的话就返回null
        if(clip.getItemAt(0).getText().toString().equals(old)){
            return null;
        }
        //获取 text
        old=clip.getItemAt(0).getText().toString();
        return old;
    }

    //通过网络获取翻译后的值
    public String getTranslate(String text){
        //为null或者为空的话就不必上传翻译了
       if(text==null || "".equals(text)){
           return null;
       }
        String url = "http://dict-co.iciba.com/api/dictionary.php?w="+text+"&key=935A55FB7EB2563DCCF6ED190E2BE25F";
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder() .url(url) .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            String xml=response.body().string().toString();
            String acceptation = xml.substring(xml.indexOf("acceptation") + 12, xml.indexOf("</acceptation") );
            return acceptation;
        } catch (Exception e) {
            e.printStackTrace();
            return "获取失败";
        }
    }

    android.os.Handler handler=new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
                Toast.makeText(context,msg.obj.toString(),Toast.LENGTH_LONG).show();

        }
    };

}


