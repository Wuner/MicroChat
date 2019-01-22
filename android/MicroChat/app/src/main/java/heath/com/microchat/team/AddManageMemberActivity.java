package heath.com.microchat.team;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.adapter.AddManageMemberAdapter;
import heath.com.microchat.entity.UserInfo;
import heath.com.microchat.service.ITeamService;
import heath.com.microchat.service.IUserService;
import heath.com.microchat.service.impl.TeamServiceImpl;
import heath.com.microchat.service.impl.UserServiceImpl;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ToastUtil;

public class AddManageMemberActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout mLlReturn;
    private SearchView mSvSearchFriends;
    private ListView mLvMember;
    private Button mBtnAdd;
    private JSONArray memberList;
    private LoadingUtils loadingUtils;
    private AddManageMemberAdapter adapter;
    private Team team;
    private IUserService userServiceImpl;
    private Gson gson;
    private List<UserInfo> listdata;
    private List<UserInfo> contrastData;
    private Handler handler;
    private JSONArray memberArray;
    private ITeamService teamServiceImpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_manage_member);
        initView();
        initListener();
        initData();
    }

    private void initView() {
        Intent intent = getIntent();
        team = (Team) intent.getSerializableExtra("team");
        mLlReturn = findViewById(R.id.ll_return);
        mSvSearchFriends = findViewById(R.id.sv_search_friends);
        mLvMember = findViewById(R.id.lv_member);
        mBtnAdd = findViewById(R.id.btn_add);
        loadingUtils = new LoadingUtils(AddManageMemberActivity.this, "正在加载中");
        loadingUtils.creat();
        userServiceImpl = new UserServiceImpl();
        gson = new Gson();
        listdata = new ArrayList<>();
        contrastData = new ArrayList<>();
        handler = new Handler();
        memberArray = new JSONArray();
        teamServiceImpl = new TeamServiceImpl();
    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
        mBtnAdd.setOnClickListener(this);
        mSvSearchFriends.setSubmitButtonEnabled(false);
        mSvSearchFriends.setQueryHint("查找好友");

        mSvSearchFriends.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String s) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listdata.clear();
                        modifyData(s);
                        adapter.setList(listdata);
                        adapter.notifyDataSetChanged();
                    }
                });
                return false;
            }
        });
    }

    private void initData(){
        initManageMember();
        initButton();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.ll_return:
                finish();
                break;
            case R.id.btn_add:
                Log.e("member", "onClick: "+memberArray.toString());
                List<String> members = gson.fromJson(memberArray.toString(), new TypeToken<List<String>>() {}.getType());
                setTeamManager(team.getId(),members);
                break;
        }
    }

    private void initManageMember() {
        memberList = new JSONArray();
        NIMClient.getService(TeamService.class).queryMemberList(team.getId()).setCallback(new RequestCallbackWrapper<List<TeamMember>>() {
            @Override
            public void onResult(int code, final List<TeamMember> members, Throwable exception) {
                for (TeamMember member : members) {
                    if (member.getType() != TeamMemberType.Manager && member.getType() != TeamMemberType.Owner) {
                        memberList.put(member.getAccount());
                    }
                }
                Log.e("组员数量2", "run: " + memberList.length());
                com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("组员数量", "run: " + memberList.length());
                        if (memberList.length() == 0) {
                            loadingUtils.dismissOnUiThread();
                            return;
                        }
                        try {
                            JSONObject parameterData = new JSONObject();
                            parameterData.put("accounts", memberList);
                            String result = userServiceImpl.queryUsersInfo(parameterData);
                            JSONObject resultObj = new JSONObject(result);
                            if (resultObj.getString("code").equals("200")) {
                                loadingUtils.dismissOnUiThread();
                                listdata = gson.fromJson(resultObj.getJSONArray("userInfos").toString(), new TypeToken<List<UserInfo>>() {
                                }.getType());
                                contrastData = gson.fromJson(resultObj.getJSONArray("userInfos").toString(), new TypeToken<List<UserInfo>>() {
                                }.getType());
                                com.heath.recruit.utils.ThreadUtils.runInUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (adapter != null) {
                                            com.heath.recruit.utils.ThreadUtils.runInUIThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    List<UserInfo> userInfos = new ArrayList<>();
                                                    adapter.setList(userInfos);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            });
                                            return;
                                        }
                                        adapter = new AddManageMemberAdapter(AddManageMemberActivity.this, listdata, cache);
                                        mLvMember.setAdapter(adapter);
                                        mLvMember.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                CheckBox mCbSelect = view.findViewById(R.id.cb_select);
                                                try {
                                                    String member = listdata.get(position).getAccount();
                                                    if (mCbSelect.isChecked()) {
                                                        mCbSelect.setChecked(false);
                                                        memberArray = Common.remove(memberArray, member);
                                                    } else {
                                                        mCbSelect.setChecked(true);
                                                        memberArray.put(member);
                                                    }
                                                    initButton();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                });
                            } else {
                                loadingUtils.dismissOnUiThread();
                                ToastUtil.toastOnUiThread(AddManageMemberActivity.this, resultObj.get("msg").toString());
                            }
                        } catch (Exception e) {
                            loadingUtils.dismissOnUiThread();
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void modifyData(String data) {
        int length = contrastData.size();
        String nickname;
        String account;
        for (int i = 0; i < length; ++i) {
            nickname = contrastData.get(i).getNickname();
            account = contrastData.get(i).getAccount();
            if ((nickname != null && (nickname.contains(data)) || (account != null && account.contains(data)))) {
                UserInfo item = contrastData.get(i);
                listdata.add(item);
            }
        }
    }

    private void setTeamManager(final String teamId, final List<String> accountList) {
        NIMClient.getService(TeamService.class).addManagers(teamId, accountList).setCallback(new RequestCallback<List<TeamMember>>() {
            @Override
            public void onSuccess(List<TeamMember> managers) {
                // 添加群管理员成功
                StringBuilder accounts = new StringBuilder();
                for (String account:accountList){
                    accounts.append(account).append(",");
                }
                modify(teamId,accounts.substring(0, accounts.length()-1),TeamMemberType.Manager.toString());
                ToastUtil.toastOnUiThread(AddManageMemberActivity.this, "添加群管理员成功");
            }

            @Override
            public void onFailed(int code) {
                // 添加群管理员失败
                ToastUtil.toastOnUiThread(AddManageMemberActivity.this, "添加群管理员失败");
            }

            @Override
            public void onException(Throwable exception) {
                // 错误
                ToastUtil.toastOnUiThread(AddManageMemberActivity.this, "添加群管理员错误");
            }
        });
    }

    private void modify(final String tid, final String account, final String text) {
        com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("account", account);
                    parameterData.put("tid", tid);
                    parameterData.put("type", text);
                    String result = teamServiceImpl.modifyTeamMember(parameterData);
                    Log.e("TAG", "run: " + result + "0---------------------------");
                    JSONObject resultObj = new JSONObject(result);
                    if (!resultObj.getString("code").equals("200")) {
                        ToastUtil.toastOnUiThread(AddManageMemberActivity.this, resultObj.get("msg").toString());
                    }else {
                        ManageMemberActivity.updateData();
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(AddManageMemberActivity.this, "发生异常");
                }
            }
        });
    }

    private void initButton(){
        if (memberArray.length()==0){
            mBtnAdd.setEnabled(false);
            mBtnAdd.setBackgroundColor(getResources().getColor(R.color.gray));
        }else{
            mBtnAdd.setEnabled(true);
            mBtnAdd.setBackgroundColor(getResources().getColor(R.color.deepskyblue));
        }
    }

}
