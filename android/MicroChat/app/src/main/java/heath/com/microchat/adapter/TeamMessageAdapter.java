package heath.com.microchat.adapter;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shuyu.gsyvideoplayer.utils.GSYVideoHelper;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import heath.com.microchat.R;
import heath.com.microchat.db.MicroChatDB;
import heath.com.microchat.entity.MessageBean;
import heath.com.microchat.entity.UserInfo;
import heath.com.microchat.message.MediaManager;
import heath.com.microchat.provider.MicroChatProvider;
import heath.com.microchat.utils.ACache;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;
import heath.com.microchat.utils.TimeUtils;

public class TeamMessageAdapter extends BaseAdapter {

    private Activity context;
    private List<MessageBean> messageList;
    private final int SEND_TEXT = 0;
    private final int SEND_IMAGE = 1;
    private final int RECEIVE_TEXT = 2;
    private final int RECEIVE_IMAGE = 3;
    private final int RECEIVE_VOICE = 4;
    private final int SEND_VOICE = 5;
    private final int RECEIVE_VIDEO = 6;
    private final int SEND_VIDEO = 7;
    private String fromAccount;
    private UserInfo userInfo;
    private DisplayMetrics dm;
    private int width;
    private ImageUitl imageUitl;
    private int mMinItemWidth;
    private int mMxxItemWidth;
    private View recorderAnim;
    final int[] adj = {R.drawable.adj_right1};
    private GSYVideoHelper smallVideoHelper;
    private GSYVideoHelper.GSYVideoHelperBuilder gsySmallVideoHelperBuilder;
    public final static String TAG = "TT22";

