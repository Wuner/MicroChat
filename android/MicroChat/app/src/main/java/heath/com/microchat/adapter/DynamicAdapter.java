package heath.com.microchat.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shuyu.gsyvideoplayer.utils.GSYVideoHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.entity.CommentReply;
import heath.com.microchat.entity.DynamicBean;
import heath.com.microchat.entity.Praise;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;
import heath.com.microchat.utils.PicturePreviewActivity;
import heath.com.microchat.utils.TimeUtils;

public class DynamicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<DynamicBean> dynamics;
    public static Activity context;
    File cache;
    private LayoutInflater mInflater;
    private int VIDEO = 0;
    private int IMAGE_TEXT = 1;
    private GSYVideoHelper smallVideoHelper;
    private GSYVideoHelper.GSYVideoHelperBuilder gsySmallVideoHelperBuilder;
    public final static String TAG = "TT22";

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    public interface OnOtherClickListener {
        void onPraiseClick(View view, int position);

        void onCommentClick(View view, int position);

        void onSendClick(View view, int position);

        void onHeadPhotoClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener;
    private OnOtherClickListener mOnOtherClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnOtherClickListener(OnOtherClickListener listener) {
        this.mOnOtherClickListener = listener;
    }

    public DynamicAdapter(Activity context, List<DynamicBean> dynamics, File cache, GSYVideoHelper smallVideoHelper, GSYVideoHelper.GSYVideoHelperBuilder gsySmallVideoHelperBuilder) {
        this.context = context;
        this.dynamics = dynamics;
        this.cache = cache;
        mInflater = LayoutInflater.from(context);
        this.smallVideoHelper = smallVideoHelper;
        this.gsySmallVideoHelperBuilder = gsySmallVideoHelperBuilder;
    }

    @Override
    public int getItemViewType(int position) {
        if (dynamics.get(position).getType().equals(Common.DYNAMIC_TYPE_VIDEO)) {
            return VIDEO;
        } else {
            return IMAGE_TEXT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == VIDEO) {
            View convertView = mInflater.inflate(R.layout.item_dynamic_video, viewGroup, false);
            DynamicVideoViewHolder viewHolder = new DynamicVideoViewHolder(convertView);
            return viewHolder;
        } else {
            View convertView = mInflater.inflate(R.layout.item_dynamic_image_text, viewGroup, false);
            DynamicImageTextViewHolder viewHolder = new DynamicImageTextViewHolder(convertView);
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof DynamicVideoViewHolder) {
            final DynamicVideoViewHolder dynamicVideoViewHolder = (DynamicVideoViewHolder) holder;
            ImageUitl imageUitl = new ImageUitl(cache);
            imageUitl.asyncloadImage(dynamicVideoViewHolder.mIvHeadPhoto, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + dynamics.get(position).getUserInfo().getIcon());
            dynamicVideoViewHolder.mTvNickname.setText(dynamics.get(position).getUserInfo().getNickname());
            dynamicVideoViewHolder.mTvPraiseNum.setText(dynamics.get(position).getPraiseNums());
            String commentReplys = dynamics.get(position).getCommentReplys().size() + "";
            dynamicVideoViewHolder.mTvCommentNum.setText(commentReplys);
            String time;
            String release = dynamics.get(position).getReleaseTime();
            try {
                if (TimeUtils.IsToday(release)) {
                    time = release.substring(11, 16);
                } else if (TimeUtils.IsYesterday(release)) {
                    time = "昨天";
                } else if (!TimeUtils.IsToyear(release)) {
                    time = release.substring(0, 10);
                } else {
                    time = release.substring(5, 10);
                }
                dynamicVideoViewHolder.mTvReleaseTime.setText(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            dynamicVideoViewHolder.mTvContent.setText(dynamics.get(position).getContent());
            try {
                final JSONArray paths = new JSONArray(dynamics.get(position).getPath().replace("\"", ""));
                MediaMetadataRetriever retr = new MediaMetadataRetriever();
                final String path = Common.HTTP_ADDRESS + Common.DYNAMIC_VIDEO_PATH + "/" + paths.getString(0);
                retr.setDataSource(path, new HashMap<String, String>());
                Bitmap bitmap = retr.getFrameAtTime();
                ViewGroup.LayoutParams lp = dynamicVideoViewHolder.mRlVideo.getLayoutParams();
                lp.width = bitmap.getWidth();
                lp.height = bitmap.getHeight();
                dynamicVideoViewHolder.mRlVideo.setLayoutParams(lp);
                dynamicVideoViewHolder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                dynamicVideoViewHolder.imageView.setImageBitmap(bitmap);
                smallVideoHelper.addVideoPlayer(position, dynamicVideoViewHolder.imageView, TAG, dynamicVideoViewHolder.mListItemContainer, dynamicVideoViewHolder.mListItemBtn);
                dynamicVideoViewHolder.mListItemBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        notifyDataSetChanged();
                        smallVideoHelper.setPlayPositionAndTag(position, TAG);
                        gsySmallVideoHelperBuilder.setVideoTitle("小窗口 " + position)
                                .setUrl(path);
                        smallVideoHelper.startPlay();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dynamicVideoViewHolder.mIvPraise.setImageDrawable(context.getResources().getDrawable(R.drawable.un_select_praise));
            if (dynamics.get(position).getPraises() != null) {
                for (Praise praise : dynamics.get(position).getPraises()) {
                    if (praise.getAccount().equals(BaseActivity.aCache.getAsString("account"))) {
                        dynamicVideoViewHolder.mIvPraise.setImageDrawable(context.getResources().getDrawable(R.drawable.select_praise));
                        break;
                    }
                }
            }
            List<CommentReply> commentReplies = dynamics.get(position).getCommentReplys();
            if (commentReplies != null) {
                DynamicCommentReplyAdapter mAdapter = new DynamicCommentReplyAdapter(context, commentReplies, BaseActivity.cache);
                dynamicVideoViewHolder.mRvComments.setAdapter(mAdapter);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
                dynamicVideoViewHolder.mRvComments.setLayoutManager(linearLayoutManager);
                mAdapter.setOnNicknameClickListener(new DynamicCommentReplyAdapter.OnNicknameClickListener() {
                    @Override
                    public void onAccountNicknameClick(View view, int position) {
                        TextView content = view.findViewById(R.id.tv_account_nickname);
                        String text = context.getResources().getString(R.string.tv_reply) + " " + content.getText().toString();
                        dynamicVideoViewHolder.mEtContent.setHint(text);
                    }

                    @Override
                    public void onBeAccountNicknameClick(View view, int position) {
                        TextView content = view.findViewById(R.id.tv_be_account_nickname);
                        String text = context.getResources().getString(R.string.tv_reply) + " " + content.getText().toString();
                        dynamicVideoViewHolder.mEtContent.setHint(text);
                    }
                });
            }

            if (mOnItemClickListener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int layoutPosition = holder.getLayoutPosition();
                        mOnItemClickListener.onItemClick(holder.itemView, layoutPosition);
                    }
                });
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int layoutPosition = holder.getLayoutPosition();
                        mOnItemClickListener.onItemLongClick(holder.itemView, layoutPosition);
                        return false;
                    }
                });
            }
            if (mOnOtherClickListener != null) {
                dynamicVideoViewHolder.mIvPraise.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int layoutPosition = holder.getLayoutPosition();
                        mOnOtherClickListener.onPraiseClick(dynamicVideoViewHolder.mIvPraise, layoutPosition);
                    }
                });
                dynamicVideoViewHolder.mIvComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int layoutPosition = holder.getLayoutPosition();
                        mOnOtherClickListener.onCommentClick(dynamicVideoViewHolder.mEtContent, layoutPosition);
                    }
                });
                dynamicVideoViewHolder.mBtnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int layoutPosition = holder.getLayoutPosition();
                        mOnOtherClickListener.onSendClick(dynamicVideoViewHolder.mEtContent, layoutPosition);
                    }
                });
                dynamicVideoViewHolder.mIvHeadPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int layoutPosition = holder.getLayoutPosition();
                        mOnOtherClickListener.onHeadPhotoClick(dynamicVideoViewHolder.mIvHeadPhoto, layoutPosition);
                    }
                });
            }

            dynamicVideoViewHolder.mEtContent.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String content = dynamicVideoViewHolder.mEtContent.getText().toString();
                    if (content.length() >= 2) {
                        dynamicVideoViewHolder.mBtnSend.setBackgroundColor(context.getResources().getColor(R.color.deepskyblue));
                        dynamicVideoViewHolder.mBtnSend.setTextColor(context.getResources().getColor(R.color.white));
                        dynamicVideoViewHolder.mBtnSend.setEnabled(true);
                    } else {
                        dynamicVideoViewHolder.mBtnSend.setBackground(context.getResources().getDrawable(R.drawable.border2));
                        dynamicVideoViewHolder.mBtnSend.setTextColor(context.getResources().getColor(R.color.gainsboro));
                        dynamicVideoViewHolder.mBtnSend.setEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        } else if (holder instanceof DynamicImageTextViewHolder) {
            final DynamicImageTextViewHolder dynamicImageTextViewHolder = (DynamicImageTextViewHolder) holder;
            ImageUitl imageUitl = new ImageUitl(cache);
            imageUitl.asyncloadImage(dynamicImageTextViewHolder.mIvHeadPhoto, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + dynamics.get(position).getUserInfo().getIcon());
            dynamicImageTextViewHolder.mTvNickname.setText(dynamics.get(position).getUserInfo().getNickname());
            dynamicImageTextViewHolder.mTvPraiseNum.setText(dynamics.get(position).getPraiseNums());
            String commentReplys = dynamics.get(position).getCommentReplys().size() + "";
            dynamicImageTextViewHolder.mTvCommentNum.setText(commentReplys);
            String time;
            String release = dynamics.get(position).getReleaseTime();
            try {
                if (TimeUtils.IsToday(release)) {
                    time = release.substring(11, 16);
                } else if (TimeUtils.IsYesterday(release)) {
                    time = "昨天";
                } else if (!TimeUtils.IsToyear(release)) {
                    time = release.substring(0, 10);
                } else {
                    time = release.substring(5, 10);
                }
                dynamicImageTextViewHolder.mTvReleaseTime.setText(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            dynamicImageTextViewHolder.mTvContent.setText(dynamics.get(position).getContent());
            try {
                if (dynamics.get(position).getPath() != null && !dynamics.get(position).getPath().equals("")) {
                    JSONArray paths = new JSONArray(dynamics.get(position).getPath().replace("\"", ""));
                    final List<String> images = new ArrayList<>();
                    for (int i = 0; i < paths.length(); i++) {
                        images.add(paths.get(i).toString());
                    }
                    DynamicImageAdapter imageAdapter = new DynamicImageAdapter(context, images, cache);
                    dynamicImageTextViewHolder.mRvImages.setAdapter(imageAdapter);
                    GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3);
                    dynamicImageTextViewHolder.mRvImages.setLayoutManager(gridLayoutManager);
                    imageAdapter.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Intent intent = new Intent(
                                    context,
                                    PicturePreviewActivity.class);
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("position", position);
                            map.put("url", images.get(position));
                            map.put("type", Common.NETWORK_PICTURE);
                            intent.putExtra("map", map);
                            context.startActivityForResult(intent, 0);
                        }

                        @Override
                        public void onItemLongClick(View view, int position) {

                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dynamicImageTextViewHolder.mIvPraise.setImageDrawable(context.getResources().getDrawable(R.drawable.un_select_praise));
            if (dynamics.get(position).getPraises() != null) {
                for (Praise praise : dynamics.get(position).getPraises()) {
                    if (praise.getAccount().equals(BaseActivity.aCache.getAsString("account"))) {
                        dynamicImageTextViewHolder.mIvPraise.setImageDrawable(context.getResources().getDrawable(R.drawable.select_praise));
                        break;
                    }
                }
            }
            List<CommentReply> commentReplies = dynamics.get(position).getCommentReplys();
            if (commentReplies != null) {
                DynamicCommentReplyAdapter mAdapter = new DynamicCommentReplyAdapter(context, commentReplies, BaseActivity.cache);
                dynamicImageTextViewHolder.mRvComments.setAdapter(mAdapter);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
                dynamicImageTextViewHolder.mRvComments.setLayoutManager(linearLayoutManager);
                mAdapter.setOnNicknameClickListener(new DynamicCommentReplyAdapter.OnNicknameClickListener() {
                    @Override
                    public void onAccountNicknameClick(View view, int position) {
                        TextView content = view.findViewById(R.id.tv_account_nickname);
                        String text = context.getResources().getString(R.string.tv_reply) + " " + content.getText().toString();
                        dynamicImageTextViewHolder.mEtContent.setHint(text);
                    }

                    @Override
                    public void onBeAccountNicknameClick(View view, int position) {
                        TextView content = view.findViewById(R.id.tv_be_account_nickname);
                        String text = context.getResources().getString(R.string.tv_reply) + " " + content.getText().toString();
                        dynamicImageTextViewHolder.mEtContent.setHint(text);
                    }
                });
            }

            if (mOnItemClickListener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int layoutPosition = holder.getLayoutPosition();
                        mOnItemClickListener.onItemClick(holder.itemView, layoutPosition);
                    }
                });
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int layoutPosition = holder.getLayoutPosition();
                        mOnItemClickListener.onItemLongClick(holder.itemView, layoutPosition);
                        return false;
                    }
                });
            }
            if (mOnOtherClickListener != null) {
                dynamicImageTextViewHolder.mIvPraise.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int layoutPosition = holder.getLayoutPosition();
                        mOnOtherClickListener.onPraiseClick(dynamicImageTextViewHolder.mIvPraise, layoutPosition);
                    }
                });
                dynamicImageTextViewHolder.mIvComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int layoutPosition = holder.getLayoutPosition();
                        mOnOtherClickListener.onCommentClick(dynamicImageTextViewHolder.mEtContent, layoutPosition);
                    }
                });
                dynamicImageTextViewHolder.mBtnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int layoutPosition = holder.getLayoutPosition();
                        mOnOtherClickListener.onSendClick(dynamicImageTextViewHolder.mEtContent, layoutPosition);
                    }
                });
                dynamicImageTextViewHolder.mIvHeadPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int layoutPosition = holder.getLayoutPosition();
                        mOnOtherClickListener.onHeadPhotoClick(dynamicImageTextViewHolder.mIvHeadPhoto, layoutPosition);
                    }
                });
            }

            dynamicImageTextViewHolder.mEtContent.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String content = dynamicImageTextViewHolder.mEtContent.getText().toString();
                    if (content.length() >= 2) {
                        dynamicImageTextViewHolder.mBtnSend.setBackgroundColor(context.getResources().getColor(R.color.deepskyblue));
                        dynamicImageTextViewHolder.mBtnSend.setTextColor(context.getResources().getColor(R.color.white));
                        dynamicImageTextViewHolder.mBtnSend.setEnabled(true);
                    } else {
                        dynamicImageTextViewHolder.mBtnSend.setBackground(context.getResources().getDrawable(R.drawable.border2));
                        dynamicImageTextViewHolder.mBtnSend.setTextColor(context.getResources().getColor(R.color.gainsboro));
                        dynamicImageTextViewHolder.mBtnSend.setEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

    }

    public void setData(int position, List<DynamicBean> list) {
        Collections.reverse(list);
        for (DynamicBean dynamicBean : list) {
            dynamics.add(position, dynamicBean);
            notifyItemInserted(position);
        }
    }

    public void setData1(List<DynamicBean> list) {
        dynamics = list;
        notifyDataSetChanged();
    }

    public void updateData(int position, DynamicBean dynamicBean) {
        dynamics.set(position, dynamicBean);
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return dynamics.size();
    }

}

