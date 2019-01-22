package heath.com.microchat.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nimlib.sdk.team.model.Team;

import java.util.List;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;

public class TeamAdapter extends RecyclerView.Adapter<TeamHolder> {

    private List<Team> teams;
    private Activity context;
    private LayoutInflater mInflater;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public TeamAdapter(Activity context, List<Team> teams) {
        this.context = context;
        this.teams = teams;
        mInflater = LayoutInflater.from(context);
    }


    @NonNull
    @Override
    public TeamHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View convertView = mInflater.inflate(R.layout.item_team, viewGroup, false);
        TeamHolder viewHolder = new TeamHolder(convertView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final TeamHolder holder, int position) {
        ImageUitl imageUitl = new ImageUitl(BaseActivity.cache);
        imageUitl.asyncloadImage(holder.mIvTeamIcon, Common.HTTP_ADDRESS + Common.TEAM_FOLDER_PATH + "/" + teams.get(position).getIcon());
        holder.mTvTeamName.setText(teams.get(position).getName());
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.itemView, layoutPosition);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

}

class TeamHolder extends RecyclerView.ViewHolder {
    ImageView mIvTeamIcon;
    TextView mTvTeamName;

    TeamHolder(@NonNull View itemView) {
        super(itemView);
        mIvTeamIcon = itemView.findViewById(R.id.iv_team_icon);
        mTvTeamName = itemView.findViewById(R.id.tv_team_name);
    }
}
