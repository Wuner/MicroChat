package heath.com.microchat.message;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cjt2325.cameralibrary.JCameraView;
import com.cjt2325.cameralibrary.listener.ClickListener;
import com.cjt2325.cameralibrary.listener.ErrorListener;
import com.cjt2325.cameralibrary.listener.JCameraListener;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.model.Team;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.db.MicroChatDB;
import heath.com.microchat.provider.MicroChatProvider;
import heath.com.microchat.service.CacheService;
import heath.com.microchat.service.IMessageService;
import heath.com.microchat.service.impl.MessageServiceImpl;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageFactory;

public class RecordVideoActivity extends BaseActivity {

    private JCameraView mJcvVideo;
    private String sessionType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_video);
        initView();
        initListener();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mJcvVideo.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mJcvVideo.onPause();
    }

    private void initView() {
        Intent intent = getIntent();
        sessionType = (String) intent.getSerializableExtra("sessionType");
        mJcvVideo = findViewById(R.id.jcv_video);
    }

    private void initListener() {
        mJcvVideo.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {
                //打开Camera失败回调
                Log.i("CJT", "open camera error");
            }

            @Override
            public void AudioPermissionError() {
                //没有录取权限回调
                Log.i("CJT", "AudioPermissionError");
            }
        });
        mJcvVideo.setJCameraLisenter(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                //获取图片bitmap
                final String path = Environment.getExternalStorageDirectory() + "/" + getPackageName() + "/nim/cache/"+Common.getRandomCode()+".jpg";
                final File file = ImageFactory.saveBitmapFile(bitmap,path);
                Log.i("文件名", "文件名 = " +  file.getName()+"文件路径"+path);
                if (Common.SESSION_TYPE_P2P.equals(sessionType)){
                    SendMessageActivity.sendPhoto(file,path);
                }else if(Common.SESSION_TYPE_TEAM.equals(sessionType)){
                    SendTeamMessageActivity.sendPhoto(file,path);
                }
                finish();
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                //获取视频路径
                final String path = Environment.getExternalStorageDirectory() + "/" + getPackageName() + "/nim/cache/"+Common.getRandomCode()+".jpg";
                ImageFactory.saveBitmapFile(firstFrame,path);
                try {
                    if (Common.SESSION_TYPE_P2P.equals(sessionType)){
                        SendMessageActivity.sendVideo(url,path);
                    }else if(Common.SESSION_TYPE_TEAM.equals(sessionType)){
                        SendTeamMessageActivity.sendVideo(url,path);
                    }
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i("CJT", "url = " + url);
            }
        });
        mJcvVideo.setLeftClickListener(new ClickListener() {
            @Override
            public void onClick() {
                finish();
            }
        });
        mJcvVideo.setRightClickListener(new ClickListener() {
            @Override
            public void onClick() {
                Toast.makeText(RecordVideoActivity.this, "Right", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init() {
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
        //设置视频保存路径
        mJcvVideo.setSaveVideoPath(Environment.getExternalStorageDirectory() + "/" + getPackageName() + "/nim/"+"cache");

        //设置只能录像或只能拍照或两种都可以（默认两种都可以）
        mJcvVideo.setFeatures(JCameraView.BUTTON_STATE_BOTH);
        //设置视频质量
        mJcvVideo.setMediaQuality(JCameraView.MEDIA_QUALITY_HIGH);
    }

}
