package heath.com.microchat.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import heath.com.microchat.R;
import heath.com.microchat.entity.UserInfo;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;

public class MemberAdapter extends BaseAdapter {

    private List<UserInfo> userInfos;
    private Activity context;
    private ViewHolder holder;
    private File cache;

    public MemberAdapter(Activity context, List<UserInfo> userInfos, File cache) {
        this.context = context;
        this.userInfos = userInfos;
        this.cache = cache;
        holder = null;
    }

    @Override
    public int getCount() {
        return userInfos.size()+1;
    }

    @Override
    public Object getItem(int position) {
        return userInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_member, null);
            holder = new ViewHolder();
            holder.mIvIcon = convertView.findViewById(R.id.iv_icon);
            holder.mTvNickName = convertView.findViewById(R.id.tv_nickname);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            if (position==userInfos.size()){
                holder.mIvIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.add_team_icon));
            }else{
                ImageUitl imageUitl = new ImageUitl(cache);
                imageUitl.asyncloadImage(holder.mIvIcon, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + userInfos.get(position).getIcon());
                holder.mTvNickName.setText(userInfos.get(position).getNickname());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 1;
    }

    class ViewHolder {
        ImageView mIvIcon;
        TextView mTvNickName;
    }

}
