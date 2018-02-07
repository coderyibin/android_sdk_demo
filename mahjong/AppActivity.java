/****************************************************************************
Copyright (c) 2015 Chukong Technologies Inc.
 
http://www.cocos2d-x.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
****************************************************************************/
package com.xmwm.mahjong;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.yaya.sdk.RTV;
import com.yaya.sdk.VideoTroopsRespondListener;
import com.yaya.sdk.YayaNetStateListener;
import com.yaya.sdk.YayaRTV;
import com.yaya.sdk.tlv.protocol.message.TextMessageNotify;

import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
public class AppActivity extends Cocos2dxActivity implements VideoTroopsRespondListener {
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    public static AppActivity app;
    public String TAG = "AppActivity";
    public WxClass wxClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Workaround in https://stackoverflow.com/questions/16283079/re-launch-of-activity-on-home-button-but-only-the-first-time/16447508
        if (!isTaskRoot()) {
            // Android launched another instance of the root activity into an existing task
            //  so just quietly finish and go away, dropping the user back into the activity
            //  at the top of the stack (ie: the last state of this task)
            // Don't need to finish it again since it's finished in super.onCreate .
            return;
        }
        // DO OTHER INITIALIZATION BELOW
        app = this;
        SDKWrapper.getInstance().init(this);
        this.wxClass = WxClass.getInstance();
        Log.d(TAG, "onCreater");
    }

    /*===========================YaYa语音start===================================*/
    public static void GameStart (String account, String password, String roomId) {
        app.initYaYa(account, password, roomId);
    }
    public static void EndStart () {
        YayaRTV.getInstance().logout();
    }
    public static void StartSay () {
        Log.d(app.TAG, "上麦");
        YayaRTV.getInstance().micUp();
    }
    public static void EndSay () {
        YayaRTV.getInstance().micDown();
    }
    private int mode;
    private int env;
    private String appId;
    private String _sAccount = "";
    private String _sPassword = "";
    private String _sRoomId = "";
    public void initYaYa (String account, String password, String roomId) {
        this._sAccount = account;
        this._sPassword = password;
        this._sRoomId = roomId;
        YayaRTV.Env sdkEnv = null;
        if (env == 0) {
            sdkEnv = RTV.Env.Test;
        } else if (env == 1) {
            sdkEnv = RTV.Env.Product;
        } else if (env == 2) {
            sdkEnv = RTV.Env.Oversea;
        }

        sdkEnv = RTV.Env.Product;
        RTV.Mode rtvMode = null;
        if (mode == 0) {
            rtvMode = RTV.Mode.Free;
        } else if (mode == 1) {
            rtvMode = RTV.Mode.Robmic;
        } else if (mode == 2) {
            rtvMode = RTV.Mode.Leader;
        }
        appId = "1002307";
        //初始化实时语音
        YayaRTV.getInstance().init(this, appId, this, sdkEnv, rtvMode);
        YayaRTV.getInstance().setLogEnable(true);
    }
    private YayaNetStateListener netStateListener = new YayaNetStateListener() {
        @Override
        public void onNetStateUpdate(long send, long recv) {
            final long elapse = recv - send;
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "当前网络状态"+elapse);

                }
            });
        }
    };

    String tt;
    @Override
    public void initComplete() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "初始化结束，开始登录-");

                tt = "{\"uid\":\""+app._sAccount+"\", \"nickname\":\""+app._sPassword+"\"}";
                YayaRTV.getInstance().loginBinding(tt, app._sRoomId);
            }
        });
    }

    @Override
    public void onLoginResp(final int result, final String msg, final long yunvaId, final byte mode, boolean isLeader, int leaderId) {
        Log.d(TAG, "onLoginResp: result:" + result + "  msg:" + msg + "  yunvaId:" + yunvaId + "  mode:" + mode + "  isLeader:" + isLeader + " leaderId:" + leaderId);
        mainHandler.post(new Runnable() {
            @Override
            public void run() {

                if (result == 0) {
                    Toast.makeText(AppActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "登录成功-" + result +"-" +msg);
                    app.StartSay();
                } else {
                    Toast.makeText(AppActivity.this, "登录失败 " + result + "," + msg, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "登录失败-" + result +"-" +msg);
                }
            }
        });
    }

    @Override
    public void onLogoutResp(final long result, final String msg) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (result == 0) {
                    Toast.makeText(AppActivity.this, "登出成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AppActivity.this, "登出异常 " + result + "," + msg, Toast.LENGTH_SHORT).show();
                }

//                finish();
            }
        });
    }

    @Override
    public void onMicResp(final long result, final String msg, final String actionType) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (result != 0) {
                    //失败
                    Toast.makeText(AppActivity.this, "麦请求失败 " + result + "," + msg, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "上麦失败"+msg);
                    return;
                }
            }
        });
    }

    @Override
    public void onModeSettingResp(long result, String msg, RTV.Mode mode) {
        Log.d(TAG, "onModeSettingResp:" + "result:" + result + "  msg:" + msg + "  mode:" + mode);
    }

    @Override
    public void onSendRealTimeVoiceMessageResp(long result, String msg) {
    }

    //接收到文字消息的回调
    @Override
    public void onTextMessageNotify(final TextMessageNotify textMessageNotify) {
        Log.d(TAG, textMessageNotify.toString());
    }

    //发送消息会回调此方法,只有失败会回调,发送成功不回调
    @Override
    public void onSendTextMessageResp(final long result, final String msg, String expand) {
        Log.d("MainActivity", "onSendTextMessageResp " + result);
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (result != 0) {
                    Toast.makeText(AppActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //实时语音消息通知回调,当有人说话时周期回调
    @Override
    public void onRealTimeVoiceMessageNotify(String troopsId, long yunvaId, final String expand) {
        Log.i("Listener", "有人说话 " + yunvaId);
    }

    //当队伍房间模式发生改变时的通知回调
    @Override
    public void onTroopsModeChangeNotify(final RTV.Mode mode, final boolean isLeader) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AppActivity.this, "mode:" + mode + "  isLeader:" + isLeader, Toast.LENGTH_SHORT).show();
            }
        });
        Log.d(TAG, "onTroopsModeChangeNotify:" + "mode:" + mode + "  isLeader:" + isLeader);
    }

    //实时语音录音异常时回调,如:没有录音权限
    @Override
    public void audioRecordUnavailableNotify(int result, String msg) {
    }

    @Override
    public void onAuthResp(long result, String msg) {
    }

    @Override
    public void onGetRoomResp(long result, String msg) {
    }


    @Override
    public void onReconnectStart() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AppActivity.this,
                        "开始重连...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReconnectFail(int errCode, final String msg) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AppActivity.this,
                        msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReconnectSuccess() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AppActivity.this,
                        "重连成功", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //当队伍房间人数变化时的通知回调,如:有人登陆或者登出,type字段区分,userInfo字段是信息
    @Override
    public void onTroopsListChangeNotify(final String troopsId, final long yunvaId, final String userInfo, final int type) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AppActivity.this, "troopsId:" + troopsId + ",yunvaId:" + yunvaId + ",userInfo:" + userInfo + ",type:" + type, Toast.LENGTH_SHORT).show();
            }
        });
        Log.d(TAG, "troopsId:" + troopsId + ",yunvaId:" + yunvaId + ",userInfo:" + userInfo + ",type:" + type);
    }

    //当队伍房间有人上麦或者下麦时的通知回调,type字段区分,userInfo字段是信息
    @Override
    public void onMicStateChangeNotify(final String troopsId, final long yunvaId, final String userInfo, final int type) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AppActivity.this, "troopsId:" + troopsId + ",yunvaId:" + yunvaId + ",userInfo:" + userInfo + ",type:" + type, Toast.LENGTH_SHORT).show();
            }
        });
        Log.d(TAG, "troopsId:" + troopsId + ",yunvaId:" + yunvaId + ",userInfo:" + userInfo + ",type:" + type);
    }

    //实时语音录音分贝值
    @Override
    public void onRecordVolumeNotify(float value, float maxValue) {
    }

    //实时语音播放分贝值
    @Override
    public void onPlayVolumeNotify(final float value, float maxValue) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
