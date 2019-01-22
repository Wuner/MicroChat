package heath.com.microchat.team;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.team.model.Team;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.entity.UserInfo;
import heath.com.microchat.friend.ReqAddFriendActivity;
import heath.com.microchat.message.SendMessageActivity;
import heath.com.microchat.service.IFriendService;
import heath.com.microchat.service.impl.FriendServiceImpl;
import heath.com.microchat.utils.ACache;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;
import heath.com.microchat.utils.ToastUtil;

public class TeamMemberInfoActivity extends BaseActivity implements View.OnClickListener {


    private HashMap map;
    private HashMap friendInfoMap;
    private HashMap<String, Object> userInfoMap;
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
    private IFriendService iFriendService;
    private ImageView mIvTeamMemberInfo;

    private ACache aCache;
    public static Activity context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_member_info);
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
        mIvTeamMemberInfo = this.findViewById(R.id.iv_team_member_info);
        iFriendService = new FriendServiceImpl();
        userInfoMap = new HashMap<>();
        context = this;
    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
        mBtnAdd.setOnClickListener(this);
        mBtnSendMessage.setOnClickListener(this);
        mIvTeamMemberInfo.setOnClickListener(this);
    }

    private void init() {
        UserInfo userInfo = (UserInfo) map.get("userinfo");
        if (map.containsKey("gone")){
            mIvTeamMemberInfo.setVisibility(View.GONE);
        }
        String account = aCache.getAsString("account");
        List<String> friends = NIMClient.getService(FriendService.class).getFriendAccounts();
        assert userInfo != null;
        userInfoMap.put("account", userInfo.getAccount());
        userInfoMap.put("nickname", userInfo.getNickname());
        userInfoMap.put("icon", userInfo.getIcon());
        mTvAccount.setText(userInfo.getAccount());
        if (friends.contains(userInfo.getAccount())) {
            mBtnSendMessage.setVisibility(View.VISIBLE);
            mBtnAdd.setVisibility(View.GONE);
            friendInfoMap = get(userInfo.getAccount());
        } else {
            mBtnAdd.setVisibility(View.VISIBLE);
            mBtnSendMessage.setVisibility(View.GONE);
            if (account.equals(userInfo.getAccount())) {
                mBtnAdd.setEnabled(false);
                mBtnAdd.setBackgroundColor(getResources().getColor(R.color.gray));
            }
        }
        if (userInfo.getNickname() != null) {
            mTvNickname.setText(userInfo.getNickname());
        }
        if (userInfo.getIcon() != null) {
            ImageUitl imageUitl = new ImageUitl(cache);
            imageUitl.asyncloadImage(mIvHeadPhoto, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + userInfo.getIcon());
        }
        if (userInfo.getSign() != null) {
            mTvSign.setText(userInfo.getSign());
        }
        if (userInfo.getEmail() != null) {
            mTvEmail.setText(userInfo.getEmail());
        }
        if (userInfo.getBirth() != null) {
            mTvBirth.setText(userInfo.getBirth());
        }
        if (userInfo.getMobile() != null) {
            mTvMobile.setText(userInfo.getMobile());
        }
        if (userInfo.getGender() != null) {
            String gender = null;
            switch (userInfo.getGender()) {
                case "1":
                    gender = "男";
                    break;
                case "2":
                    gender = "女";
                    break;
                case "0":
                    gender = "未知";
                    break;
            }
            mTvSex.setText(gender);
        }
        if (userInfo.getEx() != null) {
            try {
                JSONObject ex = new JSONObject(userInfo.getEx().replace("\"", ""));
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
                        TeamMemberInfoActivity.this,
                        ReqAddFriendActivity.class);
                intent.putExtra("map", userInfoMap);
                startActivityForResult(intent, 0);
                break;
            case R.id.btn_send_message:
                intent = new Intent(
                        TeamMemberInfoActivity.this,
                        SendMessageActivity.class);
                intent.putExtra("map", friendInfoMap);
                startActivityForResult(intent, 0);
                break;
            case R.id.iv_team_member_info:
                intent = new Intent(
                        TeamMemberInfoActivity.this,
                        TeamMemberInfoMoreActivity.class);
                intent.putExtra("map", map);
                startActivityForResult(intent, 0);
                break;

            default:
                break;
        }
    }

    private HashMap<String, Object> get(final String account) {
        final HashMap<String, Object> listmap = new HashMap<>();
        com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject parameterData = new JSONObject();
                    parameterData.put("account", aCache.getAsString("account"));
                    parameterData.put("fromAccount", account);
                    String results = iFriendService.queryFriendInfoByAccount(parameterData);
                    JSONObject resultObj = new JSONObject(results);
                    if (resultObj.getString("code").equals("200")) {
                        final JSONObject friendInfo = resultObj.getJSONObject("friendInfo");
                        Log.e("TAG", "onResult: " + "----------===================" + friendInfo.toString());
                        if (friendInfo.has("account")) {
                            listmap.put("account", friendInfo.getString("account"));
                        }
                        if (friendInfo.has("fromAccount")) {
                            listmap.put("fromAccount", friendInfo.getString("fromAccount"));
                        }
                        if (friendInfo.has("nickname")) {
                            listmap.put("nickname", friendInfo.getString("nickname"));
                        }
                        if (friendInfo.has("icon")) {
                            listmap.put("icon", friendInfo.getString("icon"));
                        }
                        if (friendInfo.has("sign")) {
                            listmap.put("sign", friendInfo.getString("sign"));
                        }

                        if (friendInfo.has("remarks")) {
                            listmap.put("remarks", friendInfo.getString("remarks"));
                        }

                        if (friendInfo.has("birth")) {
                            listmap.put("birth", friendInfo.getString("birth"));
                        }

                        if (friendInfo.has("email")) {
                            listmap.put("email", friendInfo.getString("email"));
                        }

                        if (friendInfo.has("mobile")) {
                            listmap.put("mobile", friendInfo.getString("mobile"));
                        }

                        if (friendInfo.has("ex") && friendInfo.getString("ex").length() > 0) {
                            JSONObject ex = new JSONObject(friendInfo.getString("ex").replace("\"", ""));
                            if (ex.has("age")) {
                                listmap.put("age", ex.getString("age"));
                            }
                            listmap.put("ex", friendInfo.getString("ex"));
                        }
                        if (friendInfo.has("gender")) {
                            String gender = friendInfo.getString("gender");
                            switch (gender) {
                                case "0":
                                    listmap.put("gender", "http://testwx.club:8080/SpongeWaySpringMvc/upload/unknown.png");
                                    listmap.put("genderStr", "未知");
                                    break;
                                case "1":
                                    listmap.put("gender", "http://testwx.club:8080/SpongeWaySpringMvc/upload/boy.png");
                                    listmap.put("genderStr", "男");
                                    break;
                                case "2":
                                    listmap.put("gender", "http://testwx.club:8080/SpongeWaySpringMvc/upload/girl.png");
                                    listmap.put("genderStr", "女");
                                    break;
                                default:
                                    break;
                            }
                        }
                    }else{
                        ToastUtil.toastOnUiThread(TeamMemberInfoActivity.this,resultObj.get("msg").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(TeamMemberInfoActivity.this,"发生异常");
                }
            }
        });
        return listmap;
    }
}
