package heath.com.microchat.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import heath.com.microchat.R;
import heath.com.microchat.entity.CommentReply;

public class DynamicCommentReplyAdapter extends RecyclerView.Adapter<DynamicCommentReplyViewHolder> {

    private List<CommentReply> commentReplies;
    private Activity context;
    private LayoutInflater mInflater;

    public interface OnNicknameClickListener {
        void onAccountNicknameClick(View view, int position);

        void onBeAccountNicknameClick(View view, int position);
    }

    private OnNicknameClickListener mOnNicknameClickListener;

    public void setOnNicknameClickListener(OnNicknameClickListener listener) {
        this.mOnNicknameClickListener = listener;
    }

    public DynamicCommentReplyAdapter(Activity context, List<CommentReply> commentReplies, File cache) {
        this.context = context;
        this.commentReplies = commentReplies;
        mInflater = LayoutInflater.from(context);
    }


    @NonNull
    @Override
    public DynamicCommentReplyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View convertView = mInflater.inflate(R.layout.item_comment_reply, viewGroup, false);
        DynamicCommentReplyViewHolder viewHolder = new DynamicCommentReplyViewHolder(convertView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final DynamicCommentReplyViewHolder viewHolder, int position) {
        viewHolder.mTvAccountNickname.setText(commentReplies.get(position).getAccountNickname());
        if (commentReplies.get(position).getType().equals("0")){
            viewHolder.mTvReply.setVisibility(View.GONE);
        }else{
            viewHolder.mTvBeAccountNickname.setText(commentReplies.get(position).getBeAccountNickname());
            viewHolder.mTvReply.setVisibility(View.VISIBLE);
        }
        viewHolder.mTvContent.setText(commentReplies.get(position).getContent());
        if (mOnNicknameClickListener != null) {
            viewHolder.mTvAccountNickname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = viewHolder.getLayoutPosition();
                    mOnNicknameClickListener.onAccountNicknameClick( viewHolder.mTvAccountNickname, layoutPosition);
                }
            });
            viewHolder.mTvBeAccountNickname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = viewHolder.getLayoutPosition();
                    mOnNicknameClickListener.onBeAccountNicknameClick(viewHolder.mTvBeAccountNickname, layoutPosition);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return commentReplies.size();
    }
}

class DynamicCommentReplyViewHolder extends RecyclerView.ViewHolder {
    TextView mTvAccountNickname;
    TextView mTvBeAccountNickname;
    TextView mTvReply;
    TextView mTvContent;

    public DynamicCommentReplyViewHolder(@NonNull View itemView) {
        super(itemView);
        mTvAccountNickname = itemView.findViewById(R.id.tv_account_nickname);
        mTvBeAccountNickname = itemView.findViewById(R.id.tv_be_account_nickname);
        mTvReply = itemView.findViewById(R.id.tv_reply);
        mTvContent = itemView.findViewById(R.id.tv_content);
    }
}
