package heath.com.microchat.mine;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flyco.animation.BaseAnimatorSet;
import com.flyco.animation.BounceEnter.BounceTopEnter;
import com.flyco.animation.SlideExit.SlideBottomExit;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;

import java.io.File;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.LoginActivity;
import heath.com.microchat.R;
import heath.com.microchat.TabHostActivity;
import heath.com.microchat.provider.MicroChatProvider;
import heath.com.microchat.utils.ACache;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.StorageCleanUtils;

public class SettingsActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout mLlExit;
    private LinearLayout mLlReturn;
    private ACache aCache;
    private TextView mTvCache;
    private LinearLayout mLlCache;
    private LinearLayout mLlModifyPassword;
    private BaseAnimatorSet mBasIn;
    private BaseAnimatorSet mBasOut;
    private Handler handler;
    private LinearLayout mLlClearChatHistory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        aCache = ACache.get(this);
        initView();
        init();
        initListener();
    }

    private void initView() {
        mLlExit = this.findViewById(R.id.ll_exit);
        mLlReturn = this.findViewById(R.id.ll_return);
        mTvCache = this.findViewById(R.id.tv_cache);
        mLlCache = this.findViewById(R.id.ll_cache);
        mLlModifyPassword = findViewById(R.id.ll_modify_password);
        mLlClearChatHistory = findViewById(R.id.ll_clear_chat_history);
        mBasIn = new BounceTopEnter();
        mBasOut = new SlideBottomExit();
        handler = new IHandler();
    }

    private void init() {
        long size = StorageCleanUtils.getFolderSize(new File(Environment.getExternalStorageDirectory() + "/" + getPackageName() + "/nim"));
        String text = "清除缓存 " + StorageCleanUtils.getFormatSize(size);
        mTvCache.setText(text);
    }

    private void initListener() {
        mLlExit.setOnClickListener(this);
        mLlReturn.setOnClickListener(this);
        mLlCache.setOnClickListener(this);
        mLlModifyPassword.setOnClickListener(this);
        mLlClearChatHistory.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_exit:
                NIMClient.getService(AuthService.class).logout();
                aCache.put("autoLogin", "0");
                startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                finishAll();
                break;
            case R.id.ll_return:
                finish();
                break;
            case R.id.ll_cache:
                NormalDialogStyleTwo();
                break;
            case R.id.ll_modify_password:
                startActivity(new Intent(SettingsActivity.this, ModifyPasswordActivity.class));
                break;
            default:
                break;
            case R.id.ll_clear_chat_history:
                NormalDialogStyleTwo1();
                break;
        }
    }

    private void NormalDialogStyleTwo() {
        final NormalDialog dialog = new NormalDialog(this);
        dialog.content("是否删除缓存")//
                .style(NormalDialog.STYLE_TWO)//
                .titleTextSize(23)//
                .showAnim(mBasIn)//
                .dismissAnim(mBasOut)//
                .show();

        dialog.setOnBtnClickL(
                new OnBtnClickL() {
                    @Override
                    public void onBtnClick() {
                        dialog.dismiss();
                    }
                },
                new OnBtnClickL() {
                    @Override
                    public void onBtnClick() {
                        dialog.dismiss();
                        Common.deleteDir(Environment.getExternalStorageDirectory() + "/" + getPackageName() + "/nim");
                        updateData();
                    }
                });
    }

    private void NormalDialogStyleTwo1() {
        final NormalDialog dialog = new NormalDialog(this);
        dialog.content("是否清空聊天记录")//
                .style(NormalDialog.STYLE_TWO)//
                .titleTextSize(23)//
                .showAnim(mBasIn)//
                .dismissAnim(mBasOut)//
                .show();

        dialog.setOnBtnClickL(
                new OnBtnClickL() {
                    @Override
                    public void onBtnClick() {
                        dialog.dismiss();
                    }
                },
                new OnBtnClickL() {
                    @Override
                    public void onBtnClick() {
                        dialog.dismiss();
                        getContentResolver().delete(MicroChatProvider.URI_MESSAGE, null, null);
                    }
                });
    }

    private void updateData() {
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
            init();
        }
    }

}
