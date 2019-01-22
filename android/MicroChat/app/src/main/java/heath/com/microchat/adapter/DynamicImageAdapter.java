package heath.com.microchat.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.List;

import heath.com.microchat.R;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;

public class DynamicImageAdapter extends RecyclerView.Adapter<DynamicImageViewHolder> {

    private List<String> images;
    private Activity context;
    private File cache;
    private LayoutInflater mInflater;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private DynamicAdapter.OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(DynamicAdapter.OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public DynamicImageAdapter(Activity context, List<String> images, File cache) {
        this.context = context;
        this.images = images;
        this.cache = cache;
        mInflater = LayoutInflater.from(context);
    }


    @NonNull
    @Override
    public DynamicImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View convertView = mInflater.inflate(R.layout.item_image, viewGroup, false);
        DynamicImageViewHolder viewHolder = new DynamicImageViewHolder(convertView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final DynamicImageViewHolder viewHolder,int position) {
        try {
            ImageUitl imageUitl = new ImageUitl(cache);
            imageUitl.asyncloadImage(viewHolder.mIvIcon, Common.HTTP_ADDRESS + Common.DYNAMIC_PICTURE_PATH + "/" + images.get(position));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mOnItemClickListener != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = viewHolder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(viewHolder.itemView, layoutPosition);
                }
            });
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int layoutPosition = viewHolder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(viewHolder.itemView, layoutPosition);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }
}

class DynamicImageViewHolder extends RecyclerView.ViewHolder {
    ImageView mIvIcon;

    public DynamicImageViewHolder(@NonNull View itemView) {
        super(itemView);
        mIvIcon = itemView.findViewById(R.id.iv_icon);
    }
}
