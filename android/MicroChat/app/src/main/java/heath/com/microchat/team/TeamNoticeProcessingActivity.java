package heath.com.microchat.team;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;

import org.json.JSONObject;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.TabHostActivity;
import heath.com.microchat.entity.TeamRelationship;
import heath.com.microchat.friend.FriendsFragment;
import heath.com.microchat.service.ITeamService;
import heath.com.microchat.service.impl.TeamServiceImpl;
import heath.com.microchat.utils.ClearEditText;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ThreadUtils;

public class TeamNoticeProcessingActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout mLlReturn;
    private ImageView mIvIcon;
    private TextView mTvNickname;
    private TextView mTvTeamName;
    private Button mBtnRefuse;
    private Button mBtnAgree;
    private LoadingUtils loadingUtils;
    private TeamRelationship teamRelationship;
    private ITeamService iTeamService;
    private Gson gson;
    private TextView mTvPostscript;
    private ClearEditText mEtContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_notice_processing);
        initView();
        initListener();
        init();
        initData();
    }

    private void initView() {
        mLlReturn = findViewById(R.id.ll_return);
        mIvIcon = findViewById(R.id.iv_icon);
        mTvNickname = findViewById(R.id.tv_nickname);
        mTvTeamName = findViewById(R.id.tv_team_name);
        mBtnRefuse = findViewById(R.id.btn_refuse);
        mBtnAgree = findViewById(R.id.btn_agree);
        mTvPostscript = findViewById(R.id.tv_postscript);
        mEtContent = findViewById(R.id.et_content);
    }

    private void initListener() {
        mBtnAgree.setOnClickListener(this);
        mLlReturn.setOnClickListener(this);
        mBtnRefuse.setOnClickListener(this);
    }

    private void init() {
        loadingUtils = new LoadingUtils(TeamNoticeProcessingActivity.this, "努力加载中");
        loadingUtils.creat();
        Intent intent = getIntent();
        teamRelationship = (TeamRelationship) intent.getSerializableExtra("teamRelationship");
        iTeamService = new TeamServiceImpl();
        gson = new Gson();
    }

    private void initData() {
        ImageUitl imageUitl = new ImageUitl(BaseActivity.cache);
        imageUitl.asyncloadImage(mIvIcon, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + teamRelationship.getUserInfo().getIcon());
        mTvNickname.setText(teamRelationship.getUserInfo().getNickname());
        mTvTeamName.setText(teamRelationship.getTeam().getTname());
        mTvPostscript.setText(teamRelationship.getMsg());
        if (teamRelationship.getReadState().equals("0")) {
            ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    TeamRelationship teamRelationship1 = new TeamRelationship();
                    teamRelationship1.setReadState("1");
                    teamRelationship1.setId(teamRelationship.getId());
                    try {
                        JSONObject parameterData = new JSONObject(gson.toJson(teamRelationship1));
                        String result = iTeamService.modifyTeamRelationshipById(parameterData);
                        JSONObject resultObj = new JSONObject(result);
                        if (resultObj.getString("code").equals("200")) {
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
                finish();
                break;
            case R.id.btn_refuse:
                loadingUtils.show();
                NIMClient.getService(TeamService.class).rejectApply(teamRelationship.getTid(), teamRelationship.getBeinviter(), "您已被拒绝").setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        ThreadUtils.runInThread(new Runnable() {
                            @Override
                            public void run() {
                                TeamRelationship teamRelationship1 = new TeamRelationship();
                                teamRelationship1.setState("2");
                                teamRelationship1.setId(teamRelationship.getId());
                                try {
                                    JSONObject parameterData = new JSONObject(gson.toJson(teamRelationship1));
                                    String result = iTeamService.modifyTeamRelationshipById(parameterData);
                                    JSONObject resultObj = new JSONObject(result);
                                    if (resultObj.getString("code").equals("200")) {
                                        String postscript = mEtContent.getText().toString();
                                        TeamRelationship teamRelationship2 = new TeamRelationship(teamRelationship.getTid(), aCache.getAsString("account"), teamRelationship.getBeinviter(), postscript,
                                                "2", "apply");
                                        JSONObject parameterData1 = new JSONObject(gson.toJson(teamRelationship2));
                                        String result1 = iTeamService.applyJoinTeam(parameterData1);
                                        JSONObject resultObj1 = new JSONObject(result1);
                                        if (resultObj1.getString("code").equals("200")) {
                                            TeamNoticeActivity.updateData();
                                            FriendsFragment.queryTeamNoticeNums(aCache.getAsString("account"));
                                            TabHostActivity.queryReqAddNums();
                                            TeamActivity.updateData();
                                            finish();
                                        } else {
                                            loadingUtils.dismissOnUiThread();
                                        }
                                    } else {
                                        loadingUtils.dismissOnUiThread();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    loadingUtils.dismissOnUiThread();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailed(int code) {
                        loadingUtils.dismissOnUiThread();
                    }

                    @Override
                    public void onException(Throwable exception) {
                        loadingUtils.dismissOnUiThread();
                    }
                });
                break;
            case R.id.btn_agree:
                loadingUtils.show();
                NIMClient.getService(TeamService.class).passApply(teamRelationship.getTid(), teamRelationship.getBeinviter()).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        ThreadUtils.runInThread(new Runnable() {
                            @Override
                            public void run() {
                                TeamRelationship teamRelationship1 = new TeamRelationship();
                                teamRelationship1.setState("1");
                                teamRelationship1.setId(teamRelationship.getId());
                                teamRelationship1.setTid(teamRelationship.getTid());
                                teamRelationship1.setBeinviter(teamRelationship.getBeinviter());
                                try {
                                    JSONObject parameterData = new JSONObject(gson.toJson(teamRelationship1));
                                    String result = iTeamService.modifyTeamRelationshipById(parameterData);
                                    JSONObject resultObj = new JSONObject(result);
                                    if (resultObj.getString("code").equals("200")) {
                                        String postscript = mEtContent.getText().toString();
                                        TeamRelationship teamRelationship2 = new TeamRelationship(teamRelationship.getTid(), aCache.getAsString("account"), teamRelationship.getBeinviter(), postscript,
                                                "1", "apply");
                                        JSONObject parameterData1 = new JSONObject(gson.toJson(teamRelationship2));
                                        String result1 = iTeamService.applyJoinTeam(parameterData1);
                                        JSONObject resultObj1 = new JSONObject(result1);
                                        if (resultObj1.getString("code").equals("200")) {
                                            TeamNoticeActivity.updateData();
                                            FriendsFragment.queryTeamNoticeNums(aCache.getAsString("account"));
                                            TabHostActivity.queryReqAddNums();
                                            TeamActivity.updateData();
                                            finish();
                                        } else {
                                            loadingUtils.dismissOnUiThread();
                                        }
                                    } else {
                                        loadingUtils.dismissOnUiThread();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    loadingUtils.dismissOnUiThread();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailed(int code) {
                        loadingUtils.dismissOnUiThread();
                    }

                    @Override
                    public void onException(Throwable exception) {
                        loadingUtils.dismissOnUiThread();
                    }
                });
                break;
        }
    }
}
