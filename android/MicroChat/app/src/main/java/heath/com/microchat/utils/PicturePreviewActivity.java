package heath.com.microchat.utils;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.dynamic.ReleaseActivity;
import heath.com.microchat.service.CacheService;

public class PicturePreviewActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout mLlReturn;
    private ImageView mIvPicture;
    private LinearLayout mLlDel;
    private TextView mTvViewOriginalImage;
    private HashMap map;
    private int type;
    private static Handler handler;
    private LoadingUtils loadingUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_preview);
        initView();
        initListener();
        initData();
    }

    private void initView() {
        Intent intent = getIntent();
        map = (HashMap) intent.getSerializableExtra("map");
        type = (int) map.get("type");
        mLlReturn = findViewById(R.id.ll_return);
        mIvPicture = findViewById(R.id.iv_picture);
        mLlDel = findViewById(R.id.ll_del);
        mTvViewOriginalImage = findViewById(R.id.tv_view_original_image);
        handler = new IHandler();
        loadingUtils = new LoadingUtils(PicturePreviewActivity.this, "努力加载中");
        loadingUtils.creat();
    }

    private void initListener() {
        mLlDel.setOnClickListener(this);
        mLlReturn.setOnClickListener(this);
        mTvViewOriginalImage.setOnClickListener(this);
    }

    private void initData() {
        if (Common.NETWORK_PICTURE == type) {
            mLlDel.setVisibility(View.GONE);
            if (CacheService.exists(Common.HTTP_ADDRESS + Common.DYNAMIC_PICTURE_PATH + "/" + map.get("url"), original_drawing)) {
                ImageUitl imageUitl = new ImageUitl(original_drawing);
                imageUitl.asyncloadImage(mIvPicture, Common.HTTP_ADDRESS + Common.DYNAMIC_PICTURE_PATH + "/" + map.get("url"));
                mTvViewOriginalImage.setVisibility(View.GONE);
            } else {
                ImageUitl imageUitl = new ImageUitl(cache);
                imageUitl.asyncloadImage(mIvPicture, Common.HTTP_ADDRESS + Common.DYNAMIC_PICTURE_PATH + "/" + map.get("url"));
                mTvViewOriginalImage.setVisibility(View.VISIBLE);
            }
        } else if (Common.LOCAL_PICTURE == type) {
            mLlDel.setVisibility(View.VISIBLE);
            Bitmap bitmap = BitmapFactory.decodeFile((String) map.get("url"));
            mIvPicture.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_return:
                finish();
                break;
            case R.id.ll_del:
                settingDel();
                break;
            case R.id.tv_view_original_image:
                loadingUtils.show();
                load();
                break;
        }
    }

    private void settingDel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.alert_light_frame);
        builder.setMessage("要删除这张照片吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ReleaseActivity.del((int) map.get("position"));
                finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog ad = builder.create();
        ad.show();
    }

    public static void load() {
        Message message = new Message();
        Bundle data = new Bundle();
        message.what = 1;
        message.setData(data);
        handler.sendMessage(message);
    }

    private class IHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                CacheService.cacheImage(Common.HTTP_ADDRESS + Common.DYNAMIC_PICTURE_PATH + "/" + map.get("url"), original_drawing,mIvPicture);
                loadingUtils.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
