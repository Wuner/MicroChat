package heath.com.microchat.friend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;

public class AddFriendResultActivity extends BaseActivity implements View.OnClickListener {

    private ImageView mIvResult;
    private LinearLayout mLlReturn;
    private TextView mTvResult;
    private HashMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend_result);
        initView();
        initListener();
        init();
    }

    private void initView() {
        mLlReturn = this.findViewById(R.id.ll_return);
        mIvResult = this.findViewById(R.id.iv_result);
        mTvResult = this.findViewById(R.id.tv_result);
        Intent intent = getIntent();
        map = (HashMap) intent.getSerializableExtra("map");
    }

    private void init() {
        int result = (int) map.get("result");
        if(result==1){
            mIvResult.setImageDrawable(getResources().getDrawable(R.drawable.ok));
            mTvResult.setText(getResources().getString(R.string.tv_result_ok));
        }else{
            mIvResult.setImageDrawable(getResources().getDrawable(R.drawable.error));
            mTvResult.setText(getResources().getString(R.string.tv_result_error));
        }
    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_return:
                finish();
                break;
            default:
                break;
        }
    }
}
