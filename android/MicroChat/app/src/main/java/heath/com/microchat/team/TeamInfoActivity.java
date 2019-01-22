package heath.com.microchat.team;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flyco.animation.BaseAnimatorSet;
import com.flyco.animation.BounceEnter.BounceTopEnter;
import com.flyco.animation.SlideExit.SlideBottomExit;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
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
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.adapter.MemberAdapter;
import heath.com.microchat.entity.UserInfo;
import heath.com.microchat.message.SendTeamMessageActivity;
import heath.com.microchat.service.ITeamService;
import heath.com.microchat.service.IUserService;
import heath.com.microchat.service.impl.TeamServiceImpl;
import heath.com.microchat.service.impl.UserServiceImpl;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ToastUtil;
import heath.com.microchat.utils.UploadServerUtils;

public class TeamInfoActivity extends BaseActivity implements View.OnClickListener {

    public static Team team;
    private ImageView mIvTeamIcon;
    private TextView mTvTeamName;
    private TextView mTvTeamId;
    private GridView mGvMembers;
    private LinearLayout mLlReturn;
    private MemberAdapter memberAdapter;
    private IUserService userServiceImpl;
    private Gson gson;
    private RelativeLayout mRlMyTeamName;
    private TextView mTvMyTeamName;
    private RelativeLayout mRlMyTeamManage;
    private Button mBtnRemoveTeam;
    private TeamMember member;
    private LoadingUtils loadingUtils;
    private int IMAGE = 1;
    private ITeamService teamServiceImpl;
    private static Handler handler;
    private BaseAnimatorSet mBasIn;
    private BaseAnimatorSet mBasOut;
    private Button mBtnDissolutionTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_info);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        Intent intent = getIntent();
        team = (Team) intent.getSerializableExtra("team");
        mIvTeamIcon = this.findViewById(R.id.iv_team_icon);
        mTvTeamName = this.findViewById(R.id.tv_team_name);
        mTvTeamId = this.findViewById(R.id.tv_team_id);
        mGvMembers = this.findViewById(R.id.gv_members);
        mLlReturn = this.findViewById(R.id.ll_return);
        mRlMyTeamManage = this.findViewById(R.id.rl_my_team_manage);
        userServiceImpl = new UserServiceImpl();
        gson = new Gson();
        mRlMyTeamName = this.findViewById(R.id.rl_my_team_name);
        mTvMyTeamName = this.findViewById(R.id.tv_my_team_name);
        teamServiceImpl = new TeamServiceImpl();
        handler = new IHandler();
        loadingUtils = new LoadingUtils(TeamInfoActivity.this, "努力加载资料中");
        loadingUtils.creat();
        loadingUtils.show();
        mBtnRemoveTeam = findViewById(R.id.btn_remove_team);
        mBasIn = new BounceTopEnter();
        mBasOut = new SlideBottomExit();
        mBtnDissolutionTeam = findViewById(R.id.btn_dissolution_team);
    }

    private void initData() {
        getMember();
        ImageUitl imageUitl = new ImageUitl(cache);
        imageUitl.asyncloadImage(mIvTeamIcon, Common.HTTP_ADDRESS + Common.TEAM_FOLDER_PATH + "/" + team.getIcon());
        mTvTeamName.setText(team.getName());
        mTvTeamId.setText(team.getId());
        NIMClient.getService(TeamService.class).queryTeamMember(team.getId(), aCache.getAsString("account")).setCallback(new RequestCallbackWrapper<TeamMember>() {
            @Override
            public void onResult(int code, TeamMember teamMember, Throwable exception) {
                mTvMyTeamName.setText(teamMember.getTeamNick());
                member = teamMember;
            }
        });
        NIMClient.getService(TeamService.class).queryMemberList(team.getId()).setCallback(new RequestCallbackWrapper<List<TeamMember>>() {
            @Override
            public void onResult(int code, final List<TeamMember> members, Throwable exception) {
                if (code == 200) {
                    final JSONArray memberList = new JSONArray();
                    final List<String> memberList1 = new ArrayList<>();
                    for (TeamMember member : members) {
                        if (member.isInTeam()) {
                            Log.e("组员id", "run: " + member.getAccount());
                            memberList.put(member.getAccount());
                            memberList1.add(member.getAccount());
                        }
                    }
                    ThreadUtils.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("组员数量", "run: " + memberList.length());
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
                                            memberAdapter = new MemberAdapter(TeamInfoActivity.this, userInfos, cache);
                                            mGvMembers.setAdapter(memberAdapter);
                                            mGvMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    if (position == userInfos.size()) {
                                                        Intent intent = new Intent(
                                                                TeamInfoActivity.this,
                                                                InvitationActivity.class);
                                                        Map<String, Object> map = new HashMap<>();
                                                        map.put("members", memberList1);
                                                        map.put("team", team);
                                                        intent.putExtra("map", (Serializable) map);
                                                        startActivityForResult(intent, 0);
                                                    } else {
                                                        Map<String, Object> map = new HashMap<>();
                                                        map.put("userinfo", userInfos.get(position));
                                                        map.put("member", members.get(position));
                                                        map.put("team", team);
                                                        Intent intent = new Intent(
                                                                TeamInfoActivity.this,
                                                                TeamMemberInfoActivity.class);
                                                        intent.putExtra("map", (Serializable) map);
                                                        startActivityForResult(intent, 0);
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    loadingUtils.dismissOnUiThread();
                                    ToastUtil.toastOnUiThread(TeamInfoActivity.this, resultObj.get("msg").toString());
                                }
                            } catch (Exception e) {
                                loadingUtils.dismissOnUiThread();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
        mRlMyTeamName.setOnClickListener(this);
        mIvTeamIcon.setOnClickListener(this);
        mRlMyTeamManage.setOnClickListener(this);
        mBtnRemoveTeam.setOnClickListener(this);
        mBtnDissolutionTeam.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.ll_return:
                finish();
                break;
            case R.id.rl_my_team_name:
                intent = new Intent(
                        TeamInfoActivity.this,
                        UpdateTeamMemberInfoActivity.class);
                Map<String, Object> map = new HashMap<>();
                map.put("updateInfo", getResources().getString(R.string.tv_team_member_name));
                map.put("team", team);
                map.put("member", member);
                map.put("from", "TeamInfoActivity");
                intent.putExtra("map", (Serializable) map);
                startActivityForResult(intent, 0);
                break;
            case R.id.iv_team_icon:
                //调用相册
                intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE);
                break;
            case R.id.rl_my_team_manage:
                intent = new Intent(
                        TeamInfoActivity.this,
                        TeamManageActivity.class);
                intent.putExtra("team", team);
                startActivityForResult(intent, 0);
                break;
            case R.id.btn_remove_team:
                loadingUtils.show();
                NormalDialogStyleTwo();
                break;
            case R.id.btn_dissolution_team:
                loadingUtils.show();
                NormalDialogStyleTwo1();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            loadingUtils.show();
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            final String imagePath = c.getString(columnIndex);
            ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    final String resultPath = UploadServerUtils.uploadLogFile(Common.HTTP_ADDRESS + "upload/fileUpload.action", imagePath, Common.TEAM_FOLDER_PATH);
                    if (resultPath.equals("error")) {
                        ToastUtil.toastOnUiThread(TeamInfoActivity.this, "上传失败，请重新上传");
                        return;
                    }
                    JSONObject parameterData = new JSONObject();
                    try {
                        parameterData.put("tid", team.getId());
                        parameterData.put("owner", team.getCreator());
                        parameterData.put("icon", resultPath);
                        String result = teamServiceImpl.modifyTeamByTid(parameterData);
                        Log.e("TAG", "run: " + result + "0---------------------------");
                        JSONObject resultObj = new JSONObject(result);
                        if (resultObj.getString("code").equals("200")) {
                            getTeamInfo();
                        } else {
                            ToastUtil.toastOnUiThread(TeamInfoActivity.this, resultObj.get("msg").toString());
                            loadingUtils.dismissOnUiThread();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        loadingUtils.dismissOnUiThread();
                    }
                }
            });
            c.close();
        }
    }

    public static void getTeamInfo() {
        NIMClient.getService(TeamService.class).searchTeam(team.getId()).setCallback(new RequestCallback<Team>() {
            @Override
            public void onSuccess(Team t) {
                // 查询成功，获得群组资料
                team = t;
                updateData();
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

    private void getMember() {
        NIMClient.getService(TeamService.class).queryTeamMember(team.getId(), aCache.getAsString("account")).setCallback(new RequestCallbackWrapper<TeamMember>() {
            @Override
            public void onResult(int code, TeamMember member, Throwable exception) {
                if (member.getType() == TeamMemberType.Owner) {
                    mRlMyTeamManage.setVisibility(View.VISIBLE);
                    mBtnRemoveTeam.setVisibility(View.GONE);
                    mBtnDissolutionTeam.setVisibility(View.VISIBLE);
                } else {
                    mBtnRemoveTeam.setVisibility(View.VISIBLE);
                    mRlMyTeamManage.setVisibility(View.GONE);
                    mBtnDissolutionTeam.setVisibility(View.GONE);
                }
            }
        });
    }

    private void NormalDialogStyleTwo() {
        final NormalDialog dialog = new NormalDialog(this);
        dialog.content("是否退出本群")//
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
                        NIMClient.getService(TeamService.class).quitTeam(team.getId()).setCallback(new RequestCallback<Void>() {
                            @Override
                            public void onSuccess(Void param) {
                                // 退群成功
                                ThreadUtils.runInThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        JSONObject parameterData = new JSONObject();
                                        try {
                                            parameterData.put("tid", team.getId());
                                            parameterData.put("account", aCache.getAsString("account"));
                                            String result = teamServiceImpl.removeMember(parameterData);
                                            teamServiceImpl.delTeamMemberByTidAndAccount(parameterData);
                                            JSONObject resultObj = new JSONObject(result);
                                            if (resultObj.getString("code").equals("200")) {
                                                loadingUtils.dismissOnUiThread();
                                                TeamActivity.updateData();
                                                finish();
                                                SendTeamMessageActivity.context.finish();
                                            } else {
                                                loadingUtils.dismissOnUiThread();
                                                ToastUtil.toastOnUiThread(TeamInfoActivity.this, resultObj.get("msg").toString());
                                            }
                                        } catch (Exception e) {
                                            loadingUtils.dismissOnUiThread();
                                            e.printStackTrace();
                                            ToastUtil.toastOnUiThread(TeamInfoActivity.this, "发生异常");
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onFailed(int code) {
                                // 退群失败
                                loadingUtils.dismissOnUiThread();
                                ToastUtil.toastOnUiThread(TeamInfoActivity.this, "发生异常");
                            }

                            @Override
                            public void onException(Throwable exception) {
                                // 错误
                                loadingUtils.dismissOnUiThread();
                                ToastUtil.toastOnUiThread(TeamInfoActivity.this, "发生异常");
                            }
                        });
                    }
                });
    }

    private void NormalDialogStyleTwo1() {
        final NormalDialog dialog = new NormalDialog(this);
        dialog.content("是否解散本群")//
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
                        ThreadUtils.runInThread(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject parameterData = new JSONObject();
                                try {
                                    parameterData.put("tid", team.getId());
                                    parameterData.put("owner", aCache.getAsString("account"));
                                    String result = teamServiceImpl.dissolution(parameterData);
                                    JSONObject resultObj = new JSONObject(result);
                                    if (resultObj.getString("code").equals("200")) {
                                        loadingUtils.dismissOnUiThread();
                                        TeamActivity.updateData();
                                        finish();
                                        SendTeamMessageActivity.context.finish();
                                    } else {
                                        loadingUtils.dismissOnUiThread();
                                        ToastUtil.toastOnUiThread(TeamInfoActivity.this, resultObj.get("msg").toString());
                                    }
                                } catch (Exception e) {
                                    loadingUtils.dismissOnUiThread();
                                    e.printStackTrace();
                                    ToastUtil.toastOnUiThread(TeamInfoActivity.this, "发生异常");
                                }
                            }
                        });
                    }
                });
    }
}
