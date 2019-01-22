package heath.com.microchat.friend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;

import org.json.JSONObject;

import java.util.HashMap;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.TabHostActivity;
import heath.com.microchat.service.IFriendService;
import heath.com.microchat.service.impl.FriendServiceImpl;
import heath.com.microchat.utils.ACache;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ToastUtil;

public class FriendsNoticeProcessedActivity extends BaseActivity implements View.OnClickListener {

    private HashMap map;
    private LinearLayout mLlReturn;
    private ImageView mIvHeadPhoto;
    private ImageView mIvSex;
    private TextView mTvNickname;
    private TextView mTvAccount;
    private TextView mTvAge;
    private TextView mTvContent;
    private TextView mTvState;
    private Button mBtnAgree;
    private Button mBtnRefuse;
    private LinearLayout mLlGotoUserinfo;
    private IFriendService friendService;
    private static ACache aCache;
    private LoadingUtils loadingUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_notice_processed);
        initView();
        initListener();
        init();
    }

    private void initView() {
        Intent intent = getIntent();
        map = (HashMap) intent.getSerializableExtra("map");
        mLlReturn = this.findViewById(R.id.ll_return);
        mIvHeadPhoto = this.findViewById(R.id.iv_head_photo);
        mTvNickname = this.findViewById(R.id.tv_nickname);
        mTvAccount = this.findViewById(R.id.tv_account);
        mIvSex = this.findViewById(R.id.iv_sex);
        mTvAge = this.findViewById(R.id.tv_age);
        mTvContent = this.findViewById(R.id.tv_content);
        mTvState = this.findViewById(R.id.tv_state);
        mBtnAgree = this.findViewById(R.id.btn_agree);
        mBtnRefuse = this.findViewById(R.id.btn_refuse);
        mLlGotoUserinfo = this.findViewById(R.id.ll_goto_userinfo);

        aCache = ACache.get(this);
        friendService = new FriendServiceImpl();
        loadingUtils = new LoadingUtils(FriendsNoticeProcessedActivity.this, "正在搜索");
        loadingUtils.creat();
    }

    private void init() {
        if (map.containsKey("fromAccount")) {
            mTvAccount.setText("(" + map.get("fromAccount").toString() + ")");
        }else{
            if (map.containsKey("account")) {
                mTvAccount.setText("(" + map.get("account").toString() + ")");
            }
        }
        if (map.containsKey("nickname")) {
            mTvNickname.setText(map.get("nickname").toString());
        }
        if (map.containsKey("icon")) {
            ImageUitl imageUitl = new ImageUitl(cache);
            imageUitl.asyncloadImage(mIvHeadPhoto, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + map.get("icon").toString());
        }
        if (map.containsKey("gender")) {
            ImageUitl.setImageBitmap(map.get("gender").toString(), mIvSex);
        }
        if (map.containsKey("age")) {
            mTvAge.setText(map.get("age").toString());
        }
        if (map.containsKey("state")) {
            String state = map.get("state").toString();
            if (state.equals("1")) {
                mTvState.setVisibility(View.VISIBLE);
                mBtnAgree.setVisibility(View.GONE);
                mBtnRefuse.setVisibility(View.GONE);
                mTvState.setText(getResources().getString(R.string.tv_agree1));
            } else if (state.equals("2")) {
                mTvState.setVisibility(View.VISIBLE);
                mBtnAgree.setVisibility(View.GONE);
                mBtnRefuse.setVisibility(View.GONE);
                mTvState.setText(getResources().getString(R.string.tv_refuse1));
            } else {
                mTvState.setVisibility(View.GONE);
                mBtnAgree.setVisibility(View.VISIBLE);
                mBtnRefuse.setVisibility(View.VISIBLE);
            }
        }
        if (map.containsKey("content")) {
            mTvContent.setText(map.get("content").toString());
        }
    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
        mLlGotoUserinfo.setOnClickListener(this);
        mBtnAgree.setOnClickListener(this);
        mBtnRefuse.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_return:
                finish();
                break;
            case R.id.ll_goto_userinfo:
                Intent intent = new Intent(
                        FriendsNoticeProcessedActivity.this,
                        UserDetailedInfoActivity.class);
                map.put("from_activity", "activity_friends_notice_processed");
                intent.putExtra("map", map);
                startActivityForResult(intent, 0);
                break;
            case R.id.btn_agree:
                loadingUtils.show();
                NIMClient.getService(FriendService.class).ackAddFriendRequest(map.get("fromAccount").toString(), true).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        loadingUtils.dismiss();
                        modifyState(map.get("fromAccount").toString(), "1");
                        map.put("state", "1");
                        mBtnAgree.setVisibility(View.GONE);
                        mBtnRefuse.setVisibility(View.GONE);
                        mTvState.setVisibility(View.VISIBLE);
                        mTvState.setText(getResources().getString(R.string.tv_agree1));
                        Log.e("添加好友成功", "run: "  + "==============================");
                    }

                    @Override
                    public void onFailed(int code) {
                        loadingUtils.dismiss();
                        System.out.print(1);
                        Log.e("添加好友失败", "run: " + code + "==============================");
                    }

                    @Override
                    public void onException(Throwable exception) {
                        loadingUtils.dismiss();
                    }
                });
                break;
            case R.id.btn_refuse:
                loadingUtils.show();
                NIMClient.getService(FriendService.class).ackAddFriendRequest(map.get("fromAccount").toString(), false).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        loadingUtils.dismiss();
                        modifyState(map.get("account").toString(), "2");
                        map.put("state", "2");
                        mBtnAgree.setVisibility(View.GONE);
                        mBtnRefuse.setVisibility(View.GONE);
                        mTvState.setText(getResources().getString(R.string.tv_refuse1));
                        mTvState.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailed(int code) {
                        loadingUtils.dismiss();
                        System.out.print(1);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        loadingUtils.dismiss();
                    }
                });
                break;
            default:
                break;
        }
    }

    private void modifyState(final String fromAccount, final String state) {
        com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String account = aCache.getAsString("account");
                    JSONObject data = new JSONObject();
                    data.put("fromAccount", fromAccount);
                    data.put("account", account);
                    data.put("state", state);
                    String result = friendService.modifyFriendRelationshipState(data);
                    Log.e("TAG", "run: " + result + "==============================");
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        TabHostActivity.queryReqAddNums();
                        FriendsFragment.queryReqAddNums(account);
                    } else {
                        ToastUtil.toastOnUiThread(FriendsNoticeProcessedActivity.this, resultObj.get("msg").toString());
                    }
                } catch (Exception e) {
                    Log.e("TAG", "onSuccess: ", e);
                }
            }
        });
    }
}
