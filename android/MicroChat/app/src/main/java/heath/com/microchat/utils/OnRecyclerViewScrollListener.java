package heath.com.microchat.utils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cjt2325.cameralibrary.util.LogUtil;

public class OnRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

    private OnReboundListener onReboundListener;

    public OnRecyclerViewScrollListener(OnReboundListener onReboundListener) {
        this.onReboundListener = onReboundListener;
    }

    /**
     * 滑动中回调
     *
     * @param recyclerView 控件对象
     * @param dx           [距离][X轴]
     * @param dy           [距离][Y轴][正数代表向下][负数代表向上]
     */
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        LogUtil.e("onScrolled : " + "dx = " + dx + "dy = " + dy);
    }

    /**
     * 滑动状态发生改变的时候回调
     *
     * @param recyclerView 控件对象
     * @param newState     [状态值][每次滑动时候三个状态都会回调一次][手指拖动(惯性滑动)静止]
     */
    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        LogUtil.e("onScrolled : " + "newState = " + newState);
        switch (newState) {
            case RecyclerView.SCROLL_STATE_IDLE:// 静止
                LogUtil.e("静止");
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {// 只有LinearLayoutManager才有查找第一个和最后一个可见view位置的方法
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                    int totalItemCount = layoutManager.getItemCount();// 当前RecyclerView的所有子项个数
                    int firstItemPosition = linearLayoutManager.findFirstVisibleItemPosition();// 获取第一个可见view的位置
                    int lastItemPosition = linearLayoutManager.findLastVisibleItemPosition();// 获取最后一个可见view的位置
                    View childView = linearLayoutManager.findViewByPosition(firstItemPosition);// 获取显示的第一个 View
                    int childViewHeight = childView.getHeight();
                    int childViewTop = childView.getTop();
                    if (Math.abs(childViewTop) == recyclerView.getPaddingTop() || Math.abs(childViewTop) == recyclerView.getPaddingTop() * 2) {
                        if (onReboundListener != null) {// 反弹结束
                            onReboundListener.onReboundFinish(firstItemPosition);
                        }
                    } else if (Math.abs(childViewTop) >= childViewHeight / 2.0f) {// 定位下一个
                        int nextPosition = firstItemPosition + 1;
                        smoothMoveToPosition(recyclerView, nextPosition);
                    } else {// 定位当前
                        recyclerView.smoothScrollToPosition(firstItemPosition);
                    }

                    LogUtil.e("childViewHeight = " + childViewHeight);
                    LogUtil.e("totalItemCount = " + totalItemCount);
                    LogUtil.e("firstItemPosition = " + firstItemPosition);
                    LogUtil.e("lastItemPosition = " + lastItemPosition);
                    LogUtil.e("childViewGetTop = " + childView.getTop());
                    LogUtil.e("childViewGetY = " + childView.getY());
                    LogUtil.e("childViewGetPaddingTop = " + childView.getPaddingTop());
                    LogUtil.e("PaddingTop = " + Common.dp2px(recyclerView.getContext(), 10.0f));
                    LogUtil.e("Padding = " + recyclerView.getPaddingTop());
                }
                break;
            case RecyclerView.SCROLL_STATE_DRAGGING:// 手指拖动
                LogUtil.e("手指拖动");
                if (onReboundListener != null) {
                    onReboundListener.onRebounding();
                }
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:// 惯性滑动
                LogUtil.e("惯性滑动");
                break;
        }
    }

    /**
     * 缓慢移动到指定的位置
     *
     * @param position
     */
    private void smoothMoveToPosition(RecyclerView recyclerView, int position) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {// 只有LinearLayoutManager才有查找第一个和最后一个可见view位置的方法
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            int firstItem = linearLayoutManager.findFirstVisibleItemPosition();// 先从RecyclerView的LayoutManager中获取第一项和最后一项的Position
            int lastItem = linearLayoutManager.findLastVisibleItemPosition();
            if (position <= firstItem) {// 当要置顶的项在当前显示的第一个项的前面时
                recyclerView.smoothScrollToPosition(position);
            } else if (position <= lastItem) {// 当要置顶的项已经在屏幕上显示时
                int top = recyclerView.getChildAt(position - firstItem).getTop();
                recyclerView.smoothScrollBy(0, top);
            } else {// 当要置顶的项在当前显示的最后一项的后面时
                recyclerView.smoothScrollToPosition(position);
            }
        }
    }

}