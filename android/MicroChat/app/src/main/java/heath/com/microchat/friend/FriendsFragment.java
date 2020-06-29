package heath.com.microchat.friend;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.TabHostActivity;
import heath.com.microchat.adapter.FriendAdapter;
import heath.com.microchat.db.MicroChatDB;
import heath.com.microchat.entity.FriendBean;
import heath.com.microchat.entity.UserInfo;
import heath.com.microchat.provider.MicroChatProvider;
import heath.com.microchat.service.IFriendService;
import heath.com.microchat.service.ITeamService;
import heath.com.microchat.service.impl.FriendServiceImpl;
import heath.com.microchat.service.impl.TeamServiceImpl;
import heath.com.microchat.team.TeamActivity;
import heath.com.microchat.utils.ACache;
import heath.com.microchat.utils.DividerItemDecoration;
import heath.com.microchat.utils.ThreadUtils;

public class FriendsFragment extends Fragment implements View.OnClickListener {

    private static ACache aCache;
    private LinearLayout mLlNewFriends;
    private LinearLayout mLlTeam;
    private TextView mTvReqAddFriendNums;
    private TextView mTvTeamNoticeNums;
    private static IFriendService friendService;
    private static Handler handler;
    private String account;
    private RecyclerView mRvFriends;
    private FriendAdapter friendAdapter;
    private List<FriendBean> listdata;
    private Cursor cursor;
    private RefreshLayout mSrlRefreshLayout;
    private static ITeamService iTeamService;
    private Gson gson;
    private MicroChatDB mcDB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container,
                false);
        initView(view);
        init();
        initListener();
        loadFriendsList(account);
        return view;
    }

    private void initView(View view) {
        mLlNewFriends = view.findViewById(R.id.ll_new_friends);
        mLlTeam = view.findViewById(R.id.ll_team);
        mTvReqAddFriendNums = view.findViewById(R.id.tv_req_add_friend_nums);
        mTvTeamNoticeNums = view.findViewById(R.id.tv_team_notice_nums);
        mRvFriends = view.findViewById(R.id.rv_friends);

        aCache = ACache.get(getActivity());
        handler = new IHandler();
        account = aCache.getAsString("account");
        friendService = new FriendServiceImpl();
        listdata = new ArrayList<>();
        mSrlRefreshLayout = view.findViewById(R.id.srl_refresh_layout);
        TabHostActivity.loadingUtils.show();
        iTeamService = new TeamServiceImpl();
        gson = new Gson();
        mcDB = new MicroChatDB(getActivity());
    }

    private void init() {
        queryReqAddNums(account);
        queryTeamNoticeNums(account);
    }

    private void initListener() {
        mLlNewFriends.setOnClickListener(this);
        mLlTeam.setOnClickListener(this);
        mSrlRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                updateFriends1(account);
                refreshlayout.finishRefresh();
            }
        });
    }

    public static void queryReqAddNums(final String account) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject textData = new JSONObject();
                    textData.put("account", account);
                    String results = friendService.queryReqAddNums(textData);
                    final JSONObject resultObj = new JSONObject(results);
                    if (resultObj.getString("code").equals("200")) {
                        Message message = new Message();
                        Bundle data = new Bundle();
                        message.what = 1;
                        data.putSerializable("count", resultObj.getString("count"));
                        message.setData(data);
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void queryTeamNoticeNums(final String account) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject textData = new JSONObject();
                    textData.put("account", account);
                    String results = iTeamService.queryTeamRelationshipNoticeNumByAccount(textData);
                    final JSONObject resultObj = new JSONObject(results);
                    if (resultObj.getString("code").equals("200")) {
                        Message message = new Message();
                        Bundle data = new Bundle();
                        message.what = 3;
                        data.putSerializable("num", resultObj.getString("num"));
                        message.setData(data);
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void updateFriends1(final String account) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject textData = new JSONObject();
                    textData.put("account", account);
                    String results = friendService.updateFriend(textData);
                    final JSONObject resultObj = new JSONObject(results);
                    if (resultObj.getString("code").equals("200")) {
                        List<FriendBean> friendBeans = gson.fromJson(resultObj.getJSONArray("friends").toString(), new TypeToken<List<FriendBean>>() {
                        }.getType());
                        updateFriends(friendBeans);
                        updateUserInfos(friendBeans);
                        loadFriendsList(account);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateFriends(List<FriendBean> friendBeans) {
        mcDB.getWritableDatabase().beginTransaction();
        for (FriendBean friendBean : friendBeans) {
            mcDB.getWritableDatabase().execSQL("replace into " + MicroChatDB.T_FRIENDS + "(" + MicroChatDB.FriendsTable.ID + "," + MicroChatDB.FriendsTable.ACCOUNT + "," + MicroChatDB.FriendsTable.FROM_ACCOUNT + "," + MicroChatDB.FriendsTable.REMARKS + ") values ( '" + friendBean.getId() + "','" + friendBean.getAccount() + "','" + friendBean.getFromAccount() + "','" + friendBean.getRemarks() + "')");
        }
        mcDB.getWritableDatabase().setTransactionSuccessful();
        mcDB.getWritableDatabase().endTransaction();
    }

    private void updateUserInfos(List<FriendBean> friendBeans) {
        mcDB.getWritableDatabase().beginTransaction();
        for (FriendBean friendBean : friendBeans) {
            String sql = "replace into " + MicroChatDB.T_USERINFO
                    + "(" + MicroChatDB.UserInfoTable.ID + ","
                    + MicroChatDB.UserInfoTable.ACCOUNT + ","
                    + MicroChatDB.UserInfoTable.ICON + ","
                    + MicroChatDB.UserInfoTable.SIGN + ","
                    + MicroChatDB.UserInfoTable.EMAIL + ","
                    + MicroChatDB.UserInfoTable.BIRTH + ","
                    + MicroChatDB.UserInfoTable.MOBILE + ","
                    + MicroChatDB.UserInfoTable.GENDER + ","
                    + MicroChatDB.UserInfoTable.NICKNAME + ","
                    + MicroChatDB.UserInfoTable.EX + ") " +
                    "values ( '" + friendBean.getUserInfo().getId() + "','"
                    + friendBean.getUserInfo().getAccount() + "','"
                    + friendBean.getUserInfo().getIcon() + "','"
                    + friendBean.getUserInfo().getSign() + "','"
                    + friendBean.getUserInfo().getEmail() + "','"
                    + friendBean.getUserInfo().getBirth() + "','"
                    + friendBean.getUserInfo().getMobile() + "','"
                    + friendBean.getUserInfo().getGender() + "','"
                    + friendBean.getUserInfo().getNickname() + "','"
                    + friendBean.getUserInfo().getEx() + "')";
            mcDB.getWritableDatabase().execSQL(sql);
        }
        mcDB.getWritableDatabase().setTransactionSuccessful();
        mcDB.getWritableDatabase().endTransaction();
    }

    private class IHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int flag = msg.what;
            switch (flag) {
                case 1:
                    int count = Integer.parseInt((String) msg.getData().getSerializable("count"));
                    if (count <= 0) {
                        mTvReqAddFriendNums.setVisibility(View.GONE);
                    } else if (count > 99) {
                        mTvReqAddFriendNums.setVisibility(View.VISIBLE);
                        mTvReqAddFriendNums.setText("99+");
                    } else {
                        mTvReqAddFriendNums.setVisibility(View.VISIBLE);
                        mTvReqAddFriendNums.setText(count + "");
                    }
                    break;
                case 2:
                    if (getActivity() != null) {
                        String account = (String) msg.getData().getSerializable("account");
                        loadFriendsList(account);
                    }
                    break;
                case 3:
                    int num = Integer.parseInt((String) msg.getData().getSerializable("num"));
                    if (num <= 0) {
                        mTvTeamNoticeNums.setVisibility(View.GONE);
                    } else if (num > 99) {
                        mTvTeamNoticeNums.setVisibility(View.VISIBLE);
                        mTvTeamNoticeNums.setText("99+");
                    } else {
                        mTvTeamNoticeNums.setVisibility(View.VISIBLE);
                        mTvTeamNoticeNums.setText(num + "");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_new_friends:
                startActivity(new Intent(getActivity(), NewFriendsActivity.class));
                break;
            case R.id.ll_team:
                startActivity(new Intent(getActivity(), TeamActivity.class));
                break;
            default:
                break;
        }
    }

    public static void updateData(String account) {
        Message message = new Message();
        message.what = 2;
        Bundle data = new Bundle();
        data.putSerializable("account", account);
        message.setData(data);
        if (handler != null) {
            handler.sendMessage(message);
        }
    }

    private void loadFriendsList(final String account) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                processingData(account);
                Log.e("TAG", "json数组---------: " + listdata.toString() + "-----------------------------------------------------------");
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        friendAdapter = new FriendAdapter(getActivity(), listdata);
                        mRvFriends.setAdapter(friendAdapter);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                        mRvFriends.setLayoutManager(linearLayoutManager);
                        mRvFriends.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
                        friendAdapter.setOnItemClickListener(new FriendAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                FriendBean friendBean = listdata.get(position);
                                HashMap<String, Object> map = O2M(friendBean);
                                map.put("from_activity", "fragment_friends");
                                Log.e("map", "onItemClick: " + map.toString());
                                Intent intent = new Intent(
                                        getActivity(),
                                        UserDetailedInfoActivity.class);
                                intent.putExtra("map", map);
                                startActivityForResult(intent, 0);
                            }
                        });
                    }
                });
            }
        });
    }

    private void processingData(String account) {
        cursor = getActivity().getContentResolver().query(MicroChatProvider.URI_QUERY_FRIENDS, null, null, new String[]{account}, null);
        listdata.clear();
        listdata = setData(cursor);
        TabHostActivity.loadingUtils.dismissOnUiThread();
        System.out.println(listdata.toString() + "----");
    }

    private List<FriendBean> setData(Cursor cursor) {
        List<FriendBean> friendBeans = new ArrayList<>();
        FriendBean friendBean;
        while (cursor.moveToNext()) {
            friendBean = new FriendBean();
            friendBean.setAccount(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.FriendsTable.ACCOUNT)));
            friendBean.setFromAccount(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.FriendsTable.FROM_ACCOUNT)));
            friendBean.setRemarks(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.FriendsTable.REMARKS)));

            UserInfo userInfo = new UserInfo();
            userInfo.setAccount(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.UserInfoTable.ACCOUNT)));
            userInfo.setIcon(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.UserInfoTable.ICON)));
            userInfo.setSign(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.UserInfoTable.SIGN)));
            userInfo.setEmail(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.UserInfoTable.EMAIL)));
            userInfo.setBirth(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.UserInfoTable.BIRTH)));
            userInfo.setMobile(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.UserInfoTable.MOBILE)));
            userInfo.setGender(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.UserInfoTable.GENDER)));
            userInfo.setNickname(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.UserInfoTable.NICKNAME)));
            userInfo.setEx(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.UserInfoTable.EX)));
            friendBean.setUserInfo(userInfo);
            friendBeans.add(friendBean);
        }

        return friendBeans;
    }

    private HashMap<String, Object> O2M(FriendBean friendBean) {
        HashMap<String, Object> listmap = new HashMap<>();
        if (friendBean.getAccount() != null && !friendBean.getAccount().equals("null")) {
            listmap.put("account", friendBean.getAccount());
        }
        if (friendBean.getFromAccount() != null && !friendBean.getFromAccount().equals("null")) {
            listmap.put("fromAccount", friendBean.getFromAccount());
        }
        if (friendBean.getUserInfo().getNickname() != null && !friendBean.getUserInfo().getNickname().equals("null")) {
            listmap.put("nickname", friendBean.getUserInfo().getNickname());
        }
        if (friendBean.getUserInfo().getIcon() != null && !friendBean.getUserInfo().getIcon().equals("null")) {
            listmap.put("icon", friendBean.getUserInfo().getIcon());
        }
        if (friendBean.getUserInfo().getSign() != null && !friendBean.getUserInfo().getSign().equals("null")) {
            listmap.put("sign", friendBean.getUserInfo().getSign());
        }
        if (friendBean.getRemarks() != null && !friendBean.getRemarks().equals("null")) {
            listmap.put("remarks", friendBean.getRemarks());
        }
        if (friendBean.getUserInfo().getEmail() != null && !friendBean.getUserInfo().getEmail().equals("null")) {
            listmap.put("email", friendBean.getUserInfo().getEmail());
        }
        if (friendBean.getUserInfo().getBirth() != null && !friendBean.getUserInfo().getBirth().equals("null")) {
            listmap.put("birth", friendBean.getUserInfo().getBirth());
        }
        if (friendBean.getUserInfo().getMobile() != null && !friendBean.getUserInfo().getMobile().equals("null")) {
            listmap.put("mobile", friendBean.getUserInfo().getMobile());
        }
        if (friendBean.getUserInfo().getGender() != null && !friendBean.getUserInfo().getGender().equals("null")) {
            String gender = friendBean.getUserInfo().getGender();
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

        if (friendBean.getUserInfo().getEx() != null && !friendBean.getUserInfo().getEx().equals("null")) {
            JSONObject ex = null;
            try {
                ex = new JSONObject(friendBean.getUserInfo().getEx().replace("\"", ""));
                if (ex.has("age")) {
                    listmap.put("age", ex.getString("age"));
                }
                listmap.put("ex", friendBean.getUserInfo().getEx());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return listmap;
    }
}
