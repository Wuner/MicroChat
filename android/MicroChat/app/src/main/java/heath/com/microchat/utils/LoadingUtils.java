package heath.com.microchat.utils;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.ybq.android.spinkit.style.Circle;

import heath.com.microchat.R;

public class LoadingUtils {
    private Activity activity;
    private ProgressBar mPbLoading;
    private RelativeLayout mRlLoading;
    private TextView mTvLoadingText;
    private String mLoadingText;

    public LoadingUtils(Activity activity, String mLoadingText) {
        this.activity = activity;
        this.mPbLoading = activity.findViewById(R.id.pb_loading);
        this.mRlLoading = activity.findViewById(R.id.rl_loading);
        this.mTvLoadingText = activity.findViewById(R.id.tv_loading_text);
        this.mLoadingText = mLoadingText;
    }

    public void creat() {
        Circle circle = new Circle();
        mPbLoading.setIndeterminateDrawable(circle);
        mRlLoading.setVisibility(View.GONE);
        mTvLoadingText.setText(mLoadingText);
    }

    public void show() {
        mRlLoading.setVisibility(View.VISIBLE);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void dismiss() {
        mRlLoading.setVisibility(View.GONE);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void dismissOnUiThread() {


        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRlLoading.setVisibility(View.GONE);
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });

    }

}
