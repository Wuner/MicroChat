package heath.com.microchat.adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import heath.com.microchat.R;
import heath.com.microchat.TabHostActivity;
import heath.com.microchat.friend.FriendsFragment;
import heath.com.microchat.friend.NewFriendsActivity;
import heath.com.microchat.service.IFriendService;
import heath.com.microchat.service.impl.FriendServiceImpl;
import heath.com.microchat.utils.ACache;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;
import heath.com.microchat.utils.ToastUtil;

public class FriendsNoticeAdapter extends BaseAdapter {

    private ArrayList<Map<String, Object>> listdata;
    private Context context;
    private ViewHolder holder;
    private IFriendService friendService = new FriendServiceImpl();
    private Handler handler = new IHandler();
    private static ACache aCache;
    private File cache;

    public FriendsNoticeAdapter(Context context, ArrayList<Map<String, Object>> listdata, File cache) {
        this.context = context;
        this.listdata = listdata;
        this.cache = cache;
        aCache = ACache.get(context);
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
    public int getItemViewType(int position) {
        String state = (String) listdata.get(position).get("state");
        switch (state) {
            case "0":
                return 0;
            case "1":
                return 1;
            default:
                return 2;
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_friends_notice_processed, null);
            holder = new ViewHolder();
            holder.mIvHeadPhoto = convertView.findViewById(R.id.iv_head_photo);
            holder.mIvSex = convertView.findViewById(R.id.iv_sex);
            holder.mTvAccount = convertView.findViewById(R.id.tv_account);
            holder.mTvAge = convertView.findViewById(R.id.tv_age);
            holder.mTvContent = convertView.findViewById(R.id.tv_content);
            holder.mTvNickname = convertView.findViewById(R.id.tv_nickname);
            holder.mTvState = convertView.findViewById(R.id.tv_state);
            holder.mBtnAgree = convertView.findViewById(R.id.btn_agree);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (listdata.get(position).containsKey("icon")) {
            ImageUitl imageUitl = new ImageUitl(cache);
            imageUitl.asyncloadImage(holder.mIvHeadPhoto, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + listdata.get(position).get("icon").toString());
        }
        if (listdata.get(position).containsKey("gender")) {
            ImageUitl.setImageBitmap(listdata.get(position).get("gender").toString(), holder.mIvSex);
        }
        if (listdata.get(position).containsKey("fromAccount")){
            if (listdata.get(position).containsKey("fromAccount")) {
                holder.mTvAccount.setText("(" + listdata.get(position).get("fromAccount").toString() + ")");
            }
        }else{
            if (listdata.get(position).containsKey("account")) {
                holder.mTvAccount.setText("(" + listdata.get(position).get("account").toString() + ")");
            }
        }
        if (listdata.get(position).containsKey("nickname")) {
            holder.mTvNickname.setText(listdata.get(position).get("nickname").toString());
        }
        if (listdata.get(position).containsKey("state")) {
            if (getItemViewType(position) == 1) {
                holder.mTvState.setText(context.getResources().getString(R.string.tv_agree));
                holder.mTvState.setTextColor(context.getResources().getColor(R.color.green));
                holder.mTvState.setVisibility(View.VISIBLE);
                holder.mBtnAgree.setVisibility(View.GONE);
            } else if (getItemViewType(position) == 2) {
                holder.mTvState.setText(context.getResources().getString(R.string.tv_refuse));
                holder.mTvState.setTextColor(context.getResources().getColor(R.color.red));
                holder.mTvState.setVisibility(View.VISIBLE);
                holder.mBtnAgree.setVisibility(View.GONE);
            } else {
                holder.mTvState.setVisibility(View.GONE);
                holder.mBtnAgree.setVisibility(View.VISIBLE);
                holder.mBtnAgree.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        NewFriendsActivity.loadingUtils.show();
                        NIMClient.getService(FriendService.class).ackAddFriendRequest(listdata.get(position).get("fromAccount").toString(), true).setCallback(new RequestCallback<Void>() {
                            @Override
                            public void onSuccess(Void param) {
                                NewFriendsActivity.loadingUtils.dismiss();
                                listdata.get(position).put("state", "1");
                                modifyState(listdata.get(position).get("fromAccount").toString(), "1");
                            }

                            @Override
                            public void onFailed(int code) {
                                NewFriendsActivity.loadingUtils.dismiss();
                                System.out.print(1);
                            }

                            @Override
                            public void onException(Throwable exception) {
                                NewFriendsActivity.loadingUtils.dismiss();
                            }
                        });
                    }
                });
            }
        }
        if (listdata.get(position).containsKey("content")) {
            holder.mTvContent.setText(listdata.get(position).get("content").toString());
        }
        if (listdata.get(position).containsKey("age")) {
            holder.mTvAge.setText(listdata.get(position).get("age").toString());
        }
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 2;
    }

    class ViewHolder {
        ImageView mIvHeadPhoto;
        ImageView mIvSex;
        TextView mTvNickname;
        TextView mTvAccount;
        TextView mTvAge;
        TextView mTvContent;
        TextView mTvState;
        Button mBtnAgree;
    }

    private void modifyState(final String fromAccount, final String state) {
        com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String account = aCache.getAsString("account");
                    JSONObject data = new JSONObject();
                    data.put("account", account);
                    data.put("fromAccount", fromAccount);
                    data.put("state", state);
                    String result = friendService.modifyFriendRelationshipState(data);
                    Log.e("TAG", "run: " + result + "==============================");
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.getString("code").equals("200")) {
                        FriendsFragment friendsFragment = new FriendsFragment();
                        friendsFragment.updateData(aCache.getAsString("account"));
                        TabHostActivity.queryReqAddNums();
                        FriendsFragment.queryReqAddNums(account);
                        Message message = new Message();
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    Log.e("TAG", "onSuccess: ", e);
                }
            }
        });
    }

    private class IHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            holder.mBtnAgree.setText(context.getResources().getString(R.string.tv_agree));
            holder.mBtnAgree.setBackgroundColor(context.getResources().getColor(R.color.white));
            holder.mBtnAgree.setTextColor(context.getResources().getColor(R.color.green));
            holder.mBtnAgree.setEnabled(false);
        }
    }
}
