package heath.com.microchat.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

import heath.com.microchat.R;

public class ImageAdapter extends BaseAdapter {

    private List<String> images;
    private Activity context;
    private ViewHolder holder;

    public ImageAdapter(Activity context, List<String> images) {
        this.context = context;
        this.images = images;
        holder = null;
    }

    @Override
    public int getCount() {
        return images.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_image, null);
            holder = new ViewHolder();
            holder.mIvIcon = convertView.findViewById(R.id.iv_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            if (position == images.size()) {
                holder.mIvIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.camera));
            } else {
                Log.i("图片路径1", "onActivityResult: " + images.get(position));
                Bitmap bitmap = BitmapFactory.decodeFile(images.get(position));
                holder.mIvIcon.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 1;
    }

    class ViewHolder {
        ImageView mIvIcon;
    }

}
