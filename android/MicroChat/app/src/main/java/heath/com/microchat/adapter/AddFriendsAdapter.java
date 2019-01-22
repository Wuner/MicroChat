package heath.com.microchat.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import heath.com.microchat.R;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;

public class AddFriendsAdapter extends BaseAdapter{

    private ArrayList<Map<String, Object>> listdata;
    private Context context;
    private ViewHolder holder;
    private File cache;

    public AddFriendsAdapter(Context context, ArrayList<Map<String, Object>> listdata,File cache){
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
            convertView = View.inflate(context, R.layout.item_add_friend,null);
            holder = new ViewHolder();
            holder.mIvHeadPhoto = convertView.findViewById(R.id.iv_head_photo);
            holder.mIvSex = convertView.findViewById(R.id.iv_sex);
            holder.mTvAccount = convertView.findViewById(R.id.tv_account);
            holder.mTvAge = convertView.findViewById(R.id.tv_age);
            holder.mTvSign = convertView.findViewById(R.id.tv_sign);
            holder.mTvNickname = convertView.findViewById(R.id.tv_nickname);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        if(listdata.get(position).containsKey("icon")){
            ImageUitl imageUitl = new ImageUitl(cache);
            imageUitl.asyncloadImage(holder.mIvHeadPhoto, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH+"/" + listdata.get(position).get("icon").toString());
        }
        if(listdata.get(position).containsKey("gender")){
            ImageUitl.setImageBitmap(listdata.get(position).get("gender").toString(),holder.mIvSex);
        }
        if(listdata.get(position).containsKey("account")){
            holder.mTvAccount.setText("("+listdata.get(position).get("account").toString()+")");
        }
        if(listdata.get(position).containsKey("nickname")){
            holder.mTvNickname.setText(listdata.get(position).get("nickname").toString());
        }
        if(listdata.get(position).containsKey("sign")){
            holder.mTvSign.setText(listdata.get(position).get("sign").toString());
        }
        if(listdata.get(position).containsKey("age")){
            holder.mTvAge.setText(listdata.get(position).get("age").toString());
        }
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 1;
    }

    class ViewHolder {
        ImageView mIvHeadPhoto;
        ImageView mIvSex;
        TextView mTvNickname;
        TextView mTvAccount;
        TextView mTvAge;
        TextView mTvSign;
    }

}
