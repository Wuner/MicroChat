package heath.com.microchat.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import heath.com.microchat.R;
import heath.com.microchat.utils.ACache;

public class FunctionAdapter extends BaseAdapter {

    private List<Map<String, Object>> listdata;
    private Activity context;
    private ViewHolder holder;
    private ACache aCache;

    public FunctionAdapter(Activity context, List<Map<String, Object>> listdata, ACache aCache) {
        this.context = context;
        this.listdata = listdata;
        this.aCache = aCache;
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
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_function, null);
            holder = new ViewHolder();
            holder.mIvFunction = convertView.findViewById(R.id.iv_function);
            holder.mTvFunction = convertView.findViewById(R.id.tv_function);
            holder.mRlFunction = convertView.findViewById(R.id.rl_function);
            ViewGroup.LayoutParams params = holder.mRlFunction.getLayoutParams();
            if (aCache.getAsString("KeyboardHeight") != null && Integer.parseInt(aCache.getAsString("KeyboardHeight")) > 0) {
                params.height = Integer.parseInt(aCache.getAsString("KeyboardHeight")) / 2;
            }else {
                params.height = 90;
            }
            holder.mRlFunction.setLayoutParams(params);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mIvFunction.setImageResource((int) listdata.get(position).get("image"));
        holder.mTvFunction.setText((String) listdata.get(position).get("text"));
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 1;
    }

    class ViewHolder {
        ImageView mIvFunction;
        TextView mTvFunction;
        RelativeLayout mRlFunction;
    }
}
