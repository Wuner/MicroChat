package heath.com.microchat.friend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.friend.FriendService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.entity.FriendBean;
import heath.com.microchat.message.SendMessageActivity;
import heath.com.microchat.utils.ACache;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;

public class UserDetailedInfoActivity extends BaseActivity implements View.OnClickListener {


    private HashMap map;
    private LinearLayout mLlReturn;
    private ImageView mIvHeadPhoto;
    private TextView mTvNickname;
    private TextView mTvAccount;
    private TextView mTvSex;
    private TextView mTvAge;
    private TextView mTvConstellation;
    private TextView mTvRegion;
    private TextView mTvSign;
    private TextView mTvBirth;
    private TextView mTvEmail;
    private TextView mTvMobile;
    private Button mBtnAdd;
    private Button mBtnSendMessage;
    private ImageView mIvUserMoreInfo;
    public static Activity activity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detailed_info);
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
        mTvSex = this.findViewById(R.id.tv_sex);
        mTvAge = this.findViewById(R.id.tv_age);
        mTvConstellation = this.findViewById(R.id.tv_constellation);
        mTvRegion = this.findViewById(R.id.tv_region);
        mTvSign = this.findViewById(R.id.tv_sign);
        mTvBirth = this.findViewById(R.id.tv_birth);
        mTvEmail = this.findViewById(R.id.tv_email);
        mTvMobile = this.findViewById(R.id.tv_mobile);
        mBtnAdd = this.findViewById(R.id.btn_add);
        mBtnSendMessage = this.findViewById(R.id.btn_send_message);
        mIvUserMoreInfo = findViewById(R.id.iv_user_more_info);
        activity = this;
    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
        mBtnAdd.setOnClickListener(this);
        mBtnSendMessage.setOnClickListener(this);
        mIvUserMoreInfo.setOnClickListener(this);
    }

    private void init() {
        /*if ("fragment_friends".equals(map.get("from_activity"))){
            if (friendBean.getAccount()!=null && friendBean.getAccount().equals("null")) {
                String account = aCache.getAsString("account");
                List<String> friends = NIMClient.getService(FriendService.class).getFriendAccounts();
                if (friendBean.getFromAccount()!=null && friendBean.getFromAccount().equals("null")) {
                    mTvAccount.setText(friendBean.getFromAccount());
                    if (account.equals(friendBean.getFromAccount())) {
                        mBtnSendMessage.setVisibility(View.GONE);
                        mBtnAdd.setEnabled(false);
                        mBtnAdd.setBackgroundColor(getResources().getColor(R.color.gray));
                    } else {
                        if (friends.contains(friendBean.getFromAccount())) {
                            mBtnSendMessage.setVisibility(View.VISIBLE);
                            mIvUserMoreInfo.setVisibility(View.VISIBLE);
                            mBtnAdd.setVisibility(View.GONE);
                        } else {
                            mBtnAdd.setVisibility(View.VISIBLE);
                            mIvUserMoreInfo.setVisibility(View.GONE);
                            mBtnSendMessage.setVisibility(View.GONE);
                        }
                    }
                } else {
                    mTvAccount.setText(friendBean.getAccount());
                    if (account.equals(friendBean.getAccount())) {
                        mBtnSendMessage.setVisibility(View.GONE);
                        mIvUserMoreInfo.setVisibility(View.GONE);
                        mBtnAdd.setEnabled(false);
                        mBtnAdd.setBackgroundColor(getResources().getColor(R.color.gray));
                    } else {
                        if (friends.contains(friendBean.getAccount())) {
                            mBtnSendMessage.setVisibility(View.VISIBLE);
                            mIvUserMoreInfo.setVisibility(View.VISIBLE);
                            mBtnAdd.setVisibility(View.GONE);
                        } else {
                            mBtnAdd.setVisibility(View.VISIBLE);
                            mIvUserMoreInfo.setVisibility(View.GONE);
                            mBtnSendMessage.setVisibility(View.GONE);
                        }
                    }
                }
            }
            if (friendBean.getUserInfo().getNickname()!=null && friendBean.getUserInfo().getNickname().equals("null")) {
                mTvNickname.setText(friendBean.getUserInfo().getNickname());
            }
            if (friendBean.getUserInfo().getIcon()!=null && friendBean.getUserInfo().getIcon().equals("null")) {
                ImageUitl imageUitl = new ImageUitl(cache);
                imageUitl.asyncloadImage(mIvHeadPhoto, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + friendBean.getUserInfo().getIcon());
            }
            if (friendBean.getUserInfo().getSign()!=null && friendBean.getUserInfo().getSign().equals("null")) {
                mTvSign.setText(friendBean.getUserInfo().getSign());
            }
            if (friendBean.getUserInfo().getEmail()!=null && friendBean.getUserInfo().getEmail().equals("null")) {
                mTvEmail.setText(friendBean.getUserInfo().getEmail());
            }
            if (friendBean.getUserInfo().getBirth()!=null && friendBean.getUserInfo().getBirth().equals("null")) {
                mTvBirth.setText(friendBean.getUserInfo().getBirth());
            }
            if (friendBean.getUserInfo().getMobile()!=null && friendBean.getUserInfo().getMobile().equals("null")) {
                mTvMobile.setText(friendBean.getUserInfo().getMobile());
            }
            if (friendBean.getUserInfo().getGender()!=null && friendBean.getUserInfo().getGender().equals("null")) {
                mTvSex.setText((String) map.get("genderStr"));
            }
            if (friendBean.getUserInfo().getEmail()!=null && friendBean.getUserInfo().getEmail().equals("null")) {
                try {
                    JSONObject ex = new JSONObject(((String) map.get("ex")).replace("\"", ""));
                    if (ex.has("age")) {
                        mTvAge.setText(ex.getString("age"));
                    }
                    if (ex.has("constellation")) {
                        mTvConstellation.setText(ex.getString("constellation"));
                    }
                    if (ex.has("region")) {
                        mTvRegion.setText(ex.getString("region"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }else{

        }*/

        if (map.containsKey("account")) {
            String account = aCache.getAsString("account");
            List<String> friends = NIMClient.getService(FriendService.class).getFriendAccounts();
            if (map.containsKey("fromAccount")) {
                mTvAccount.setText((String) map.get("fromAccount"));
                if (account.equals(map.get("fromAccount"))) {
                    mBtnSendMessage.setVisibility(View.GONE);
                    mBtnAdd.setEnabled(false);
                    mBtnAdd.setBackgroundColor(getResources().getColor(R.color.gray));
                } else {
                    if (friends.contains(map.get("fromAccount"))) {
                        mBtnSendMessage.setVisibility(View.VISIBLE);
                        mIvUserMoreInfo.setVisibility(View.VISIBLE);
                        mBtnAdd.setVisibility(View.GONE);
                    } else {
                        mBtnAdd.setVisibility(View.VISIBLE);
                        mIvUserMoreInfo.setVisibility(View.GONE);
                        mBtnSendMessage.setVisibility(View.GONE);
                    }
                }
            } else {
                mTvAccount.setText((String) map.get("account"));
                if (account.equals(map.get("account"))) {
                    mBtnSendMessage.setVisibility(View.GONE);
                    mIvUserMoreInfo.setVisibility(View.GONE);
                    mBtnAdd.setEnabled(false);
                    mBtnAdd.setBackgroundColor(getResources().getColor(R.color.gray));
                } else {
                    if (friends.contains(map.get("account"))) {
                        mBtnSendMessage.setVisibility(View.VISIBLE);
                        mIvUserMoreInfo.setVisibility(View.VISIBLE);
                        mBtnAdd.setVisibility(View.GONE);
                    } else {
                        mBtnAdd.setVisibility(View.VISIBLE);
                        mIvUserMoreInfo.setVisibility(View.GONE);
                        mBtnSendMessage.setVisibility(View.GONE);
                    }
                }
            }
        }
        if (map.containsKey("nickname")) {
            mTvNickname.setText((String) map.get("nickname"));
        }
        if (map.containsKey("icon")) {
            ImageUitl imageUitl = new ImageUitl(cache);
            imageUitl.asyncloadImage(mIvHeadPhoto, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + map.get("icon"));
        }
        if (map.containsKey("sign")) {
            mTvSign.setText((String) map.get("sign"));
        }
        if (map.containsKey("email")) {
            mTvEmail.setText((String) map.get("email"));
        }
        if (map.containsKey("birth")) {
            mTvBirth.setText((String) map.get("birth"));
        }
        if (map.containsKey("mobile")) {
            mTvMobile.setText((String) map.get("mobile"));
        }
        if (map.containsKey("genderStr")) {
            mTvSex.setText((String) map.get("genderStr"));
        }
        if (map.containsKey("ex")) {
            try {
                JSONObject ex = new JSONObject(((String) map.get("ex")).replace("\"", ""));
                if (ex.has("age")) {
                    mTvAge.setText(ex.getString("age"));
                }
                if (ex.has("constellation")) {
                    mTvConstellation.setText(ex.getString("constellation"));
                }
                if (ex.has("region")) {
                    mTvRegion.setText(ex.getString("region"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        final Intent intent;
        switch (view.getId()) {
            case R.id.ll_return:
                finish();
                break;
            case R.id.btn_add:
                intent = new Intent(
                        UserDetailedInfoActivity.this,
                        ReqAddFriendActivity.class);
                intent.putExtra("map", map);
                startActivityForResult(intent, 0);
                break;
            case R.id.btn_send_message:
                intent = new Intent(
                        UserDetailedInfoActivity.this,
                        SendMessageActivity.class);
                intent.putExtra("map", map);
                startActivityForResult(intent, 0);
                break;

            case R.id.iv_user_more_info:
                intent = new Intent(
                        UserDetailedInfoActivity.this,
                        UserMoreInfoActivity.class);
                intent.putExtra("map", map);
                startActivityForResult(intent, 0);
                break;
        }
    }
}
