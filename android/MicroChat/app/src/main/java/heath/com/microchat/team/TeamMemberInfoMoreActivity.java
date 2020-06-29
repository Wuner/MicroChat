package heath.com.microchat.team;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.suke.widget.SwitchButton;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.service.ITeamService;
import heath.com.microchat.service.impl.TeamServiceImpl;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ThreadUtils;
import heath.com.microchat.utils.TimeUtils;
import heath.com.microchat.utils.ToastUtil;

public class TeamMemberInfoMoreActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout mLlReturn;
    private TextView mTvTeamMemberName;
    private TextView mTvEntryTeamTime;
    private SwitchButton mSbSettingMute;
    private HashMap<String, Object> map;
    private TeamMember member;
    private SwitchButton mSbSettingTeamManager;
    private RelativeLayout mRlTeamMemberName;
    private ITeamService teamServiceImpl;
    private Button mBtnRemoveTeam;
    private LoadingUtils loadingUtils;
    private RelativeLayout mRlSettingTeamManager;
    private RelativeLayout mRlSettingTeamBanned;
    private static Handler handler;
    private Team team;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_member_info_more);
        initView();
        initData(map);
        initListener();
    }

    private void initView() {
        Intent intent = getIntent();
        map = (HashMap<String, Object>) intent.getSerializableExtra("map");
        handler = new IHandler();
        mLlReturn = this.findViewById(R.id.ll_return);
        mTvTeamMemberName = this.findViewById(R.id.tv_team_member_name);
        mTvEntryTeamTime = this.findViewById(R.id.tv_entry_team_time);
        mSbSettingTeamManager = this.findViewById(R.id.sb_setting_team_manager);
        mRlTeamMemberName = this.findViewById(R.id.rl_team_member_name);
        mBtnRemoveTeam = this.findViewById(R.id.btn_remove_team);
        mRlSettingTeamManager = findViewById(R.id.rl_setting_team_manager);
        mRlSettingTeamBanned = findViewById(R.id.rl_setting_team_banned);
        mSbSettingMute = findViewById(R.id.sb_setting_mute);
        teamServiceImpl = new TeamServiceImpl();
        loadingUtils = new LoadingUtils(TeamMemberInfoMoreActivity.this, "正在努力加载中");
        loadingUtils.creat();
    }

    private void initData(HashMap map) {
        member = (TeamMember) map.get("member");
        team = (Team) map.get("team");
        assert member != null;
        mTvTeamMemberName.setText(member.getTeamNick());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(member.getJoinTime());
        String lastSendTime = simpleDateFormat.format(date);
        String time;
        try {
            if (TimeUtils.IsToday(lastSendTime)) {
                time = lastSendTime.substring(11, 16);
            } else if (TimeUtils.IsYesterday(lastSendTime)) {
                time = "昨天";
            } else if (!TimeUtils.IsToyear(lastSendTime)) {
                time = lastSendTime.substring(0, 10);
            } else {
                time = lastSendTime.substring(5, 10);
            }
            mTvEntryTeamTime.setText(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (member.getType() == TeamMemberType.Manager) {
            mSbSettingTeamManager.setChecked(true);
        } else {
            mSbSettingTeamManager.setChecked(false);
        }
        if (member.isMute()) {
            mSbSettingMute.setChecked(true);
        } else {
            mSbSettingMute.setChecked(false);
        }
        getMember();

    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
        mRlTeamMemberName.setOnClickListener(this);
        mBtnRemoveTeam.setOnClickListener(this);
        mSbSettingTeamManager.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                List<String> accountList = new ArrayList<>();
                accountList.add(member.getAccount());
                if (isChecked) {
                    setTeamManager(member.getTid(), accountList);
                } else {
                    removeTeamManager(member.getTid(), accountList);
                }
            }
        });
        mSbSettingMute.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                loadingUtils.show();
                if (isChecked) {
                    setMute(1);
                } else {
                    setMute(0);
                }
            }
        });
    }

    private void setMute(final int mute){
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject parameterData = new JSONObject();
                    parameterData.put("owner", team.getCreator());
                    parameterData.put("tid", team.getId());
                    parameterData.put("mute", mute);
                    parameterData.put("account", member.getAccount());
                    String result = teamServiceImpl.muteMember(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    loadingUtils.dismissOnUiThread();
                    if (resultObj.getString("code").equals("200")) {
                        ToastUtil.toastOnUiThread(TeamMemberInfoMoreActivity.this, resultObj.get("msg").toString());
                    } else {
                        ToastUtil.toastOnUiThread(TeamMemberInfoMoreActivity.this, resultObj.get("msg").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    loadingUtils.dismissOnUiThread();
                }
            }
        });
    }

    private void setTeamManager(final String teamId, final List<String> accountList) {
        NIMClient.getService(TeamService.class).addManagers(teamId, accountList).setCallback(new RequestCallback<List<TeamMember>>() {
            @Override
            public void onSuccess(List<TeamMember> managers) {
                // 添加群管理员成功
                ToastUtil.toastOnUiThread(TeamMemberInfoMoreActivity.this, "添加群管理员成功");
                getTeamMemberInfo(teamId, accountList.get(0));
                modify(teamId, accountList.get(0), TeamMemberType.Manager.toString(), 0);
            }

            @Override
            public void onFailed(int code) {
                // 添加群管理员失败
                ToastUtil.toastOnUiThread(TeamMemberInfoMoreActivity.this, "添加群管理员失败");
            }

            @Override
            public void onException(Throwable exception) {
                // 错误
                ToastUtil.toastOnUiThread(TeamMemberInfoMoreActivity.this, "添加群管理员错误");
            }
        });
    }

    private void removeTeamManager(final String teamId, final List<String> accountList) {
        NIMClient.getService(TeamService.class).removeManagers(teamId, accountList).setCallback(new RequestCallback<List<TeamMember>>() {
            @Override
            public void onSuccess(List<TeamMember> members) {
                // 移除群管理员成功
                ToastUtil.toastOnUiThread(TeamMemberInfoMoreActivity.this, "移除群管理员成功");
                getTeamMemberInfo(teamId, accountList.get(0));
                modify(teamId, accountList.get(0), TeamMemberType.Normal.toString(), 0);
            }

            @Override
            public void onFailed(int code) {
                // 移除群管理员失败
                ToastUtil.toastOnUiThread(TeamMemberInfoMoreActivity.this, "移除群管理员失败");
            }

            @Override
            public void onException(Throwable exception) {
                // 错误
                ToastUtil.toastOnUiThread(TeamMemberInfoMoreActivity.this, "移除群管理员错误");
            }
        });
    }

    private void getTeamMemberInfo(String teamId, String account) {
        NIMClient.getService(TeamService.class).queryTeamMember(teamId, account).setCallback(new RequestCallbackWrapper<TeamMember>() {
            @Override
            public void onResult(int code, TeamMember teamMember, Throwable exception) {
                map.put("member", teamMember);
            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.ll_return:
                finish();
                break;
            case R.id.rl_team_member_name:
                intent = new Intent(
                        TeamMemberInfoMoreActivity.this,
                        UpdateTeamMemberInfoActivity.class);
                map.put("updateInfo", getResources().getString(R.string.tv_team_member_name));
                map.put("from", "TeamMemberInfoMoreActivity");
                intent.putExtra("map", map);
                startActivityForResult(intent, 0);
                break;
            case R.id.btn_remove_team:
                loadingUtils.show();
                removeMember(member.getTid(), member.getAccount());
                break;
        }
    }

    private void modify(final String tid, final String account, final String text, final int type) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("account", account);
                    parameterData.put("tid", tid);
                    switch (type) {
                        case 0:
                            parameterData.put("type", text);
                            break;
                        case 1:
                            parameterData.put("isMute", text);
                            break;
                    }
                    String result = teamServiceImpl.modifyTeamMember(parameterData);
                    Log.e("TAG", "run: " + result + "0---------------------------");
                    JSONObject resultObj = new JSONObject(result);
                    if (!resultObj.getString("code").equals("200")) {
                        ToastUtil.toastOnUiThread(TeamMemberInfoMoreActivity.this, resultObj.get("msg").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(TeamMemberInfoMoreActivity.this, "发生异常");
                }
            }
        });
    }

    private void removeMember(final String tid, final String account) {
        NIMClient.getService(TeamService.class).removeMember(tid, account).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                // 成功
                ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject parameterData = new JSONObject();
                        try {
                            parameterData.put("tid", tid);
                            parameterData.put("account", member.getAccount());
                            String result = teamServiceImpl.removeMember(parameterData);
                            teamServiceImpl.delTeamMemberByTidAndAccount(parameterData);
                            JSONObject resultObj = new JSONObject(result);
                            if (resultObj.getString("code").equals("200")) {
                                loadingUtils.dismissOnUiThread();
                                TeamInfoActivity.updateData();
                                finish();
                                TeamMemberInfoActivity.context.finish();
                            } else {
                                loadingUtils.dismissOnUiThread();
                                ToastUtil.toastOnUiThread(TeamMemberInfoMoreActivity.this, resultObj.get("msg").toString());
                            }
                        } catch (Exception e) {
                            loadingUtils.dismissOnUiThread();
                            e.printStackTrace();
                            ToastUtil.toastOnUiThread(TeamMemberInfoMoreActivity.this, "发生异常");
                        }
                    }
                });
            }

            @Override
            public void onFailed(int code) {
                // 失败
                ToastUtil.toastOnUiThread(TeamMemberInfoMoreActivity.this, "移除失败");
                loadingUtils.dismissOnUiThread();
            }

            @Override
            public void onException(Throwable exception) {
                // 错误
                ToastUtil.toastOnUiThread(TeamMemberInfoMoreActivity.this, "发生异常");
                loadingUtils.dismissOnUiThread();
            }
        });
    }


    private void getMember() {
        NIMClient.getService(TeamService.class).queryTeamMember(member.getTid(), aCache.getAsString("account")).setCallback(new RequestCallbackWrapper<TeamMember>() {
            @Override
            public void onResult(int code, TeamMember member, Throwable exception) {
                if (member.getType() == TeamMemberType.Manager || member.getType() == TeamMemberType.Owner) {
                    mBtnRemoveTeam.setVisibility(View.VISIBLE);
                    mRlSettingTeamBanned.setVisibility(View.VISIBLE);
                } else {
                    mBtnRemoveTeam.setVisibility(View.GONE);
                    mRlSettingTeamBanned.setVisibility(View.GONE);
                }
                if (member.getType() == TeamMemberType.Owner) {
                    mRlSettingTeamManager.setVisibility(View.VISIBLE);
                } else {
                    mRlSettingTeamManager.setVisibility(View.GONE);
                }
            }
        });
    }

    public static void updateData(HashMap map) {
        Message message = new Message();
        Bundle data = new Bundle();
        message.what = 1;
        data.putSerializable("map", map);
        message.setData(data);
        handler.sendMessage(message);
    }

    private class IHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HashMap map = (HashMap) msg.getData().getSerializable(
                    "map");
            initData(map);
            loadingUtils.dismissOnUiThread();
        }
    }

}
