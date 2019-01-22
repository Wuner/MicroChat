package heath.com.microchat.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shuyu.gsyvideoplayer.utils.GSYVideoHelper;

import org.json.JSONArray;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.entity.DynamicBean;
import heath.com.microchat.entity.Praise;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;

public class DynamicVideoAdapter extends RecyclerView.Adapter<DynamicVideosViewHolder> {

    private List<DynamicBean> dynamics;
    public static Activity context;
    private LayoutInflater mInflater;
    private GSYVideoHelper smallVideoHelper;
    private GSYVideoHelper.GSYVideoHelperBuilder gsySmallVideoHelperBuilder;
    public final static String TAG = "TT22";

    public interface OnOtherClickListener {
        void onPraiseClick(View view, int position);

        void onCommentClick(View view, int position);

        void onHeadPhotoClick(View view, int position);
    }

    private OnOtherClickListener mOnOtherClickListener;


    public void setOnOtherClickListener(OnOtherClickListener listener) {
        this.mOnOtherClickListener = listener;
    }

    public DynamicVideoAdapter(Activity context, List<DynamicBean> dynamics, GSYVideoHelper smallVideoHelper, GSYVideoHelper.GSYVideoHelperBuilder gsySmallVideoHelperBuilder) {
        this.context = context;
        this.dynamics = dynamics;
        mInflater = LayoutInflater.from(context);
        this.smallVideoHelper = smallVideoHelper;
        this.gsySmallVideoHelperBuilder = gsySmallVideoHelperBuilder;
    }


    @NonNull
    @Override
    public DynamicVideosViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View convertView = mInflater.inflate(R.layout.item_dynamic_videos, viewGroup, false);
        DynamicVideosViewHolder viewHolder = new DynamicVideosViewHolder(convertView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final DynamicVideosViewHolder viewHolder, final int position) {
        try {
            ImageUitl imageUitl = new ImageUitl(BaseActivity.cache);
            imageUitl.asyncloadImage(viewHolder.mIvHeadPhoto, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + dynamics.get(position).getUserInfo().getIcon());
            viewHolder.mIvPraise.setImageDrawable(context.getResources().getDrawable(R.drawable.un_select_praise));
            if (dynamics.get(position).getPraises() != null) {
                for (Praise praise : dynamics.get(position).getPraises()) {
                    if (praise.getAccount().equals(BaseActivity.aCache.getAsString("account"))) {
                        viewHolder.mIvPraise.setImageDrawable(context.getResources().getDrawable(R.drawable.select_praise));
                        break;
                    }
                }
            }
            viewHolder.mTvNickname.setText(dynamics.get(position).getUserInfo().getNickname());
            viewHolder.mTvPraiseNum.setText(dynamics.get(position).getPraiseNums());
            String commentReplys = dynamics.get(position).getCommentReplys().size() + "";
            viewHolder.mTvCommentNum.setText(commentReplys);
            viewHolder.mTvContent.setText(dynamics.get(position).getContent());
            MediaMetadataRetriever retr = new MediaMetadataRetriever();
            final JSONArray paths = new JSONArray(dynamics.get(position).getPath().replace("\"", ""));
            final String path = Common.HTTP_ADDRESS + Common.DYNAMIC_VIDEO_PATH + "/" + paths.getString(0);
            retr.setDataSource(path, new HashMap<String, String>());
            Bitmap bitmap = retr.getFrameAtTime();
            viewHolder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            viewHolder.imageView.setImageBitmap(bitmap);
            smallVideoHelper.addVideoPlayer(position, viewHolder.imageView, TAG, viewHolder.mListItemContainer, viewHolder.mListItemBtn);
            viewHolder.mListItemBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifyDataSetChanged();
                    smallVideoHelper.setPlayPositionAndTag(position, TAG);
                    gsySmallVideoHelperBuilder.setVideoTitle("小窗口 " + position)
                            .setUrl(path);
                    smallVideoHelper.startPlay();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mOnOtherClickListener != null) {
            viewHolder.mIvPraise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = viewHolder.getLayoutPosition();
                    mOnOtherClickListener.onPraiseClick(viewHolder.mIvPraise, layoutPosition);
                }
            });
            viewHolder.mIvComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = viewHolder.getLayoutPosition();
                    mOnOtherClickListener.onCommentClick(viewHolder.mIvComment, layoutPosition);
                }
            });
            viewHolder.mIvHeadPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = viewHolder.getLayoutPosition();
                    mOnOtherClickListener.onHeadPhotoClick(viewHolder.mIvHeadPhoto, layoutPosition);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return dynamics.size();
    }

    public void setData(int position, List<DynamicBean> list) {
        Collections.reverse(list);
        for (DynamicBean dynamicBean : list) {
            dynamics.add(position, dynamicBean);
            notifyItemInserted(position);
        }
    }

    public void updateData(int position, DynamicBean dynamicBean) {
        dynamics.set(position, dynamicBean);
        notifyItemChanged(position);
    }

}

class DynamicVideosViewHolder extends RecyclerView.ViewHolder {
    FrameLayout mListItemContainer;
    ImageView mListItemBtn;
    RelativeLayout mRlVideo;
    ImageView mIvHeadPhoto;
    ImageView mIvPraise;
    ImageView imageView;
    TextView mTvPraiseNum;
    ImageView mIvComment;
    TextView mTvCommentNum;
    TextView mTvNickname;
    TextView mTvContent;

    public DynamicVideosViewHolder(@NonNull View view) {
        super(view);
        this.mListItemContainer = view.findViewById(R.id.list_item_container);
        this.mListItemBtn = view.findViewById(R.id.list_item_btn);
        this.mRlVideo = view.findViewById(R.id.rl_video);
        this.mIvHeadPhoto = view.findViewById(R.id.iv_head_photo);
        this.mIvPraise =  view.findViewById(R.id.iv_praise);
        this.mTvPraiseNum =  view.findViewById(R.id.tv_praise_num);
        this.mIvComment = view.findViewById(R.id.iv_comment);
        this.mTvCommentNum = view.findViewById(R.id.tv_comment_num);
        this.mTvNickname = view.findViewById(R.id.tv_nickname);
        this.mTvContent = view.findViewById(R.id.tv_content);
        imageView = new ImageView(DynamicVideoAdapter.context);
    }
}
