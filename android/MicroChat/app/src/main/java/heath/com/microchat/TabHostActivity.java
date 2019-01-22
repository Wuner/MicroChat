package heath.com.microchat;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flyco.animation.BaseAnimatorSet;
import com.flyco.animation.BounceEnter.BounceTopEnter;
import com.flyco.animation.SlideExit.SlideBottomExit;
import com.flyco.dialog.entity.DialogMenuItem;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.NormalListDialog;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;

import heath.com.microchat.dynamic.DynamicFragment;
import heath.com.microchat.friend.AddFriendsActivity;
import heath.com.microchat.friend.FriendsFragment;
import heath.com.microchat.message.MessageFragment;
import heath.com.microchat.mine.MineFragment;
import heath.com.microchat.provider.MicroChatProvider;
import heath.com.microchat.service.IFriendService;
import heath.com.microchat.service.ITeamService;
import heath.com.microchat.service.impl.FriendServiceImpl;
import heath.com.microchat.service.impl.TeamServiceImpl;
import heath.com.microchat.team.AddTeamActivity;
import heath.com.microchat.team.CreateTeamActivity;
import heath.com.microchat.utils.ACache;
import heath.com.microchat.utils.LoadingUtils;

public class TabHostActivity extends BaseActivity implements View.OnClickListener {
    /**
     * 用于展示消息的Fragment
     */
    private MessageFragment mMessageFragment;
    private FriendsFragment mFriendsFragment;
    private DynamicFragment mDynamicFragment;
    private MineFragment mMineFragment;
    private View mMessageLayout;
    private View mFriendsLayout;
    private View mDynamicLayout;
    private View mMineLayout;
    private ImageView mIvMessage;
    private ImageView mIvFriends;
    private ImageView mIvDynamic;
    private ImageView mIvMine;
    private TextView mTvMessage;
    private TextView mTvFriends;
    private TextView mTvDynamic;
    private TextView mTvMine;
    private TextView mTvReqAddFriendNums;
    private TextView mTvMessageCount;

    private TextView mTvTitle;
    private ImageView mIvAdd;
    public static LoadingUtils loadingUtils;


    private long mExitTime = 0;

    private static IFriendService friendService;
    private static ITeamService iTeamService;
    private static ACache aCache;
    private static Handler handler;
    private BaseAnimatorSet mBasIn;
    private BaseAnimatorSet mBasOut;
    private ArrayList<DialogMenuItem> mMenuItems = new ArrayList<>();
    public static Activity context;

