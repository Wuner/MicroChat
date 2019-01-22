package heath.com.microchat.utils;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import java.util.List;
import java.util.Map;

import heath.com.microchat.R;

public class BottomMenu implements OnClickListener, OnTouchListener {

    private PopupWindow popupWindow;
    private View mMenuView;
    private Activity mContext;
    private OnClickListener clickListener;
    private LinearLayout popLayout;

    public BottomMenu(Activity context, OnClickListener clickListener, List<Map<String, Object>> list) {
        LayoutInflater inflater = LayoutInflater.from(context);
        this.clickListener = clickListener;
        mContext = context;
        mMenuView = inflater.inflate(R.layout.custom_popwindow, null);
        popLayout = mMenuView.findViewById(R.id.pop_layout);
        for (Map<String, Object> map : list) {
            Button btn = (Button) LayoutInflater.from(mMenuView.getContext())
                    .inflate(R.layout.item_button, popLayout, false);
            btn.setId((int) map.get("id"));
            btn.setText((String) map.get("text"));
            popLayout.addView(btn, (int) map.get("index"));
            btn.setOnClickListener(this);
        }
        Button btnCancel = mMenuView.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(this);
        popupWindow = new PopupWindow(mMenuView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
        popupWindow.setAnimationStyle(R.style.popwin_anim_style);
        ColorDrawable dw = new ColorDrawable(context.getResources().getColor(R.color.touming));
        popupWindow.setBackgroundDrawable(dw);
        mMenuView.setOnTouchListener(this);
    }

    /**
     * 显示菜单
     */
    public void show() {
        //得到当前activity的rootView
        View rootView = ((ViewGroup) mContext.findViewById(android.R.id.content)).getChildAt(0);
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    @Override
    public void onClick(View view) {
        popupWindow.dismiss();
        switch (view.getId()) {
            case R.id.btn_cancel:
                break;
            default:
                clickListener.onClick(view);
                break;
        }
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        int height = mMenuView.findViewById(R.id.pop_layout).getTop();
        int y = (int) event.getY();
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (y < height) {
                popupWindow.dismiss();
            }
        }
        return true;
    }

    public void setBackground(int color) {
        popLayout.setBackgroundColor(mMenuView.getResources().getColor(color));
    }

}
