package heath.com.microchat.friend;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.adapter.AddFriendsAdapter;
import heath.com.microchat.entity.UserInfo;
import heath.com.microchat.service.IFriendService;
import heath.com.microchat.service.impl.FriendServiceImpl;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ThreadUtils;
import heath.com.microchat.utils.ToastUtil;

public class AddFriendsActivity extends BaseActivity implements View.OnClickListener {


    private SearchView mSvSearchFriends;
    private LinearLayout mLlReturn;
    private ListView mLvFriends;

    private LoadingUtils loadingUtils;

    private IFriendService friendService = new FriendServiceImpl();

    private AddFriendsAdapter searchAdapter;
    final ArrayList<Map<String, Object>> listdata = new ArrayList<>();
    private Gson gson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);
        initView();
        init();
        initListener();
    }

    private void initView() {
        mSvSearchFriends = this.findViewById(R.id.sv_search_friends);
        mLlReturn = this.findViewById(R.id.ll_return);
        mLvFriends = this.findViewById(R.id.lv_friends);
        gson = new Gson();
        loadingUtils = new LoadingUtils(AddFriendsActivity.this, "正在搜索");
    }

    private void init() {
        loadingUtils.creat();
    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
        mSvSearchFriends.setSubmitButtonEnabled(false);
        mSvSearchFriends.setQueryHint("查找好友");

        mSvSearchFriends.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                listdata.clear();
                loadingUtils.show();
                loadFriendsList(s);
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

    private void loadFriendsList(final String s) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                processingData(s);
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        searchAdapter = new AddFriendsAdapter(AddFriendsActivity.this, listdata, cache);
                        mLvFriends.setAdapter(searchAdapter); // 将整合好的adapter交给listview，显示给用户看
                        mLvFriends
                                .setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                    @SuppressWarnings("rawtypes")
                                    @Override
                                    public void onItemClick(
                                            AdapterView<?> parent, View view,
                                            int position, long id) {
                                        HashMap map = (HashMap) parent
                                                .getItemAtPosition(position);
                                        map.put("from_activity", "activity_add_friends");
                                        Intent intent = new Intent(
                                                AddFriendsActivity.this,
                                                UserDetailedInfoActivity.class);
                                        intent.putExtra("map", map);
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
            String results = friendService.queryFriends(textData);
            final JSONObject resultObj = new JSONObject(results);
            JSONArray result = resultObj.getJSONArray("uinfos");
            if (resultObj.get("code").equals("200")){
                for (int i = 0; i < result.length(); i++) {
                    HashMap<String, Object> listmap = new HashMap<String, Object>();
                    UserInfo userInfo = gson.fromJson(result.getJSONObject(i).toString(), UserInfo.class);
                    if (userInfo.getAccount() != null) {
                        listmap.put("account", userInfo.getAccount());
                    }
                    if (userInfo.getNickname() != null) {
                        listmap.put("nickname", userInfo.getNickname());
                    }
                    if (userInfo.getIcon() != null) {
                        listmap.put("icon", userInfo.getIcon());
                    }
                    if (userInfo.getSign() != null) {
                        listmap.put("sign", userInfo.getSign());
                    }

                    if (userInfo.getBirth() != null) {
                        listmap.put("birth", userInfo.getBirth());
                    }

                    if (userInfo.getEmail() != null) {
                        listmap.put("email", userInfo.getEmail());
                    }

                    if (userInfo.getMobile() != null) {
                        listmap.put("mobile", userInfo.getMobile());
                    }

                    if (userInfo.getEx() != null && userInfo.getEx().length() > 0) {
                        JSONObject ex = new JSONObject(userInfo.getEx().replace("\"", ""));
                        if (ex.has("age")) {
                            listmap.put("age", ex.getString("age"));
                        }
                        listmap.put("ex", userInfo.getEx());
                    }
                    if (userInfo.getGender() != null) {
                        String gender = userInfo.getGender();
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
            }else{
                ToastUtil.toastOnUiThread(AddFriendsActivity.this,resultObj.get("msg").toString());
            }
        } catch (Exception e) {
            Log.e("TAG", "loadFriendsList: " + e.toString() + "----------------------");
        }
    }
}