    public TeamMessageAdapter(Activity context, File cache, List<MessageBean> messageList, ACache aCache,GSYVideoHelper smallVideoHelper, GSYVideoHelper.GSYVideoHelperBuilder gsySmallVideoHelperBuilder) {
        this.context = context;
        this.messageList = messageList;
        this.fromAccount = aCache.getAsString("account");
        userInfo = (UserInfo) aCache.getAsObject("userInfo");

        dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        imageUitl = new ImageUitl(cache);

        mMinItemWidth = (int) (dm.widthPixels * 0.15f);
        mMxxItemWidth = (int) (dm.widthPixels * 0.7f);
        this.smallVideoHelper = smallVideoHelper;
        this.gsySmallVideoHelperBuilder = gsySmallVideoHelperBuilder;
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        String fromAccount1 = messageList.get(position).getAccount();
        String type = messageList.get(position).getMessageType();
        if (fromAccount.equals(fromAccount1)) {
            switch (type) {
                case "text":
                    return SEND_TEXT;
                case "image":
                    return SEND_IMAGE;
                case "voice":
                    return SEND_VOICE;
                default:
                    return SEND_VIDEO;
            }
        } else {
            switch (type) {
                case "text":
                    return RECEIVE_TEXT;
                case "image":
                    return RECEIVE_IMAGE;
                case "voice":
                    return RECEIVE_VOICE;
                default:
                    return RECEIVE_VIDEO;
            }
        }
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 7;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        switch (getItemViewType(position)) {
            case RECEIVE_TEXT:
                if (convertView == null) {
                    convertView = View.inflate(
                            context,
                            R.layout.item_chat_receive_text,
                            null);
                    holder = new ViewHolder();

                    // 给holder赋值、
                    holder.body = convertView
                            .findViewById(R.id.content);
                    holder.time = convertView
                            .findViewById(R.id.time);
                    holder.head = convertView
                            .findViewById(R.id.head);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView
                            .getTag();
                }
                break;
            case RECEIVE_IMAGE:
                if (convertView == null) {
                    convertView = View.inflate(
                            context,
                            R.layout.item_chat_receive_image,
                            null);
                    holder = new ViewHolder();

                    // 给holder赋值、
                    holder.time = convertView
                            .findViewById(R.id.time);
                    holder.head = convertView
                            .findViewById(R.id.head);
                    holder.bodyImg = convertView
                            .findViewById(R.id.iv_content_img);
                    ViewGroup.LayoutParams lp = holder.bodyImg.getLayoutParams();
                    lp.width = width / 4;
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    holder.bodyImg.setLayoutParams(lp);
                    holder.bodyImg.setMaxWidth(width / 4);
                    holder.bodyImg.setMaxHeight(width * 5);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView
                            .getTag();
                }
                break;
            case RECEIVE_VOICE:
                if (convertView == null) {
                    convertView = View.inflate(
                            context,
                            R.layout.item_chat_receive_voice,
                            null);
                    holder = new ViewHolder();

                    // 给holder赋值、
                    holder.time = convertView
                            .findViewById(R.id.time);
                    holder.head = convertView
                            .findViewById(R.id.head);
                    holder.recorderTime = convertView
                            .findViewById(R.id.tv_recorder_time);
                    holder.recorderLength = convertView
                            .findViewById(R.id.fl_recorder_length);
                    ViewGroup.LayoutParams lp = holder.recorderLength.getLayoutParams();
                    long temp = Long.parseLong(messageList.get(position).getDuration());
                    lp.width = (int) (mMinItemWidth + (mMxxItemWidth / 60f * temp / 1000));
                    holder.recorderLength.setLayoutParams(lp);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView
                            .getTag();
                }
                break;
            case RECEIVE_VIDEO:
                if (convertView == null) {
                    convertView = View.inflate(
                            context,
                            R.layout.item_chat_receive_video,
                            null);
                    holder = new TeamMessageAdapter.ViewHolder();

                    // 给holder赋值、
                    holder.time = convertView
                            .findViewById(R.id.time);
                    holder.head = convertView
                            .findViewById(R.id.head);
                    holder.videoContainer = convertView.findViewById(R.id.list_item_container);
                    holder.playerBtn = convertView.findViewById(R.id.list_item_btn);
                    holder.imageView = new ImageView(context);
                    holder.rlVideo = convertView.findViewById(R.id.rl_video);
                    ViewGroup.LayoutParams lp = holder.rlVideo.getLayoutParams();
                    lp.width = Integer.parseInt(messageList.get(position).getWidth());
                    lp.height = Integer.parseInt(messageList.get(position).getHeight());
                    holder.rlVideo.setLayoutParams(lp);
                    convertView.setTag(holder);
                } else {
                    holder = (TeamMessageAdapter.ViewHolder) convertView
                            .getTag();
                }
                break;
            case SEND_TEXT:
                if (convertView == null) {
                    convertView = View.inflate(
                            context,
                            R.layout.item_chat_send_text, null);
                    holder = new ViewHolder();


                    // 给holder赋值、
                    holder.body = convertView
                            .findViewById(R.id.content);
                    holder.time = convertView
                            .findViewById(R.id.time);
                    holder.head = convertView
                            .findViewById(R.id.head);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView
                            .getTag();
                }
                break;
            case SEND_IMAGE:
                if (convertView == null) {
                    convertView = View.inflate(
                            context,
                            R.layout.item_chat_send_image,
                            null);
                    holder = new ViewHolder();

                    // 给holder赋值、
                    holder.time = convertView
                            .findViewById(R.id.time);
                    holder.head = convertView
                            .findViewById(R.id.head);
                    holder.bodyImg = convertView
                            .findViewById(R.id.iv_content_img);
                    ViewGroup.LayoutParams lp = holder.bodyImg.getLayoutParams();
                    lp.width = width / 4;
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    holder.bodyImg.setLayoutParams(lp);
                    holder.bodyImg.setMaxWidth(width / 4);
                    holder.bodyImg.setMaxHeight(width * 5);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView
                            .getTag();
                }
                break;
            case SEND_VOICE:
                if (convertView == null) {
                    convertView = View.inflate(
                            context,
                            R.layout.item_chat_send_voice,
                            null);
                    holder = new ViewHolder();

                    // 给holder赋值、
                    holder.time = convertView
                            .findViewById(R.id.time);
                    holder.head = convertView
                            .findViewById(R.id.head);
                    holder.recorderTime = convertView
                            .findViewById(R.id.tv_recorder_time);
                    holder.recorderLength = convertView
                            .findViewById(R.id.fl_recorder_length);
                    ViewGroup.LayoutParams lp = holder.recorderLength.getLayoutParams();
                    long temp = Long.parseLong(messageList.get(position).getDuration());
                    lp.width = (int) (mMinItemWidth + (mMxxItemWidth / 60f * temp / 1000));
                    holder.recorderLength.setLayoutParams(lp);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView
                            .getTag();
                }
                break;
            case SEND_VIDEO:
                if (convertView == null) {
                    convertView = View.inflate(
                            context,
                            R.layout.item_chat_send_video,
                            null);
                    holder = new TeamMessageAdapter.ViewHolder();

                    // 给holder赋值、
                    holder.time = convertView
                            .findViewById(R.id.time);
                    holder.head = convertView
                            .findViewById(R.id.head);
                    holder.videoContainer = convertView.findViewById(R.id.list_item_container);
                    holder.playerBtn = convertView.findViewById(R.id.list_item_btn);
                    holder.imageView = new ImageView(context);
                    holder.rlVideo = convertView.findViewById(R.id.rl_video);
                    ViewGroup.LayoutParams lp = holder.rlVideo.getLayoutParams();
                    lp.width = Integer.parseInt(messageList.get(position).getWidth());
                    lp.height = Integer.parseInt(messageList.get(position).getHeight());
                    holder.rlVideo.setLayoutParams(lp);
                    convertView.setTag(holder);
                } else {
                    holder = (TeamMessageAdapter.ViewHolder) convertView
                            .getTag();
                }
                break;
            default:
                holder = (ViewHolder) convertView
                        .getTag();
                break;
        }

        // 得到数据，展示数据
        final String body = messageList.get(position).getContent();
        String fromAccount1 = messageList.get(position).getAccount();
        String type = messageList.get(position).getMessageType();
        long SendTime = Long.parseLong(messageList.get(position).getSendTime());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(SendTime);
        String lastSendTime = simpleDateFormat.format(date);
        String time;
        try {
            if (TimeUtils.IsToday(lastSendTime)) {
                time = lastSendTime.substring(11, 16);
            } else if (TimeUtils.IsYesterday(lastSendTime)) {
                time = "昨天";
            } else if (!TimeUtils.IsToyear(lastSendTime)) {
                time = lastSendTime.substring(0, 10);
            } else {
                time = lastSendTime.substring(5, 10);
            }
            holder.time.setText(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        imageUitl.asyncloadImage(holder.head, imagePath(fromAccount1));
        switch (type) {
            case "text":
                holder.body.setText(body);
                break;
            case "image":
                Log.e("URI", "onSuccess: " + body);
                holder.bodyImg.setImageURI(Uri.parse(body));
                break;
            case "voice":
                int duration = (int) (Long.parseLong(messageList.get(position).getDuration()) / 1000);
                Log.e("消息状态=====", "onSuccess: " + messageList.get(position).getState());
                if (!fromAccount1.equals(fromAccount)) {
                    holder.recorderTime.setText(duration + "\"");
                    holder.recorderLength.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (recorderAnim != null) {
                                recorderAnim.setBackgroundResource(adj[0]);
                                recorderAnim = null;
                            }
                            adj[0] = R.drawable.adj_right1;
                            recorderAnim = v.findViewById(R.id.v_recorder_anim);
                            //播放语音动画
                            recorderAnim.setBackgroundResource(R.drawable.play_anim_right);
                            AnimationDrawable anim = (AnimationDrawable) recorderAnim.getBackground();
                            anim.start();
                            //播放音频
                            String filePath = messageList.get(position).getContent();
                            MediaManager.playSound(filePath, new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    recorderAnim.setBackgroundResource(R.drawable.adj_right1);
                                }
                            });
                        }
                    });
                } else {
                    holder.recorderTime.setText(duration + "\"");
                    holder.recorderLength.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (recorderAnim != null) {
                                recorderAnim.setBackgroundResource(adj[0]);
                                recorderAnim = null;
                            }
                            adj[0] = R.drawable.adj;
                            recorderAnim = v.findViewById(R.id.v_recorder_anim);
                            //播放语音动画
                            recorderAnim.setBackgroundResource(R.drawable.play_anim);
                            AnimationDrawable anim = (AnimationDrawable) recorderAnim.getBackground();
                            anim.start();
                            //播放音频
                            String filePath = messageList.get(position).getContent();
                            MediaManager.playSound(filePath, new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    recorderAnim.setBackgroundResource(R.drawable.adj);
                                }
                            });
                        }
                    });
                }
                break;
            case "video":
                Log.e("videoURI", "onSuccess: " + body);
                Log.e("getThumb", "onSuccess: " + messageList.get(position).getThumb());
                //增加封面
                holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Bitmap bitmap = BitmapFactory.decodeFile(messageList.get(position).getThumb());
                holder.imageView.setImageBitmap(bitmap);
                smallVideoHelper.addVideoPlayer(position, holder.imageView, TAG, holder.videoContainer, holder.playerBtn);
                holder.playerBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        notifyDataSetChanged();
                        smallVideoHelper.setPlayPositionAndTag(position, TAG);
                        gsySmallVideoHelperBuilder.setVideoTitle("小窗口 " + position)
                                .setUrl(body);
                        smallVideoHelper.startPlay();
                    }
                });
                break;
        }

        return convertView;
    }


    public void setList(List<MessageBean> messageList) {
        this.messageList = messageList;
    }

    private String imagePath(String fromAccount1) {
        if (!fromAccount1.equals(fromAccount)) {
            Cursor cursor = context.getContentResolver().query(MicroChatProvider.URI_QUERY_USERINFO, null, null, new String[]{fromAccount1}, null);
            UserInfo userInfo = setData(cursor);
            if (userInfo.getIcon()!=null) {
                return Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + userInfo.getIcon();
            }
        } else {
            if (userInfo.getIcon() != null && userInfo.getIcon().length() > 0) {
                return Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + userInfo.getIcon();
            }
        }
        return null;
    }

    private UserInfo setData(Cursor cursor) {
        UserInfo userInfo = new UserInfo();
        cursor.moveToPosition(0);
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

        return userInfo;
    }

    class ViewHolder {
        TextView time;
        TextView body;
        ImageView head;
        ImageView bodyImg;
        TextView recorderTime;
        View recorderLength;
        FrameLayout videoContainer;
        ImageView playerBtn;
        ImageView imageView;
        RelativeLayout rlVideo;
    }
}
