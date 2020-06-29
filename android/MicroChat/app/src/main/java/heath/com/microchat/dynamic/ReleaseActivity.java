package heath.com.microchat.dynamic;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.adapter.ImageAdapter;
import heath.com.microchat.entity.DynamicBean;
import heath.com.microchat.service.IDynamicService;
import heath.com.microchat.service.impl.DynamicServiceImpl;
import heath.com.microchat.utils.BottomMenu;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.PicturePreviewActivity;
import heath.com.microchat.utils.ThreadUtils;
import heath.com.microchat.utils.ToastUtil;
import heath.com.microchat.utils.UploadServerUtils;

public class ReleaseActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout mLlReturn;
    private TextView mTvRelease;
    private EditText mEtContent;
    private GridView mGvImages;
    private ImageAdapter mAdapter;
    private List<String> images;
    private static final int CAMERA = 0;
    private static final int IMAGE = 1;
    private String path;
    private static Handler handler;
    private LoadingUtils loadingUtils;
    private IDynamicService iDynamicService;
    private Gson gson;
    private HashMap map;
    private StandardGSYVideoPlayer videoPlayer;
    private OrientationUtils orientationUtils;
    private String fromActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_release);
        initView();
        initListener();
        initData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoPlayer.onVideoResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }

    @Override
    public void onBackPressed() {
        //先返回正常状态
        if (!fromActivity.equals("DynamicFragment")){
            if (orientationUtils.getScreenType() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                videoPlayer.getFullscreenButton().performClick();
                return;
            }
            //释放所有
            videoPlayer.setVideoAllCallBack(null);
        }
        super.onBackPressed();
    }

    private void initView() {
        Intent intent = getIntent();
        map = (HashMap) intent.getSerializableExtra("map");
        fromActivity = (String) map.get("formActivity");
        mLlReturn = findViewById(R.id.ll_return);
        mTvRelease = findViewById(R.id.tv_release);
        mEtContent = findViewById(R.id.et_content);
        mGvImages = findViewById(R.id.gv_images);
        images = new ArrayList<>();
        handler = new IHandler();
        loadingUtils = new LoadingUtils(ReleaseActivity.this, "正在发布中");
        loadingUtils.creat();
        iDynamicService = new DynamicServiceImpl();
        gson = new Gson();
        videoPlayer =  findViewById(R.id.video_player);
    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
        mTvRelease.setOnClickListener(this);
    }

    private void initData() {
        if (fromActivity.equals("DynamicFragment")){
            videoPlayer.setVisibility(View.GONE);
            mAdapter = new ImageAdapter(ReleaseActivity.this, images);
            mGvImages.setAdapter(mAdapter);
            mGvImages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position == images.size()) {
                        String[] texts = new String[]{getResources().getString(R.string.tv_choose_from_mobile_phones), getResources().getString(R.string.tv_shot)};
                        int[] ids = new int[]{R.id.btn1, R.id.btn2};
                        int[] index = new int[]{0, 1};
                        List<Map<String, Object>> list = Common.setBtn(texts, ids, index);
                        BottomMenu menuWindow = new BottomMenu(ReleaseActivity.this, clickListener, list);
                        menuWindow.show();
                    } else {
                        Intent intent = new Intent(
                                ReleaseActivity.this,
                                PicturePreviewActivity.class);
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("position", position);
                        map.put("url", images.get(position));
                        map.put("type", Common.LOCAL_PICTURE);
                        intent.putExtra("map", map);
                        startActivityForResult(intent, 0);
                    }
                }
            });
        }else{
            mGvImages.setVisibility(View.GONE);
            ViewGroup.LayoutParams lp = videoPlayer.getLayoutParams();
            lp.width = (int) map.get("width")/2;
            lp.height = (int) map.get("height")/2;
            videoPlayer.setLayoutParams(lp);
            String source1 = (String) map.get("url");
            videoPlayer.setUp(source1, true, "");
            //增加封面
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Bitmap bitmap = BitmapFactory.decodeFile((String) map.get("path"));
            imageView.setImageBitmap(bitmap);
            videoPlayer.setThumbImageView(imageView);
            //增加title
            videoPlayer.getTitleTextView().setVisibility(View.GONE);
            //设置返回键
            videoPlayer.getBackButton().setVisibility(View.GONE);
            //设置旋转
            orientationUtils = new OrientationUtils(this, videoPlayer);
            //设置全屏按键功能,这是使用的是选择屏幕，而不是全屏
            videoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    orientationUtils.resolveByClick();
                }
            });
            //是否可以滑动调整
            videoPlayer.setIsTouchWiget(true);
            //设置返回按键功能
            videoPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            videoPlayer.startPlayLogic();
        }
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {

        public void onClick(View v) {
            Intent intent;
            switch (v.getId()) {
                case R.id.btn1:
                    intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, IMAGE);
                    break;
                case R.id.btn2:
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Date newTime = new Date();
                    SimpleDateFormat newSimpleDateFormat = new SimpleDateFormat(
                            "yyyyMMddHHmmss", Locale.getDefault());
                    String time = newSimpleDateFormat.format(newTime);
                    path = Environment.getExternalStorageDirectory() + "/" + getPackageName() + "/nim/cache/IMAGE_" + time + ".jpg";
                    Uri uri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        uri = FileProvider.getUriForFile(ReleaseActivity.this, getPackageName() + ".provider", new File(path));
                    } else {
                        uri = Uri.fromFile(new File(path));
                    }
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, CAMERA);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_return:
                finish();
                break;
            case R.id.tv_release:
                loadingUtils.show();
                String content = mEtContent.getText().toString();
                if (fromActivity.equals("DynamicFragment")){
                    if (content.length() == 0 && images.size() == 0) {
                        Toast.makeText(this, "文本和图片不能同时为空", Toast.LENGTH_SHORT).show();
                        loadingUtils.dismiss();
                    } else {
                        upload();
                    }
                }else {
                    uploadVideo();
                }
                break;
        }
    }

    private void upload() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                String content = mEtContent.getText().toString();
                if (images.size() == 0) {
                    DynamicBean dynamicBean = new DynamicBean();
                    dynamicBean.setAccount(aCache.getAsString("account"));
                    dynamicBean.setContent(content);
                    dynamicBean.setType(Common.DYNAMIC_TYPE_IMAGE_TEXT);
                    dynamicBean.setPath("");
                    release(dynamicBean);
                } else {
                    String result = UploadServerUtils.uploadLogFiles(Common.HTTP_ADDRESS + "upload/filesUpload.action", images, Common.DYNAMIC_PICTURE_PATH);
                    try {
                        JSONObject resultObj = new JSONObject(result);
                        if (resultObj.getString("code").equals("200")) {
                            Log.i("返回图片路径", "run: " + resultObj.getString("fileNames"));
                            DynamicBean dynamicBean = new DynamicBean();
                            dynamicBean.setAccount(aCache.getAsString("account"));
                            if (content.length()!=0){
                                dynamicBean.setContent(content);
                            }else{
                                dynamicBean.setContent("");
                            }
                            dynamicBean.setType(Common.DYNAMIC_TYPE_IMAGE_TEXT);
                            dynamicBean.setPath(resultObj.getString("fileNames"));
                            release(dynamicBean);
                        } else {
                            ToastUtil.toastOnUiThread(ReleaseActivity.this, resultObj.get("msg").toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        loadingUtils.dismissOnUiThread();
                    }
                }

            }
        });
    }

    private void uploadVideo() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                String content = mEtContent.getText().toString();
                List<String> list = new ArrayList<>();
                list.add((String) map.get("url"));
                String result = UploadServerUtils.uploadLogFiles(Common.HTTP_ADDRESS + "upload/filesUpload.action", list, Common.DYNAMIC_VIDEO_PATH);
                try {
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        Log.i("返回路径", "run: " + resultObj.getString("fileNames"));
                        DynamicBean dynamicBean = new DynamicBean();
                        dynamicBean.setAccount(aCache.getAsString("account"));
                        if (content.length()!=0){
                            dynamicBean.setContent(content);
                        }else{
                            dynamicBean.setContent("");
                        }
                        dynamicBean.setType(Common.DYNAMIC_TYPE_VIDEO);
                        dynamicBean.setPath(resultObj.getString("fileNames"));
                        release(dynamicBean);
                    } else {
                        ToastUtil.toastOnUiThread(ReleaseActivity.this, resultObj.get("msg").toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    loadingUtils.dismissOnUiThread();
                }
            }
        });
    }

    private void release(DynamicBean dynamicBean) {
        try {
            JSONObject parameterData = new JSONObject(gson.toJson(dynamicBean));
            String result = iDynamicService.release(parameterData);
            Log.e("TAG", "run: " + result + "0---------------------------");
            JSONObject resultObj = new JSONObject(result);
            loadingUtils.dismissOnUiThread();
            if (resultObj.getString("code").equals("200")) {
                ToastUtil.toastOnUiThread(ReleaseActivity.this, resultObj.get("msg").toString());
                finish();
            } else {
                ToastUtil.toastOnUiThread(ReleaseActivity.this, resultObj.get("msg").toString());
            }
        } catch (Exception e) {
            loadingUtils.dismissOnUiThread();
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (resultCode != Activity.RESULT_CANCELED)
            switch (requestCode) {
                case IMAGE:
                    Uri selectedImage = data.getData();
                    String[] filePathColumns = {MediaStore.Images.Media.DATA};
                    Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
                    c.moveToFirst();
                    int columnIndex = c.getColumnIndex(filePathColumns[0]);
                    final String imagePath = c.getString(columnIndex);
                    Log.i("图片路径", "onActivityResult: " + imagePath);
                    if (imagePath.endsWith(".jpg") || imagePath.endsWith(".jpeg") || imagePath.endsWith(".bmp") || imagePath.endsWith(".gif") || imagePath.endsWith(".png")) {
                        images.add(imagePath);
                    } else {
                        Toast.makeText(this, "请选择图片", Toast.LENGTH_SHORT).show();
                    }
                    c.close();
                    break;
                case CAMERA:
                    images.add(path);
                    Log.i("图片路径", "onActivityResult: " + path);
                    break;
            }
        mAdapter.notifyDataSetChanged();
    }

    public static void del(int index) {
        Message message = new Message();
        Bundle data = new Bundle();
        data.putInt("index", index);
        message.what = 1;
        message.setData(data);
        handler.sendMessage(message);
    }

    private class IHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int index = msg.getData().getInt("index");
            images.remove(index);
            mAdapter.notifyDataSetChanged();
        }
    }

}
