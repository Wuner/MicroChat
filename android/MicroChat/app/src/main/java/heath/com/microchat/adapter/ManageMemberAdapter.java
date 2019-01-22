package heath.com.microchat.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import heath.com.microchat.R;
import heath.com.microchat.entity.MessageBean;
import heath.com.microchat.entity.UserInfo;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;

public class ManageMemberAdapter extends BaseAdapter{

    private List<UserInfo> listdata;
    private Context context;
    private ViewHolder holder;
    private File cache;

    public ManageMemberAdapter(Context context, List<UserInfo> listdata, File cache){
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
            convertView = View.inflate(context, R.layout.item_friend,null);
            holder = new ViewHolder();
            holder.mIvHeadPhoto = convertView.findViewById(R.id.iv_head_photo);
            holder.mTvNickname = convertView.findViewById(R.id.tv_nickname);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        if(listdata.get(position).getIcon()!=null){
            ImageUitl imageUitl = new ImageUitl(cache);
            imageUitl.asyncloadImage(holder.mIvHeadPhoto, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH+"/" + listdata.get(position).getIcon());
        }
        if(listdata.get(position).getNickname()!=null){
            holder.mTvNickname.setText(listdata.get(position).getNickname());
        }
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 1;
    }

    public void setList(List<UserInfo> listdata) {
        this.listdata = listdata;
    }

    class ViewHolder {
        ImageView mIvHeadPhoto;
        TextView mTvNickname;
    }
}
