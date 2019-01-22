package heath.com.microchat.friend;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.db.MicroChatDB;
import heath.com.microchat.mine.UpdateInfoActivity;
import heath.com.microchat.provider.MicroChatProvider;
import heath.com.microchat.service.IFriendService;
import heath.com.microchat.service.impl.FriendServiceImpl;
import heath.com.microchat.utils.BottomMenu;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ToastUtil;

public class UserMoreInfoActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout mLlReturn;
    private TextView mTvRemakes;
    private RelativeLayout mRlRemakes;
    private Button mBtnDelFriend;
    private LoadingUtils loadingUtils;
    private static HashMap map;
    private static Handler handler;
    private BottomMenu menuWindow;
    private IFriendService friendServiceImpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_more_info);
        initView();
        initListener();
        initData();
    }

    private void initView() {
        Intent intent = getIntent();
        map = (HashMap) intent.getSerializableExtra("map");
        mLlReturn = findViewById(R.id.ll_return);
        mTvRemakes = findViewById(R.id.tv_remakes);
        mRlRemakes = findViewById(R.id.rl_remakes);
        mBtnDelFriend = findViewById(R.id.btn_del_friend);
        loadingUtils = new LoadingUtils(UserMoreInfoActivity.this, "努力加载中");
        loadingUtils.creat();
        handler = new IHandler();
        friendServiceImpl = new FriendServiceImpl();
    }

    private void initListener(){
        mLlReturn.setOnClickListener(this);
        mRlRemakes.setOnClickListener(this);
        mBtnDelFriend.setOnClickListener(this);
    }

    private void initData(){
        if (map.containsKey("remarks")){
            mTvRemakes.setText((String) map.get("remarks"));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_return:
                finish();
                break;
            case R.id.rl_remakes:
                Intent intent = new Intent(
                        UserMoreInfoActivity.this,
                        UpdateInfoActivity.class);
                map.put("updateInfo",getResources().getString(R.string.tv_remakes));
                intent.putExtra("map", map);
                startActivityForResult(intent, 0);
                break;
            case R.id.btn_del_friend:
                String[] texts = new String[]{getResources().getString(R.string.btn_del_friend)};
                int[] ids = new int[]{R.id.btn1};
                int[] index = new int[]{0, 1};
                List<Map<String, Object>> list = Common.setBtn(texts, ids, index);
                menuWindow = new BottomMenu(UserMoreInfoActivity.this, clickListener, list);
                menuWindow.show();
                menuWindow.setBackground(R.color.white);
                break;
        }
    }

    public static void updateData(HashMap<String,Object> map1) {
        map = map1;
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
            loadingUtils.dismiss();
            initData();
        }
    }

    private void delFriend(String account){
        NIMClient.getService(FriendService.class).deleteFriend(account)
                .setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject parameterData = new JSONObject();
                                try {
                                    parameterData.put("account", aCache.getAsString("account"));
                                    parameterData.put("fromAccount", map.get("fromAccount"));
                                    String result = friendServiceImpl.delFriend(parameterData);
                                    Log.e("TAG", "run: " + result + "0---------------------------");
                                    JSONObject resultObj = new JSONObject(result);
                                    if (resultObj.getString("code").equals("200")) {
                                        loadingUtils.dismissOnUiThread();
                                        finish();
                                        UserDetailedInfoActivity.activity.finish();
                                    }else{
                                        loadingUtils.dismissOnUiThread();
                                        ToastUtil.toastOnUiThread(UserMoreInfoActivity.this,resultObj.get("msg").toString());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    loadingUtils.dismissOnUiThread();
                                    ToastUtil.toastOnUiThread(UserMoreInfoActivity.this,"发生异常");
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailed(int code) {

                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn1:
                    loadingUtils.show();
                    delFriend((String) map.get("fromAccount"));
                    break;
                default:
                    break;
            }
        }
    };

}
