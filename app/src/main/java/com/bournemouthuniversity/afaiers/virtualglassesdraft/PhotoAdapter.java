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


/*
    Adapter class to allow the Recycler view to interpret the data saved within the Photo class
*/
class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.FrameViewHolder>{

    //holder handles the layout of the view
    public class FrameViewHolder extends RecyclerView.ViewHolder{
        TextView nameText;
        ImageView photoImage;

        public FrameViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.photoText);
            photoImage = itemView.findViewById(R.id.photoImage);
        }
    }

    //list to hold all photos (size of which defines number of cards shown in recycler view
    List<Photo> photos;

    public PhotoAdapter(List<Photo> _photolist) {
        photos = _photolist;
    }

    //re-assign the photolist once already created
    public void UpdateList(List<Photo> _photolist)
    {
        photos = _photolist;
    }

    //create a new card (called for each frame)
    @Override
    public PhotoAdapter.FrameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_photo,parent,false);
        return new FrameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PhotoAdapter.FrameViewHolder holder,final int position) {
        //create a thumbnail of each image in order to not fill up ram with huge photo files
        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(photos.get(position).GetImagePath()), 384,512);
        //set image and text
        holder.photoImage.setImageBitmap(thumbnail);
        holder.nameText.setText(photos.get(position).GetName());
        //Set click listener for each photo adapter to send the correct photo filepath to photoviewer activity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity main = (MainActivity) holder.itemView.getContext();
                main.SwitchToPhoto(photos.get(position).GetImagePath());
            }
        });
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
