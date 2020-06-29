package heath.com.microchat.team;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.adapter.AddTeamAdapter;
import heath.com.microchat.entity.TeamBean;
import heath.com.microchat.friend.UserDetailedInfoActivity;
import heath.com.microchat.service.ITeamService;
import heath.com.microchat.service.impl.TeamServiceImpl;
import heath.com.microchat.utils.DividerItemDecoration;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ThreadUtils;
import heath.com.microchat.utils.ToastUtil;

public class AddTeamActivity extends BaseActivity implements View.OnClickListener {

    private SearchView mSvSearchTeams;
    private LinearLayout mLlReturn;
    private RecyclerView mRvTeams;

    private LoadingUtils loadingUtils;

    private ITeamService iTeamService;

    private AddTeamAdapter addTeamAdapter;
    List<TeamBean> listdata;
    private Gson gson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_team);
        initView();
        init();
        initListener();
    }

    private void initView() {
        mSvSearchTeams = this.findViewById(R.id.sv_search_teams);
        mLlReturn = this.findViewById(R.id.ll_return);
        mRvTeams = this.findViewById(R.id.rv_teams);
    }

    private void init() {
        gson = new Gson();
        loadingUtils = new LoadingUtils(AddTeamActivity.this, "正在搜索");
        loadingUtils.creat();
        iTeamService = new TeamServiceImpl();
        listdata = new ArrayList<>();
    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
        mSvSearchTeams.setSubmitButtonEnabled(false);
        mSvSearchTeams.setQueryHint("查找群组");

        mSvSearchTeams.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                listdata.clear();
                loadingUtils.show();
                loadTeamsList(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_return:
                finish();
                break;
            default:
                break;
        }
    }

    private void loadTeamsList(final String s) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                processingData(s);
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        addTeamAdapter = new AddTeamAdapter(AddTeamActivity.this, listdata);
                        mRvTeams.setAdapter(addTeamAdapter); // 将整合好的adapter交给listview，显示给用户看
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AddTeamActivity.this, LinearLayoutManager.VERTICAL, false);
                        mRvTeams.setLayoutManager(linearLayoutManager);
                        mRvTeams.addItemDecoration(new DividerItemDecoration(AddTeamActivity.this, DividerItemDecoration.VERTICAL_LIST));
                        addTeamAdapter.setOnItemClickListener(new AddTeamAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                TeamBean teamBean = listdata.get(position);
                                Intent intent = new Intent(
                                        AddTeamActivity.this,
                                        ApplyTeamInfoActivity.class);
                                intent.putExtra("teamBean", teamBean);
                                startActivityForResult(intent, 0);
                            }
                        });
                        loadingUtils.dismiss();
                    }
                });
            }
        });

    }

    private void processingData(String s) {

        try {
            final JSONObject textData = new JSONObject();
            textData.put("text", s);
            String results = iTeamService.queryTeams(textData);
            final JSONObject resultObj = new JSONObject(results);
            if (resultObj.get("code").equals("200")) {
                listdata = gson.fromJson(resultObj.getJSONArray("teams").toString(), new TypeToken<List<TeamBean>>() {
                }.getType());
            } else {
                ToastUtil.toastOnUiThread(AddTeamActivity.this, resultObj.get("msg").toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "loadFriendsList: " + e.toString() + "----------------------");
        }
    }
}
