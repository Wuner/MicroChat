package heath.com.microchat.team;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.heath.recruit.utils.ThreadUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.adapter.TeamAdapter;
import heath.com.microchat.message.SendTeamMessageActivity;
import heath.com.microchat.service.ITeamService;
import heath.com.microchat.service.impl.TeamServiceImpl;
import heath.com.microchat.utils.DividerItemDecoration;
import heath.com.microchat.utils.LoadingUtils;

public class TeamActivity extends BaseActivity implements View.OnClickListener {

    private RecyclerView mRvTeams;
    private LinearLayout mLlReturn;
    private TeamAdapter teamAdapter;
    private static Handler handler;
    private LoadingUtils loadingUtils;
    private RefreshLayout mSrlRefreshLayout;
    private TextView mTvTeamNoticeNums;
    private LinearLayout mLlTeamNotice;
    private ITeamService iTeamService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);
        initView();
        initListener();
        init();
    }

    private void initView() {
        mRvTeams = this.findViewById(R.id.rv_teams);
        mLlReturn = this.findViewById(R.id.ll_return);
        mSrlRefreshLayout = this.findViewById(R.id.srl_refresh_layout);
        mTvTeamNoticeNums = findViewById(R.id.tv_team_notice_nums);
        mLlTeamNotice =  findViewById(R.id.ll_team_notice);
        handler = new IHandler();
        loadingUtils = new LoadingUtils(TeamActivity.this, "努力加载中");
        loadingUtils.creat();
        loadingUtils.show();
        iTeamService = new TeamServiceImpl();
    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
        mLlTeamNotice.setOnClickListener(this);
        mSrlRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                init();
                refreshlayout.finishRefresh();
            }
        });
    }

    private void init() {
        queryTeamNoticeNums();
        NIMClient.getService(TeamService.class).queryTeamList().setCallback(new RequestCallback<List<Team>>() {
            @Override
            public void onSuccess(final List<Team> teams) {
                // 获取成功，teams为加入的所有群组
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("群组数量", "run: " + teams.size());
                        loadingUtils.dismissOnUiThread();
                        teamAdapter = new TeamAdapter(TeamActivity.this, teams);
                        mRvTeams.setAdapter(teamAdapter);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TeamActivity.this, LinearLayoutManager.VERTICAL, false);
                        mRvTeams.setLayoutManager(linearLayoutManager);
                        teamAdapter.setOnItemClickListener(new TeamAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Intent intent = new Intent(
                                        TeamActivity.this,
                                        SendTeamMessageActivity.class);
                                intent.putExtra("team", teams.get(position));
                                startActivityForResult(intent, 0);
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailed(int i) {
                // 获取失败，具体错误码见i参数
            }

            @Override
            public void onException(Throwable throwable) {
                // 获取异常
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_return:
                finish();
                break;
            case R.id.ll_team_notice:
                startActivity(new Intent(TeamActivity.this,TeamNoticeActivity.class));
                break;
        }
    }

    public void queryTeamNoticeNums() {
        com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject textData = new JSONObject();
                    textData.put("account", aCache.getAsString("account"));
                    String results = iTeamService.queryTeamRelationshipNoticeNumByAccount(textData);
                    final JSONObject resultObj = new JSONObject(results);
                    if (resultObj.getString("code").equals("200")) {
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    int count = Integer.parseInt(resultObj.getString("num"));
                                    if (count <= 0) {
                                        mTvTeamNoticeNums.setVisibility(View.GONE);
                                    } else if (count > 99) {
                                        mTvTeamNoticeNums.setVisibility(View.VISIBLE);
                                        mTvTeamNoticeNums.setText("99+");
                                    } else {
                                        mTvTeamNoticeNums.setVisibility(View.VISIBLE);
                                        mTvTeamNoticeNums.setText(count + "");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
            init();
        }
    }
}
