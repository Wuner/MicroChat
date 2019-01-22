package heath.com.microchat.mine;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.entity.UserInfo;
import heath.com.microchat.utils.ACache;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;

public class MineFragment extends Fragment implements View.OnClickListener {

    private TextView mTvNickname;
    private TextView mTvAccount;
    private LinearLayout mLlGotoMyInfo;
    private LinearLayout mLlSettings;
    private ImageView mIvIcon;
    private ACache aCache;
    private static Handler handler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container,
                false);
        initView(view);
        init();
        initListener();
        return view;
    }

    private void init() {
        UserInfo userInfo = (UserInfo) aCache.getAsObject("userInfo");
        mTvAccount.setText(userInfo.getAccount());
        mTvNickname.setText(userInfo.getNickname());
        Log.e("TAG", "run: " + Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + userInfo.getIcon() + "0---------------------------");
        if (userInfo.getIcon() != null) {
            ImageUitl imageUitl = new ImageUitl(BaseActivity.cache);
            imageUitl.asyncloadImage(mIvIcon, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + userInfo.getIcon());
        }
    }

    private void initView(View view) {
        mLlGotoMyInfo = view.findViewById(R.id.ll_goto_my_info);
        mTvAccount = view.findViewById(R.id.tv_account);
        mTvNickname = view.findViewById(R.id.tv_nickname);
        mLlSettings = view.findViewById(R.id.ll_settings);
        mIvIcon = view.findViewById(R.id.iv_icon);
        aCache = ACache.get(getActivity());
        handler = new IHandler();

    }

    private void initListener() {
        mLlSettings.setOnClickListener(this);
        mLlGotoMyInfo.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                break;
            case R.id.ll_goto_my_info:
                startActivity(new Intent(getActivity(), MyInfoActivity.class));
                break;
            default:
                break;
        }
    }


    public static void updateData() {
        Message message = new Message();
        Bundle data = new Bundle();
        message.what = 1;
        message.setData(data);
        handler.sendMessage(message);
    }

    private class IHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            init();
        }
    }
}
