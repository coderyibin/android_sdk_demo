package com.xmwm.mahjong.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.xmwm.mahjong.AppActivity;
import com.xmwm.mahjong.Config;
import com.xmwm.mahjong.R;
import com.xmwm.mahjong.Util;
import com.xmwm.mahjong.WxClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;


/**
 * Created by cjc on 2018/1/26.
 */

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    public static WXEntryActivity wx;
    private IWXAPI iwxapi;
    private String curUrl;
    private Button getCodeBtn;
    private String userInfo;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        wx = this;

        iwxapi = WXAPIFactory.createWXAPI(this, WxClass.getInstance().getAPPID(), true);
        iwxapi.handleIntent(getIntent(), this);
    }

    private void toEnd(){
//        wx = null;
        Intent intent2 =new  Intent(this, AppActivity.class);
        setIntent(intent2);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        iwxapi.handleIntent(intent, this);//必须调用此句话
        Log.d("onNewIntent", "onNewIntent");
    }
    @Override
    public void onReq(BaseReq req) {

    }
    //登录回调
    @Override
    public void onResp(BaseResp resp) {
        Log.i("qqq###bbb_resp", "授权回调===============resp.errCode= "+resp.errCode);
        String errString = "";
        switch (resp.errCode){
            //授权成功
            case BaseResp.ErrCode.ERR_OK:
                if (resp instanceof SendAuth.Resp) {
                    errString = "login success";
                    SendAuth.Resp send = (SendAuth.Resp) resp;
                    String Code = send.code;
                    //String Url1 = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
                    String Url1 = "https://api.weixin.qq.com/sns/oauth2/access_token?";
                    String Url2 = "appid=" + WxClass.getInstance().getAPPID();
                    String Url3 = "&secret=" + "4197c007876260281ff13a4edb0bb1dc";
                    String Url4 = "&code=" + Code;
                    String Url5 = "&grant_type=authorization_code";
                    String UrlAll = Url1 + Url2 + Url3 + Url4 + Url5;
                    curUrl = UrlAll;
                    DoHttp();
                    this.finish();
                } else {
                    Log.d("wx share entry","@@@@@@@@@@@@@@@@@@@@@ wx share entry@@@@@@@@@@@@@@@@@@");
//                    toEnd();
                    this.finish();
                }

                break;
            //用户取消
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                errString = "ERR_USER_CANCEL";
                this.finish();
                break;
            //登录失败
            case BaseResp.ErrCode.ERR_SENT_FAILED:
                errString = "ERR_SENT_FAILED";
                break;
            //登录被拒
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                errString = "ERR_AUTH_DENIED";
                break;
            //不支持
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                errString = "ERR_UNSUPPORT";
                break;
        }
//        Toast.makeText(this, errString, Toast.LENGTH_LONG).show();
    }

    private void Rec_token(String info){
        Log.i("qqq###bbb_Rec_token", "授权回调==============="+info);
        try {
            JSONObject contentJson = new JSONObject(info);
            String access_token = contentJson.getString("access_token");
            String openid = contentJson.getString("openid");
//        String expires_in = jsonObject.getString("expires_in");
//        String refresh_token = jsonObject.getString("refresh_token");
//        String scope = jsonObject.getString("scope");
//        String unionid = jsonObject.getString("unionid");
            //请求玩家信息的url
            String infoUrl1 = "https://api.weixin.qq.com/sns/userinfo?";
            String infoUrl2 = "access_token="+access_token;
            String infoUrl3 = "&openid="+openid;
            curUrl = infoUrl1 + infoUrl2 + infoUrl3;
            DoHttp();
        }catch (JSONException e){

        }finally {

        }
    }
    private void Rec_userInfo(String info) {
        Log.i("qqq###bbb_Rec_userInfo1", "玩家信息获取到了aaaaaaaaaaa" + info);
        WxClass.getInstance().setUserInfo(info);
        toEnd();
    }

    private void wxShare () {
        WXWebpageObject webpage = new WXWebpageObject();
        // 分享跳转网址
        webpage.webpageUrl = Config.ShareUrl;

        WXMediaMessage msg = new WXMediaMessage(webpage);

        msg.title = Config.ShareTitle;
        msg.description = Config.ShareContent;
        //Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.send_music_thumb);
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
        bmp.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
//        if(type == 0){
//            Log.d(TAG, "@@@@@@@sendToNative  webpage.webpageUrl roomId@@@@@@: " + type+"分享到朋友圈");
//            req.scene = SendMessageToWX.Req.WXSceneTimeline;
//        }else if (type == 1){
//            Log.d(TAG, "@@@@@@@sendToNative  webpage.webpageUrl roomId@@@@@@: " + type+"分享到好友");
//            req.scene = SendMessageToWX.Req.WXSceneSession;
//        }
        req.scene = SendMessageToWX.Req.WXSceneTimeline;
        iwxapi.sendReq(req);
    }
    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    private void DoHttp (){
        new Thread(getThread).start();
    }
    private Thread getThread = new Thread(){
        public void run() {
            HttpURLConnection connection = null;
            try {
                Random r = new Random();
                int version = r.nextInt(100000);
                URL url = new URL(curUrl);
                connection = (HttpURLConnection) url.openConnection();
                // 设置请求方法，默认是GET
                connection.setRequestMethod("GET");
                // 设置字符集
                connection.setRequestProperty("Charset", "UTF-8");
                // 设置文件类型
                connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
                // 设置请求参数，可通过Servlet的getHeader()获取
//                connection.setRequestProperty("Cookie", "AppName=" + URLEncoder.encode("你好", "UTF-8"));
                // 设置自定义参数
//                connection.setRequestProperty("MyProperty", "this is me!");
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while((line = reader.readLine())!=null){
                    response.append(line);
                }
                String Result = response.toString();
                if(curUrl.indexOf("access_token?") != -1){
                    //获取token信息
                    wx.Rec_token(Result);
                }else if(curUrl.indexOf("userinfo?") != -1){
                    //获取玩家信息
                    wx.Rec_userInfo(Result);
                }

//                if(connection.getResponseCode() == 200){
//                    InputStream is = connection.getInputStream();
//                    BufferedReader bf=new BufferedReader(new InputStreamReader(is,"UTF-8"));
//                    //最好在将字节流转换为字符流的时候 进行转码
//                    StringBuffer buffer=new StringBuffer();
//                    String line="";
//                    while((line=bf.readLine())!=null){
//                        buffer.append(line);
//                    }
//                    String str = buffer.toString();
////                    Toast.makeText(WXEntryActivity.this, "get Info success", Toast.LENGTH_LONG).show();
//                    Toast.makeText(WXEntryActivity.this, str, Toast.LENGTH_LONG).show();
//                    System.out.print(str);
//                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if(connection != null){
                    connection.disconnect();
                }
            }
        };
    };
}
