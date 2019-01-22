package heath.com.microchat.message;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.heath.recruit.utils.ThreadUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.GSYVideoHelper;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.adapter.FunctionAdapter;
import heath.com.microchat.adapter.MessageAdapter;
import heath.com.microchat.db.MicroChatDB;
import heath.com.microchat.entity.MessageBean;
import heath.com.microchat.friend.UserDetailedInfoActivity;
import heath.com.microchat.mine.MyInfoActivity;
import heath.com.microchat.provider.MicroChatProvider;
import heath.com.microchat.service.CacheService;
import heath.com.microchat.service.IMService;
import heath.com.microchat.service.IMessageService;
import heath.com.microchat.service.impl.MessageServiceImpl;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ToastUtil;
import heath.com.microchat.utils.UploadServerUtils;

public class SendMessageActivity extends BaseActivity implements View.OnClickListener {

    private TextView mTvNickname;
    public static Button mBtnSend;
    private EditText mEtMessageContent;
    private LinearLayout mLlReturn;
    private ListView mLvMessage;

    private HashMap map;
    private static IMessageService iMessageService;
    private static String account;
    private static String fromAccount;
    private MessageAdapter messageAdapter;

    private static IMService mImService;
    private ImageView mIvKeyboard;
    private ImageView mIvVoice;
    private ImageView mIvBrow;
    private ImageView mIvSendAdd;

    private CacheService cacheService;

    //调用系统相册-选择图片
    private static final int IMAGE = 1;

    private AudioRecorderButton mAudioRecorderButton;
    private Cursor cursor;