    /**
     * 用于对Fragment进行管理
     */
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabhost);
        registerContentObserver();
        open();
        // 初始化布局元素
        initViews();
        initListener();
        fragmentManager = getFragmentManager();
        // 第一次启动时选中第0个tab
        setTabSelection(0);
        queryReqAddNums();
        queryMessageCount();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterContentObserver();
    }

    /**
     * 在这里获取到每个需要用到的控件的实例
     */
    private void initViews() {
        mMessageLayout = this.findViewById(R.id.message_layout);
        mFriendsLayout = this.findViewById(R.id.friends_layout);
        mDynamicLayout = this.findViewById(R.id.dynamic_layout);
        mMineLayout = this.findViewById(R.id.mine_layout);

        mIvMessage = this.findViewById(R.id.iv_message);
        mIvFriends = this.findViewById(R.id.iv_friends);
        mIvDynamic = this.findViewById(R.id.iv_dynamic);
        mIvMine = this.findViewById(R.id.iv_mine);

        mTvMessage = this.findViewById(R.id.tv_message);
        mTvFriends = this.findViewById(R.id.tv_friends);
        mTvDynamic = this.findViewById(R.id.tv_dynamic);
        mTvMine = this.findViewById(R.id.tv_mine);
        mTvReqAddFriendNums = this.findViewById(R.id.tv_req_add_friend_nums);
        mTvMessageCount = this.findViewById(R.id.tv_message_count);

        mTvTitle = this.findViewById(R.id.tv_title);
        mIvAdd = this.findViewById(R.id.iv_add);

        aCache = ACache.get(this);
        handler = new IHandler();
        friendService = new FriendServiceImpl();
        iTeamService = new TeamServiceImpl();
        loadingUtils = new LoadingUtils(TabHostActivity.this, "努力加载中");
        loadingUtils.creat();
        mBasIn = new BounceTopEnter();
        mBasOut = new SlideBottomExit();
        mMenuItems.add(new DialogMenuItem(getResources().getString(R.string.tv_add_friend), R.drawable.new_friends));
        mMenuItems.add(new DialogMenuItem(getResources().getString(R.string.tv_create_team), R.drawable.team));
        mMenuItems.add(new DialogMenuItem(getResources().getString(R.string.tv_add_team), R.drawable.team));
        context = this;
    }

    /*给它们设置好必要的点击事件。*/
    private void initListener() {
        mMessageLayout.setOnClickListener(this);
        mFriendsLayout.setOnClickListener(this);
        mDynamicLayout.setOnClickListener(this);
        mMineLayout.setOnClickListener(this);
        mIvAdd.setOnClickListener(this);
        mIvAdd.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.message_layout:
                setTabSelection(0);
                break;
            case R.id.friends_layout:
                // 当点击了消息tab时，选中第1个tab
                setTabSelection(1);
                break;
            case R.id.dynamic_layout:
                setTabSelection(2);
                break;
            case R.id.mine_layout:
                setTabSelection(3);
                break;
            case R.id.iv_add:
                NormalListDialog();
            default:
                break;
        }
    }

    private void NormalListDialog() {
        final NormalListDialog dialog = new NormalListDialog(this, mMenuItems);
        dialog.title("请选择")//
                .showAnim(mBasIn)//
                .dismissAnim(mBasOut)//
                .show();
        dialog.setOnOperItemClickL(new OnOperItemClickL() {
            @Override
            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                switch (position) {
                    case 0:
                        startActivity(new Intent(TabHostActivity.this, AddFriendsActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(TabHostActivity.this, CreateTeamActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(TabHostActivity.this, AddTeamActivity.class));
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 根据传入的index参数来设置选中的tab页。
     *
     * @param index 每个tab页对应的下标。0表示消息，1表示联系人，2表示动态，3表示设置。
     */
    private void setTabSelection(int index) {
        // 每次选中之前先清楚掉上次的选中状态
        clearSelection();
        // 开启一个Fragment事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
        hideFragments(transaction);
        Resources resource = getBaseContext().getResources();
        ColorStateList csl = resource.getColorStateList(R.color.deepskyblue);
        switch (index) {
            case 0:
                mIvMessage.setImageResource(R.drawable.select_message);
                mTvMessage.setTextColor(csl);
                mTvTitle.setText(getString(R.string.tv_message));
                if (mMessageFragment == null) {
                    mMessageFragment = new MessageFragment();
                    transaction.add(R.id.content, mMessageFragment);
                } else {
                    transaction.show(mMessageFragment);
                }
                NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_ALL, SessionTypeEnum.None);
                break;
            case 1:
                mIvFriends.setImageResource(R.drawable.select_friends);
                mTvFriends.setTextColor(csl);
                mTvTitle.setText(getString(R.string.tv_friend));
                if (mFriendsFragment == null) {
                    mFriendsFragment = new FriendsFragment();
                    transaction.add(R.id.content, mFriendsFragment);
                } else {
                    transaction.show(mFriendsFragment);
                }
                NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None);
                break;
            case 2:
                mIvDynamic.setImageResource(R.drawable.select_dynamic);
                mTvDynamic.setTextColor(csl);
                mTvTitle.setText(getString(R.string.tv_dynamic));
                if (mDynamicFragment == null) {
                    mDynamicFragment = new DynamicFragment();
                    transaction.add(R.id.content, mDynamicFragment);
                } else {
                    transaction.show(mDynamicFragment);
                }
                NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None);
                break;
            case 3:
                mIvMine.setImageResource(R.drawable.select_mine);
                mTvMine.setTextColor(csl);
                mTvTitle.setText(getString(R.string.tv_mine));
                if (mMineFragment == null) {
                    mMineFragment = new MineFragment();
                    transaction.add(R.id.content, mMineFragment);
                } else {
                    transaction.show(mMineFragment);
                }
                NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None);
                break;

            default:
                break;
        }
        transaction.commit();
    }

    /**
     * 清除掉所有的选中状态。
     */
    private void clearSelection() {
        mIvMessage.setImageResource(R.drawable.unselect_message);
        mTvMessage.setTextColor(Color.parseColor("#DCDCDC"));
        mIvFriends.setImageResource(R.drawable.unselect_friends);
        mTvFriends.setTextColor(Color.parseColor("#DCDCDC"));
        mIvDynamic.setImageResource(R.drawable.unselect_dynamic);
        mTvDynamic.setTextColor(Color.parseColor("#DCDCDC"));
        mIvMine.setImageResource(R.drawable.unselect_mine);
        mTvMine.setTextColor(Color.parseColor("#DCDCDC"));
    }

    /**
     * 将所有的Fragment都置为隐藏状态。
     *
     * @param transaction 用于对Fragment执行操作的事务
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (mMessageFragment != null) {
            transaction.hide(mMessageFragment);
        }
        if (mFriendsFragment != null) {
            transaction.hide(mFriendsFragment);
        }
        if (mDynamicFragment != null) {
            transaction.hide(mDynamicFragment);
        }
        if (mMineFragment != null) {
            transaction.hide(mMineFragment);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - mExitTime > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {

                for (int i = 0; i < activityList.size(); i++) {
                    if (null != activityList.get(i)) {
                        activityList.get(i).finish();
                    }
                }
                System.exit(0);// 否则退出程序
            }
            return true;
        }
        return true;
    }

    public static void queryReqAddNums() {
        final String account = aCache.getAsString("account");
        com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject textData = new JSONObject();
                    textData.put("account", account);
                    String results = friendService.queryReqAddNums(textData);
                    String results1 = iTeamService.queryTeamRelationshipNoticeNumByAccount(textData);
                    final JSONObject resultObj = new JSONObject(results);
                    final JSONObject resultObj1 = new JSONObject(results1);
                    if (resultObj.getString("code").equals("200")) {
                        if (resultObj1.getString("code").equals("200")) {
                            Message message = new Message();
                            Bundle data = new Bundle();
                            int num = Integer.parseInt(resultObj.getString("count")) + Integer.parseInt(resultObj1.getString("num"));
                            data.putSerializable("count", num + "");
                            message.what = 0;
                            message.setData(data);
                            handler.sendMessage(message);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void queryMessageCount() {
        com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                final Cursor cc = context.getContentResolver().query(MicroChatProvider.URI_ALL_COUNT, null, null, null, null);
                cc.moveToPosition(0);
                Log.e("TAG", "run: " + "----------------------" + cc.getString(cc.getColumnIndex("count")));
                Message message = new Message();
                Bundle data = new Bundle();
                data.putSerializable("count", cc.getString(cc.getColumnIndex("count")));
                message.what = 1;
                message.setData(data);
                handler.sendMessage(message);
            }
        });
    }

    public void updateMessageCount(String count) {
        Message message = new Message();
        Bundle data = new Bundle();
        data.putSerializable("count", count);
        message.what = 1;
        message.setData(data);
        handler.sendMessage(message);
    }

    private class IHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int flag = msg.what;
            int count = Integer.parseInt((String) msg.getData().getSerializable("count"));
            switch (flag) {
                case 0:
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
                case 1:
                    if (count <= 0) {
                        mTvMessageCount.setVisibility(View.GONE);
                    } else if (count > 99) {
                        mTvMessageCount.setVisibility(View.VISIBLE);
                        mTvMessageCount.setText("99+");
                    } else {
                        mTvMessageCount.setVisibility(View.VISIBLE);
                        mTvMessageCount.setText(count + "");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    MycontentObserver mMyContentObserver = new MycontentObserver(new Handler());

    /**
     * 注册监听
     */
    public void registerContentObserver() {
        getContentResolver().registerContentObserver(MicroChatProvider.URI_MESSAGE, true,
                mMyContentObserver);
    }

    /**
     * 反注册监听
     */
    public void unRegisterContentObserver() {
        getContentResolver().unregisterContentObserver(mMyContentObserver);
    }

    class MycontentObserver extends ContentObserver {

        public MycontentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            queryMessageCount();
            super.onChange(selfChange, uri);
        }

    }

    private void open() {
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.CAMERA};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                }
            }
        }
    }

    private void updateMessage() {
        MessageFragment.updateData();
    }
}
