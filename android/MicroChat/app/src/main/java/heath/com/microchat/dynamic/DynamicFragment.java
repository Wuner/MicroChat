package heath.com.microchat.dynamic;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.GSYVideoHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.TabHostActivity;
import heath.com.microchat.adapter.DynamicAdapter;
import heath.com.microchat.adapter.DynamicVideoAdapter;
import heath.com.microchat.adapter.MessageAdapter;
import heath.com.microchat.entity.DynamicBean;
import heath.com.microchat.entity.UserInfo;
import heath.com.microchat.service.IDynamicService;
import heath.com.microchat.service.impl.DynamicServiceImpl;
import heath.com.microchat.utils.BottomMenu;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.DividerItemDecoration;
import heath.com.microchat.utils.OnReboundListener;
import heath.com.microchat.utils.OnRecyclerViewScrollListener;
import heath.com.microchat.utils.ThreadUtils;
import heath.com.microchat.utils.ToastUtil;

public class DynamicFragment extends Fragment implements View.OnClickListener {

    private TextView mTvMine;
    private TextView mTvFriend;
    private TextView mTvFollow;
    private ViewPager mVpDynamic;
    private TextView mTvVideo;
    private TextView mTvImageText;
    private List<View> mViews;
    private ImageView mIvRelease;
    private RecyclerView mRvDynamicMine;
    private RecyclerView mRvDynamicFriend;
    private RecyclerView mRvDynamicImageText;
    private RecyclerView mRvDynamicFollow;
    private RecyclerView mRvDynamicVideo;
    private IDynamicService iDynamicService;
    private Gson gson;
    private DynamicAdapter mAdapter;
    private DynamicAdapter mAdapterFriend;
    private DynamicAdapter mAdapterImageText;
    private DynamicAdapter mAdapterFollow;
    private DynamicVideoAdapter mAdapterVideo;
    private RefreshLayout mSrlMineRefreshLayout;
    private RefreshLayout mSrlFriendRefreshLayout;
    private RefreshLayout mSrlImageTextRefreshLayout;
    private RefreshLayout mSrlFollowRefreshLayout;
    private RefreshLayout mSrlVideoRefreshLayout;
    private List<DynamicBean> dynamics;
    private List<DynamicBean> friendDynamics;
    private List<DynamicBean> imageTextDynamics;
    private List<DynamicBean> followDynamics;
    private List<DynamicBean> videoDynamics;
    private static final int REFRESH = 0;
    private static final int LOAD = 1;
    private EditText mEtContent;
    private Handler handler;
    private GSYVideoHelper smallVideoHelper;
    private GSYVideoHelper.GSYVideoHelperBuilder gsySmallVideoHelperBuilder;
    private int lastVisibleItem;
    private int firstVisibleItem;

    private final int MINE = 0;
    private final int FOLLOW = 1;
    private final int FRIEND = 2;
    private final int VIDEO = 3;
    private final int IMAGE_TEXT = 4;
    
