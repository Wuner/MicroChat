package heath.com.microchat.team;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamAllMuteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamBeInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamUpdateModeEnum;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.message.SendTeamMessageActivity;
import heath.com.microchat.service.ITeamService;
import heath.com.microchat.service.impl.TeamServiceImpl;
import heath.com.microchat.utils.BottomMenu;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ThreadUtils;
import heath.com.microchat.utils.ToastUtil;

public class TeamManageActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout mLlReturn;
    private RelativeLayout mRlEdtTeamInfo;
    private RelativeLayout mRlUpgradeTeam;
    private RelativeLayout mRlAdministrators;
    private TextView mTvMute;
    private RelativeLayout mRlMute;
    private TextView mTvJoinMode;
    private RelativeLayout mRlJoinMode;
    private TextView mTvBeInviteMode;
    private RelativeLayout mRlBeInviteMode;
    private TextView mTvInviteMode;
    private RelativeLayout mRlInviteMode;
    private TextView mTvUpdateInfoMode;
    private RelativeLayout mRlUpdateInfoMode;
    private Team team;
    private BottomMenu menuWindow;
    private ITeamService teamServiceImpl;
    private LoadingUtils loadingUtils;
    private int JOIN_MODE = 0;
    private int BE_INVITE_MODE = 1;
    private int INVITE_MODE = 2;
    private int UPDATE_INFO_MODE = 3;
    private static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_manage);
        initView();
        initListener();
        initData();
    }

    private void initView() {
        Intent intent = getIntent();
        team = (Team) intent.getSerializableExtra("team");
        mLlReturn = findViewById(R.id.ll_return);
        mRlEdtTeamInfo = findViewById(R.id.rl_edt_team_info);
        mRlUpgradeTeam = findViewById(R.id.rl_upgrade_team);
        mRlAdministrators = findViewById(R.id.rl_administrators);
        mTvMute = findViewById(R.id.tv_mute);
        mRlMute = findViewById(R.id.rl_mute);
        mTvJoinMode = findViewById(R.id.tv_join_mode);
        mRlJoinMode = findViewById(R.id.rl_join_mode);
        mTvBeInviteMode = findViewById(R.id.tv_be_invite_mode);
        mRlBeInviteMode = findViewById(R.id.rl_be_invite_mode);
        mTvInviteMode = findViewById(R.id.tv_invite_mode);
        mRlInviteMode = findViewById(R.id.rl_invite_mode);
        mTvUpdateInfoMode = findViewById(R.id.tv_update_info_mode);
        mRlUpdateInfoMode = findViewById(R.id.rl_update_info_mode);
        teamServiceImpl = new TeamServiceImpl();
        loadingUtils = new LoadingUtils(TeamManageActivity.this, "正在修改资料中");
        loadingUtils.creat();
        handler = new IHandler();
    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
        mRlEdtTeamInfo.setOnClickListener(this);
        mRlUpgradeTeam.setOnClickListener(this);
        mRlAdministrators.setOnClickListener(this);
        mRlMute.setOnClickListener(this);
        mRlJoinMode.setOnClickListener(this);
        mRlBeInviteMode.setOnClickListener(this);
        mRlInviteMode.setOnClickListener(this);
        mRlUpdateInfoMode.setOnClickListener(this);
    }

    private void initData() {
        if (team.getVerifyType() == VerifyTypeEnum.Private) {
            mTvJoinMode.setText(getResources().getString(R.string.tv_private));
        } else if (team.getVerifyType() == VerifyTypeEnum.Apply) {
            mTvJoinMode.setText(getResources().getString(R.string.tv_need_apply));
        } else if (team.getVerifyType() == VerifyTypeEnum.Free) {
            mTvJoinMode.setText(getResources().getString(R.string.tv_free));
        }
        if (team.getTeamBeInviteMode() == TeamBeInviteModeEnum.NeedAuth) {
            mTvBeInviteMode.setText(getResources().getString(R.string.tv_need_auth));
        } else if (team.getTeamBeInviteMode() == TeamBeInviteModeEnum.NoAuth) {
            mTvBeInviteMode.setText(getResources().getString(R.string.tv_no_auth));
        }
        if (team.getTeamInviteMode() == TeamInviteModeEnum.Manager) {
            mTvInviteMode.setText(getResources().getString(R.string.tv_manager));
        } else if (team.getTeamInviteMode() == TeamInviteModeEnum.All) {
            mTvInviteMode.setText(getResources().getString(R.string.tv_all));
        }
        if (team.getTeamUpdateMode() == TeamUpdateModeEnum.Manager) {
            mTvUpdateInfoMode.setText(getResources().getString(R.string.tv_manager));
        } else if (team.getTeamUpdateMode() == TeamUpdateModeEnum.All) {
            mTvUpdateInfoMode.setText(getResources().getString(R.string.tv_all));
        }
        if (team.getMuteMode() == TeamAllMuteModeEnum.MuteNormal) {
            mTvMute.setText(getResources().getString(R.string.tv_mute_normal));
        } else if (team.getMuteMode() == TeamAllMuteModeEnum.MuteALL) {
            mTvMute.setText(getResources().getString(R.string.tv_mute_all));
        } else {
            mTvMute.setText(getResources().getString(R.string.tv_null));
        }
    }

    @Override
    public void onClick(View v) {
        String[] texts;
        int[] ids;
        int[] index;
        List<Map<String, Object>> list;
        Intent intent;
        switch (v.getId()) {
            case R.id.ll_return:
                finish();
                break;
            case R.id.rl_edt_team_info:
                intent = new Intent(
                        TeamManageActivity.this,
                        EditTeamInfoActivity.class);
                intent.putExtra("team", team);
                startActivityForResult(intent, 0);
                break;
            case R.id.rl_upgrade_team:
                break;
            case R.id.rl_administrators:
                intent = new Intent(
                        TeamManageActivity.this,
                        ManageMemberActivity.class);
                intent.putExtra("team", team);
                startActivityForResult(intent, 0);
                break;
            case R.id.rl_mute:
                texts = new String[]{getResources().getString(R.string.tv_null), getResources().getString(R.string.tv_mute_normal), getResources().getString(R.string.tv_mute_all)};
                ids = new int[]{R.id.btn1, R.id.btn2, R.id.btn3};
                index = new int[]{0, 1, 2};
                list = Common.setBtn(texts, ids, index);
                menuWindow = new BottomMenu(TeamManageActivity.this, MClickListener, list);
                menuWindow.show();
                break;
            case R.id.rl_join_mode:
                texts = new String[]{getResources().getString(R.string.tv_need_apply), getResources().getString(R.string.tv_free), getResources().getString(R.string.tv_private)};
                ids = new int[]{R.id.btn1, R.id.btn2, R.id.btn3};
                index = new int[]{0, 1, 2};
                list = Common.setBtn(texts, ids, index);
                menuWindow = new BottomMenu(TeamManageActivity.this, JMClickListener, list);
                menuWindow.show();
                break;
            case R.id.rl_be_invite_mode:
                texts = new String[]{getResources().getString(R.string.tv_need_auth), getResources().getString(R.string.tv_no_auth)};
                ids = new int[]{R.id.btn1, R.id.btn2};
                index = new int[]{0, 1};
                list = Common.setBtn(texts, ids, index);
                menuWindow = new BottomMenu(TeamManageActivity.this, BIMClickListener, list);
                menuWindow.show();
                break;
            case R.id.rl_invite_mode:
                texts = new String[]{getResources().getString(R.string.tv_manager), getResources().getString(R.string.tv_all)};
                ids = new int[]{R.id.btn1, R.id.btn2};
                index = new int[]{0, 1};
                list = Common.setBtn(texts, ids, index);
                menuWindow = new BottomMenu(TeamManageActivity.this, IMClickListener, list);
                menuWindow.show();
                break;
            case R.id.rl_update_info_mode:
                texts = new String[]{getResources().getString(R.string.tv_manager), getResources().getString(R.string.tv_all)};
                ids = new int[]{R.id.btn1, R.id.btn2};
                index = new int[]{0, 1};
                list = Common.setBtn(texts, ids, index);
                menuWindow = new BottomMenu(TeamManageActivity.this, UIMClickListener, list);
                menuWindow.show();
                break;
        }
    }

    private View.OnClickListener JMClickListener = new View.OnClickListener() {

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn1:
                    loadingUtils.show();
                    modify(JOIN_MODE, "1");
                    break;
                case R.id.btn2:
                    loadingUtils.show();
                    modify(JOIN_MODE, "0");
                    break;
                case R.id.btn3:
                    loadingUtils.show();
                    modify(JOIN_MODE, "2");
                    break;
                default:
                    break;
            }
        }
    };
    private View.OnClickListener MClickListener = new View.OnClickListener() {

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn1:
                    loadingUtils.show();
                    mute(0);
                    break;
                case R.id.btn2:
                    loadingUtils.show();
                    mute(1);
                    break;
                case R.id.btn3:
                    loadingUtils.show();
                    mute(3);
                    break;
                default:
                    break;
            }
        }
    };
    private View.OnClickListener BIMClickListener = new View.OnClickListener() {

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn1:
                    loadingUtils.show();
                    modify(BE_INVITE_MODE, "0");
                    break;
                case R.id.btn2:
                    loadingUtils.show();
                    modify(BE_INVITE_MODE, "1");
                    break;
                default:
                    break;
            }
        }
    };
    private View.OnClickListener IMClickListener = new View.OnClickListener() {

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn1:
                    loadingUtils.show();
                    modify(INVITE_MODE, "0");
                    break;
                case R.id.btn2:
                    loadingUtils.show();
                    modify(INVITE_MODE, "1");
                    break;
                default:
                    break;
            }
        }
    };
    private View.OnClickListener UIMClickListener = new View.OnClickListener() {

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn1:
                    loadingUtils.show();
                    modify(UPDATE_INFO_MODE, "0");
                    break;
                case R.id.btn2:
                    loadingUtils.show();
                    modify(UPDATE_INFO_MODE, "1");
                    break;
                default:
                    break;
            }
        }
    };

    private void mute(final int muteType) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject parameterData = new JSONObject();
                    parameterData.put("owner", team.getCreator());
                    parameterData.put("tid", team.getId());
                    parameterData.put("muteType", muteType);
                    String result = teamServiceImpl.mute(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        getTeamInfo();
                    } else {
                        ToastUtil.toastOnUiThread(TeamManageActivity.this, resultObj.get("msg").toString());
                        loadingUtils.dismissOnUiThread();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    loadingUtils.dismissOnUiThread();
                }
            }
        });
    }

    private void modify(final int type, final String select) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject parameterData = new JSONObject();
                    parameterData.put("owner", team.getCreator());
                    parameterData.put("tid", team.getId());
                    if (type == JOIN_MODE) {
                        parameterData.put("joinmode", select);
                    } else if (type == BE_INVITE_MODE) {
                        parameterData.put("beinvitemode", select);
                    } else if (type == INVITE_MODE) {
                        parameterData.put("invitemode", select);
                    } else if (type == UPDATE_INFO_MODE) {
                        parameterData.put("uptinfomode", select);
                    }
                    String result = teamServiceImpl.modifyTeamByTid(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        getTeamInfo();
                    } else {
                        ToastUtil.toastOnUiThread(TeamManageActivity.this, resultObj.get("msg").toString());
                        loadingUtils.dismissOnUiThread();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    loadingUtils.dismissOnUiThread();
                }
            }
        });
    }

    private void getTeamInfo() {
        NIMClient.getService(TeamService.class).searchTeam(team.getId()).setCallback(new RequestCallback<Team>() {
            @Override
            public void onSuccess(Team t) {
                // 查询成功，获得群组资料
                team = t;
                updateData();
                TeamInfoActivity.team = t;
                SendTeamMessageActivity.team = t;
                SendTeamMessageActivity.updateData();
                TeamActivity.updateData();
            }

            @Override
            public void onFailed(int code) {
                // 失败
            }

            @Override
            public void onException(Throwable exception) {
                // 错误
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
            initData();
            loadingUtils.dismissOnUiThread();
        }
    }
}
