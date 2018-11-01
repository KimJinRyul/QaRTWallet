package jrkim.rcash.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import jrkim.rcash.R;
import jrkim.rcash.data.PhotoInfo;

public class PhotoSelectAdapter extends RecyclerView.Adapter<PhotoSelectAdapter.PhotoSelectViewHolder> {

    @NonNull
    @Override
    public PhotoSelectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_editqrcode_image, parent, false);
        return new PhotoSelectViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoSelectViewHolder holder, int position) {
        final PhotoInfo photoInfo = photoList.get(position);
        if(holder.ivPhoto != null) {
            Glide.with(context)
                    .load(photoInfo.data)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .centerCrop()
                    .crossFade()
                    .into(holder.ivPhoto);

            holder.ivPhoto.setOnClickListener((v)-> {
                listener.onSelectPhoto(photoInfo.data);
            });
        }
    }

    @Override
    public int getItemCount() {
        if(photoList != null)
            return photoList.size();
        return 0;
    }

    public interface PhotoSelectListener {
        void onSelectPhoto(String path);
    }


    public static ArrayList<PhotoInfo> photoList = null;
    private PhotoSelectListener listener = null;
    private Context context = null;

    public PhotoSelectAdapter(Context context, PhotoSelectListener listener) {
        this.context = context;
        this.listener = listener;
    }


    public class PhotoSelectViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto = null;

        public PhotoSelectViewHolder(View v) {
            super(v);
            ivPhoto = v.findViewById(R.id.ivPhoto);
        }
    }
}
