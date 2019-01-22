package heath.com.microchat.team;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import org.json.JSONArray;

import java.util.List;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.entity.TeamBean;
import heath.com.microchat.message.SendTeamMessageActivity;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;
import heath.com.microchat.utils.LoadingUtils;

public class ApplyTeamInfoActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout mLlReturn;
    private ImageView mIvTeamIcon;
    private TextView mTvTeamName;
    private TextView mTvTeamId;
    private TextView mTvCreateTime;
    private TextView mTvIntroduce;
    private TextView mTvTeamNumber;
    private TextView mTvManagerNumber;
    private RelativeLayout mRlManager;
    private Button mBtnApply;
    private Button mBtnSendMessage;
    private Team team;
    private TeamBean teamBean;
    private JSONArray memberList;
    private LoadingUtils loadingUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_team_info);
        initView();
        initListener();
        init();
    }

    private void initView() {
        mLlReturn = findViewById(R.id.ll_return);
        mIvTeamIcon = findViewById(R.id.iv_team_icon);
        mTvTeamName = findViewById(R.id.tv_team_name);
        mTvTeamId = findViewById(R.id.tv_team_id);
        mTvCreateTime = findViewById(R.id.tv_create_time);
        mTvIntroduce = findViewById(R.id.tv_introduce);
        mTvTeamNumber = findViewById(R.id.tv_team_number);
        mTvManagerNumber = findViewById(R.id.tv_manager_number);
        mRlManager = findViewById(R.id.rl_manager);
        mBtnApply = findViewById(R.id.btn_apply);
        mBtnSendMessage = findViewById(R.id.btn_send_message);
    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
        mRlManager.setOnClickListener(this);
        mBtnApply.setOnClickListener(this);
        mBtnSendMessage.setOnClickListener(this);
    }

    private void init() {
        loadingUtils = new LoadingUtils(ApplyTeamInfoActivity.this, "正在加载中");
        loadingUtils.creat();
        loadingUtils.show();
        Intent intent = getIntent();
        teamBean = (TeamBean) intent.getSerializableExtra("teamBean");
        NIMClient.getService(TeamService.class).searchTeam(teamBean.getTid()).setCallback(new RequestCallback<Team>() {
            @Override
            public void onSuccess(Team team1) {
                // 查询成功，获得群组资料
                team = team1;
                initManageMember();
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

    private void initData() {
        loadingUtils.dismiss();
        ImageUitl imageUitl = new ImageUitl(BaseActivity.cache);
        imageUitl.asyncloadImage(mIvTeamIcon, Common.HTTP_ADDRESS + Common.TEAM_FOLDER_PATH + "/" + team.getIcon());
        mTvTeamName.setText(team.getName());
        mTvTeamId.setText(team.getId());
        String text = "本群创建于" + Common.conversionTimeDate(team.getCreateTime());
        mTvCreateTime.setText(text);
        mTvIntroduce.setText(team.getIntroduce());
        String memberNum = team.getMemberCount() + "";
        mTvTeamNumber.setText(memberNum);
        String num = memberList.length() + 1 + "";
        mTvManagerNumber.setText(num);
        if (team.isMyTeam()) {
            mBtnSendMessage.setVisibility(View.VISIBLE);
            mBtnApply.setVisibility(View.GONE);
        } else {
            mBtnSendMessage.setVisibility(View.GONE);
            mBtnApply.setVisibility(View.VISIBLE);
        }
    }

    private void initManageMember() {
        memberList = new JSONArray();
        NIMClient.getService(TeamService.class).queryMemberList(teamBean.getTid()).setCallback(new RequestCallbackWrapper<List<TeamMember>>() {
            @Override
            public void onResult(int code, final List<TeamMember> members, Throwable exception) {
                for (TeamMember member : members) {
                    if (member.getType() == TeamMemberType.Manager) {
                        memberList.put(member.getAccount());
                    }
                }
                initData();
            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            default:
                break;
            case R.id.ll_return:
                finish();
                break;
            case R.id.rl_manager:
                break;
            case R.id.btn_apply:
                intent = new Intent(
                        ApplyTeamInfoActivity.this,
                        ReqJoinTeamActivity.class);
                intent.putExtra("team", team);
                startActivityForResult(intent, 0);
                break;
            case R.id.btn_send_message:
                intent = new Intent(
                        ApplyTeamInfoActivity.this,
                        SendTeamMessageActivity.class);
                intent.putExtra("team", team);
                startActivityForResult(intent, 0);
                break;
        }
    }
}