class DynamicImageTextViewHolder extends RecyclerView.ViewHolder {
    ImageView mIvHeadPhoto;
    TextView mTvNickname;
    TextView mTvReleaseTime;
    TextView mTvContent;
    RecyclerView mRvImages;
    ImageView mIvPraise;
    TextView mTvPraiseNum;
    ImageView mIvComment;
    TextView mTvCommentNum;
    RecyclerView mRvComments;
    EditText mEtContent;
    Button mBtnSend;

    DynamicImageTextViewHolder(@NonNull View itemView) {
        super(itemView);
        mIvHeadPhoto = itemView.findViewById(R.id.iv_head_photo);
        mTvNickname = itemView.findViewById(R.id.tv_nickname);
        mTvReleaseTime = itemView.findViewById(R.id.tv_release_time);
        mTvContent = itemView.findViewById(R.id.tv_content);
        mRvImages = itemView.findViewById(R.id.rv_images);
        mIvPraise = itemView.findViewById(R.id.iv_praise);
        mTvPraiseNum = itemView.findViewById(R.id.tv_praise_num);
        mIvComment = itemView.findViewById(R.id.iv_comment);
        mTvCommentNum = itemView.findViewById(R.id.tv_comment_num);
        mRvComments = itemView.findViewById(R.id.rv_comments);
        mEtContent = itemView.findViewById(R.id.et_content);
        mBtnSend = itemView.findViewById(R.id.btn_send);
    }
}

