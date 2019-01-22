package heath.com.microchat.team;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.heath.recruit.utils.ThreadUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.adapter.ManageMemberAdapter;
import heath.com.microchat.entity.UserInfo;
import heath.com.microchat.service.IUserService;
import heath.com.microchat.service.impl.UserServiceImpl;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ToastUtil;

public class ApplyManageMemberActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout mLlReturn;
    private ImageView mIvHeadPhoto;
    private TextView mTvNickname;
    private LinearLayout mLlAddManageMember;
    private SwipeMenuListView mLvManageMember;
    private LoadingUtils loadingUtils;
    private Team team;
    private IUserService userServiceImpl;
    private Gson gson;
    private ManageMemberAdapter manageMemberAdapter;
    public static Context context;
    private JSONArray memberList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_member);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        Intent intent = getIntent();
        team = (Team) intent.getSerializableExtra("team");
        mLlReturn = findViewById(R.id.ll_return);
        mIvHeadPhoto = findViewById(R.id.iv_head_photo);
        mTvNickname = findViewById(R.id.tv_nickname);
        mLvManageMember = findViewById(R.id.lv_manage_member);
        mLlAddManageMember = findViewById(R.id.ll_add_manage_member);
        loadingUtils = new LoadingUtils(ApplyManageMemberActivity.this, "正在加载中");
        loadingUtils.creat();
        userServiceImpl = new UserServiceImpl();
        gson = new Gson();
        context = this;
        mLlAddManageMember.setVisibility(View.GONE);
    }

    private void initData() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("account", team.getCreator());
                    String result = userServiceImpl.queryUserInfo(parameterData);
                    Log.e("群主信息", "run: " + result);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        final UserInfo userInfo = gson.fromJson(resultObj.getJSONObject("userInfo").toString(), UserInfo.class);
                        ImageUitl imageUitl = new ImageUitl(cache);
                        imageUitl.asyncloadImage(mIvHeadPhoto, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + userInfo.getIcon());
                        Common.asyncloadText(mTvNickname, userInfo.getNickname());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        initManageMember();
    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_return:
                finish();
                break;
        }
    }

    private void initManageMember() {
        memberList = new JSONArray();
        NIMClient.getService(TeamService.class).queryMemberList(team.getId()).setCallback(new RequestCallbackWrapper<List<TeamMember>>() {
            @Override
            public void onResult(int code, final List<TeamMember> members, Throwable exception) {
                for (TeamMember member : members) {
                    if (member.getType() == TeamMemberType.Manager) {
                        memberList.put(member.getAccount());
                    }
                }
                Log.e("组员数量2", "run: " + memberList.length());
                ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("组员数量", "run: " + memberList.length());
                        if (memberList.length() == 0) {
                            loadingUtils.dismissOnUiThread();
                            if (manageMemberAdapter != null) {
                                ThreadUtils.runInUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        List<UserInfo> userInfos = new ArrayList<>();
                                        manageMemberAdapter.setList(userInfos);
                                        manageMemberAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                            return;
                        }
                        try {
                            JSONObject parameterData = new JSONObject();
                            parameterData.put("accounts", memberList);
                            String result = userServiceImpl.queryUsersInfo(parameterData);
                            JSONObject resultObj = new JSONObject(result);
                            if (resultObj.getString("code").equals("200")) {
                                loadingUtils.dismissOnUiThread();
                                final List<UserInfo> userInfos = gson.fromJson(resultObj.getJSONArray("userInfos").toString(), new TypeToken<List<UserInfo>>() {
                                }.getType());
                                Log.e("组员数量1", "run: " + userInfos.size());
                                Log.e("名字", "run: " + userInfos.get(0).getNickname());
                                ThreadUtils.runInUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        manageMemberAdapter = new ManageMemberAdapter(ApplyManageMemberActivity.this, userInfos, cache);
                                        mLvManageMember.setAdapter(manageMemberAdapter);
                                    }
                                });
                            } else {
                                loadingUtils.dismissOnUiThread();
                                ToastUtil.toastOnUiThread(ApplyManageMemberActivity.this, resultObj.get("msg").toString());
                            }
                        } catch (Exception e) {
                            loadingUtils.dismissOnUiThread();
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }


}
