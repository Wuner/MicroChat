package heath.com.microchat.team;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.entity.TeamRelationship;
import heath.com.microchat.service.ITeamService;
import heath.com.microchat.service.impl.TeamServiceImpl;
import heath.com.microchat.utils.ClearEditText;
import heath.com.microchat.utils.ThreadUtils;

public class ReqJoinTeamActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout mLlReturn;
    private ClearEditText mEtContent;
    private Button mBtnApplyJoinTeam;
    private Team team;
    private ITeamService iTeamService;
    private JSONArray memberList;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_req_join_team);
        initView();
        init();
        initListener();
    }

    private void initView() {
        mLlReturn = findViewById(R.id.ll_return);
        mEtContent = findViewById(R.id.et_content);
        mBtnApplyJoinTeam = findViewById(R.id.btn_apply_join_team);
    }

    private void init() {
        Intent intent = getIntent();
        team = (Team) intent.getSerializableExtra("team");
        iTeamService = new TeamServiceImpl();
        initManageMember();
        gson = new Gson();
    }

    private void initListener() {
        mBtnApplyJoinTeam.setOnClickListener(this);
        mLlReturn.setOnClickListener(this);
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
            }
        });
        memberList.put(team.getCreator());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.ll_return:
                finish();
                break;
            case R.id.btn_apply_join_team:
                final String postscript = mEtContent.getText().toString();
                NIMClient.getService(TeamService.class).applyJoinTeam(team.getId(), postscript).setCallback(new RequestCallback<Team>() {
                    @Override
                    public void onSuccess(final Team team) {
                        ThreadUtils.runInThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // 申请成功, 等待验证入群
                                    TeamRelationship teamRelationship = new TeamRelationship(team.getId(), memberList.toString(), aCache.getAsString("account"), postscript,
                                            "0", "apply");
                                    JSONObject parameterData = new JSONObject(gson.toJson(teamRelationship));
                                    String result = iTeamService.applyJoinTeam(parameterData);
                                    JSONObject resultObj = new JSONObject(result);
                                    Intent intent = new Intent(
                                            ReqJoinTeamActivity.this,
                                            ApplyTeamResultActivity.class);
                                    HashMap<String,Object> map = new HashMap<>();
                                    if (resultObj.getString("code").equals("200")) {
                                        map.put("result", 1);
                                    } else {
                                        map.put("result", 0);
                                    }
                                    intent.putExtra("map", map);
                                    startActivityForResult(intent, 0);
                                    finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailed(int code) {
                        if (code==808){
                            ThreadUtils.runInThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        // 申请成功, 等待验证入群
                                        TeamRelationship teamRelationship = new TeamRelationship(team.getId(), memberList.toString(), aCache.getAsString("account"), postscript,
                                                "0", "apply");
                                        JSONObject parameterData = new JSONObject(gson.toJson(teamRelationship));
                                        String result = iTeamService.applyJoinTeam(parameterData);
                                        JSONObject resultObj = new JSONObject(result);
                                        Intent intent = new Intent(
                                                ReqJoinTeamActivity.this,
                                                ApplyTeamResultActivity.class);
                                        HashMap<String,Object> map = new HashMap<>();
                                        if (resultObj.getString("code").equals("200")) {
                                            map.put("result", 1);
                                        } else {
                                            map.put("result", 0);
                                        }
                                        intent.putExtra("map", map);
                                        startActivityForResult(intent, 0);
                                        finish();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }else {
                            // error
                            Intent intent = new Intent(
                                    ReqJoinTeamActivity.this,
                                    ApplyTeamResultActivity.class);
                            HashMap<String,Object> map = new HashMap<>();
                            map.put("result", 2);
                            intent.putExtra("map", map);
                            startActivityForResult(intent, 0);
                            finish();
                        }
                    }

                    @Override
                    public void onException(Throwable exception) {
                        // error
                        Intent intent = new Intent(
                                ReqJoinTeamActivity.this,
                                ApplyTeamResultActivity.class);
                        HashMap<String,Object> map = new HashMap<>();
                        map.put("result", 0);
                        intent.putExtra("map", map);
                        startActivityForResult(intent, 0);
                        finish();
                    }
                });
                break;
        }
    }
}
