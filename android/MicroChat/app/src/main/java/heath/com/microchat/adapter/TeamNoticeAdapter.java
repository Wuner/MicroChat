package heath.com.microchat.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.entity.TeamRelationship;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;

public class TeamNoticeAdapter extends RecyclerView.Adapter<TeamNoticeHolder> {

    private List<TeamRelationship> teamRelationships;
    private Activity context;
    private LayoutInflater mInflater;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnOtherClickListener {
        void onIvIconClick(View view, int position);

        void onTvNicknameClick(View view, int position);

        void onTeamNameClick(View view, int position);

    }

    private OnItemClickListener mOnItemClickListener;
    private OnOtherClickListener mOnOtherClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnOtherClickListener(OnOtherClickListener listener) {
        this.mOnOtherClickListener = listener;
    }

    public TeamNoticeAdapter(Activity context, List<TeamRelationship> teamRelationships) {
        this.context = context;
        this.teamRelationships = teamRelationships;
        mInflater = LayoutInflater.from(context);
    }


    @NonNull
    @Override
    public TeamNoticeHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View convertView = mInflater.inflate(R.layout.item_team_notice, viewGroup, false);
        TeamNoticeHolder viewHolder = new TeamNoticeHolder(convertView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final TeamNoticeHolder holder, int position) {
        ImageUitl imageUitl = new ImageUitl(BaseActivity.cache);
        imageUitl.asyncloadImage(holder.mIvIcon, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + teamRelationships.get(position).getUserInfo().getIcon());
        holder.mTvNickname.setText(teamRelationships.get(position).getUserInfo().getNickname());
        holder.mTvTeamName.setText(teamRelationships.get(position).getTeam().getTname());
        if (teamRelationships.get(position).getType().equals("inviter")) {
            holder.mTvInvite.setVisibility(View.VISIBLE);
            holder.mTvApply.setVisibility(View.GONE);
        } else {
            holder.mTvInvite.setVisibility(View.GONE);
            holder.mTvApply.setVisibility(View.VISIBLE);
        }
        if (teamRelationships.get(position).getReadState().equals("0")){
            holder.mIvSpot.setVisibility(View.VISIBLE);
        }else {
            holder.mIvSpot.setVisibility(View.GONE);
        }
        switch (teamRelationships.get(position).getState()) {
            case "0":
                holder.mTvAgree.setVisibility(View.GONE);
                holder.mTvRefuse.setVisibility(View.GONE);
                holder.mTvUntreated.setVisibility(View.VISIBLE);
                break;
            case "1":
                holder.mTvAgree.setVisibility(View.VISIBLE);
                holder.mTvRefuse.setVisibility(View.GONE);
                holder.mTvUntreated.setVisibility(View.GONE);
                break;
            case "2":
                holder.mTvAgree.setVisibility(View.GONE);
                holder.mTvRefuse.setVisibility(View.VISIBLE);
                holder.mTvUntreated.setVisibility(View.GONE);
                break;
        }
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.itemView, layoutPosition);
                }
            });
        }
        if (mOnOtherClickListener != null) {
            holder.mIvIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = holder.getLayoutPosition();
                    mOnOtherClickListener.onIvIconClick(holder.mIvIcon, layoutPosition);
                }
            });
            holder.mTvNickname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = holder.getLayoutPosition();
                    mOnOtherClickListener.onTvNicknameClick(holder.mTvNickname, layoutPosition);
                }
            });
            holder.mTvTeamName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = holder.getLayoutPosition();
                    mOnOtherClickListener.onTeamNameClick(holder.mTvTeamName, layoutPosition);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return teamRelationships.size();
    }

}

class TeamNoticeHolder extends RecyclerView.ViewHolder {
    ImageView mIvIcon;
    ImageView mIvSpot;
    TextView mTvNickname;
    TextView mTvInvite;
    TextView mTvApply;
    TextView mTvTeamName;
    TextView mTvAgree;
    TextView mTvRefuse;
    TextView mTvUntreated;

    TeamNoticeHolder(@NonNull View itemView) {
        super(itemView);
        this.mIvIcon = itemView.findViewById(R.id.iv_icon);
        this.mIvSpot = itemView.findViewById(R.id.iv_spot);
        this.mTvNickname = itemView.findViewById(R.id.tv_nickname);
        this.mTvInvite = itemView.findViewById(R.id.tv_invite);
        this.mTvApply = itemView.findViewById(R.id.tv_apply);
        this.mTvTeamName = itemView.findViewById(R.id.tv_team_name);
        this.mTvAgree = itemView.findViewById(R.id.tv_agree);
        this.mTvRefuse = itemView.findViewById(R.id.tv_refuse);
        this.mTvUntreated = itemView.findViewById(R.id.tv_untreated);
    }
}
