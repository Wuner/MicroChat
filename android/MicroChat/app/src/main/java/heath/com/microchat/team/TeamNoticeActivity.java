package heath.com.microchat.team;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.adapter.TeamNoticeAdapter;
import heath.com.microchat.entity.TeamRelationship;
import heath.com.microchat.service.ITeamService;
import heath.com.microchat.service.impl.TeamServiceImpl;
import heath.com.microchat.utils.DividerItemDecoration;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ThreadUtils;

public class TeamNoticeActivity extends BaseActivity {
    private RecyclerView mRvTeamNotice;
    private SmartRefreshLayout mSrlRefreshLayout;
    private ITeamService iTeamService;
    private Gson gson;
    private List<TeamRelationship> teamRelationships;
    private TeamNoticeAdapter teamNoticeAdapter;
    private LoadingUtils loadingUtils;
    private static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_notice);
        initView();
        init();
        initListener();
        initData();
    }

    private void initView() {
        mRvTeamNotice = findViewById(R.id.rv_team_notice);
        mSrlRefreshLayout = findViewById(R.id.srl_refresh_layout);
    }

    private void init() {
        iTeamService = new TeamServiceImpl();
        gson = new Gson();
        loadingUtils = new LoadingUtils(TeamNoticeActivity.this, "努力加载中");
        loadingUtils.creat();
        loadingUtils.show();
        handler = new IHandler();
    }

    private void initListener() {
        mSrlRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                initData();
                refreshlayout.finishRefresh();
            }
        });
    }

    private void initData() {
        requestData();
    }

    private void requestData() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject textData = new JSONObject();
                    textData.put("account", aCache.getAsString("account"));
                    String results = iTeamService.queryTeamRelationshipNoticeByAccount(textData);
                    final JSONObject resultObj = new JSONObject(results);
                    if (resultObj.getString("code").equals("200")) {
                        teamRelationships = gson.fromJson(resultObj.getJSONArray("teamRelationships").toString(), new TypeToken<List<TeamRelationship>>() {
                        }.getType());
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                teamNoticeAdapter = new TeamNoticeAdapter(TeamNoticeActivity.this, teamRelationships);
                                mRvTeamNotice.setAdapter(teamNoticeAdapter);
                                loadingUtils.dismissOnUiThread();
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TeamNoticeActivity.this, LinearLayoutManager.VERTICAL, false);
                                mRvTeamNotice.setLayoutManager(linearLayoutManager);
                                teamNoticeAdapter.setOnItemClickListener(new TeamNoticeAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {
                                        if (teamRelationships.get(position).getState().equals("0")) {
                                            Intent intent = new Intent(
                                                    TeamNoticeActivity.this,
                                                    TeamNoticeProcessingActivity.class);
                                            intent.putExtra("teamRelationship", teamRelationships.get(position));
                                            startActivityForResult(intent, 0);
                                        }else {
                                            Intent intent = new Intent(
                                                    TeamNoticeActivity.this,
                                                    TeamNoticeProcessingResultActivity.class);
                                            intent.putExtra("teamRelationship",  teamRelationships.get(position));
                                            startActivityForResult(intent, 0);
                                        }
                                    }
                                });
                                teamNoticeAdapter.setOnOtherClickListener(new TeamNoticeAdapter.OnOtherClickListener() {
                                    @Override
                                    public void onIvIconClick(View view, int position) {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("userinfo", teamRelationships.get(position).getUserInfo());
                                        map.put("gone", "1");
                                        Intent intent = new Intent(
                                                TeamNoticeActivity.this,
                                                TeamMemberInfoActivity.class);
                                        intent.putExtra("map", (Serializable) map);
                                        startActivityForResult(intent, 0);
                                    }

                                    @Override
                                    public void onTvNicknameClick(View view, int position) {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("userinfo", teamRelationships.get(position).getUserInfo());
                                        map.put("gone", "1");
                                        Intent intent = new Intent(
                                                TeamNoticeActivity.this,
                                                TeamMemberInfoActivity.class);
                                        intent.putExtra("map", (Serializable) map);
                                        startActivityForResult(intent, 0);
                                    }

                                    @Override
                                    public void onTeamNameClick(View view, int position) {
                                        Intent intent = new Intent(
                                                TeamNoticeActivity.this,
                                                ApplyTeamInfoActivity.class);
                                        intent.putExtra("teamBean", teamRelationships.get(position).getTeam());
                                        startActivityForResult(intent, 0);
                                    }
                                });
                            }
                        });
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
