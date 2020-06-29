package heath.com.microchat.friend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.adapter.FriendsNoticeAdapter;
import heath.com.microchat.service.IFriendService;
import heath.com.microchat.service.impl.FriendServiceImpl;
import heath.com.microchat.team.TeamInfoActivity;
import heath.com.microchat.utils.ACache;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ThreadUtils;
import heath.com.microchat.utils.ToastUtil;

public class NewFriendsActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout mLlReturn;
    private ListView mLvFriendsNotice;
    private IFriendService friendService;
    private ArrayList<Map<String, Object>> listdata;
    private String account;
    private FriendsNoticeAdapter friendsNoticeAdapter;
    public static LoadingUtils loadingUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friends);
        initView();
        initListener();
        init();
    }

    private void initView() {
        mLlReturn = this.findViewById(R.id.ll_return);
        mLvFriendsNotice = this.findViewById(R.id.lv_friends_notice);

        listdata = new ArrayList<>();
        friendService = new FriendServiceImpl();
        account = aCache.getAsString("account");
        loadingUtils = new LoadingUtils(NewFriendsActivity.this, "努力加载中");
        loadingUtils.creat();
        loadingUtils.show();
    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
    }

    private void init() {
        loadFriendsList();
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

    private void loadFriendsList() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                processingData();
                Log.e("TAG", "json数组: " + listdata.toString() + "-----------------------------------------------------------");
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        friendsNoticeAdapter = new FriendsNoticeAdapter(NewFriendsActivity.this, listdata, cache);
                        mLvFriendsNotice.setAdapter(friendsNoticeAdapter);
                        mLvFriendsNotice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                                HashMap map = (HashMap) parent
                                        .getItemAtPosition(position);
                                Log.e("TAG", "map: " + "=======================" + map.toString());
                                Intent intent = new Intent(
                                        NewFriendsActivity.this,
                                        FriendsNoticeProcessedActivity.class);
                                intent.putExtra("map", map);
                                startActivityForResult(intent, 0);
                            }
                        });
                    }
                });
            }
        });

    }

    private void processingData() {
        try {
            final JSONObject textData = new JSONObject();
            textData.put("account", account);
            String results = friendService.queryFriendsNotice(textData);
            final JSONObject resultObj = new JSONObject(results);
            JSONArray result = resultObj.getJSONArray("friendsNotices");
            loadingUtils.dismissOnUiThread();
            Log.e("TAG", "processingData: " + "=======================" + result.toString());
            if (resultObj.getString("code").equals("200")) {
                for (int i = 0; i < result.length(); i++) {
                    HashMap<String, Object> listmap = new HashMap<String, Object>();
                    JSONObject userInfo = result.getJSONObject(i);
                    if (userInfo.has("account")) {
                        listmap.put("account", userInfo.getString("account"));
                    }
                    if (userInfo.has("fromAccount")) {
                        listmap.put("fromAccount", userInfo.getString("fromAccount"));
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

                    if (userInfo.has("id")) {
                        listmap.put("id", userInfo.getString("id"));
                    }

                    if (userInfo.has("state")) {
                        listmap.put("state", userInfo.getString("state"));
                    }

                    if (userInfo.has("content")) {
                        listmap.put("content", userInfo.getString("content"));
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
                }
            } else {
                ToastUtil.toastOnUiThread(NewFriendsActivity.this, resultObj.get("msg").toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadingUtils.dismissOnUiThread();
            Log.e("TAG", "loadFriendsList: " + e.toString() + "----------------------");
        }
    }
}
