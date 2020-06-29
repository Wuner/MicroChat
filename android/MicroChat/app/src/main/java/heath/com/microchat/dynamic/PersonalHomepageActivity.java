package heath.com.microchat.dynamic;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.GSYVideoHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.adapter.DynamicAdapter;
import heath.com.microchat.adapter.MessageAdapter;
import heath.com.microchat.entity.DynamicBean;
import heath.com.microchat.entity.Follow;
import heath.com.microchat.entity.UserInfo;
import heath.com.microchat.service.IDynamicService;
import heath.com.microchat.service.impl.DynamicServiceImpl;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.DividerItemDecoration;
import heath.com.microchat.utils.ImageUitl;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ThreadUtils;
import heath.com.microchat.utils.ToastUtil;

public class PersonalHomepageActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout mLlReturn;
    private ImageView mIvHeadPhoto;
    private TextView mTvNickname;
    private TextView mTvAccount;
    private Button mBtnFollow;
    private TextView mTvDynamicNums;
    private TextView mTvFollowNum;
    private TextView mTvPraiseNum;
    private TextView mTvCommentNum;
    private RecyclerView mRvDynamicMine;
    private SmartRefreshLayout mSrlMineRefreshLayout;
    private HashMap map;
    private IDynamicService iDynamicService;
    private Gson gson;
    private DynamicAdapter mAdapter;
    private List<DynamicBean> dynamics;
    private static final int REFRESH = 0;
    private static final int LOAD = 1;
    private EditText mEtContent;
    private Handler handler;
    private GSYVideoHelper smallVideoHelper;
    private GSYVideoHelper.GSYVideoHelperBuilder gsySmallVideoHelperBuilder;
    private int lastVisibleItem;
    private int firstVisibleItem;
    private LoadingUtils loadingUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_homepage);
        initView();
        intiListener();
        initData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        smallVideoHelper.releaseVideoPlayer();
        GSYVideoManager.releaseAllVideos();
    }

    @Override
    public void onBackPressed() {
        if (smallVideoHelper.backFromFull()) {
            return;
        }
        super.onBackPressed();
    }

    private void initView() {
        Intent intent = getIntent();
        map = (HashMap) intent.getSerializableExtra("map");
        mLlReturn = findViewById(R.id.ll_return);
        mIvHeadPhoto = findViewById(R.id.iv_head_photo);
        mTvNickname = findViewById(R.id.tv_nickname);
        mTvAccount = findViewById(R.id.tv_account);
        mBtnFollow = findViewById(R.id.btn_follow);
        mTvDynamicNums = findViewById(R.id.tv_dynamic_nums);
        mTvFollowNum = findViewById(R.id.tv_follow_num);
        mTvPraiseNum = findViewById(R.id.tv_praise_num);
        mTvCommentNum = findViewById(R.id.tv_comment_num);
        mRvDynamicMine = findViewById(R.id.rv_dynamic_mine);
        mSrlMineRefreshLayout = findViewById(R.id.srl_mine_refresh_layout);
        gson = new Gson();
        handler = new IHandler();
        iDynamicService = new DynamicServiceImpl();
        loadingUtils = new LoadingUtils(PersonalHomepageActivity.this, "正在加载中");
        loadingUtils.creat();
        loadingUtils.show();
    }

    private void intiListener() {
        mLlReturn.setOnClickListener(this);
        mBtnFollow.setOnClickListener(this);
        mSrlMineRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                RefreshAndLoad();
                refreshlayout.finishLoadMore();
            }
        });
        //创建小窗口帮助类
        smallVideoHelper = new GSYVideoHelper(this);
        //配置
        gsySmallVideoHelperBuilder = new GSYVideoHelper.GSYVideoHelperBuilder();
        gsySmallVideoHelperBuilder
                .setHideStatusBar(true)
                .setNeedLockFull(true)
                .setCacheWithPlay(true)
                .setShowFullAnimation(false)
                .setSetUpLazy(true)
                .setRotateViewAuto(false)
                .setLockLand(true)
                .setVideoAllCallBack(new GSYSampleCallBack() {
                    @Override
                    public void onQuitSmallWidget(String url, Object... objects) {
                        super.onQuitSmallWidget(url, objects);
                        //大于0说明有播放,//对应的播放列表TAG
                        if (smallVideoHelper.getPlayPosition() >= 0 && smallVideoHelper.getPlayTAG().equals(MessageAdapter.TAG)) {
                            //当前播放的位置
                            int position = smallVideoHelper.getPlayPosition();
                            //不可视的是时候
                            if ((position < firstVisibleItem || position > lastVisibleItem)) {
                                //释放掉视频
                                smallVideoHelper.releaseVideoPlayer();
                                mAdapter.notifyDataSetChanged();
                            }
                        }

                    }
                });

        smallVideoHelper.setGsyVideoOptionBuilder(gsySmallVideoHelperBuilder);
    }

    private void initData() {
        if (map.get("account").equals(aCache.getAsString("account"))){
            mBtnFollow.setVisibility(View.GONE);
        }
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("account", map.get("account"));
                    String result = iDynamicService.queryDynamicByAccount(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        dynamics = gson.fromJson(resultObj.getJSONArray("dynamics").toString(), new TypeToken<List<DynamicBean>>() {
                        }.getType());
                        BaseActivity.aCache.put("mmeid", dynamics.get(dynamics.size() - 1).getId());
                        Log.i("动态数据", "run: " + result);
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ImageUitl imageUitl = new ImageUitl(cache);
                                    imageUitl.asyncloadImage(mIvHeadPhoto, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + dynamics.get(0).getUserInfo().getIcon());
                                    mTvNickname.setText(dynamics.get(0).getUserInfo().getNickname());
                                    mTvAccount.setText(dynamics.get(0).getUserInfo().getAccount());
                                    if (dynamics.get(0).getFollows() != null) {
                                        for (Follow follow : dynamics.get(0).getFollows()) {
                                            if (follow.getAccount().equals(BaseActivity.aCache.getAsString("account"))) {
                                                mBtnFollow.setBackground(getResources().getDrawable(R.drawable.shape_follow_button_bule));
                                                mBtnFollow.setText(getResources().getString(R.string.tv_already_follow));
                                                break;
                                            }
                                        }
                                    }
                                    mAdapter = new DynamicAdapter(PersonalHomepageActivity.this, dynamics, BaseActivity.cache,smallVideoHelper, gsySmallVideoHelperBuilder);
                                    mRvDynamicMine.setAdapter(mAdapter);
                                    loadingUtils.dismissOnUiThread();
                                    final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PersonalHomepageActivity.this, LinearLayoutManager.VERTICAL, false);
                                    mRvDynamicMine.setLayoutManager(linearLayoutManager);
                                    mRvDynamicMine.addItemDecoration(new DividerItemDecoration(PersonalHomepageActivity.this, DividerItemDecoration.VERTICAL_LIST));
                                    mRvDynamicMine.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                        @Override
                                        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                            super.onScrollStateChanged(recyclerView, newState);
                                        }

                                        @Override
                                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                            super.onScrolled(recyclerView, dx, dy);
                                            firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                                            lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                                            //大于0说明有播放,//对应的播放列表TAG
                                            if (smallVideoHelper.getPlayPosition() >= 0 && smallVideoHelper.getPlayTAG().equals(MessageAdapter.TAG)) {
                                                //当前播放的位置
                                                int position = smallVideoHelper.getPlayPosition();
                                                //不可视的是时候
                                                if ((position < firstVisibleItem || position > lastVisibleItem)) {
                                                    //如果是小窗口就不需要处理
                                                    if (!smallVideoHelper.isSmall()) {
                                                        //小窗口
                                                        int size = CommonUtil.dip2px(PersonalHomepageActivity.this, 150);
                                                        smallVideoHelper.showSmallVideo(new Point(size, size), false, true);
                                                    }
                                                } else {
                                                    if (smallVideoHelper.isSmall()) {
                                                        smallVideoHelper.smallVideoToNormal();
                                                    }
                                                }
                                            }
                                        }
                                    });
                                    mAdapter.setOnOtherClickListener(new DynamicAdapter.OnOtherClickListener() {
                                        @Override
                                        public void onPraiseClick(View view, final int position) {
                                            ThreadUtils.runInThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    JSONObject parameterData = new JSONObject();
                                                    try {
                                                        parameterData.put("account", BaseActivity.aCache.getAsString("account"));
                                                        parameterData.put("dynamicId", dynamics.get(position).getId());
                                                        String result = iDynamicService.praise(parameterData);
                                                        JSONObject resultObj = new JSONObject(result);
                                                        if (resultObj.get("code").equals("200")) {
                                                            final DynamicBean dynamicBean = gson.fromJson(resultObj.getJSONObject("dynamic").toString(), DynamicBean.class);
                                                            ThreadUtils.runInUIThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    mAdapter.updateData(position, dynamicBean);
                                                                }
                                                            });
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCommentClick(final View view, final int position) {
                                            EditText editText = view.findViewById(R.id.et_content);
                                            Common.showSoftInputFromWindow(PersonalHomepageActivity.this, editText);
                                        }

                                        @Override
                                        public void onSendClick(final View view, final int position) {
                                            ThreadUtils.runInThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    JSONObject parameterData = new JSONObject();
                                                    mEtContent = view.findViewById(R.id.et_content);
                                                    String content = mEtContent.getText().toString();
                                                    String hint = mEtContent.getHint().toString();
                                                    Log.i("hint", "run: " + hint);
                                                    UserInfo userInfo = (UserInfo) BaseActivity.aCache.getAsObject("userInfo");
                                                    try {
                                                        parameterData.put("account", BaseActivity.aCache.getAsString("account"));
                                                        parameterData.put("accountNickname", userInfo.getNickname());
                                                        parameterData.put("beAccount", dynamics.get(position).getAccount());
                                                        parameterData.put("beAccountNickname", dynamics.get(position).getUserInfo().getNickname());
                                                        parameterData.put("dynamicId", dynamics.get(position).getId());
                                                        parameterData.put("content", content);
                                                        if (hint.contains(getResources().getString(R.string.tv_reply))) {
                                                            parameterData.put("type", "1");
                                                        } else {
                                                            parameterData.put("type", "0");
                                                        }
                                                        String result = iDynamicService.addCommentReply(parameterData);
                                                        JSONObject resultObj = new JSONObject(result);
                                                        if (resultObj.get("code").equals("200")) {
                                                            updateData();
                                                            final DynamicBean dynamicBean = gson.fromJson(resultObj.getJSONObject("dynamic").toString(), DynamicBean.class);
                                                            ThreadUtils.runInUIThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    mAdapter.updateData(position, dynamicBean);
                                                                    mRvDynamicMine.scrollToPosition(position);
                                                                }
                                                            });
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        }

                                        @Override
                                        public void onHeadPhotoClick(View view, int position) {
                                            Intent intent = new Intent(
                                                    PersonalHomepageActivity.this,
                                                    PersonalHomepageActivity.class);
                                            HashMap<String,Object> map = new HashMap<>();
                                            map.put("account",dynamics.get(position).getAccount());
                                            intent.putExtra("map", map);
                                            startActivityForResult(intent, 0);
                                        }

                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();

                                }
                            }
                        });
                    } else {
                        ToastUtil.toastOnUiThread(PersonalHomepageActivity.this, resultObj.get("msg").toString());
                        loadingUtils.dismissOnUiThread();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(PersonalHomepageActivity.this, Common.MSG_SERVER_ERROR);
                    loadingUtils.dismissOnUiThread();
                }
            }
        });

        setData();
    }

    private void setData(){
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("account",map.get("account"));
                    String result = iDynamicService.queryDynamicNums(parameterData);
                    final JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mTvCommentNum.setText(resultObj.getString("commentReplyNums"));
                                    mTvDynamicNums.setText(resultObj.getString("dynamicNums"));
                                    mTvPraiseNum.setText(resultObj.getString("praiseNums"));
                                    mTvFollowNum.setText(resultObj.getString("followNums"));
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_return:
                finish();
                break;
            case R.id.btn_follow:
                ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject parameterData = new JSONObject();
                        try {
                            parameterData.put("account",aCache.getAsString("account"));
                            parameterData.put("followAccount",map.get("account"));
                            String result = iDynamicService.follow(parameterData);
                            final JSONObject resultObj = new JSONObject(result);
                            if (resultObj.getString("code").equals("200")) {
                                ThreadUtils.runInUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setData();
                                        try {
                                            if (resultObj.getString("state").equals("0")){
                                                mBtnFollow.setBackground(getResources().getDrawable(R.drawable.shape_follow_button_red));
                                                mBtnFollow.setText(getResources().getString(R.string.tv_follow));
                                            }else {
                                                mBtnFollow.setBackground(getResources().getDrawable(R.drawable.shape_follow_button_bule));
                                                mBtnFollow.setText(getResources().getString(R.string.tv_already_follow));
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
                break;
        }
    }

    private void RefreshAndLoad() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("eid", BaseActivity.aCache.getAsString("mmeid"));
                    parameterData.put("account", BaseActivity.aCache.getAsString("account"));
                    String result = iDynamicService.queryDynamicByAccount(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        final List<DynamicBean> dynamicsTemp = gson.fromJson(resultObj.getJSONArray("dynamics").toString(), new TypeToken<List<DynamicBean>>() {
                        }.getType());
                        if (dynamicsTemp.size() == 0) {
                            ToastUtil.toastOnUiThread(PersonalHomepageActivity.this, Common.NO_MORE_DATA);
                            return;
                        }
                        BaseActivity.aCache.put("mmeid", dynamicsTemp.get(dynamicsTemp.size() - 1).getId());
                        Log.i("动态数据", "run: " + dynamicsTemp.toString());
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.setData(dynamics.size() - 1, dynamicsTemp);
                                mRvDynamicMine.scrollToPosition(dynamics.size() - dynamicsTemp.size());
                                ToastUtil.toastOnUiThread(PersonalHomepageActivity.this, "加载" + dynamicsTemp.size() + "条数据");
                            }
                        });
                    } else {
                        ToastUtil.toastOnUiThread(PersonalHomepageActivity.this, resultObj.get("msg").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(PersonalHomepageActivity.this, Common.MSG_SERVER_ERROR);
                }
            }
        });
    }

    private void updateData() {
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
            mEtContent.setText("");
        }
    }
}
