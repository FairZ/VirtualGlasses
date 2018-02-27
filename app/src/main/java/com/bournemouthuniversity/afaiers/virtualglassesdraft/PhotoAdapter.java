package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Adam on 28/01/2018.
 */

class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.FrameViewHolder>{

    public class FrameViewHolder extends RecyclerView.ViewHolder{
        TextView nameText;
        ImageView photoImage;

        public FrameViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.photoText);
            photoImage = itemView.findViewById(R.id.photoImage);
        }
    }

    List<Photo> photos;

    public PhotoAdapter(List<Photo> _photolist) {
        photos = _photolist;
    }

    public void UpdateList(List<Photo> _photolist)
    {
        photos = _photolist;
    }

    @Override
    public PhotoAdapter.FrameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_photo,parent,false);
        return new FrameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PhotoAdapter.FrameViewHolder holder, int position) {
        //TODO: MAKE THUMBNAIL GETTING MORE EFFICIENT
        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(photos.get(position).GetImagePath()), 384,512);
        holder.photoImage.setImageBitmap(thumbnail);
        holder.nameText.setText(photos.get(position).GetName());
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
