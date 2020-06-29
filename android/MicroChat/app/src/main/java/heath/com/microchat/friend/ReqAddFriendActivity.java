package heath.com.microchat.friend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;

import org.json.JSONObject;

import java.util.HashMap;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.service.IFriendService;
import heath.com.microchat.service.impl.FriendServiceImpl;
import heath.com.microchat.utils.ACache;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;
import heath.com.microchat.utils.ThreadUtils;

public class ReqAddFriendActivity extends BaseActivity implements View.OnClickListener {

    private IFriendService friendService = new FriendServiceImpl();
    private HashMap map;
    private LinearLayout mLlReturn;
    private ImageView mIvHeadPhoto;
    private TextView mTvNickname;
    private TextView mTvAccount;
    private EditText mEtContent;
    private Button mBtnAdd;
    private ACache aCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_req_add_friend);
        aCache = ACache.get(this);
        initView();
        init();
        initListener();
    }

    private void initView() {
        Intent intent = getIntent();
        map = (HashMap) intent.getSerializableExtra("map");
        mLlReturn = this.findViewById(R.id.ll_return);
        mIvHeadPhoto = this.findViewById(R.id.iv_head_photo);
        mTvNickname = this.findViewById(R.id.tv_nickname);
        mTvAccount = this.findViewById(R.id.tv_account);
        mEtContent = this.findViewById(R.id.et_content);
        mBtnAdd = this.findViewById(R.id.btn_add);
    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
        mBtnAdd.setOnClickListener(this);
    }

    private void init() {
        if (map.containsKey("account")) {
            mTvAccount.setText(map.get("account").toString());
            String account = aCache.getAsString("account");
            if (map.get("account").toString().equals(account)) {
                mBtnAdd.setEnabled(false);
            }
        }
        if (map.containsKey("nickname")) {
            mTvNickname.setText((String) map.get("nickname"));
        }
        if (map.containsKey("icon")) {
            ImageUitl imageUitl = new ImageUitl(cache);
            imageUitl.asyncloadImage(mIvHeadPhoto, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + map.get("icon").toString());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_return:
                finish();
                break;
            case R.id.btn_add:
                final VerifyType verifyType = VerifyType.VERIFY_REQUEST; // 发起好友验证请求
                final String msg = mEtContent.getText().toString();
                NIMClient.getService(FriendService.class).addFriend(new AddFriendData(map.get("account").toString(), verifyType, msg))
                        .setCallback(new RequestCallback<Void>() {
                            @Override
                            public void onSuccess(Void param) {
                                ThreadUtils.runInThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            JSONObject data = new JSONObject();
                                            String account = aCache.getAsString("account");
                                            data.put("account", map.get("account").toString());
                                            data.put("fromAccount", account);
                                            data.put("content", msg);
                                            String result = friendService.requstAddFriends(data);
                                            Log.e("TAG", "run: " + result + "==============================");
                                            JSONObject resultObj = new JSONObject(result);
                                            Intent intent = new Intent(
                                                    ReqAddFriendActivity.this,
                                                    AddFriendResultActivity.class);
                                            if (resultObj.getString("code").equals("200")) {
                                                map.put("result", 1);
                                            } else {
                                                map.put("result", 0);
                                            }
                                            intent.putExtra("map", map);
                                            startActivityForResult(intent, 0);
                                            finish();
                                        } catch (Exception e) {
                                            Log.e("TAG", "onSuccess: ", e);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onFailed(int code) {
                                Intent intent = new Intent(
                                        ReqAddFriendActivity.this,
                                        AddFriendResultActivity.class);
                                intent.putExtra("result", 0);
                                startActivityForResult(intent, 0);
                                finish();
                            }

                            @Override
                            public void onException(Throwable exception) {
                                Intent intent = new Intent(
                                        ReqAddFriendActivity.this,
                                        AddFriendResultActivity.class);
                                intent.putExtra("result", 0);
                                startActivityForResult(intent, 0);
                                finish();
                                Toast.makeText(ReqAddFriendActivity.this, "服务器异常，请求添加好友失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                break;
            default:
                break;
        }
    }
}
