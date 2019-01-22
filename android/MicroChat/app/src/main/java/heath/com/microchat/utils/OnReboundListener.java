package heath.com.microchat.utils;

import org.json.JSONException;

public interface OnReboundListener {

    /**
     * 正在滑动回调
     */
    void onRebounding();

    /**
     * 反弹结束回调
     *
     * @param position 静止的位置
     */
    void onReboundFinish(int position);

}