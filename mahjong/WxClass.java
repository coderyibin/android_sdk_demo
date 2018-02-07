package com.xmwm.mahjong;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.cocos2dx.lib.Cocos2dxJavascriptJavaBridge;

import java.io.File;

/**
 * Created by Administrator on 2018/2/3.
 */

public class WxClass {
    private static WxClass wx;
    public static WxClass getInstance () {
        if (wx == null) {
            wx = new WxClass();
            wx.app = AppActivity.app;
        } return wx;
    }

    private static String APP_ID = "wxeee071748c08ad0a";
    private static String userInfo = "";
    private AppActivity app;
    private IWXAPI wxapi;

    private WxClass () {
        wxapi = WXAPIFactory.createWXAPI(AppActivity.app, APP_ID, true);
        wxapi.registerApp(APP_ID);
//        Toast.makeText(AppActivity.app, "微信登录初始化", Toast.LENGTH_SHORT).show();
    }

    public void WxLogin (String appId) {
//        if (appId != null) {
//            APP_ID = appId;
//        }
        this.startWX(APP_ID);
    }

    /**
     * 微信分享：朋友圈，好友
     * WxClass.getInstance().WxShare(type, app.getResources());*/
    public void WxShare (int type, Resources res, String Title, String Content, String Url) {
        this.wxShare(type, res, Title, Content, Url);
    }

    /**
     * 微信分享：截图分享
     * @return
     */
    public void ImageWxShare (String filename, int scene_id) {
        Log.i("WxClass", "=============androidWeiXinShareImage================"+filename);
        // filename 打印结果为jsb.fileUtils.getWritablePath() + name + ".png" ==> /data/user/0/com.xhkmj.www/files/screen.png

        if (new File(filename).exists())
        {
            Object localObject1 = BitmapFactory.decodeFile(filename);
            Object localObject2 = new WXImageObject((Bitmap) localObject1);

            WXMediaMessage localWXMediaMessage = new WXMediaMessage();
            localWXMediaMessage.mediaObject = ((WXMediaMessage.IMediaObject) localObject2);
            localObject2 = Bitmap.createScaledBitmap((Bitmap) localObject1, 128, 72, true);
            ((Bitmap) localObject1).recycle();
            localWXMediaMessage.thumbData = Util.bmpToByteArray((Bitmap) localObject2, true);
            localObject1 = new SendMessageToWX.Req();
            ((SendMessageToWX.Req) localObject1).transaction = String.valueOf(System.currentTimeMillis());
            ((SendMessageToWX.Req) localObject1).message = localWXMediaMessage;

            int sceneID = -1;
//            if(scene_id.equals("WXSceneSession")){
//                sceneID = SendMessageToWX.Req.WXSceneSession;//发送到聊天界面
//            }else if (scene_id.equals("WXSceneTimeline")){
//                sceneID = SendMessageToWX.Req.WXSceneTimeline;//发送到朋友圈
//            }else  if(scene_id.equals("WXSceneFavorite")){
//                sceneID = SendMessageToWX.Req.WXSceneFavorite;//添加到微信收藏
//            }
            if(scene_id == 0){
                Log.d(this.app.TAG, "@@@@@@@sendToNative  webpage.webpageUrl roomId@@@@@@:分享到朋友圈");
                sceneID = SendMessageToWX.Req.WXSceneTimeline;
            }else if (scene_id == 1){
                Log.d(this.app.TAG, "@@@@@@@sendToNative  webpage.webpageUrl roomId@@@@@@:分享到好友");
                sceneID = SendMessageToWX.Req.WXSceneSession;
            }

            ((SendMessageToWX.Req) localObject1).scene = sceneID;

            wxapi.sendReq((BaseReq) localObject1);
        }
        else
        {
            Toast.makeText(app.getApplicationContext(), "file not exists", Toast.LENGTH_SHORT).show();
        }
    }

    public String getAPPID (){
        return APP_ID;
    }

    //开始调用微信登录
    private void startWX(String appid){
        Log.i("qqq###bbb", "获取APP_ID"+appid);
        if (appid != null) {
            APP_ID = appid;
        }
        IWXAPI api = WXAPIFactory.createWXAPI(app, APP_ID, true);
        if(api.isWXAppInstalled()){
            final SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";
            req.state = "wechat_test";
            api.sendReq(req);
        }else{
            //没有安装微信
            Toast.makeText(app, "请安装微信客户端", Toast.LENGTH_LONG).show();
            Log.i("qqq###bbb", "请安装微信客户端");
        }
    }

    private void wxShare (int type, Resources res, String Title, String Content, String Url) {
        WXWebpageObject webpage = new WXWebpageObject();
        // 分享跳转网址
        webpage.webpageUrl = Url;

        WXMediaMessage msg = new WXMediaMessage(webpage);

        msg.title = Title;
        msg.description = Content;
        //Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.send_music_thumb);
        Bitmap bmp = BitmapFactory.decodeResource(res, R.mipmap.ic_launcher);
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
        bmp.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        if(type == 0){
            Log.d(this.app.TAG, "@@@@@@@sendToNative  webpage.webpageUrl roomId@@@@@@: " + type+"分享到朋友圈");
            req.scene = SendMessageToWX.Req.WXSceneTimeline;
        }else if (type == 1){
            Log.d(this.app.TAG, "@@@@@@@sendToNative  webpage.webpageUrl roomId@@@@@@: " + type+"分享到好友");
            req.scene = SendMessageToWX.Req.WXSceneSession;
        }
        wxapi.sendReq(req);
    }

    //成功授权微信
    public void setUserInfo(String userInfo){
        Log.i("qqq###userInfo", "玩家信息获取到了aaaaaaaaaaa"+userInfo);
        userInfo = userInfo.replace("\"", "\\\"");
//        app.userInfo = userInfo;
        WxClass.getInstance().userInfo = userInfo;
        app.runOnGLThread(new Runnable() {
            @Override
            public void run() {
                String value ="G_JAVA_OUTPUT.loginSuccess(\"" + WxClass.getInstance().userInfo + "\")";
                Log.i("qqq",value);
                Cocos2dxJavascriptJavaBridge.evalString(value);
//                appCopy = null;
//                APP_ID = null;
            }
        });
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
}
