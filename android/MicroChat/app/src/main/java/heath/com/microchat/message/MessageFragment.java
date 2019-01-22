package heath.com.microchat.message;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.gson.Gson;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.TabHostActivity;
import heath.com.microchat.adapter.RecentSessionAdapter;
import heath.com.microchat.db.MicroChatDB;
import heath.com.microchat.entity.TeamBean;
import heath.com.microchat.provider.MicroChatProvider;
import heath.com.microchat.service.IFriendService;
import heath.com.microchat.service.ITeamService;
import heath.com.microchat.service.impl.FriendServiceImpl;
import heath.com.microchat.service.impl.TeamServiceImpl;
import heath.com.microchat.utils.ACache;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ToastUtil;

public class MessageFragment extends Fragment {

    private SwipeMenuListView mLvRecentSession;
    private IFriendService iFriendService;
    private ITeamService iTeamService;
    private ACache aCache;
    private RecentSessionAdapter recentSessionAdapter;
    private static Handler handler;
    private Gson gson;
    private List<Map<String, Object>> listdata1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container,
                false);
        initView(view);
        getRecentSessioList();
        return view;
    }

    private void initView(View view) {
        mLvRecentSession = view.findViewById(R.id.lv_recent_session);
        iFriendService = new FriendServiceImpl();
        iTeamService = new TeamServiceImpl();
        handler = new IHandler();
        aCache = ACache.get(getActivity());
        TabHostActivity.loadingUtils.show();
        gson = new Gson();
    }

    private void init(final List<HashMap<String, Object>> recents) {
        com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                final List<Map<String, Object>> listdata = processingData(recents);
                listdata1 = listdata;
                com.heath.recruit.utils.ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        TabHostActivity.loadingUtils.dismissOnUiThread();
                        Log.e("TAG", "onResult09090909: " + "----------===================" + listdata.toString());
                        if (recentSessionAdapter != null) {
                            recentSessionAdapter.setList(listdata);
                            recentSessionAdapter.notifyDataSetChanged();
                            return;
                        }
                        recentSessionAdapter = new RecentSessionAdapter(getActivity(), listdata, BaseActivity.cache);
                        mLvRecentSession.setAdapter(recentSessionAdapter);
                        setLeftRemove();
                        mLvRecentSession.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                                HashMap map = (HashMap) parent
                                        .getItemAtPosition(position);
                                if (Common.SESSION_TYPE_P2P.equals(map.get("sessionType").toString())) {
                                    ContentValues values = new ContentValues();
                                    values.put("state", "1");
                                    getActivity().getContentResolver().update(
                                            MicroChatProvider.URI_MESSAGE, values,
                                            MicroChatDB.MessageTable.ACCOUNT + "=? and " + MicroChatDB.MessageTable.FROM_ACCOUNT + "=?",
                                            new String[]{map.get("fromAccount").toString(), aCache.getAsString("account")});
                                    recentSessionAdapter.notifyDataSetChanged();
                                    Intent intent = new Intent(
                                            getActivity(),
                                            SendMessageActivity.class);
                                    intent.putExtra("map", map);
                                    startActivityForResult(intent, 0);
                                } else if (Common.SESSION_TYPE_TEAM.equals(map.get("sessionType").toString())) {
                                    ContentValues values = new ContentValues();
                                    values.put("state", "1");
                                    TeamBean teamBean = (TeamBean) map.get("team");
                                    getActivity().getContentResolver().update(
                                            MicroChatProvider.URI_MESSAGE, values,
                                            MicroChatDB.MessageTable.ACCOUNT + "=? or " + MicroChatDB.MessageTable.FROM_ACCOUNT + "=?",
                                            new String[]{teamBean.getTid(), teamBean.getTid()});
                                    recentSessionAdapter.notifyDataSetChanged();
                                    NIMClient.getService(TeamService.class).queryTeam(teamBean.getTid()).setCallback(new RequestCallbackWrapper<Team>() {
                                        @Override
                                        public void onResult(int code, Team t, Throwable exception) {
                                            if (code == ResponseCode.RES_SUCCESS) {
                                                Intent intent = new Intent(
                                                        getActivity(),
                                                        SendTeamMessageActivity.class);
                                                intent.putExtra("team", t);
                                                startActivityForResult(intent, 0);
                                            } else {
                                                // 失败，错误码见code
                                            }

                                            if (exception != null) {
                                                // error
                                            }
                                        }
                                    });
                                }

                            }
                        });
                    }
                });
            }
        });

    }

    private void setLeftRemove() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getActivity().getApplicationContext());
                openItem.setBackground(new ColorDrawable(getResources().getColor(R.color.red1)));
                openItem.setWidth(Common.dp2px(getActivity(), (float) 80));
                openItem.setTitle(getResources().getString(R.string.btn_del));
                openItem.setTitleSize(18);
                openItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(openItem);
            }
        };
        mLvRecentSession.setMenuCreator(creator);
        mLvRecentSession.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        try {
                            String sessionType = listdata1.get(position).get("sessionType").toString();
                            if (Common.SESSION_TYPE_P2P.equals(sessionType)){
                                String account = (String) listdata1.get(position).get("fromAccount");
                                NIMClient.getService(MsgService.class).deleteRecentContact2(account, SessionTypeEnum.P2P);
                                updateData();
                            }else {
                                TeamBean teamBean = (TeamBean) listdata1.get(position).get("team");
                                NIMClient.getService(MsgService.class).deleteRecentContact2(teamBean.getTid(), SessionTypeEnum.Team);
                                updateData();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
                return false;
            }
        });
    }

    private void getRecentSessioList() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        NIMClient.getService(MsgService.class).queryRecentContacts()
                .setCallback(new RequestCallbackWrapper<List<RecentContact>>() {
                    @Override
                    public void onResult(int code, List<RecentContact> recents, Throwable e) {
                        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
                        for (RecentContact recent : recents) {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("fromAccount", recent.getContactId());
                            map.put("content", recent.getContent());
                            map.put("lastSendTime", recent.getTime());
                            map.put("sessionType", recent.getSessionType());
                            list.add(map);
                        }
                        Log.e("最近联系人", "onResult: " + list.toString());
                        init(list);
                    }
                });
    }

    private List<Map<String, Object>> processingData(List<HashMap<String, Object>> recents) {
        final String account = aCache.getAsString("account");
        List<Map<String, Object>> listdata = new ArrayList<>();
        recents = Common.removal(recents);
        for (final HashMap<String, Object> recent : recents) {
            try {
                if (Common.SESSION_TYPE_P2P.equals(recent.get("sessionType").toString())) {
                    JSONObject parameterData = new JSONObject();
                    parameterData.put("account", account);
                    parameterData.put("fromAccount", recent.get("fromAccount"));
                    String results = iFriendService.queryFriendInfoByAccount(parameterData);
                    JSONObject resultObj = new JSONObject(results);
                    if (resultObj.getString("code").equals("200")) {
                        final JSONObject friendInfo = resultObj.getJSONObject("friendInfo");
                        Log.e("TAG", "onResult: " + "----------===================" + friendInfo.toString());
                        final HashMap<String, Object> listmap = new HashMap<String, Object>();
                        listmap.put("content", recent.get("content"));
                        listmap.put("lastSendTime", recent.get("lastSendTime"));
                        listmap.put("sessionType", recent.get("sessionType"));
                        if (friendInfo.has("account")) {
                            listmap.put("account", friendInfo.getString("account"));
                        }
                        if (friendInfo.has("fromAccount")) {
                            listmap.put("fromAccount", friendInfo.getString("fromAccount"));
                        }
                        if (friendInfo.has("nickname")) {
                            listmap.put("nickname", friendInfo.getString("nickname"));
                        }
                        if (friendInfo.has("icon")) {
                            listmap.put("icon", friendInfo.getString("icon"));
                        }
                        if (friendInfo.has("sign")) {
                            listmap.put("sign", friendInfo.getString("sign"));
                        }

                        if (friendInfo.has("remarks")) {
                            listmap.put("remarks", friendInfo.getString("remarks"));
                        }

                        if (friendInfo.has("birth")) {
                            listmap.put("birth", friendInfo.getString("birth"));
                        }

                        if (friendInfo.has("email")) {
                            listmap.put("email", friendInfo.getString("email"));
                        }

                        if (friendInfo.has("mobile")) {
                            listmap.put("mobile", friendInfo.getString("mobile"));
                        }

                        if (friendInfo.has("ex") && friendInfo.getString("ex").length() > 0) {
                            JSONObject ex = new JSONObject(friendInfo.getString("ex").replace("\"", ""));
                            if (ex.has("age")) {
                                listmap.put("age", ex.getString("age"));
                            }
                            listmap.put("ex", friendInfo.getString("ex"));
                        }
                        if (friendInfo.has("gender")) {
                            String gender = friendInfo.getString("gender");
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
                        Log.e("TAG", "onResult: " + "----------===================" + listdata.toString());
                    } else {
                        ToastUtil.toastOnUiThread(getActivity(), resultObj.get("msg").toString());
                    }
                } else if (Common.SESSION_TYPE_TEAM.equals(recent.get("sessionType").toString())) {
                    JSONObject parameterData = new JSONObject();
                    parameterData.put("tid", recent.get("fromAccount"));
                    String results = iTeamService.queryTeamInfoByTid(parameterData);
                    JSONObject resultObj = new JSONObject(results);
                    if (resultObj.getString("code").equals("200")) {
                        TeamBean teamBean = gson.fromJson(resultObj.getJSONObject("team").toString(), TeamBean.class);
                        final HashMap<String, Object> listmap = new HashMap<String, Object>();
                        listmap.put("content", recent.get("content"));
                        listmap.put("lastSendTime", recent.get("lastSendTime"));
                        listmap.put("sessionType", recent.get("sessionType"));
                        listmap.put("team", teamBean);
                        listdata.add(listmap);
                    }
                }
            } catch (Exception e) {
                TabHostActivity.loadingUtils.dismissOnUiThread();
                e.printStackTrace();
            }
        }
        return listdata;
    }

    public static void updateData() {
        Message message = new Message();
        Bundle data = new Bundle();
        message.setData(data);
        handler.sendMessage(message);
    }

    private class IHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            getRecentSessioList();
        }
    }


}