//                mVolume_value.setText("语音播放分贝值:" + value);
            }
        });
    }

    @Override
    public void onProxyQueryNotification(String date, int Residual, int state) {
        Toast.makeText(AppActivity.this, "截止日期："+date + "剩余流量：" + Residual+ "流量状态：" + state, Toast.LENGTH_SHORT).show();
    }
    /*===========================YaYa语音end=====================================*/

    @Override
    public Cocos2dxGLSurfaceView onCreateView() {
        Cocos2dxGLSurfaceView glSurfaceView = new Cocos2dxGLSurfaceView(this);
        // TestCpp should create stencil buffer
        glSurfaceView.setEGLConfigChooser(5, 6, 5, 0, 16, 8);

        SDKWrapper.getInstance().setGLSurfaceView(glSurfaceView);

        return glSurfaceView;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SDKWrapper.getInstance().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SDKWrapper.getInstance().onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SDKWrapper.getInstance().onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SDKWrapper.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        SDKWrapper.getInstance().onNewIntent(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        SDKWrapper.getInstance().onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SDKWrapper.getInstance().onStop();
    }
        
    @Override
    public void onBackPressed() {
        SDKWrapper.getInstance().onBackPressed();
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        SDKWrapper.getInstance().onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        SDKWrapper.getInstance().onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        SDKWrapper.getInstance().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        SDKWrapper.getInstance().onStart();
        super.onStart();
    }

    public static void startWX () {
        Log.d(app.TAG, "开始微信登录");
        app.wxClass.WxLogin("");
    }

    /**
     * 分享类型：0朋友圈，1好友
     * share type:0 Circle of friends ,1 friend
     * @param type
     */
    public static void startShare (int type, String Title, String Content, String Url, String image) {
        if (image == "") {
            Log.d(app.TAG, "开始微信分享");
            app.wxClass.WxShare(type, app.getResources(), Title, Content, Url);
        } else {
            Log.d(app.TAG, "开始微信截图分享");
            app.wxClass.ImageWxShare(image, type);
        }
    }
}