class DynamicVideoViewHolder extends RecyclerView.ViewHolder {
    ImageView mIvHeadPhoto;
    TextView mTvNickname;
    TextView mTvReleaseTime;
    TextView mTvContent;
    RelativeLayout mRlVideo;
    FrameLayout mListItemContainer;
    ImageView mListItemBtn;
    ImageView imageView;
    ImageView mIvPraise;
    TextView mTvPraiseNum;
    ImageView mIvComment;
    TextView mTvCommentNum;
    RecyclerView mRvComments;
    EditText mEtContent;
    Button mBtnSend;

    DynamicVideoViewHolder(@NonNull View itemView) {
        super(itemView);
        mIvHeadPhoto = itemView.findViewById(R.id.iv_head_photo);
        mTvNickname = itemView.findViewById(R.id.tv_nickname);
        mTvReleaseTime = itemView.findViewById(R.id.tv_release_time);
        mTvContent = itemView.findViewById(R.id.tv_content);
        mRlVideo = itemView.findViewById(R.id.rl_video);
        mListItemContainer = itemView.findViewById(R.id.list_item_container);
        mListItemBtn = itemView.findViewById(R.id.list_item_btn);
        imageView = new ImageView(DynamicAdapter.context);
        mIvPraise = itemView.findViewById(R.id.iv_praise);
        mTvPraiseNum = itemView.findViewById(R.id.tv_praise_num);
        mIvComment = itemView.findViewById(R.id.iv_comment);
        mTvCommentNum = itemView.findViewById(R.id.tv_comment_num);
        mRvComments = itemView.findViewById(R.id.rv_comments);
        mEtContent = itemView.findViewById(R.id.et_content);
        mBtnSend = itemView.findViewById(R.id.btn_send);
    }
}