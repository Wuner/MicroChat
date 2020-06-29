package heath.com.microchat.adapter;

import android.app.Activity;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import heath.com.microchat.R;
import heath.com.microchat.entity.TeamBean;
import heath.com.microchat.provider.MicroChatProvider;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;
import heath.com.microchat.utils.ThreadUtils;
import heath.com.microchat.utils.TimeUtils;

public class RecentSessionAdapter extends BaseAdapter {

    private List<Map<String, Object>> listdata;
    private Activity context;
    private ViewHolder holder;
    private File cache;

    public RecentSessionAdapter(Activity context, List<Map<String, Object>> listdata, File cache) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_recent_session, null);
            holder = new ViewHolder();
            holder.mIvHeadPhoto = convertView.findViewById(R.id.iv_head_photo);
            holder.mTvNickname = convertView.findViewById(R.id.tv_nickname);
            holder.mTvContent = convertView.findViewById(R.id.tv_content);
            holder.mTvLastSendTime = convertView.findViewById(R.id.tv_last_send_time);
            holder.mTvMessageCount = convertView.findViewById(R.id.tv_message_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (listdata.get(position).containsKey("lastSendTime")) {
            long lastSendTimeMs = (long) listdata.get(position).get("lastSendTime");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(lastSendTimeMs);
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
                holder.mTvLastSendTime.setText(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (listdata.get(position).containsKey("content")) {
            holder.mTvContent.setText(listdata.get(position).get("content").toString());
        }
        if (Common.SESSION_TYPE_P2P.equals(listdata.get(position).get("sessionType").toString())){
           ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    final Cursor cc;
                    try {
                        cc = context.getContentResolver().query(MicroChatProvider.URI_COUNT,null,null,new String[]{listdata.get(position).get("account").toString(),listdata.get(position).get("fromAccount").toString()},null);
                        cc.moveToPosition(0);
                        int messageCount = Integer.parseInt(cc.getString(cc.getColumnIndex("count")));
                        if (messageCount <= 0) {
                            holder.mTvMessageCount.setVisibility(View.GONE);
                        } else if (messageCount > 99) {
                            holder.mTvMessageCount.setVisibility(View.VISIBLE);
                            holder.mTvMessageCount.setText("99+");
                        } else {
                            holder.mTvMessageCount.setVisibility(View.VISIBLE);
                            holder.mTvMessageCount.setText(messageCount + "");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            if (listdata.get(position).containsKey("icon")) {
                ImageUitl imageUitl = new ImageUitl(cache);
                imageUitl.asyncloadImage(holder.mIvHeadPhoto, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + listdata.get(position).get("icon").toString());
            }
            if (listdata.get(position).containsKey("remarks")) {
                holder.mTvNickname.setText(listdata.get(position).get("remarks").toString());
            } else {
                if (listdata.get(position).containsKey("nickname")) {
                    holder.mTvNickname.setText(listdata.get(position).get("nickname").toString());
                }
            }
        }else if(Common.SESSION_TYPE_TEAM.equals(listdata.get(position).get("sessionType").toString())){
            final TeamBean teamBean = (TeamBean) listdata.get(position).get("team");
            ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    final Cursor cc;
                    try {
                        cc = context.getContentResolver().query(MicroChatProvider.URI_TEAM_COUNT,null,null,new String[]{teamBean.getTid(),teamBean.getTid()},null);
                        cc.moveToPosition(0);
                        int messageCount = Integer.parseInt(cc.getString(cc.getColumnIndex("count")));
                        if (messageCount <= 0) {
                            holder.mTvMessageCount.setVisibility(View.GONE);
                        } else if (messageCount > 99) {
                            holder.mTvMessageCount.setVisibility(View.VISIBLE);
                            holder.mTvMessageCount.setText("99+");
                        } else {
                            holder.mTvMessageCount.setVisibility(View.VISIBLE);
                            holder.mTvMessageCount.setText(messageCount + "");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            if (teamBean.getIcon()!=null) {
                ImageUitl imageUitl = new ImageUitl(cache);
                imageUitl.asyncloadImage(holder.mIvHeadPhoto, Common.HTTP_ADDRESS + Common.TEAM_FOLDER_PATH + "/" + teamBean.getIcon());
            }
            if (teamBean.getTname()!=null) {
                holder.mTvNickname.setText(teamBean.getTname());
            }
        }
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 1;
    }

    public void setList(List<Map<String, Object>> listdata) {
        this.listdata = listdata;
    }

    class ViewHolder {
        ImageView mIvHeadPhoto;
        TextView mTvNickname;
        TextView mTvContent;
        TextView mTvLastSendTime;
        TextView mTvMessageCount;
    }

}