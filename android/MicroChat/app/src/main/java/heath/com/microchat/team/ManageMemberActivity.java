package heath.com.microchat.team;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.heath.recruit.utils.ThreadUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.adapter.ManageMemberAdapter;
import heath.com.microchat.entity.UserInfo;
import heath.com.microchat.service.ITeamService;
import heath.com.microchat.service.IUserService;
import heath.com.microchat.service.impl.TeamServiceImpl;
import heath.com.microchat.service.impl.UserServiceImpl;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ToastUtil;

public class ManageMemberActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout mLlReturn;
    private ImageView mIvHeadPhoto;
    private TextView mTvNickname;
    private SwipeMenuListView mLvManageMember;
    private LoadingUtils loadingUtils;
    private Team team;
    private IUserService userServiceImpl;
    private ITeamService teamServiceImpl;
    private Gson gson;
    private ManageMemberAdapter manageMemberAdapter;
    private Context context;
    private LinearLayout mLlAddManageMember;
    private static Handler handler;
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
        loadingUtils = new LoadingUtils(ManageMemberActivity.this, "正在加载中");
        loadingUtils.creat();
        userServiceImpl = new UserServiceImpl();
        teamServiceImpl = new TeamServiceImpl();
        gson = new Gson();
        context = this;
        mLlAddManageMember = findViewById(R.id.ll_add_manage_member);
        handler = new IHandler();
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
        mLlAddManageMember.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_return:
                finish();
                break;
            case R.id.ll_add_manage_member:
                Intent intent = new Intent(
                        ManageMemberActivity.this,
                        AddManageMemberActivity.class);
                Map<String, Object> map = new HashMap<>();
                intent.putExtra("team", team);
                startActivityForResult(intent, 0);
                break;
        }
    }

    private void setLeftRemove() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                openItem.setBackground(new ColorDrawable(getResources().getColor(R.color.red1)));
                openItem.setWidth(Common.dp2px(context, (float) 120));
                openItem.setTitle(getResources().getString(R.string.tv_remove_manage_member));
                openItem.setTitleSize(18);
                openItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(openItem);
            }
        };
        mLvManageMember.setMenuCreator(creator);
        mLvManageMember.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        List<String> list = new ArrayList<>();
                        try {
                            list.add(memberList.getString(position));
                            removeTeamManager(team.getId(),list);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                }
                return false;
            }
        });
    }

    private void initManageMember() {
        memberList = new JSONArray();
        NIMClient.getService(TeamService.class).queryMemberList(team.getId()).setCallback(new RequestCallbackWrapper<List<TeamMember>>() {
            @Override
            public void onResult(int code, final List<TeamMember> members, Throwable exception) {
                for (TeamMember member : members) {
                    if (member.getType() == TeamMemberType.Manager ) {
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
                            if(manageMemberAdapter!=null){
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
                                        manageMemberAdapter = new ManageMemberAdapter(ManageMemberActivity.this, userInfos, cache);
                                        mLvManageMember.setAdapter(manageMemberAdapter);
                                        setLeftRemove();
                                    }
                                });
                            } else {
                                loadingUtils.dismissOnUiThread();
                                ToastUtil.toastOnUiThread(ManageMemberActivity.this, resultObj.get("msg").toString());
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

    private void removeTeamManager(final String teamId, final List<String> accountList) {
        NIMClient.getService(TeamService.class).removeManagers(teamId, accountList).setCallback(new RequestCallback<List<TeamMember>>() {
            @Override
            public void onSuccess(List<TeamMember> members) {
                // 移除群管理员成功
                ToastUtil.toastOnUiThread(ManageMemberActivity.this, "移除群管理员成功");
                modify(teamId,accountList.get(0),TeamMemberType.Normal.toString());
                updateData();
            }

            @Override
            public void onFailed(int code) {
                // 移除群管理员失败
                ToastUtil.toastOnUiThread(ManageMemberActivity.this, "移除群管理员失败");
            }

            @Override
            public void onException(Throwable exception) {
                // 错误
                ToastUtil.toastOnUiThread(ManageMemberActivity.this, "移除群管理员错误");
            }
        });
    }

    public static void updateData() {
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
            initManageMember();
            loadingUtils.dismissOnUiThread();
        }
    }

    private void modify(final String tid, final String account, final String text) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("account", account);
                    parameterData.put("tid", tid);
                    parameterData.put("type", text);
                    String result = teamServiceImpl.modifyTeamMember(parameterData);
                    Log.e("TAG", "run: " + result + "0---------------------------");
                    JSONObject resultObj = new JSONObject(result);
                    if (!resultObj.getString("code").equals("200")) {
                        ToastUtil.toastOnUiThread(ManageMemberActivity.this, resultObj.get("msg").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(ManageMemberActivity.this, "发生异常");
                }
            }
        });
    }

}
