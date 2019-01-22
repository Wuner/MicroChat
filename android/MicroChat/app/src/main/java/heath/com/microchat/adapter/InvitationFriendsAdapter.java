package heath.com.microchat.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.Map;

import heath.com.microchat.R;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;

public class InvitationFriendsAdapter extends BaseAdapter{

    private List<Map<String, Object>> listdata;
    private Context context;
    private ViewHolder holder;
    private File cache;

    public InvitationFriendsAdapter(Context context, List<Map<String, Object>> listdata, File cache){
        this.context = context;
        this.listdata = listdata;
        this.cache = cache;
    }

    @Override
    public int getCount() {
        return listdata.size();
    }

    @Override
    public Object getItem(int position) {
        return listdata.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = View.inflate(context, R.layout.item_invitation_friend,null);
            holder = new ViewHolder();
            holder.mIvHeadPhoto = convertView.findViewById(R.id.iv_head_photo);
            holder.mTvNickname = convertView.findViewById(R.id.tv_nickname);
            holder.mCbSelect = convertView.findViewById(R.id.cb_select);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        if(listdata.get(position).containsKey("icon")){
            ImageUitl imageUitl = new ImageUitl(cache);
            imageUitl.asyncloadImage(holder.mIvHeadPhoto, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH+"/" + listdata.get(position).get("icon"));
        }
        if(listdata.get(position).containsKey("remarks")){
            holder.mTvNickname.setText((String) listdata.get(position).get("remarks"));
        }else{
            if(listdata.get(position).containsKey("nickname")){
                holder.mTvNickname.setText((String) listdata.get(position).get("nickname"));
            }
        }
        return convertView;
    }

    class ViewHolder {
        ImageView mIvHeadPhoto;
        TextView mTvNickname;
        CheckBox mCbSelect;
    }
}