    public static GridView mGvFunction;
    private FunctionAdapter functionAdapter;
    private static Context context;
    private GSYVideoHelper smallVideoHelper;
    private GSYVideoHelper.GSYVideoHelperBuilder gsySmallVideoHelperBuilder;
    private int lastVisibleItem;
    private int firstVisibleItem;
    private ImageView mIvUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        initView();
        init();
        initListener();
        initData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaManager.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        smallVideoHelper.releaseVideoPlayer();
        GSYVideoManager.releaseAllVideos();
        unRegisterContentObserver();
        // 解绑服务
        if (mMyServiceConnection != null) {
            unbindService(mMyServiceConnection);
        }
        MediaManager.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NIMClient.getService(MsgService.class).setChattingAccount(fromAccount, SessionTypeEnum.P2P);
        MediaManager.resume();
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
        mTvNickname = this.findViewById(R.id.tv_nickname);
        mBtnSend = this.findViewById(R.id.btn_send);
        mEtMessageContent = this.findViewById(R.id.et_message_content);
        mLlReturn = this.findViewById(R.id.ll_return);
        mLvMessage = this.findViewById(R.id.lv_message);
        mAudioRecorderButton = this.findViewById(R.id.btn_say);
        mGvFunction = this.findViewById(R.id.gv_function);
        iMessageService = new MessageServiceImpl();
        account = aCache.getAsString("account");
        fromAccount = (String) map.get("fromAccount");
        mIvKeyboard = findViewById(R.id.iv_keyboard);
        mIvVoice = findViewById(R.id.iv_voice);
        mIvBrow = findViewById(R.id.iv_brow);
        mIvSendAdd = findViewById(R.id.iv_send_add);
        mIvUserInfo = findViewById(R.id.iv_user_info);
        cacheService = new CacheService();
        context = this;
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
                                messageAdapter.notifyDataSetChanged();
                            }
                        }

                    }
                });

        smallVideoHelper.setGsyVideoOptionBuilder(gsySmallVideoHelperBuilder);
    }

    private void init() {
        registerContentObserver();
        // 绑定服务
        Intent service = new Intent(SendMessageActivity.this, IMService.class);
        bindService(service, mMyServiceConnection, BIND_AUTO_CREATE);
    }

    private void initListener() {
        mBtnSend.setOnClickListener(this);
        mLlReturn.setOnClickListener(this);
        mIvKeyboard.setOnClickListener(this);
        mIvVoice.setOnClickListener(this);
        mIvBrow.setOnClickListener(this);
        mIvSendAdd.setOnClickListener(this);
        mEtMessageContent.setOnClickListener(this);
        mIvUserInfo.setOnClickListener(this);
        mAudioRecorderButton.setAudioFinishRecorderListener(new AudioRecorderButton.AudioFinishRecorderListener() {
            @Override
            public void onFinish(final long seconds, final String filePath) {
                File audioFile = new File(filePath);
                final IMMessage audioMessage = MessageBuilder.createAudioMessage(fromAccount, SessionTypeEnum.P2P, audioFile, seconds);
                NIMClient.getService(MsgService.class).sendMessage(audioMessage, false).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        saveMessage(fromAccount, filePath, "voice", seconds, 0, 0);
                        HashMap<String, Object> localMessage = new HashMap<>();
                        localMessage.put("_id", audioMessage.getUuid());
                        localMessage.put("account", account);
                        localMessage.put("fromAccount", fromAccount);
                        localMessage.put("type", "voice");
                        localMessage.put("state", 1);
                        localMessage.put("sendTime", System.currentTimeMillis());
                        localMessage.put("content", filePath);
                        localMessage.put("duration", seconds);
                        localMessage.put("session_type", Common.SESSION_TYPE_P2P);
                        mImService.saveMessage(localMessage);
                        updateMessage();
                    }

                    @Override
                    public void onFailed(int code) {

                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });
            }
        });
        mEtMessageContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String content = mEtMessageContent.getText().toString();
                if (content.length() >= 1) {
                    mIvSendAdd.setVisibility(View.GONE);
                    mBtnSend.setVisibility(View.VISIBLE);
                } else {
                    mIvSendAdd.setVisibility(View.VISIBLE);
                    mBtnSend.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initData() {
        if (map.containsKey("remarks") && map.get("remarks") != null) {
            mTvNickname.setText((String) map.get("remarks"));
        } else {
            mTvNickname.setText((String) map.get("nickname"));
        }
        setAdapterOrNotify();
        setFunctionAdapter();
    }

    @Override
    public void onClick(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        switch (view.getId()) {
            case R.id.btn_send:
                final String content = mEtMessageContent.getText().toString();
                Log.e("TAG", "onClick: " + map.toString() + "000000----------------------");
                final IMMessage textMessage = MessageBuilder.createTextMessage(fromAccount, SessionTypeEnum.P2P, content);
                NIMClient.getService(MsgService.class).sendMessage(textMessage, false).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        saveMessage(fromAccount, content, "text", 0, 0, 0);
                        HashMap<String, Object> localMessage = new HashMap<>();
                        localMessage.put("_id", textMessage.getUuid());
                        localMessage.put("account", account);
                        localMessage.put("fromAccount", fromAccount);
                        localMessage.put("type", "text");
                        localMessage.put("state", 1);
                        localMessage.put("sendTime", System.currentTimeMillis());
                        localMessage.put("content", content);
                        localMessage.put("duration", (long) 0);
                        localMessage.put("session_type", Common.SESSION_TYPE_P2P);
                        mImService.saveMessage(localMessage);
                        updateMessage();
                    }

                    @Override
                    public void onFailed(int code) {

                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });
                mEtMessageContent.setText("");
                break;
            case R.id.ll_return:
                finish();
                break;
            case R.id.iv_keyboard:
                mGvFunction.setVisibility(View.GONE);
                mIvKeyboard.setVisibility(View.GONE);
                mIvVoice.setVisibility(View.VISIBLE);
                mEtMessageContent.setVisibility(View.VISIBLE);
                mAudioRecorderButton.setVisibility(View.GONE);
                mEtMessageContent.requestFocus();
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                break;
            case R.id.iv_voice:
                mIvKeyboard.setVisibility(View.VISIBLE);
                mIvVoice.setVisibility(View.GONE);
                mEtMessageContent.setVisibility(View.GONE);
                mAudioRecorderButton.setVisibility(View.VISIBLE);
                mEtMessageContent.clearFocus();
                imm.hideSoftInputFromWindow(mEtMessageContent.getWindowToken(), 0);
                break;
            case R.id.iv_brow:
                break;
            case R.id.iv_send_add:
                if (mGvFunction.getVisibility() == View.VISIBLE) {
                    mGvFunction.setVisibility(View.GONE);
                } else {
                    mGvFunction.setVisibility(View.VISIBLE);
                    imm.hideSoftInputFromWindow(mEtMessageContent.getWindowToken(), 0);
                }
                break;
            default:
                mGvFunction.setVisibility(View.GONE);
                break;
            case R.id.iv_user_info:
                Intent intent = new Intent(
                        SendMessageActivity.this,
                        UserDetailedInfoActivity.class);
                intent.putExtra("map", map);
                startActivityForResult(intent, 0);
                break;
        }
    }

    private static void saveMessage(final String fromAccount, final String content, final String messageType, final long duration, final int width, final int height) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject parameterData = new JSONObject();
                    if (!messageType.equals("text")) {
                        final String resultPath = UploadServerUtils.uploadLogFile(Common.HTTP_ADDRESS + "upload/fileUpload.action", content, Common.MESSAGE_PATH);
                        parameterData.put("content", resultPath);
                    }else {
                        parameterData.put("content", content);
                    }
                    parameterData.put("account", account);
                    parameterData.put("fromAccount", fromAccount);
                    parameterData.put("messageType", messageType);
                    parameterData.put("duration", duration);
                    parameterData.put("sessionType", Common.SESSION_TYPE_P2P);
                    parameterData.put("width", width);
                    parameterData.put("height", height);
                    iMessageService.addMessageInfo(parameterData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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
            setAdapterOrNotify();
            super.onChange(selfChange, uri);
        }
    }

    private List<MessageBean> setData(Cursor cursor) {
        List<MessageBean> messageList = new ArrayList<>();
        MessageBean messageBean;
        while (cursor.moveToNext()) {
            messageBean = new MessageBean();
            messageBean.setId(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.MessageTable.ID)));
            messageBean.setAccount(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.MessageTable.ACCOUNT)));
            messageBean.setFromAccount(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.MessageTable.FROM_ACCOUNT)));
            messageBean.setContent(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.MessageTable.CONTENT)));
            messageBean.setSendTime(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.MessageTable.SEND_TIME)));
            messageBean.setState(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.MessageTable.STATE)));
            messageBean.setMessageType(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.MessageTable.MESSAGE_TYPE)));
            messageBean.setDuration(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.MessageTable.DURATION)));
            messageBean.setSessionType(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.MessageTable.SESSION_TYPE)));
            messageBean.setWidth(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.MessageTable.WIDTH)));
            messageBean.setHeight(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.MessageTable.HEIGHT)));
            messageBean.setThumb(cursor.getString(cursor
                    .getColumnIndex(MicroChatDB.MessageTable.THUMB)));
            messageList.add(messageBean);
        }

        return messageList;
    }

    private void setAdapterOrNotify() {
        ContentValues values = new ContentValues();
        values.put("state", "1");
        getContentResolver().update(
                MicroChatProvider.URI_MESSAGE, values,
                MicroChatDB.MessageTable.ACCOUNT + "=? and " + MicroChatDB.MessageTable.FROM_ACCOUNT + "=?",
                new String[]{(String) map.get("fromAccount"), aCache.getAsString("account")});
        if (messageAdapter != null) {
            // 刷新
            cursor.requery();
            List<MessageBean> messageList = setData(cursor);
            messageAdapter.setList(messageList);
            messageAdapter.notifyDataSetChanged();
            mLvMessage.setSelection(cursor.getCount() - 1);
            return;
        }

        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                cursor = getContentResolver()
                        .query(MicroChatProvider.URI_MESSAGE,
                                null,
                                "((from_account = ? and account=?)or(from_account = ? and account= ?)) and session_type='" + Common.SESSION_TYPE_P2P + "'",// where条件
                                new String[]{account, fromAccount,
                                        fromAccount, account},// where条件的参数
                                MicroChatDB.MessageTable.SEND_TIME + " ASC"// 根据时间升序排序
                        );
                final List<MessageBean> messageList = setData(cursor);
                System.out.println(cursor.getCount() + "----");
                if (messageList.size() < 1) {
                    return;
                }
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        messageAdapter = new MessageAdapter(SendMessageActivity.this, cache, messageList, map, aCache, smallVideoHelper, gsySmallVideoHelperBuilder);
                        mLvMessage.setAdapter(messageAdapter);
                        mLvMessage.setSelection(messageAdapter.getCount() - 1);
                        mLvMessage.setOnScrollListener(new AbsListView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(AbsListView view, int scrollState) {
                            }

                            @Override
                            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                                SendMessageActivity.this.firstVisibleItem = firstVisibleItem;
                                lastVisibleItem = firstVisibleItem + visibleItemCount;
                                //大于0说明有播放,//对应的播放列表TAG
                                if (smallVideoHelper.getPlayPosition() >= 0 && smallVideoHelper.getPlayTAG().equals(MessageAdapter.TAG)) {
                                    //当前播放的位置
                                    int position = smallVideoHelper.getPlayPosition();
                                    //不可视的是时候
                                    if ((position < firstVisibleItem || position > lastVisibleItem)) {
                                        //如果是小窗口就不需要处理
                                        if (!smallVideoHelper.isSmall()) {
                                            //小窗口
                                            int size = CommonUtil.dip2px(SendMessageActivity.this, 150);
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
                    }
                });
            }
        });
    }

    MyServiceConnection mMyServiceConnection = new MyServiceConnection();

    class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            System.out
                    .println("--------------onServiceConnected--------------");
            IMService.MyBinder binder = (IMService.MyBinder) service;
            mImService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            System.out
                    .println("--------------onServiceDisconnected--------------");

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            final String imagePath = c.getString(columnIndex);
            final File file = new File(imagePath);
            final IMMessage message = MessageBuilder.createImageMessage(fromAccount, SessionTypeEnum.P2P, file, file.getName());
            NIMClient.getService(MsgService.class).sendMessage(message, false).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void param) {
                    try {
                        Uri uri = cacheService.getImageURI(imagePath, cache);
                        Log.e("URI", "onSuccess: " + uri);
                        saveMessage(fromAccount, imagePath, "image", 0, 0, 0);
                        HashMap<String, Object> localMessage = new HashMap<>();
                        localMessage.put("_id", message.getUuid());
                        localMessage.put("account", account);
                        localMessage.put("fromAccount", fromAccount);
                        localMessage.put("type", "image");
                        localMessage.put("content", uri.toString());
                        localMessage.put("state", 1);
                        localMessage.put("sendTime", System.currentTimeMillis());
                        localMessage.put("duration", (long) 0);
                        localMessage.put("session_type", Common.SESSION_TYPE_P2P);
                        mImService.saveMessage(localMessage);
                        updateMessage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailed(int code) {

                }

                @Override
                public void onException(Throwable exception) {

                }
            });
            c.close();
        }
    }

    private static void updateMessage() {
        MessageFragment.updateData();
    }

    private void setFunctionAdapter() {
        Log.e("屏幕高度", "onSuccess: " + Common.getBottomStatusHeight(this));
        ViewGroup.LayoutParams params = mGvFunction.getLayoutParams();
        if (aCache.getAsString("KeyboardHeight") == null) {
            params.height = 100;
        } else {
            params.height = Integer.parseInt(aCache.getAsString("KeyboardHeight"));
        }
        mGvFunction.setLayoutParams(params);
        final List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("image", R.drawable.album);
        map.put("text", "相册");
        Map<String, Object> map1 = new HashMap<>();
        map1.put("image", R.drawable.camera);
        map1.put("text", "拍摄");
        list.add(map);
        list.add(map1);
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                functionAdapter = new FunctionAdapter(SendMessageActivity.this, list, aCache);
                mGvFunction.setAdapter(functionAdapter);
                mGvFunction.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent;
                        switch (position) {
                            case 0:
                                //调用相册
                                intent = new Intent(Intent.ACTION_PICK,
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(intent, IMAGE);
                                break;
                            case 1:
                                intent = new Intent(
                                        SendMessageActivity.this,
                                        RecordVideoActivity.class);
                                intent.putExtra("sessionType", Common.SESSION_TYPE_P2P);
                                startActivityForResult(intent, 0);
                                break;
                        }
                    }
                });
            }
        });
    }

    public static void sendPhoto(File file, final String path) {
        final IMMessage message = MessageBuilder.createImageMessage(fromAccount, SessionTypeEnum.P2P, file, file.getName());
        NIMClient.getService(MsgService.class).sendMessage(message, false).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                try {
                    saveMessage(fromAccount, path, "image", 0, 0, 0);
                    HashMap<String, Object> localMessage = new HashMap<>();
                    localMessage.put("_id", message.getUuid());
                    localMessage.put("account", aCache.getAsString("account"));
                    localMessage.put("fromAccount", fromAccount);
                    localMessage.put("type", "image");
                    localMessage.put("content", path);
                    localMessage.put("state", 1);
                    localMessage.put("sendTime", System.currentTimeMillis());
                    localMessage.put("duration", (long) 0);
                    localMessage.put("session_type", Common.SESSION_TYPE_P2P);
                    mImService.saveMessage(localMessage);
                    updateMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int code) {
                Log.i("发送失败", "bitmap = " + code);
            }

            @Override
            public void onException(Throwable exception) {
                Log.i("发送异常", "bitmap = " + exception);
            }
        });
    }

    public static void sendVideo(final String url, final String path) {
        File file = new File(url);
        MediaPlayer mediaPlayer = null;
        try {
            mediaPlayer = MediaPlayer.create(context, Uri.fromFile(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 视频文件持续时间
        final long duration = mediaPlayer == null ? 0 : mediaPlayer.getDuration();
        // 视频高度
        final int height = mediaPlayer == null ? 0 : mediaPlayer.getVideoHeight();
        // 视频宽度
        final int width = mediaPlayer == null ? 0 : mediaPlayer.getVideoWidth();
        final IMMessage message = MessageBuilder.createVideoMessage(fromAccount, SessionTypeEnum.P2P, file, duration, width, height, null);
        NIMClient.getService(MsgService.class).sendMessage(message, false).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                try {
                    saveMessage(fromAccount, url, "video", duration, width, height);
                    HashMap<String, Object> localMessage = new HashMap<>();
                    localMessage.put("_id", message.getUuid());
                    Log.i("getUuid", "bitmap = " + message.getUuid());
                    localMessage.put("account", aCache.getAsString("account"));
                    localMessage.put("fromAccount", fromAccount);
                    localMessage.put("type", "video");
                    localMessage.put("content", url);
                    localMessage.put("thumb", path);
                    localMessage.put("state", 1);
                    localMessage.put("sendTime", System.currentTimeMillis());
                    localMessage.put("duration", duration);
                    localMessage.put("session_type", Common.SESSION_TYPE_P2P);
                    localMessage.put("width", width);
                    localMessage.put("height", height);
                    mImService.saveMessage(localMessage);
                    updateMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int code) {
                Log.i("发送失败", "bitmap = " + code);
            }

            @Override
            public void onException(Throwable exception) {
                Log.i("发送异常", "bitmap = " + exception);
            }
        });
    }

}
