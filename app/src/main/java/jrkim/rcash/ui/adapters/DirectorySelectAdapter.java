package jrkim.rcash.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import jrkim.rcash.R;
import jrkim.rcash.data.PhotoInfo;

public class DirectorySelectAdapter extends RecyclerView.Adapter<DirectorySelectAdapter.DirectorySelectViewHolder> {

    public interface DirectorySelectListener {
        void onSelectDirectory(int idx, String key);
    }

    private Context context;
    private DirectorySelectListener listener;

    public static HashMap<String, ArrayList<PhotoInfo>> photoDirectories = new HashMap<>();
    public static ArrayList<String> directoryKeys = new ArrayList<>();

    public DirectorySelectAdapter(Context context, DirectorySelectListener listener) {
        this.context = context;
        this.listener = listener;
        getImages();
    }

    public final static Comparator<PhotoInfo> photoComparator = (lhs, rhs) ->  Long.compare(rhs.modifiedTime, lhs.modifiedTime);

    private void getImages() {
        photoDirectories.clear();
        directoryKeys.clear();

        String [] proj = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA
        };

        String pictures = "/storage/emulated/0/Pictures/";
        String dcim = "/storage/emulated/0/DCIM/";
        String download = "/storage/emulated/0/Download/";

        Cursor imageCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, null, null, null);
        if(imageCursor != null) {
            if(imageCursor.moveToFirst()) {
                int idCol = imageCursor.getColumnIndex(MediaStore.Images.Media._ID);
                int dataCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                do {
                    PhotoInfo photoInfo = new PhotoInfo();
                    photoInfo.id = imageCursor.getString(idCol);
                    photoInfo.data = imageCursor.getString(dataCol);
//                    if(photoInfo.data.endsWith(".gif") || photoInfo.data.endsWith(".GIF")) {
//                        // do nothing...
//                    } else {
                        File file = new File(photoInfo.data);
                        if (file.exists() && file.length() > 0) {
                            photoInfo.modifiedTime = file.lastModified();
                            if (photoInfo.data.startsWith(pictures)) {
                                String temp = photoInfo.data.substring(pictures.length());
                                if (temp.lastIndexOf("/") > 0) {
                                    temp = temp.substring(0, temp.lastIndexOf("/"));
                                    if (temp.lastIndexOf("/") > 0) {
                                        temp = temp.substring(temp.lastIndexOf("/"));
                                        addToDirectory(temp, photoInfo);
                                    } else {
                                        addToDirectory(temp, photoInfo);
                                    }
                                } else {
                                    addToDirectory("Pictures", photoInfo);
                                }
                            } else if (photoInfo.data.startsWith(dcim)) {
                                String temp = photoInfo.data.substring(dcim.length());
                                if (temp.lastIndexOf("/") > 0) {
                                    temp = temp.substring(0, temp.lastIndexOf("/"));
                                    if (temp.lastIndexOf("/") > 0) {
                                        temp = temp.substring(temp.lastIndexOf("/"));
                                        addToDirectory(temp, photoInfo);
                                    } else {
                                        addToDirectory(temp, photoInfo);
                                    }
                                } else {
                                    addToDirectory("DCIM", photoInfo);
                                }
                            } else if (photoInfo.data.startsWith(download)) {
                                addToDirectory("Download", photoInfo);
                            } else {
                                String temp = photoInfo.data;
                                if (temp.lastIndexOf("/") > 0) {
                                    temp = temp.substring(0, temp.lastIndexOf("/"));
                                    if (temp.lastIndexOf("/") > 0) {
                                        temp = temp.substring(temp.lastIndexOf("/"));
                                        addToDirectory(temp, photoInfo);
                                    } else {
                                        addToDirectory(temp, photoInfo);
                                    }
                                } else {
                                    addToDirectory("ETC", photoInfo);
                                }
                            }
                        }
//                    }
                } while(imageCursor.moveToNext());
            }
            imageCursor.close();
        }


        imageCursor = context.getContentResolver().query(MediaStore.Images.Media.INTERNAL_CONTENT_URI, proj, null, null, null);
        if(imageCursor != null) {
            if(imageCursor.moveToFirst()) {
                int idCol = imageCursor.getColumnIndex(MediaStore.Images.Media._ID);
                int dataCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                do {
                    PhotoInfo photoInfo = new PhotoInfo();
                    photoInfo.id = imageCursor.getString(idCol);
                    photoInfo.data = imageCursor.getString(dataCol);
                    if(photoInfo.data.endsWith(".gif") || photoInfo.data.endsWith(".GIF")) {
                        // do nothing...
                    } else {
                        File file = new File(photoInfo.data);
                        if (file.exists() && file.length() > 0) {
                            photoInfo.modifiedTime = file.lastModified();
                            if (photoInfo.data.startsWith(pictures)) {
                                String temp = photoInfo.data.substring(pictures.length());
                                if (temp.lastIndexOf("/") > 0) {
                                    temp = temp.substring(0, temp.lastIndexOf("/"));
                                    if (temp.lastIndexOf("/") > 0) {
                                        temp = temp.substring(temp.lastIndexOf("/"));
                                        addToDirectory(temp, photoInfo);
                                    } else {
                                        addToDirectory(temp, photoInfo);
                                    }
                                } else {
                                    addToDirectory("Pictures", photoInfo);
                                }
                            } else if (photoInfo.data.startsWith(dcim)) {
                                String temp = photoInfo.data.substring(dcim.length());
                                if (temp.lastIndexOf("/") > 0) {
                                    temp = temp.substring(0, temp.lastIndexOf("/"));
                                    if (temp.lastIndexOf("/") > 0) {
                                        temp = temp.substring(temp.lastIndexOf("/"));
                                        addToDirectory(temp, photoInfo);
                                    } else {
                                        addToDirectory(temp, photoInfo);
                                    }
                                } else {
                                    addToDirectory("DCIM", photoInfo);
                                }
                            } else if (photoInfo.data.startsWith(download)) {
                                addToDirectory("Download", photoInfo);
                            } else {

                                String temp = photoInfo.data;
                                if (temp.lastIndexOf("/") > 0) {
                                    temp = temp.substring(0, temp.lastIndexOf("/"));
                                    if (temp.lastIndexOf("/") > 0) {
                                        temp = temp.substring(temp.lastIndexOf("/"));
                                        addToDirectory(temp, photoInfo);
                                    } else {
                                        addToDirectory(temp, photoInfo);
                                    }
                                } else {
                                    addToDirectory("ETC", photoInfo);
                                }
                            }
                        }
                    }
                } while(imageCursor.moveToNext());
            }
            imageCursor.close();
        }

        Set<String> keys =  photoDirectories.keySet();
        for(String key : keys) {
            Collections.sort(photoDirectories.get(key), photoComparator);
            directoryKeys.add(key);
        }
    }

    private void addToDirectory(String name, PhotoInfo photoInfo) {
        if(name.startsWith("/")) {
            name = name.substring(1);
        }
        if(photoDirectories.containsKey(name)) {
            ArrayList<PhotoInfo> directory = photoDirectories.get(name);
            directory.add(photoInfo);
        } else {
            ArrayList<PhotoInfo> newInfos = new ArrayList<>();
            newInfos.add(photoInfo);
            photoDirectories.put(name, newInfos);
        }
    }

    @NonNull
    @Override
    public DirectorySelectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_editqrcode_directory, parent, false);
        return new DirectorySelectViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DirectorySelectViewHolder holder, final int position) {
        final String key = directoryKeys.get(position);
        final ArrayList<PhotoInfo> photoInfos = photoDirectories.get(key);

        if(holder.ivPhoto != null) {
            if(photoInfos.size() > 0) {
                Glide.with(context)
                        .load(photoInfos.get(0).data)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .centerCrop()
                        .crossFade()
                        .into(holder.ivPhoto);
            }
        }

        if(holder.tvDirectoryName != null) {
            holder.tvDirectoryName.setText(key);
        }

        if(holder.tvCount != null) {
            holder.tvCount.setText(String.valueOf(photoInfos.size()));
        }

        if(holder.rlBody != null) {
            holder.rlBody.setOnClickListener((v) -> {
                if(listener != null) {
                    listener.onSelectDirectory(position, key);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return directoryKeys.size();
    }

    public class DirectorySelectViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout rlBody;
        private ImageView ivPhoto;
        private TextView tvDirectoryName;
        private TextView tvCount;

        public DirectorySelectViewHolder(View v) {
            super(v);
            rlBody = v.findViewById(R.id.rlBody);
            ivPhoto = v.findViewById(R.id.ivPhoto);
            tvDirectoryName = v.findViewById(R.id.tvDirectoryName);
            tvCount = v.findViewById(R.id.tvCount);
        }
    }
}