    private final String MSID = "msid"+BaseActivity.aCache.getAsString("account");
    private final String MEID = "meid"+BaseActivity.aCache.getAsString("account");
    private final String VSID = "vsid"+BaseActivity.aCache.getAsString("account");
    private final String VEID = "veid"+BaseActivity.aCache.getAsString("account");
    private final String FRSID = "frsid"+BaseActivity.aCache.getAsString("account");
    private final String FREID = "freid"+BaseActivity.aCache.getAsString("account");
    private final String FOSID = "fosid"+BaseActivity.aCache.getAsString("account");
    private final String FOEID = "foeid"+BaseActivity.aCache.getAsString("account");
    private final String ITSID = "itsid"+BaseActivity.aCache.getAsString("account");
    private final String ITEID = "iteid"+BaseActivity.aCache.getAsString("account");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dynamic, container,
                false);
        initView(view);
        initListener();
        initData();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        smallVideoHelper.releaseVideoPlayer();
        GSYVideoManager.releaseAllVideos();
    }


    private void initView(View view) {
        mTvMine = view.findViewById(R.id.tv_mine);
        mTvFollow = view.findViewById(R.id.tv_follow);
        mTvFriend = view.findViewById(R.id.tv_friend);
        mTvVideo = view.findViewById(R.id.tv_video);
        mTvImageText = view.findViewById(R.id.tv_image_text);
        mVpDynamic = view.findViewById(R.id.vp_dynamic);
        mIvRelease = view.findViewById(R.id.iv_release);
        iDynamicService = new DynamicServiceImpl();
        gson = new Gson();
        handler = new IHandler();
        TabHostActivity.loadingUtils.show();

        mViews = new ArrayList<>();
        LayoutInflater mInflater = LayoutInflater.from(getActivity());

        View mActivityDynamicMine = mInflater.inflate(R.layout.activity_dynamic_mine, null);
        mRvDynamicMine = mActivityDynamicMine.findViewById(R.id.rv_dynamic_mine);
        mSrlMineRefreshLayout = mActivityDynamicMine.findViewById(R.id.srl_mine_refresh_layout);

        View mActivityDynamicFollow = mInflater.inflate(R.layout.activity_dynamic_follow, null);
        mRvDynamicFollow = mActivityDynamicFollow.findViewById(R.id.rv_dynamic_follow);
        mSrlFollowRefreshLayout = mActivityDynamicFollow.findViewById(R.id.srl_follow_refresh_layout);

        View mActivityDynamicFriend = mInflater.inflate(R.layout.activity_dynamic_friend, null);
        mRvDynamicFriend = mActivityDynamicFriend.findViewById(R.id.rv_dynamic_friend);
        mSrlFriendRefreshLayout = mActivityDynamicFriend.findViewById(R.id.srl_friend_refresh_layout);

        View mActivityDynamicVideo = mInflater.inflate(R.layout.activity_dynamic_video, null);
        mRvDynamicVideo = mActivityDynamicVideo.findViewById(R.id.rv_dynamic_video);
        mSrlVideoRefreshLayout = mActivityDynamicVideo.findViewById(R.id.srl_video_refresh_layout);

        View mActivityDynamicImageText = mInflater.inflate(R.layout.activity_dynamic_image_text, null);
        mRvDynamicImageText = mActivityDynamicImageText.findViewById(R.id.rv_dynamic_image_text);
        mSrlImageTextRefreshLayout = mActivityDynamicImageText.findViewById(R.id.srl_image_text_refresh_layout);

        mViews.add(mActivityDynamicMine);
        mViews.add(mActivityDynamicFollow);
        mViews.add(mActivityDynamicFriend);
        mViews.add(mActivityDynamicVideo);
        mViews.add(mActivityDynamicImageText);

        final PagerAdapter mAdapter = new PagerAdapter() {
            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                View view1 = mViews.get(position);
                container.addView(view1);
                return view1;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView(mViews.get(position));
            }

            @Override
            public int getCount() {
                return mViews.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
                return view == o;
            }
        };

        mVpDynamic.setAdapter(mAdapter);
        mVpDynamic.setCurrentItem(1);
        resetFont();
        setSelect(2);
        mVpDynamic.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                int currentItem = mVpDynamic.getCurrentItem();
                resetFont();
                setSelect(currentItem);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        //创建小窗口帮助类
        smallVideoHelper = new GSYVideoHelper(getActivity());
        //配置
        gsySmallVideoHelperBuilder = new GSYVideoHelper.GSYVideoHelperBuilder();
        gsySmallVideoHelperBuilder
                .setHideStatusBar(true)
                .setNeedLockFull(true)
                .setCacheWithPlay(true)
                .setShowFullAnimation(false)
                .setSetUpLazy(true)
                .setRotateViewAuto(false)
                .setLockLand(true).setLooping(true)
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

    private void initListener() {
        mTvMine.setOnClickListener(this);
        mTvFriend.setOnClickListener(this);
        mTvFollow.setOnClickListener(this);
        mTvVideo.setOnClickListener(this);
        mTvImageText.setOnClickListener(this);
        mIvRelease.setOnClickListener(this);
        mSrlMineRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                RefreshAndLoad(REFRESH, MINE);
                refreshlayout.finishRefresh();
            }
        });
        mSrlMineRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                RefreshAndLoad(LOAD, MINE);
                refreshlayout.finishLoadMore();
            }
        });
        mSrlFriendRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                RefreshAndLoad(REFRESH, FRIEND);
                refreshlayout.finishRefresh();
            }
        });
        mSrlFriendRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                RefreshAndLoad(LOAD, FRIEND);
                refreshlayout.finishLoadMore();
            }
        });
        mSrlImageTextRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                RefreshAndLoad(REFRESH, IMAGE_TEXT);
                refreshlayout.finishRefresh();
            }
        });
        mSrlImageTextRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                RefreshAndLoad(LOAD, IMAGE_TEXT);
                refreshlayout.finishLoadMore();
            }
        });
        mSrlFollowRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                RefreshAndLoad(REFRESH, FOLLOW);
                refreshlayout.finishRefresh();
            }
        });
        mSrlFollowRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                RefreshAndLoad(LOAD, FOLLOW);
                refreshlayout.finishLoadMore();
            }
        });
        mSrlVideoRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                RefreshAndLoad(REFRESH, VIDEO);
                refreshlayout.finishRefresh();
            }
        });
        mSrlVideoRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                RefreshAndLoad(LOAD, VIDEO);
                refreshlayout.finishLoadMore();
            }
        });
    }

    private void initData() {
        setDynamicMineData();
        setDynamicFriendData();
        setDynamicImageTextData();
        setDynamicFollowData();
        setDynamicVideoData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_mine:
                resetFont();
                setSelect(0);
                break;
            case R.id.tv_follow:
                resetFont();
                setSelect(1);
                break;
            case R.id.tv_friend:
                resetFont();
                setSelect(2);
                break;
            case R.id.tv_video:
                resetFont();
                setSelect(3);
                break;
            case R.id.tv_image_text:
                resetFont();
                setSelect(4);
                break;
            case R.id.iv_release:
                String[] texts = new String[]{getResources().getString(R.string.tv_image_text), getResources().getString(R.string.tv_video)};
                int[] ids = new int[]{R.id.btn1, R.id.btn2};
                int[] index = new int[]{0, 1};
                List<Map<String, Object>> list = Common.setBtn(texts, ids, index);
                BottomMenu menuWindow = new BottomMenu(getActivity(), clickListener, list);
                menuWindow.show();
                break;
        }
    }

    private void setSelect(int i) {
        mVpDynamic.setCurrentItem(i);
        switch (i) {
            case 0:
                mTvMine.setTextColor(getResources().getColor(R.color.white));
                mTvMine.setTextSize(20);
                break;
            case 1:
                mTvFollow.setTextColor(getResources().getColor(R.color.white));
                mTvFollow.setTextSize(20);
                break;
            case 2:
                mTvFriend.setTextColor(getResources().getColor(R.color.white));
                mTvFriend.setTextSize(20);
                break;
            case 3:
                mTvVideo.setTextColor(getResources().getColor(R.color.white));
                mTvVideo.setTextSize(20);
                break;
            case 4:
                mTvImageText.setTextColor(getResources().getColor(R.color.white));
                mTvImageText.setTextSize(20);
                break;
        }
    }

    private void resetFont() {
        mTvMine.setTextColor(getResources().getColor(R.color.gray));
        mTvMine.setTextSize(16);
        mTvFollow.setTextColor(getResources().getColor(R.color.gray));
        mTvFollow.setTextSize(16);
        mTvFriend.setTextColor(getResources().getColor(R.color.gray));
        mTvFriend.setTextSize(16);
        mTvVideo.setTextColor(getResources().getColor(R.color.gray));
        mTvVideo.setTextSize(16);
        mTvImageText.setTextColor(getResources().getColor(R.color.gray));
        mTvImageText.setTextSize(16);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn1:
                    Intent intent = new Intent(
                            getActivity(),
                            ReleaseActivity.class);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("formActivity", "DynamicFragment");
                    intent.putExtra("map", map);
                    startActivityForResult(intent, 0);
                    break;
                case R.id.btn2:
                    startActivity(new Intent(getActivity(), RecordVideoActivity.class));
                    break;
                default:
                    break;
            }
        }
    };

    private void RefreshAndLoad(final int type, int dynamicType) {
        switch (dynamicType) {
            case MINE:
                if (type == REFRESH) {
                    refreshDynamicMine();
                } else if (type == LOAD) {
                    loadDynamicMine();
                }
                break;
            case FRIEND:
                if (type == REFRESH) {
                    refreshDynamicFriend();
                } else if (type == LOAD) {
                    loadDynamicFriend();
                }
                break;
            case IMAGE_TEXT:
                if (type == REFRESH) {
                    refreshDynamicImageText();
                } else if (type == LOAD) {
                    loadDynamicImageText();
                }
                break;
            case FOLLOW:
                if (type == REFRESH) {
                    refreshDynamicFollow();
                } else if (type == LOAD) {
                    loadDynamicFollow();
                }
                break;
            case VIDEO:
                if (type == REFRESH) {
                    refreshDynamicVideo();
                } else if (type == LOAD) {
                    loadDynamicVideo();
                }
                break;
        }
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

    private void setDynamicMineData(){
       ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    if (BaseActivity.aCache.getAsString(MSID) != null) {
                        parameterData.put("ssid", BaseActivity.aCache.getAsString(MSID));
                    }
                    parameterData.put("account", BaseActivity.aCache.getAsString("account"));
                    String result = iDynamicService.queryDynamicByAccount(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        dynamics = gson.fromJson(resultObj.getJSONArray("dynamics").toString(), new TypeToken<List<DynamicBean>>() {
                        }.getType());
                        BaseActivity.aCache.put(MSID, dynamics.get(0).getId());
                        BaseActivity.aCache.put(MEID, dynamics.get(dynamics.size() - 1).getId());
                        Log.i("动态数据", "run: " + result);
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                initDynamicAdapterMine();
                            }
                        });
                    } else {
                        ToastUtil.toastOnUiThread(getActivity(), resultObj.get("msg").toString());
                        TabHostActivity.loadingUtils.dismissOnUiThread();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(getActivity(), Common.MSG_SERVER_ERROR);
                    TabHostActivity.loadingUtils.dismissOnUiThread();
                }
            }
        });
    }

    private void setDynamicFriendData(){
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    if (BaseActivity.aCache.getAsString(FRSID) != null) {
                        parameterData.put("ssid", BaseActivity.aCache.getAsString(FRSID));
                    }
                    parameterData.put("account", BaseActivity.aCache.getAsString("account"));
                    String result = iDynamicService.queryDynamicFriendsByAccount(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        TabHostActivity.loadingUtils.dismissOnUiThread();
                        friendDynamics = gson.fromJson(resultObj.getJSONArray("dynamics").toString(), new TypeToken<List<DynamicBean>>() {
                        }.getType());
                        BaseActivity.aCache.put(FRSID, friendDynamics.get(0).getId());
                        BaseActivity.aCache.put(FREID, friendDynamics.get(friendDynamics.size() - 1).getId());
                        Log.i("动态数据", "run: " + result);
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                initDynamicAdapterFriend();
                            }
                        });
                    } else {
                        ToastUtil.toastOnUiThread(getActivity(), resultObj.get("msg").toString());
                        TabHostActivity.loadingUtils.dismissOnUiThread();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(getActivity(), Common.MSG_SERVER_ERROR);
                    TabHostActivity.loadingUtils.dismissOnUiThread();
                }
            }
        });
    }

    private void setDynamicImageTextData(){
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    if (BaseActivity.aCache.getAsString(ITSID) != null) {
                        parameterData.put("ssid", BaseActivity.aCache.getAsString(ITSID));
                    }
                    parameterData.put("type", Common.DYNAMIC_TYPE_IMAGE_TEXT);
                    String result = iDynamicService.queryDynamicByImageTextType(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        imageTextDynamics = gson.fromJson(resultObj.getJSONArray("dynamics").toString(), new TypeToken<List<DynamicBean>>() {
                        }.getType());
                        BaseActivity.aCache.put(ITSID, imageTextDynamics.get(0).getId());
                        BaseActivity.aCache.put(ITEID, imageTextDynamics.get(imageTextDynamics.size() - 1).getId());
                        Log.i("动态数据", "run: " + result);
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                initDynamicAdapterImageText();
                            }
                        });
                    } else {
                        ToastUtil.toastOnUiThread(getActivity(), resultObj.get("msg").toString());
                        TabHostActivity.loadingUtils.dismissOnUiThread();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(getActivity(), Common.MSG_SERVER_ERROR);
                    TabHostActivity.loadingUtils.dismissOnUiThread();
                }
            }
        });
    }

    private void setDynamicFollowData(){
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    if (BaseActivity.aCache.getAsString(FOSID) != null) {
                        parameterData.put("ssid", BaseActivity.aCache.getAsString(FOSID));
                    }
                    parameterData.put("account", BaseActivity.aCache.getAsString("account"));
                    String result = iDynamicService.queryDynamicFollowByAccount(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        followDynamics = gson.fromJson(resultObj.getJSONArray("dynamics").toString(), new TypeToken<List<DynamicBean>>() {
                        }.getType());
                        BaseActivity.aCache.put(FOSID, followDynamics.get(0).getId());
                        BaseActivity.aCache.put(FOEID, followDynamics.get(followDynamics.size() - 1).getId());
                        Log.i("动态数据", "run: " + result);
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                initDynamicAdapterFollow();
                            }
                        });
                    } else {
                        ToastUtil.toastOnUiThread(getActivity(), resultObj.get("msg").toString());
                        TabHostActivity.loadingUtils.dismissOnUiThread();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(getActivity(), Common.MSG_SERVER_ERROR);
                    TabHostActivity.loadingUtils.dismissOnUiThread();
                }
            }
        });
    }

    private void setDynamicVideoData(){
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    if (BaseActivity.aCache.getAsString(VSID) != null) {
                        parameterData.put("ssid", BaseActivity.aCache.getAsString(VSID));
                    }
                    parameterData.put("type", Common.DYNAMIC_TYPE_VIDEO);
                    String result = iDynamicService.queryDynamicByImageTextType(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        videoDynamics = gson.fromJson(resultObj.getJSONArray("dynamics").toString(), new TypeToken<List<DynamicBean>>() {
                        }.getType());
                        BaseActivity.aCache.put(VSID, videoDynamics.get(0).getId());
                        BaseActivity.aCache.put(VEID, videoDynamics.get(videoDynamics.size() - 1).getId());
                        Log.i("动态数据", "run: " + result);
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                initDynamicAdapterVideo();
                            }
                        });
                    } else {
                        ToastUtil.toastOnUiThread(getActivity(), resultObj.get("msg").toString());
                        TabHostActivity.loadingUtils.dismissOnUiThread();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(getActivity(), Common.MSG_SERVER_ERROR);
                    TabHostActivity.loadingUtils.dismissOnUiThread();
                }
            }
        });
    }

    private void loadDynamicVideo() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("eid", BaseActivity.aCache.getAsString(VEID));
                    parameterData.put("type", Common.DYNAMIC_TYPE_VIDEO);
                    String result = iDynamicService.queryDynamicByImageTextType(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        final List<DynamicBean> dynamicsTemp = gson.fromJson(resultObj.getJSONArray("dynamics").toString(), new TypeToken<List<DynamicBean>>() {
                        }.getType());
                        if (dynamicsTemp.size() == 0) {
                            ToastUtil.toastOnUiThread(getActivity(), Common.NO_MORE_DATA);
                            return;
                        }
                        BaseActivity.aCache.put(VEID, dynamicsTemp.get(dynamicsTemp.size() - 1).getId());
                        Log.i("动态数据", "run: " + dynamicsTemp.toString());
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (videoDynamics.size() == 0) {
                                    videoDynamics = dynamicsTemp;
                                    initDynamicAdapterVideo();
                                } else {
                                    mAdapterVideo.setData(videoDynamics.size() - 1, dynamicsTemp);
                                }
                                mRvDynamicVideo.scrollToPosition(videoDynamics.size() - dynamicsTemp.size());
                                ToastUtil.toastOnUiThread(getActivity(), "加载" + dynamicsTemp.size() + "条数据");
                            }
                        });
                    } else {
                        ToastUtil.toastOnUiThread(getActivity(), resultObj.get("msg").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(getActivity(), Common.MSG_SERVER_ERROR);
                }
            }
        });
    }

    private void refreshDynamicVideo() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("sid", BaseActivity.aCache.getAsString(VSID));
                    parameterData.put("type", Common.DYNAMIC_TYPE_VIDEO);
                    String result = iDynamicService.queryDynamicByImageTextType(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        final List<DynamicBean> dynamicsTemp = gson.fromJson(resultObj.getJSONArray("dynamics").toString(), new TypeToken<List<DynamicBean>>() {
                        }.getType());
                        if (dynamicsTemp.size() == 0) {
                            ToastUtil.toastOnUiThread(getActivity(), Common.NO_MORE_DATA);
                            return;
                        }
                        BaseActivity.aCache.put(VSID, dynamicsTemp.get(0).getId());
                        Log.i("动态数据", "run: " + dynamicsTemp.toString());
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (videoDynamics.size() == 0) {
                                    videoDynamics = dynamicsTemp;
                                    initDynamicAdapterVideo();
                                } else {
                                    mAdapterVideo.setData(0, dynamicsTemp);
                                }
                                mRvDynamicVideo.scrollToPosition(0);
                                ToastUtil.toastOnUiThread(getActivity(), "更新了" + dynamicsTemp.size() + "条数据");
                            }
                        });
                    } else {
                        ToastUtil.toastOnUiThread(getActivity(), resultObj.get("msg").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(getActivity(), Common.MSG_SERVER_ERROR);
                }
            }
        });
    }

    private void loadDynamicFollow() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("eid", BaseActivity.aCache.getAsString(FOEID));
                    parameterData.put("account", BaseActivity.aCache.getAsString("account"));
                    String result = iDynamicService.queryDynamicFollowByAccount(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        final List<DynamicBean> dynamicsTemp = gson.fromJson(resultObj.getJSONArray("dynamics").toString(), new TypeToken<List<DynamicBean>>() {
                        }.getType());
                        if (dynamicsTemp.size() == 0) {
                            ToastUtil.toastOnUiThread(getActivity(), Common.NO_MORE_DATA);
                            return;
                        }
                        BaseActivity.aCache.put(FOEID, dynamicsTemp.get(dynamicsTemp.size() - 1).getId());
                        Log.i("动态数据", "run: " + dynamicsTemp.toString());
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (followDynamics.size() == 0) {
                                    followDynamics = dynamicsTemp;
                                    initDynamicAdapterFollow();
                                } else {
                                    mAdapterFollow.setData(followDynamics.size() - 1, dynamicsTemp);
                                }
                                mRvDynamicFollow.scrollToPosition(followDynamics.size() - dynamicsTemp.size());
                                ToastUtil.toastOnUiThread(getActivity(), "加载" + dynamicsTemp.size() + "条数据");
                            }
                        });
                    } else {
                        ToastUtil.toastOnUiThread(getActivity(), resultObj.get("msg").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(getActivity(), Common.MSG_SERVER_ERROR);
                }
            }
        });
    }

    private void refreshDynamicFollow() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("sid", BaseActivity.aCache.getAsString(FOSID));
                    parameterData.put("account", BaseActivity.aCache.getAsString("account"));
                    String result = iDynamicService.queryDynamicFollowByAccount(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        final List<DynamicBean> dynamicsTemp = gson.fromJson(resultObj.getJSONArray("dynamics").toString(), new TypeToken<List<DynamicBean>>() {
                        }.getType());
                        if (dynamicsTemp.size() == 0) {
                            ToastUtil.toastOnUiThread(getActivity(), Common.NO_MORE_DATA);
                            return;
                        }
                        BaseActivity.aCache.put(FOSID, dynamicsTemp.get(0).getId());
                        Log.i("动态数据", "run: " + dynamicsTemp.toString());
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (followDynamics.size() == 0) {
                                    followDynamics = dynamicsTemp;
                                    initDynamicAdapterFollow();
                                } else {
                                    mAdapterFollow.setData(0, dynamicsTemp);
                                }
                                mRvDynamicFollow.scrollToPosition(0);
                                ToastUtil.toastOnUiThread(getActivity(), "更新了" + dynamicsTemp.size() + "条数据");
                            }
                        });
                    } else {
                        ToastUtil.toastOnUiThread(getActivity(), resultObj.get("msg").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(getActivity(), Common.MSG_SERVER_ERROR);
                }
            }
        });
    }

    private void loadDynamicImageText() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("eid", BaseActivity.aCache.getAsString(ITEID));
                    parameterData.put("type", Common.DYNAMIC_TYPE_IMAGE_TEXT);
                    String result = iDynamicService.queryDynamicByImageTextType(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        final List<DynamicBean> dynamicsTemp = gson.fromJson(resultObj.getJSONArray("dynamics").toString(), new TypeToken<List<DynamicBean>>() {
                        }.getType());
                        if (dynamicsTemp.size() == 0) {
                            ToastUtil.toastOnUiThread(getActivity(), Common.NO_MORE_DATA);
                            return;
                        }
                        BaseActivity.aCache.put(ITEID, dynamicsTemp.get(dynamicsTemp.size() - 1).getId());
                        Log.i("动态数据", "run: " + dynamicsTemp.toString());
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (imageTextDynamics.size() == 0) {
                                    imageTextDynamics = dynamicsTemp;
                                    initDynamicAdapterImageText();
                                } else {
                                    mAdapterImageText.setData(imageTextDynamics.size() - 1, dynamicsTemp);
                                }
                                mRvDynamicImageText.scrollToPosition(imageTextDynamics.size() - dynamicsTemp.size());
                                ToastUtil.toastOnUiThread(getActivity(), "加载" + dynamicsTemp.size() + "条数据");
                            }
                        });
                    } else {
                        ToastUtil.toastOnUiThread(getActivity(), resultObj.get("msg").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(getActivity(), Common.MSG_SERVER_ERROR);
                }
            }
        });
    }

    private void refreshDynamicImageText() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("sid", BaseActivity.aCache.getAsString(ITSID));
                    parameterData.put("type", Common.DYNAMIC_TYPE_IMAGE_TEXT);
                    String result = iDynamicService.queryDynamicByImageTextType(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        final List<DynamicBean> dynamicsTemp = gson.fromJson(resultObj.getJSONArray("dynamics").toString(), new TypeToken<List<DynamicBean>>() {
                        }.getType());
                        if (dynamicsTemp.size() == 0) {
                            ToastUtil.toastOnUiThread(getActivity(), Common.NO_MORE_DATA);
                            return;
                        }
                        BaseActivity.aCache.put(ITSID, dynamicsTemp.get(0).getId());
                        Log.i("动态数据", "run: " + dynamicsTemp.toString());
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (imageTextDynamics.size() == 0) {
                                    imageTextDynamics = dynamicsTemp;
                                    initDynamicAdapterImageText();
                                } else {
                                    mAdapterImageText.setData(0, dynamicsTemp);
                                }
                                mRvDynamicImageText.scrollToPosition(0);
                                ToastUtil.toastOnUiThread(getActivity(), "更新了" + dynamicsTemp.size() + "条数据");
                            }
                        });
                    } else {
                        ToastUtil.toastOnUiThread(getActivity(), resultObj.get("msg").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(getActivity(), Common.MSG_SERVER_ERROR);
                }
            }
        });
    }

    private void loadDynamicFriend() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("eid", BaseActivity.aCache.getAsString(FREID));
                    parameterData.put("account", BaseActivity.aCache.getAsString("account"));
                    String result = iDynamicService.queryDynamicFriendsByAccount(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        final List<DynamicBean> dynamicsTemp = gson.fromJson(resultObj.getJSONArray("dynamics").toString(), new TypeToken<List<DynamicBean>>() {
                        }.getType());
                        if (dynamicsTemp.size() == 0) {
                            ToastUtil.toastOnUiThread(getActivity(), Common.NO_MORE_DATA);
                            return;
                        }
                        BaseActivity.aCache.put(FREID, dynamicsTemp.get(dynamicsTemp.size() - 1).getId());
                        Log.i("动态数据", "run: " + dynamicsTemp.toString());
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (friendDynamics.size() == 0) {
                                    friendDynamics = dynamicsTemp;
                                    initDynamicAdapterFriend();
                                } else {
                                    mAdapterFriend.setData(friendDynamics.size() - 1, dynamicsTemp);
                                }
                                mRvDynamicFriend.scrollToPosition(friendDynamics.size() - dynamicsTemp.size());
                                ToastUtil.toastOnUiThread(getActivity(), "加载" + dynamicsTemp.size() + "条数据");
                            }
                        });
                    } else {
                        ToastUtil.toastOnUiThread(getActivity(), resultObj.get("msg").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(getActivity(), Common.MSG_SERVER_ERROR);
                }
            }
        });
    }

    private void refreshDynamicFriend() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("sid", BaseActivity.aCache.getAsString(FRSID));
                    parameterData.put("account", BaseActivity.aCache.getAsString("account"));
                    String result = iDynamicService.queryDynamicFriendsByAccount(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        final List<DynamicBean> dynamicsTemp = gson.fromJson(resultObj.getJSONArray("dynamics").toString(), new TypeToken<List<DynamicBean>>() {
                        }.getType());
                        if (dynamicsTemp.size() == 0) {
                            ToastUtil.toastOnUiThread(getActivity(), Common.NO_MORE_DATA);
                            return;
                        }
                        BaseActivity.aCache.put(FRSID, dynamicsTemp.get(0).getId());
                        Log.i("动态数据", "run: " + dynamicsTemp.toString());
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (friendDynamics.size() == 0) {
                                    friendDynamics = dynamicsTemp;
                                    initDynamicAdapterFriend();
                                } else {
                                    mAdapterFriend.setData(0, dynamicsTemp);
                                }
                                mRvDynamicFriend.scrollToPosition(0);
                                ToastUtil.toastOnUiThread(getActivity(), "更新了" + dynamicsTemp.size() + "条数据");
                            }
                        });
                    } else {
                        ToastUtil.toastOnUiThread(getActivity(), resultObj.get("msg").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(getActivity(), Common.MSG_SERVER_ERROR);
                }
            }
        });
    }

    private void loadDynamicMine() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("eid", BaseActivity.aCache.getAsString(MEID));
                    parameterData.put("account", BaseActivity.aCache.getAsString("account"));
                    String result = iDynamicService.queryDynamicByAccount(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        final List<DynamicBean> dynamicsTemp = gson.fromJson(resultObj.getJSONArray("dynamics").toString(), new TypeToken<List<DynamicBean>>() {
                        }.getType());
                        if (dynamicsTemp.size() == 0) {
                            ToastUtil.toastOnUiThread(getActivity(), Common.NO_MORE_DATA);
                            return;
                        }
                        BaseActivity.aCache.put(MEID, dynamicsTemp.get(dynamicsTemp.size() - 1).getId());
                        Log.i("动态数据", "run: " + dynamicsTemp.toString());
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (dynamics.size() == 0) {
                                    dynamics = dynamicsTemp;
                                    initDynamicAdapterMine();
                                } else {
                                    mAdapter.setData(dynamics.size() - 1, dynamicsTemp);
                                }
                                mRvDynamicMine.scrollToPosition(dynamics.size() - dynamicsTemp.size());
                                ToastUtil.toastOnUiThread(getActivity(), "加载" + dynamicsTemp.size() + "条数据");
                            }
                        });
                    } else {
                        ToastUtil.toastOnUiThread(getActivity(), resultObj.get("msg").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(getActivity(), Common.MSG_SERVER_ERROR);
                }
            }
        });
    }

    private void refreshDynamicMine() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("sid", BaseActivity.aCache.getAsString(MSID));
                    parameterData.put("account", BaseActivity.aCache.getAsString("account"));
                    String result = iDynamicService.queryDynamicByAccount(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        final List<DynamicBean> dynamicsTemp = gson.fromJson(resultObj.getJSONArray("dynamics").toString(), new TypeToken<List<DynamicBean>>() {
                        }.getType());
                        if (dynamicsTemp.size() == 0) {
                            ToastUtil.toastOnUiThread(getActivity(), Common.NO_MORE_DATA);
                            return;
                        }
                        BaseActivity.aCache.put(MSID, dynamicsTemp.get(0).getId());
                        Log.i("动态数据", "run: " + dynamicsTemp.toString());
                        ThreadUtils.runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (dynamics.size() == 0) {
                                        dynamics = dynamicsTemp;
                                        initDynamicAdapterMine();
                                    } else {
                                        mAdapter.setData(0, dynamicsTemp);
                                    }
                                    mRvDynamicMine.scrollToPosition(0);
                                    ToastUtil.toastOnUiThread(getActivity(), "更新了" + dynamicsTemp.size() + "条数据");
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        ToastUtil.toastOnUiThread(getActivity(), resultObj.get("msg").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(getActivity(), Common.MSG_SERVER_ERROR);
                }
            }
        });
    }

    private void initDynamicAdapterMine() {
        try {
            mAdapter = new DynamicAdapter(getActivity(), dynamics, BaseActivity.cache, smallVideoHelper, gsySmallVideoHelperBuilder);
            mRvDynamicMine.setAdapter(mAdapter);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            mRvDynamicMine.setLayoutManager(linearLayoutManager);
            mRvDynamicMine.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
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
                                int size = CommonUtil.dip2px(getActivity(), 150);
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
                    Common.showSoftInputFromWindow(getActivity(), editText);
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
                            getActivity(),
                            PersonalHomepageActivity.class);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("account", dynamics.get(position).getAccount());
                    intent.putExtra("map", map);
                    startActivityForResult(intent, 0);
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initDynamicAdapterFriend() {
        try {
            mAdapterFriend = new DynamicAdapter(getActivity(), friendDynamics, BaseActivity.cache, smallVideoHelper, gsySmallVideoHelperBuilder);
            mRvDynamicFriend.setAdapter(mAdapterFriend);
            TabHostActivity.loadingUtils.dismissOnUiThread();
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            mRvDynamicFriend.setLayoutManager(linearLayoutManager);
            mRvDynamicFriend.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
            mRvDynamicFriend.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                                int size = CommonUtil.dip2px(getActivity(), 150);
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
            mAdapterFriend.setOnOtherClickListener(new DynamicAdapter.OnOtherClickListener() {
                @Override
                public void onPraiseClick(View view, final int position) {
                    ThreadUtils.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject parameterData = new JSONObject();
                            try {
                                parameterData.put("account", BaseActivity.aCache.getAsString("account"));
                                parameterData.put("dynamicId", friendDynamics.get(position).getId());
                                String result = iDynamicService.praise(parameterData);
                                JSONObject resultObj = new JSONObject(result);
                                if (resultObj.get("code").equals("200")) {
                                    final DynamicBean dynamicBean = gson.fromJson(resultObj.getJSONObject("dynamic").toString(), DynamicBean.class);
                                    ThreadUtils.runInUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mAdapterFriend.updateData(position, dynamicBean);
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
                    Common.showSoftInputFromWindow(getActivity(), editText);
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
                                parameterData.put("beAccount", friendDynamics.get(position).getAccount());
                                parameterData.put("beAccountNickname", friendDynamics.get(position).getUserInfo().getNickname());
                                parameterData.put("dynamicId", friendDynamics.get(position).getId());
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
                                            mAdapterFriend.updateData(position, dynamicBean);
                                            mRvDynamicFriend.scrollToPosition(position);
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
                            getActivity(),
                            PersonalHomepageActivity.class);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("account", friendDynamics.get(position).getAccount());
                    intent.putExtra("map", map);
                    startActivityForResult(intent, 0);
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
            TabHostActivity.loadingUtils.dismissOnUiThread();
        }
    }

    private void initDynamicAdapterImageText() {
        try {
            mAdapterImageText = new DynamicAdapter(getActivity(), imageTextDynamics, BaseActivity.cache, smallVideoHelper, gsySmallVideoHelperBuilder);
            mRvDynamicImageText.setAdapter(mAdapterImageText);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            mRvDynamicImageText.setLayoutManager(linearLayoutManager);
            mRvDynamicImageText.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
            mRvDynamicImageText.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                                int size = CommonUtil.dip2px(getActivity(), 150);
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
            mAdapterImageText.setOnOtherClickListener(new DynamicAdapter.OnOtherClickListener() {
                @Override
                public void onPraiseClick(View view, final int position) {
                    ThreadUtils.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject parameterData = new JSONObject();
                            try {
                                parameterData.put("account", BaseActivity.aCache.getAsString("account"));
                                parameterData.put("dynamicId", imageTextDynamics.get(position).getId());
                                String result = iDynamicService.praise(parameterData);
                                JSONObject resultObj = new JSONObject(result);
                                if (resultObj.get("code").equals("200")) {
                                    final DynamicBean dynamicBean = gson.fromJson(resultObj.getJSONObject("dynamic").toString(), DynamicBean.class);
                                    ThreadUtils.runInUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mAdapterImageText.updateData(position, dynamicBean);
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
                    Common.showSoftInputFromWindow(getActivity(), editText);
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
                                parameterData.put("beAccount", imageTextDynamics.get(position).getAccount());
                                parameterData.put("beAccountNickname", imageTextDynamics.get(position).getUserInfo().getNickname());
                                parameterData.put("dynamicId", imageTextDynamics.get(position).getId());
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
                                            mAdapterImageText.updateData(position, dynamicBean);
                                            mRvDynamicImageText.scrollToPosition(position);
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
                            getActivity(),
                            PersonalHomepageActivity.class);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("account", imageTextDynamics.get(position).getAccount());
                    intent.putExtra("map", map);
                    startActivityForResult(intent, 0);
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initDynamicAdapterFollow() {
        try {
            mAdapterFollow = new DynamicAdapter(getActivity(), followDynamics, BaseActivity.cache, smallVideoHelper, gsySmallVideoHelperBuilder);
            mRvDynamicFollow.setAdapter(mAdapterFollow);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            mRvDynamicFollow.setLayoutManager(linearLayoutManager);
            mRvDynamicFollow.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
            mRvDynamicFollow.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                                int size = CommonUtil.dip2px(getActivity(), 150);
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
            mAdapterFollow.setOnOtherClickListener(new DynamicAdapter.OnOtherClickListener() {
                @Override
                public void onPraiseClick(View view, final int position) {
                    ThreadUtils.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject parameterData = new JSONObject();
                            try {
                                parameterData.put("account", BaseActivity.aCache.getAsString("account"));
                                parameterData.put("dynamicId", followDynamics.get(position).getId());
                                String result = iDynamicService.praise(parameterData);
                                JSONObject resultObj = new JSONObject(result);
                                if (resultObj.get("code").equals("200")) {
                                    final DynamicBean dynamicBean = gson.fromJson(resultObj.getJSONObject("dynamic").toString(), DynamicBean.class);
                                    ThreadUtils.runInUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mAdapterFollow.updateData(position, dynamicBean);
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
                    Common.showSoftInputFromWindow(getActivity(), editText);
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
                                parameterData.put("beAccount", followDynamics.get(position).getAccount());
                                parameterData.put("beAccountNickname", followDynamics.get(position).getUserInfo().getNickname());
                                parameterData.put("dynamicId", followDynamics.get(position).getId());
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
                                            mAdapterFollow.updateData(position, dynamicBean);
                                            mRvDynamicFollow.scrollToPosition(position);
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
                            getActivity(),
                            PersonalHomepageActivity.class);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("account", followDynamics.get(position).getAccount());
                    intent.putExtra("map", map);
                    startActivityForResult(intent, 0);
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initDynamicAdapterVideo() {
        try {
            mAdapterVideo = new DynamicVideoAdapter(getActivity(), videoDynamics, smallVideoHelper, gsySmallVideoHelperBuilder);
            mRvDynamicVideo.setAdapter(mAdapterVideo);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            mRvDynamicVideo.setLayoutManager(linearLayoutManager);
            mRvDynamicVideo.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
            mAdapterVideo.setOnOtherClickListener(new DynamicVideoAdapter.OnOtherClickListener() {
                @Override
                public void onPraiseClick(View view, final int position) {
                    ThreadUtils.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject parameterData = new JSONObject();
                            try {
                                parameterData.put("account", BaseActivity.aCache.getAsString("account"));
                                parameterData.put("dynamicId", videoDynamics.get(position).getId());
                                String result = iDynamicService.praise(parameterData);
                                JSONObject resultObj = new JSONObject(result);
                                if (resultObj.get("code").equals("200")) {
                                    final DynamicBean dynamicBean = gson.fromJson(resultObj.getJSONObject("dynamic").toString(), DynamicBean.class);
                                    ThreadUtils.runInUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mAdapterVideo.updateData(position, dynamicBean);
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
                public void onCommentClick(View view, int position) {

                }

                @Override
                public void onHeadPhotoClick(View view, int position) {
                    Intent intent = new Intent(
                            getActivity(),
                            PersonalHomepageActivity.class);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("account", videoDynamics.get(position).getAccount());
                    intent.putExtra("map", map);
                    startActivityForResult(intent, 0);
                }
            });
            mRvDynamicVideo.addOnScrollListener(new OnRecyclerViewScrollListener(new OnReboundListener() {
                @Override
                public void onRebounding() {

                }

                @Override
                public void onReboundFinish(int position) {
                    try {
                        final JSONArray paths = new JSONArray(videoDynamics.get(position).getPath().replace("\"", ""));
                        final String path = Common.HTTP_ADDRESS + Common.DYNAMIC_VIDEO_PATH + "/" + paths.getString(0);
                        mAdapterVideo.notifyDataSetChanged();
                        smallVideoHelper.setPlayPositionAndTag(position, "TT22");
                        gsySmallVideoHelperBuilder.setVideoTitle("小窗口 " + position)
                                .setUrl(path);
                        smallVideoHelper.startPlay();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
