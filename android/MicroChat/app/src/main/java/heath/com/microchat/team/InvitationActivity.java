package heath.com.microchat.team;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.team.model.Team;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.adapter.InvitationFriendsAdapter;
import heath.com.microchat.entity.TeamBean;
import heath.com.microchat.service.IFriendService;
import heath.com.microchat.service.ITeamService;
import heath.com.microchat.service.impl.FriendServiceImpl;
import heath.com.microchat.service.impl.TeamServiceImpl;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ThreadUtils;
import heath.com.microchat.utils.ToastUtil;

public class InvitationActivity extends BaseActivity implements View.OnClickListener {

    private SearchView mSvSearchFriends;
    private LinearLayout mLlReturn;
    private Button mBtnInvite;
    private ListView mLvFriends;
    private Team team;
    private List<Map<String, Object>> listdata;
    private List<Map<String, Object>> contrastData;
    private InvitationFriendsAdapter invitationFriendsAdapter;
    private static IFriendService friendService;
    private Handler handler;
    private JSONArray members;
    private Gson gson;
    private ITeamService teamServiceImpl;
    private HashMap map;
    private LoadingUtils loadingUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mSvSearchFriends = this.findViewById(R.id.sv_search_friends);
        mLlReturn = this.findViewById(R.id.ll_return);
        mBtnInvite = this.findViewById(R.id.btn_invite);
        mLvFriends = this.findViewById(R.id.lv_friends);
        listdata = new ArrayList<>();
        contrastData = new ArrayList<>();
        friendService = new FriendServiceImpl();
        handler = new Handler();
        members = new JSONArray();
        gson = new Gson();
        teamServiceImpl = new TeamServiceImpl();
        loadingUtils = new LoadingUtils(InvitationActivity.this, "努力加载中");
        loadingUtils.creat();
        loadingUtils.show();
    }

    private void initData() {
        Intent intent = getIntent();
        map = (HashMap) intent.getSerializableExtra("map");
        team = (Team) map.get("team");
        loadFriendsList(aCache.getAsString("account"));
    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
        mBtnInvite.setOnClickListener(this);
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
                        invitationFriendsAdapter.notifyDataSetChanged();
                    }
                });
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_return:
                finish();
                break;
            case R.id.btn_invite:
                loadingUtils.show();
                ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TeamBean teamBean = new TeamBean();
                            teamBean.setOwner(aCache.getAsString("account"));
                            teamBean.setMembers(members.toString());
                            teamBean.setMsg("欢迎加入");
                            teamBean.setMagree("0");
                            teamBean.setTid(team.getId());
                            JSONObject team = new JSONObject(gson.toJson(teamBean));
                            JSONObject parameterData = new JSONObject();
                            parameterData.put("team",team);
                            parameterData.put("type","inviter");
                            String result = teamServiceImpl.invitation(parameterData);
                            JSONObject resultObj = new JSONObject(result);
                            loadingUtils.dismissOnUiThread();
                            if (resultObj.getString("code").equals("200")){
                                ToastUtil.toastOnUiThread(InvitationActivity.this, resultObj.getString("msg"));
                                TeamInfoActivity.updateData();
                                finish();
                            }else{
                                ToastUtil.toastOnUiThread(InvitationActivity.this, resultObj.getString("msg"));
                            }
                        } catch (Exception e) {
                            loadingUtils.dismissOnUiThread();
                            e.printStackTrace();
                        }
                        Log.e("人员", "onClick: "+members.toString());
                    }
                });
                break;
        }
    }

    private void loadFriendsList(final String account) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                processingData(account);
                Log.e("邀请", "json数组---------: " + listdata.toString() + "-----------------------------------------------------------");
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        invitationFriendsAdapter = new InvitationFriendsAdapter(InvitationActivity.this, listdata, BaseActivity.cache);
                        mLvFriends.setAdapter(invitationFriendsAdapter);
                        mLvFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                                CheckBox mCbSelect = view.findViewById(R.id.cb_select);
                                try {
                                    String member = (String) listdata.get(position).get("account");
                                    if (mCbSelect.isChecked()) {
                                        mCbSelect.setChecked(false);
                                        members = Common.remove(members, member);
                                    } else {
                                        mCbSelect.setChecked(true);
                                        members.put(member);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void processingData(String account) {
        JSONObject data = new JSONObject();
        try {
            data.put("account", account);
            String results = friendService.queryAllFriends(data);
            Log.e("TAG", "results-----====----: " + results + "-----------------------------------------------------------");
            final JSONObject resultObj = new JSONObject(results);
            JSONArray result = resultObj.getJSONArray("friends");
            loadingUtils.dismissOnUiThread();
            List memberList = (List) map.get("members");
            for (int i = 0; i < result.length(); i++) {
                JSONObject friend = result.getJSONObject(i);
                JSONObject userInfo = friend.getJSONObject("userInfo");
                assert memberList != null;
                if (!memberList.contains(friend.getString("fromAccount"))){
                    HashMap<String, Object> listmap = new HashMap<>();
                    if (userInfo.has("account")) {
                        listmap.put("account", userInfo.getString("account"));
                    }
                    if (userInfo.has("nickname")) {
                        listmap.put("nickname", userInfo.getString("nickname"));
                    }
                    if (userInfo.has("icon")) {
                        listmap.put("icon", userInfo.getString("icon"));
                    }
                    if (userInfo.has("sign")) {
                        listmap.put("sign", userInfo.getString("sign"));
                    }

                    if (userInfo.has("remarks")) {
                        listmap.put("remarks", userInfo.getString("remarks"));
                    }

                    if (userInfo.has("birth")) {
                        listmap.put("birth", userInfo.getString("birth"));
                    }

                    if (userInfo.has("email")) {
                        listmap.put("email", userInfo.getString("email"));
                    }

                    if (userInfo.has("mobile")) {
                        listmap.put("mobile", userInfo.getString("mobile"));
                    }

                    if (userInfo.has("ex") && userInfo.getString("ex").length() > 0) {
                        JSONObject ex = new JSONObject(userInfo.getString("ex").replace("\"", ""));
                        if (ex.has("age")) {
                            listmap.put("age", ex.getString("age"));
                        }
                        listmap.put("ex", userInfo.getString("ex"));
                    }
                    if (userInfo.has("gender")) {
                        String gender = userInfo.getString("gender");
                        switch (gender) {
                            case "0":
                                listmap.put("gender", "http://testwx.club:8080/SpongeWaySpringMvc/upload/unknown.png");
                                listmap.put("genderStr", "未知");
                                break;
                            case "1":
                                listmap.put("gender", "http://testwx.club:8080/SpongeWaySpringMvc/upload/boy.png");
                                listmap.put("genderStr", "男");
                                break;
                            case "2":
                                listmap.put("gender", "http://testwx.club:8080/SpongeWaySpringMvc/upload/girl.png");
                                listmap.put("genderStr", "女");
                                break;
                            default:
                                break;
                        }
                    }
                    listdata.add(listmap);
                    contrastData.add(listmap);
                    Log.e("TAG", "json数组-----====----: " + listdata.toString() + "-----------------------------------------------------------");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void modifyData(String data) {
        int length = contrastData.size();
        String nickname;
        String account;
        String remarks;
        for (int i = 0; i < length; ++i) {
            nickname = (String) contrastData.get(i).get("nickname");
            account = (String) contrastData.get(i).get("account");
            remarks = (String) contrastData.get(i).get("remarks");
            if ((nickname != null && (nickname.contains(data)) || (account != null && account.contains(data)) || (remarks != null && remarks.contains(data)))) {
                Map<String, Object> item = contrastData.get(i);
                listdata.add(item);
            }
        }
    }

}
