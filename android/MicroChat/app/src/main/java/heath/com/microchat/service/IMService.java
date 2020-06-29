package heath.com.microchat.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.friend.FriendServiceObserve;
import com.netease.nimlib.sdk.friend.model.AddFriendNotify;
import com.netease.nimlib.sdk.friend.model.Friend;
import com.netease.nimlib.sdk.friend.model.FriendChangedNotify;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.uinfo.UserServiceObserve;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import heath.com.microchat.R;
import heath.com.microchat.TabHostActivity;
import heath.com.microchat.db.MicroChatDB;
import heath.com.microchat.entity.FriendBean;
import heath.com.microchat.friend.AddFriendsActivity;
import heath.com.microchat.friend.FriendsFragment;
import heath.com.microchat.message.MessageFragment;
import heath.com.microchat.provider.MicroChatProvider;
import heath.com.microchat.service.impl.FriendServiceImpl;
import heath.com.microchat.utils.ACache;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ThreadUtils;

public class IMService extends Service {

    private static ACache aCache;
    private IFriendService iFriendService;
    private MicroChatDB mcDB;
    private Gson gson;
    private Handler handler;

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        // 返回server的实例
        public IMService getService() {
            return IMService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        aCache = ACache.get(this);
        iFriendService = new FriendServiceImpl();
        gson = new Gson();
        mcDB = new MicroChatDB(this);
        handler = new IHandler();
        NIMClient.getService(SystemMessageObserver.class).observeReceiveSystemMsg(systemMessageObserver, true);
        NIMClient.getService(MsgServiceObserve.class).observeReceiveMessage(incomingMessageObserver, true);
        NIMClient.getService(MsgServiceObserve.class).observeMsgStatus(statusObserver, true);
        NIMClient.getService(FriendServiceObserve.class).observeFriendChangedNotify(friendChangedNotifyObserver, true);
        NIMClient.getService(UserServiceObserve.class).observeUserInfoUpdate(userInfoUpdateObserver, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NIMClient.getService(SystemMessageObserver.class).observeReceiveSystemMsg(systemMessageObserver, false);
        NIMClient.getService(MsgServiceObserve.class).observeReceiveMessage(incomingMessageObserver, false);
        NIMClient.getService(MsgServiceObserve.class).observeMsgStatus(statusObserver, false);
        NIMClient.getService(FriendServiceObserve.class).observeFriendChangedNotify(friendChangedNotifyObserver, false);
        NIMClient.getService(UserServiceObserve.class).observeUserInfoUpdate(userInfoUpdateObserver, false);
    }

    Observer<SystemMessage> systemMessageObserver = new Observer<SystemMessage>() {
        @Override
        public void onEvent(SystemMessage message) {
            if (message.getType() == SystemMessageType.AddFriend) {
                AddFriendNotify attachData = (AddFriendNotify) message.getAttachObject();
                if (attachData != null) {
                    // 针对不同的事件做处理
                    if (attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_DIRECT) {
                        // 对方直接添加你为好友
                    } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_AGREE_ADD_FRIEND) {
                        // 对方通过了你的好友验证请求
                        String fromAccount = message.getFromAccount();
                        notification("好友申请", fromAccount + "通过你的好友申请了");
                    } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_REJECT_ADD_FRIEND) {
                        // 对方拒绝了你的好友验证请求
                        String fromAccount = message.getFromAccount();
                        notification("好友申请", fromAccount + "拒绝你的好友申请了");
                    } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_VERIFY_REQUEST) {
                        // 对方请求添加好友，一般场景会让用户选择同意或拒绝对方的好友请求。
                        // 通过message.getContent()获取好友验证请求的附言
                        notification("请求添加好友", message.getContent());
                        TabHostActivity.queryReqAddNums();
                        String account = aCache.getAsString("account");
                        FriendsFragment.queryReqAddNums(account);
                    }
                }
            }else if(message.getType() == SystemMessageType.ApplyJoinTeam){
                notification("有人申请入群", message.getContent());
                TabHostActivity.queryReqAddNums();
                String account = aCache.getAsString("account");
                FriendsFragment.queryTeamNoticeNums(account);
            }
        }
    };

    private Observer<List<IMMessage>> incomingMessageObserver =
            new Observer<List<IMMessage>>() {
                @Override
                public void onEvent(List<IMMessage> messages) {
                    // 处理新收到的消息，为了上传处理方便，SDK 保证参数 messages 全部来自同一个聊天对象。
                    for (IMMessage message : messages) {
                        if (message.getMsgType() == MsgTypeEnum.text) {
                            Log.e("Tag", "onEvent: " + message.getContent() + "消息==========================");
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("fromAccount", message.getFromAccount());
                            map.put("content", message.getContent());
                            map.put("lastSendTime", message.getTime());
                            HashMap<String, Object> localMessage = new HashMap<>();
                            localMessage.put("_id", message.getUuid());
                            localMessage.put("type", "text");
                            localMessage.put("state", 0);
                            localMessage.put("sendTime", message.getTime());
                            localMessage.put("duration", (long) 0);
                            localMessage.put("content", message.getContent());
                            localMessage.put("account", message.getFromAccount());
                            if (message.getSessionType() == SessionTypeEnum.P2P) {
                                localMessage.put("session_type", Common.SESSION_TYPE_P2P);
                                localMessage.put("fromAccount", aCache.getAsString("account"));
                            } else if (message.getSessionType() == SessionTypeEnum.Team) {
                                localMessage.put("fromAccount", message.getSessionId());
                                localMessage.put("session_type", Common.SESSION_TYPE_TEAM);
                            }
                            saveMessage(localMessage);
                        } else if (message.getMsgType() == MsgTypeEnum.audio) {
                            Log.e("语音路径打印", "onResult: " + ((AudioAttachment) message.getAttachment()).getPath() + "---=====-----" + ((AudioAttachment) message.getAttachment()).getDuration());
                            String path = ((AudioAttachment) message.getAttachment()).getPath();
                            if (path == null) {
                                path = "";
                            }
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("fromAccount", message.getFromAccount());
                            map.put("content", "[语音]");
                            map.put("lastSendTime", message.getTime());
                            HashMap<String, Object> localMessage = new HashMap<>();
                            localMessage.put("_id", message.getUuid());
                            localMessage.put("type", "voice");
                            localMessage.put("state", 0);
                            localMessage.put("sendTime", message.getTime());
                            localMessage.put("duration", ((AudioAttachment) message.getAttachment()).getDuration());
                            localMessage.put("content", path);
                            localMessage.put("account", message.getFromAccount());
                            if (message.getSessionType() == SessionTypeEnum.P2P) {
                                localMessage.put("session_type", Common.SESSION_TYPE_P2P);
                                localMessage.put("fromAccount", aCache.getAsString("account"));
                            } else if (message.getSessionType() == SessionTypeEnum.Team) {
                                localMessage.put("fromAccount", message.getSessionId());
                                localMessage.put("session_type", Common.SESSION_TYPE_TEAM);
                            }
                            saveMessage(localMessage);
                        } else if (message.getMsgType() == MsgTypeEnum.image) {
                            Log.e("图片路径打印", "onResult: " + ((ImageAttachment) message.getAttachment()).getThumbPath() + "---=====-----");
                            Log.e("Tag", "onEvent: " + message.getContent() + "消息==========================");
                            String path = ((ImageAttachment) message.getAttachment()).getThumbPath();
                            if (path == null) {
                                path = "";
                            }
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("fromAccount", message.getFromAccount());
                            map.put("content", "[图片]");
                            map.put("lastSendTime", message.getTime());
                            HashMap<String, Object> localMessage = new HashMap<>();
                            localMessage.put("_id", message.getUuid());
                            localMessage.put("type", "image");
                            localMessage.put("state", 0);
                            localMessage.put("sendTime", message.getTime());
                            localMessage.put("duration", (long) 0);
                            localMessage.put("content", path);
                            localMessage.put("account", message.getFromAccount());
                            if (message.getSessionType() == SessionTypeEnum.P2P) {
                                localMessage.put("session_type", Common.SESSION_TYPE_P2P);
                                localMessage.put("fromAccount", aCache.getAsString("account"));
                            } else if (message.getSessionType() == SessionTypeEnum.Team) {
                                localMessage.put("fromAccount", message.getSessionId());
                                localMessage.put("session_type", Common.SESSION_TYPE_TEAM);
                            }
                            saveMessage(localMessage);
                        } else if (message.getMsgType() == MsgTypeEnum.video) {
                            Log.e("视频路径打印", "onResult: " + ((VideoAttachment) message.getAttachment()).getPath() + "---=====-----" + ((VideoAttachment) message.getAttachment()).getDuration());
                            String path = ((VideoAttachment) message.getAttachment()).getPath();
                            if (path == null) {
                                path = "";
                            }
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("fromAccount", message.getFromAccount());
                            map.put("content", "[视频]");
                            map.put("lastSendTime", message.getTime());
                            HashMap<String, Object> localMessage = new HashMap<>();
                            localMessage.put("_id", message.getUuid());
                            localMessage.put("type", "video");
                            localMessage.put("state", 0);
                            localMessage.put("sendTime", message.getTime());
                            localMessage.put("duration", ((VideoAttachment) message.getAttachment()).getDuration());
                            localMessage.put("content", path);
                            localMessage.put("thumb", ((VideoAttachment) message.getAttachment()).getThumbPath());
                            localMessage.put("width", ((VideoAttachment) message.getAttachment()).getWidth());
                            localMessage.put("height", ((VideoAttachment) message.getAttachment()).getHeight());
                            localMessage.put("account", message.getFromAccount());
                            if (message.getSessionType() == SessionTypeEnum.P2P) {
                                localMessage.put("session_type", Common.SESSION_TYPE_P2P);
                                localMessage.put("fromAccount", aCache.getAsString("account"));
                            } else if (message.getSessionType() == SessionTypeEnum.Team) {
                                localMessage.put("fromAccount", message.getSessionId());
                                localMessage.put("session_type", Common.SESSION_TYPE_TEAM);
                            }
                            saveMessage(localMessage);
                        }
                    }
                    final TabHostActivity tabHostActivity = new TabHostActivity();
                    ThreadUtils.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            final Cursor cc = getContentResolver().query(MicroChatProvider.URI_ALL_COUNT, null, null, null, null);
                            cc.moveToPosition(0);
                            Log.e("TAG", "run: " + "----------------------" + cc.getString(cc.getColumnIndex("count")));
                            tabHostActivity.updateMessageCount(cc.getString(cc.getColumnIndex("count")));
                        }
                    });
                    MessageFragment.updateData();
                }
            };

    private Observer<IMMessage> statusObserver = new Observer<IMMessage>() {
        @Override
        public void onEvent(IMMessage message) {
            if (message.getDirect() == MsgDirectionEnum.In) {
                if (message.getAttachStatus() == AttachStatusEnum.transferred) {
                    ContentValues values = new ContentValues();
                    if (message.getMsgType() == MsgTypeEnum.audio) {
                        values.put("content", ((AudioAttachment) message.getAttachment()).getPath());
                    } else if (message.getMsgType() == MsgTypeEnum.image) {
                        values.put("content", ((ImageAttachment) message.getAttachment()).getThumbPath());
                        Log.e("图片路径TAG", "run: " + "----------------------" + ((ImageAttachment) message.getAttachment()).getThumbPath());
                    } else if (message.getMsgType() == MsgTypeEnum.video) {
                        String path = ((VideoAttachment) message.getAttachment()).getUrl();
                        Log.i("getUuid00", "bitmap = " + message.getUuid());
                        values.put("content", path);
                        values.put("thumb", ((VideoAttachment) message.getAttachment()).getThumbPath());
                        Log.e("视频路径TAG", "run: " + "----------------------" + path);
                    }
                    if (message.getSessionType() == SessionTypeEnum.P2P) {
                        getContentResolver().update(
                                MicroChatProvider.URI_MESSAGE, values,
                                MicroChatDB.MessageTable.ID + "=? and " + MicroChatDB.MessageTable.ACCOUNT + "=? and " + MicroChatDB.MessageTable.FROM_ACCOUNT + "=?",
                                new String[]{message.getUuid(), message.getFromAccount(), aCache.getAsString("account")});
                    } else if (message.getSessionType() == SessionTypeEnum.Team) {
                        getContentResolver().update(
                                MicroChatProvider.URI_MESSAGE, values,
                                MicroChatDB.MessageTable.ID + "=? and " + MicroChatDB.MessageTable.ACCOUNT + "=? and " + MicroChatDB.MessageTable.FROM_ACCOUNT + "=?",
                                new String[]{message.getUuid(), message.getFromAccount(), message.getSessionId()});
                    }
                    getContentResolver().notifyChange(
                            MicroChatProvider.URI_MESSAGE, null);
                }
            }
        }
    };

    private Observer<FriendChangedNotify> friendChangedNotifyObserver = new Observer<FriendChangedNotify>() {
        @Override
        public void onEvent(FriendChangedNotify friendChangedNotify) {
            List<String> deletedFriendAccounts = friendChangedNotify.getDeletedFriends();
            List<Friend> addedOrUpdatedFriends = friendChangedNotify.getAddedOrUpdatedFriends();
            Log.e("deletedFriendAccounts", "onEvent: " + deletedFriendAccounts);
            for (String account : deletedFriendAccounts) {
                getContentResolver().delete(
                        MicroChatProvider.URI_QUERY_FRIENDS,
                        MicroChatDB.FriendsTable.ACCOUNT + "=? and " + MicroChatDB.FriendsTable.FROM_ACCOUNT + "=?",
                        new String[]{aCache.getAsString("account"), account});
            }
            if (addedOrUpdatedFriends.size() > 0) {
                updateData();
            }
            if (deletedFriendAccounts.size()>0){
                FriendsFragment.updateData(aCache.getAsString("account"));
            }
        }
    };

    private Observer<List<NimUserInfo>> userInfoUpdateObserver = new Observer<List<NimUserInfo>>() {
        @Override
        public void onEvent(List<NimUserInfo> users) {
            for (NimUserInfo userInfo : users) {

            }
        }
    };

    private void loadingFriends() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("account", aCache.getAsString("account"));
                    String result = iFriendService.queryAllFriends(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    List<FriendBean> friendBeans = gson.fromJson(resultObj.getJSONArray("friends").toString(), new TypeToken<List<FriendBean>>() {
                    }.getType());
                    updateFriends(friendBeans);
                    updateUserInfos(friendBeans);
                    ThreadUtils.runInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            FriendsFragment.updateData(aCache.getAsString("account"));
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
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
            loadingFriends();
        }
    }


    private void notification(String type, String content) {
        Notification.Builder builder;
        NotificationManager manager;
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        builder = new Notification.Builder(this);
        Intent intent = new Intent(this, AddFriendsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.logo);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setAutoCancel(true);
        builder.setContentTitle(type);
        builder.setContentText(content);
        manager.notify(1, builder.build());
    }

    public void saveMessage(HashMap<String, Object> map) {
        ContentValues values = new ContentValues();
        values.put(MicroChatDB.MessageTable.ID, (String) map.get("_id"));
        values.put(MicroChatDB.MessageTable.FROM_ACCOUNT, (String) map.get("fromAccount"));
        values.put(MicroChatDB.MessageTable.ACCOUNT, (String) map.get("account"));
        values.put(MicroChatDB.MessageTable.CONTENT, (String) map.get("content"));
        values.put(MicroChatDB.MessageTable.STATE, (int) map.get("state"));
        values.put(MicroChatDB.MessageTable.MESSAGE_TYPE, (String) map.get("type"));
        values.put(MicroChatDB.MessageTable.SEND_TIME, (long) map.get("sendTime"));
        values.put(MicroChatDB.MessageTable.DURATION, (long) map.get("duration"));
        values.put(MicroChatDB.MessageTable.SESSION_TYPE, (String) map.get("session_type"));
        if (map.containsKey("width")) {
            values.put(MicroChatDB.MessageTable.WIDTH, (int) map.get("width"));
        } else {
            values.put(MicroChatDB.MessageTable.WIDTH, 0);
        }
        if (map.containsKey("height")) {
            values.put(MicroChatDB.MessageTable.HEIGHT, (int) map.get("height"));
        } else {
            values.put(MicroChatDB.MessageTable.HEIGHT, 0);
        }
        if (map.containsKey("thumb")) {
            values.put(MicroChatDB.MessageTable.THUMB, (String) map.get("thumb"));
        } else {
            values.put(MicroChatDB.MessageTable.THUMB, 0);
        }
        getContentResolver().insert(MicroChatProvider.URI_MESSAGE, values);
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

}
