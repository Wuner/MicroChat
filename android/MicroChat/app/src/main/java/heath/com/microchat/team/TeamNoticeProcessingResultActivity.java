package heath.com.microchat.team;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.team.model.Team;

import org.json.JSONException;
import org.json.JSONObject;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.TabHostActivity;
import heath.com.microchat.entity.TeamRelationship;
import heath.com.microchat.friend.FriendsFragment;
import heath.com.microchat.service.ITeamService;
import heath.com.microchat.service.impl.TeamServiceImpl;

public class TeamNoticeProcessingResultActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout mLlReturn;
    private ImageView mIvResult;
    private TextView mTvResult;
    private TeamRelationship teamRelationship;
    private ITeamService iTeamService;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_notice_processing_result);
        initView();
        initListener();
        init();
        initData();
    }

    private void initView() {
        mLlReturn = findViewById(R.id.ll_return);
        mIvResult =  findViewById(R.id.iv_result);
        mTvResult = findViewById(R.id.tv_result);
    }

    private void initListener(){
        mLlReturn.setOnClickListener(this);
    }

    private void init(){
        Intent intent = getIntent();
        teamRelationship = (TeamRelationship) intent.getSerializableExtra("teamRelationship");
        iTeamService = new TeamServiceImpl();
        gson = new Gson();
    }

    private void initData(){
        if (teamRelationship.getState().equals("1")){
            mIvResult.setImageDrawable(getResources().getDrawable(R.drawable.ok));
            mTvResult.setText(getResources().getString(R.string.tv_agree2));
        }else{
            mIvResult.setImageDrawable(getResources().getDrawable(R.drawable.error));
            mTvResult.setText(getResources().getString(R.string.tv_result_error2));
        }
        if (teamRelationship.getReadState().equals("0")){
            com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    TeamRelationship teamRelationship1 = new TeamRelationship();
                    teamRelationship1.setReadState("1");
                    teamRelationship1.setId(teamRelationship.getId());
                    try {
                        JSONObject parameterData = new JSONObject(gson.toJson(teamRelationship1));
                        String result = iTeamService.modifyTeamRelationshipById(parameterData);
                        JSONObject resultObj = new JSONObject(result);
                        if (resultObj.getString("code").equals("200")){
                            TeamNoticeActivity.updateData();
                            FriendsFragment.queryTeamNoticeNums(aCache.getAsString("account"));
                            TabHostActivity.queryReqAddNums();
                            TeamActivity.updateData();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.ll_return:
                break;
        }
    }
}
