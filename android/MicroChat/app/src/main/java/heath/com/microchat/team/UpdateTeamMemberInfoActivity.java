package heath.com.microchat.team;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import org.json.JSONObject;

import java.util.HashMap;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.service.ITeamService;
import heath.com.microchat.service.impl.TeamServiceImpl;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ToastUtil;

public class UpdateTeamMemberInfoActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout mLlReturn;
    private TextView mTvSave;
    private TextView mTvUpdateName;
    private EditText mEtUpdateContent;
    private String updateInfo;
    private HashMap<String,Object> map;
    private TeamMember teamMember;
    private Team team;
    private String orgContent = "";
    private LoadingUtils loadingUtils;
    private ITeamService teamServiceImpl;
    private String tid;
    private String account;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        Intent intent = getIntent();
        map = (HashMap<String, Object>) intent.getSerializableExtra("map");
        updateInfo = (String) map.get("updateInfo");
        if ("TeamMemberInfoMoreActivity".equals(map.get("from")) || "TeamInfoActivity".equals(map.get("from"))){
            teamMember = (TeamMember) map.get("member");
            tid = teamMember.getTid();
            account = teamMember.getAccount();
            text = teamMember.getTeamNick();
        }
        if ("EditTeamInfoActivity".equals(map.get("from"))){
            team = (Team) map.get("team");
            tid = team.getId();
            text = team.getName();
        }
        mLlReturn = this.findViewById(R.id.ll_return);
        mTvSave = this.findViewById(R.id.tv_save);
        mTvUpdateName = this.findViewById(R.id.tv_update_name);
        mEtUpdateContent = this.findViewById(R.id.et_update_content);
        loadingUtils = new LoadingUtils(UpdateTeamMemberInfoActivity.this, "正在修改资料");
        teamServiceImpl = new TeamServiceImpl();
    }

    private void initData() {
        loadingUtils.creat();
        String updateName = "更改" + updateInfo;
        mTvUpdateName.setText(updateName);
        if (updateInfo.equals(getResources().getString(R.string.tv_team_member_name))) {
            if (text != null) {
                mEtUpdateContent.setText(text);
                mEtUpdateContent.setSelection(text.length());
                orgContent = text;
            }
        }

        if (updateInfo.equals(getResources().getString(R.string.tv_team_name))) {
            if (text != null) {
                mEtUpdateContent.setText(text);
                mEtUpdateContent.setSelection(text.length());
                orgContent = text;
            }
        }
    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
        mTvSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_return:
                finish();
                break;

            case R.id.tv_save:
                loadingUtils.show();
                Common.hideSoftKeyboard(UpdateTeamMemberInfoActivity.this);
                String content = mEtUpdateContent.getText().toString();
                if ("TeamMemberInfoMoreActivity".equals(map.get("from")) || "TeamInfoActivity".equals(map.get("from"))){
                    NIMClient.getService(TeamService.class).updateMemberNick(tid, account, content).setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            // 成功
                            modify();
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
                }else if ("EditTeamInfoActivity".equals(map.get("from"))){
                    modifyTeam(content);
                }
                break;

            default:
                break;

        }
    }

    private void modify(){
        com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("account", account);
                    parameterData.put("tid", tid);
                    String content = mEtUpdateContent.getText().toString();
                    if (content.equals(orgContent)) {
                        ToastUtil.toastOnUiThread(UpdateTeamMemberInfoActivity.this, "未修改");
                        loadingUtils.dismissOnUiThread();
                        return;
                    }
                    if (updateInfo.equals(getResources().getString(R.string.tv_team_member_name))) {
                        if (content.length() > 10) {
                            ToastUtil.toastOnUiThread(UpdateTeamMemberInfoActivity.this, "群昵称长度不能超过10个字");
                            loadingUtils.dismissOnUiThread();
                            return;
                        }
                        parameterData.put("teamNick", content);
                    }
                    String result = teamServiceImpl.modifyTeamMember(parameterData);
                    Log.e("TAG", "run: " + result + "0---------------------------");
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        if (updateInfo.equals(getResources().getString(R.string.tv_team_member_name))) {
                            if ("TeamMemberInfoMoreActivity".equals(map.get("from"))){
                                getTeamMemberInfo();
                            }else if ("TeamInfoActivity".equals(map.get("from"))){
                                TeamInfoActivity.updateData();
                                finish();
                                loadingUtils.dismissOnUiThread();
                            }
                        }
                    }else{
                        loadingUtils.dismissOnUiThread();
                        ToastUtil.toastOnUiThread(UpdateTeamMemberInfoActivity.this,resultObj.get("msg").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    loadingUtils.dismissOnUiThread();
                    ToastUtil.toastOnUiThread(UpdateTeamMemberInfoActivity.this,"发生异常");
                }
            }
        });
    }

    private void getTeamMemberInfo(){
        NIMClient.getService(TeamService.class).queryTeamMember(tid, account).setCallback(new RequestCallbackWrapper<TeamMember>() {
            @Override
            public void onResult(int code, TeamMember teamMember, Throwable exception) {
                map.put("member",teamMember);
                TeamMemberInfoMoreActivity.updateData(map);
                finish();
                loadingUtils.dismissOnUiThread();
            }
        });
    }

    private void modifyTeam(final String text){
        com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("tid", team.getId());
                    parameterData.put("owner", team.getCreator());
                    if (updateInfo.equals(getResources().getString(R.string.tv_team_name))){
                        parameterData.put("tname", text);
                    }else if(updateInfo.equals(getResources().getString(R.string.tv_team_introduce))){
                        parameterData.put("intro", text);
                    }
                    String result = teamServiceImpl.modifyTeamByTid(parameterData);
                    Log.e("TAG", "run: " + result + "0---------------------------");
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        EditTeamInfoActivity.getTeamInfo();
                        finish();
                        loadingUtils.dismissOnUiThread();
                    }else{
                        ToastUtil.toastOnUiThread(UpdateTeamMemberInfoActivity.this,resultObj.get("msg").toString());
                        loadingUtils.dismissOnUiThread();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    loadingUtils.dismissOnUiThread();
                }
            }
        });
    }

}
