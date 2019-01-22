package heath.com.microchat.mine;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.model.RecentContact;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.db.MicroChatDB;
import heath.com.microchat.entity.UserInfo;
import heath.com.microchat.friend.FriendsFragment;
import heath.com.microchat.friend.UserMoreInfoActivity;
import heath.com.microchat.message.MessageFragment;
import heath.com.microchat.provider.MicroChatProvider;
import heath.com.microchat.service.IFriendService;
import heath.com.microchat.service.impl.FriendServiceImpl;
import heath.com.microchat.team.UpdateTeamMemberInfoActivity;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ToastUtil;

public class UpdateInfoActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout mLlReturn;
    private TextView mTvSave;
    private TextView mTvUpdateName;
    private EditText mEtUpdateContent;
    private String updateInfo;
    private UserInfo userInfo;
    private String orgContent = "";
    private LoadingUtils loadingUtils;
    private HashMap map;
    private IFriendService friendServiceImpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        Intent intent = getIntent();
        map = (HashMap) intent.getSerializableExtra("map");
        updateInfo = (String) map.get("updateInfo");
        mLlReturn = this.findViewById(R.id.ll_return);
        mTvSave = this.findViewById(R.id.tv_save);
        mTvUpdateName = this.findViewById(R.id.tv_update_name);
        mEtUpdateContent = this.findViewById(R.id.et_update_content);
        userInfo = (UserInfo) aCache.getAsObject("userInfo");
        loadingUtils = new LoadingUtils(UpdateInfoActivity.this, "正在修改资料");
        friendServiceImpl = new FriendServiceImpl();
    }

    private void initData() {
        loadingUtils.creat();
        String updateInfoText = "更改" + updateInfo;
        mTvUpdateName.setText(updateInfoText);
        if (updateInfo.equals(getResources().getString(R.string.tv_nickname))) {
            if (userInfo.getNickname() != null) {
                mEtUpdateContent.setText(userInfo.getNickname());
                mEtUpdateContent.setSelection(userInfo.getNickname().length());
                orgContent = userInfo.getNickname();
            }
        }
        if (updateInfo.equals(getResources().getString(R.string.tv_email))) {
            if (userInfo.getEmail() != null) {
                mEtUpdateContent.setText(userInfo.getEmail());
                mEtUpdateContent.setSelection(userInfo.getEmail().length());
                orgContent = userInfo.getEmail();
            }
        }
        if (updateInfo.equals(getResources().getString(R.string.tv_sign))) {
            if (userInfo.getSign() != null) {
                mEtUpdateContent.setText(userInfo.getSign());
                mEtUpdateContent.setLines(3);
                mEtUpdateContent.setSelection(userInfo.getSign().length());
                orgContent = userInfo.getSign();
            }
        }
        if (updateInfo.equals(getResources().getString(R.string.tv_mobile))) {
            if (userInfo.getMobile() != null) {
                mEtUpdateContent.setText(userInfo.getMobile());
                mEtUpdateContent.setSelection(userInfo.getMobile().length());
                orgContent = userInfo.getMobile();
            }
        }

        if (updateInfo.equals(getResources().getString(R.string.tv_remakes))) {
            if (map.get("remarks") != null) {
                mEtUpdateContent.setText((String) map.get("remarks"));
                mEtUpdateContent.setSelection(map.get("remarks").toString().length());
                orgContent = (String) map.get("remarks");
            }
        }

    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
        mTvSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_return:
                finish();
                break;

            case R.id.tv_save:
                loadingUtils.show();
                Common.hideSoftKeyboard(UpdateInfoActivity.this);
                String content = mEtUpdateContent.getText().toString();
                if (updateInfo.equals(getResources().getString(R.string.tv_remakes))){
                    modifyRemakes(content);
                }else{
                    modifyUserInfo(content);
                }
                break;

            default:
                break;

        }
    }

    private void modifyUserInfo(final String content){
        com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("account", aCache.getAsString("account"));
                    if (content.equals(orgContent)) {
                        ToastUtil.toastOnUiThread(UpdateInfoActivity.this, "未修改");
                        loadingUtils.dismissOnUiThread();
                        return;
                    }
                    if (updateInfo.equals(getResources().getString(R.string.tv_nickname))) {
                        if (content.length() > 10) {
                            ToastUtil.toastOnUiThread(UpdateInfoActivity.this, "昵称长度不能超过10个字");
                            loadingUtils.dismissOnUiThread();
                            return;
                        }
                        parameterData.put("nickname", content);
                    }
                    if (updateInfo.equals(getResources().getString(R.string.tv_email))) {
                        boolean isEmail = Common.isEmail(content);
                        if (!isEmail) {
                            ToastUtil.toastOnUiThread(UpdateInfoActivity.this, "请输入正确邮箱");
                            loadingUtils.dismissOnUiThread();
                            return;
                        }
                        parameterData.put("email", content);
                    }
                    if (updateInfo.equals(getResources().getString(R.string.tv_sign))) {
                        parameterData.put("sign", content);
                    }
                    if (updateInfo.equals(getResources().getString(R.string.tv_mobile))) {
                        parameterData.put("mobile", content);
                    }
                    String result = userService.updateMyInfo(parameterData);
                    Log.e("TAG", "run: " + result + "0---------------------------");
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        if (updateInfo.equals(getResources().getString(R.string.tv_nickname))) {
                            userInfo.setNickname(content);
                            aCache.put("userInfo", (Serializable) userInfo);
                        }
                        if (updateInfo.equals(getResources().getString(R.string.tv_email))) {
                            userInfo.setEmail(content);
                            aCache.put("userInfo", (Serializable) userInfo);
                        }
                        if (updateInfo.equals(getResources().getString(R.string.tv_sign))) {
                            userInfo.setSign(content);
                            aCache.put("userInfo", (Serializable) userInfo);
                        }
                        if (updateInfo.equals(getResources().getString(R.string.tv_mobile))) {
                            userInfo.setMobile(content);
                            aCache.put("userInfo", (Serializable) userInfo);
                        }
                        MineFragment.updateData();
                        MyInfoActivity.updateData();
                        finish();
                        loadingUtils.dismissOnUiThread();
                    }else{
                        loadingUtils.dismissOnUiThread();
                        ToastUtil.toastOnUiThread(UpdateInfoActivity.this,resultObj.get("msg").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    loadingUtils.dismissOnUiThread();
                    ToastUtil.toastOnUiThread(UpdateInfoActivity.this,"发生异常");
                }
            }
        });
    }

    private void modifyRemakes(final String content){
        com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("account", aCache.getAsString("account"));
                    parameterData.put("fromAccount", map.get("fromAccount"));
                    if (content.equals(orgContent)) {
                        ToastUtil.toastOnUiThread(UpdateInfoActivity.this, "未修改");
                        loadingUtils.dismissOnUiThread();
                        return;
                    }
                    if (content.length() > 10) {
                        ToastUtil.toastOnUiThread(UpdateInfoActivity.this, "备注长度不能超过10个字");
                        loadingUtils.dismissOnUiThread();
                        return;
                    }
                    parameterData.put("remarks", content);
                    String result = friendServiceImpl.modifyFriendRemarks(parameterData);
                    Log.e("TAG", "run: " + result + "0---------------------------");
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        updateMessage();
                        ContentValues values = new ContentValues();
                        values.put("remarks", content);
                        getContentResolver().update(
                                MicroChatProvider.URI_QUERY_FRIENDS, values,
                                MicroChatDB.FriendsTable.ACCOUNT + "=? and "+MicroChatDB.FriendsTable.FROM_ACCOUNT+"=?",
                                new String[] { aCache.getAsString("account"),map.get("fromAccount").toString()});
                        FriendsFragment.updateData(aCache.getAsString("account"));
                        map.put("remarks",content);
                        UserMoreInfoActivity.updateData(map);
                        finish();
                        loadingUtils.dismissOnUiThread();
                    }else{
                        loadingUtils.dismissOnUiThread();
                        ToastUtil.toastOnUiThread(UpdateInfoActivity.this,resultObj.get("msg").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    loadingUtils.dismissOnUiThread();
                    ToastUtil.toastOnUiThread(UpdateInfoActivity.this,"发生异常");
                }
            }
        });
    }

    private void updateMessage() {
        MessageFragment.updateData();
    }

}
