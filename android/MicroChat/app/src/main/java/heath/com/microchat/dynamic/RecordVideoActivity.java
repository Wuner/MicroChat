package heath.com.microchat.dynamic;

import android.content.Intent;
import android.graphics.Bitmap;
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

import java.io.File;
import java.util.HashMap;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.friend.AddFriendResultActivity;
import heath.com.microchat.friend.UserDetailedInfoActivity;
import heath.com.microchat.message.SendMessageActivity;
import heath.com.microchat.message.SendTeamMessageActivity;
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
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                //获取视频路径
                final String path = Environment.getExternalStorageDirectory() + "/" + getPackageName() + "/nim/cache/"+Common.getRandomCode()+".jpg";
                ImageFactory.saveBitmapFile(firstFrame,path);
                try {
                    Intent intent = new Intent(
                            RecordVideoActivity.this,
                            ReleaseActivity.class);
                    HashMap<String,Object> map = new HashMap<>();
                    map.put("url",url);
                    map.put("path",path);
                    map.put("width",firstFrame.getWidth());
                    map.put("height",firstFrame.getHeight());
                    map.put("formActivity","RecordVideoActivity");
                    intent.putExtra("map", map);
                    startActivityForResult(intent, 0);
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
        mJcvVideo.setFeatures(JCameraView.BUTTON_STATE_ONLY_RECORDER);
        //设置视频质量
        mJcvVideo.setMediaQuality(JCameraView.MEDIA_QUALITY_HIGH);
    }

}
