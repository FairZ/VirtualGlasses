package com.bournemouthuniversity.afaiers.virtualglassesdraft;

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

class FrameAdapter extends RecyclerView.Adapter<FrameAdapter.FrameViewHolder>{

    public class FrameViewHolder extends RecyclerView.ViewHolder{
        TextView nameText;
        ImageView frameImage;

        public FrameViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.frameText);
            frameImage = itemView.findViewById(R.id.frameImage);
        }
    }

    List<Frame> frames;

    public FrameAdapter(List<Frame> _framelist) {
        frames = _framelist;
    }

    @Override
    public FrameAdapter.FrameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_frame,parent,false);
        return new FrameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FrameAdapter.FrameViewHolder holder, final int position) {
        holder.frameImage.setImageResource(frames.get(position).GetImageRef());
        holder.nameText.setText(frames.get(position).GetName());
        //Set click listener for each frame adapter to send the correct frame data to try on
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity main = (MainActivity) holder.itemView.getContext();
                main.SwitchToCamera(frames.get(position).GetFrameData());
            }
        });
    }

    @Override
    public int getItemCount() {
        return frames.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
