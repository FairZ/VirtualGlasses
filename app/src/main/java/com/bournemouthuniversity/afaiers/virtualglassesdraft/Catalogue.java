package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


public class Catalogue extends Fragment {

    private List<Frame> frameList;

    public Catalogue() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_catalogue, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //get the RecyclerView and fix its size
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.catalogue_view);
        recyclerView.setHasFixedSize(true);

        //create a new grid layout manager and assign it to the recycler view
        GridLayoutManager gridLayout = new GridLayoutManager(getContext(),2);
        recyclerView.setLayoutManager(gridLayout);

        //initialise the frame list
        InitializeFrameList();

        //initialise and set the adapter for frames
        FrameAdapter adapter = new FrameAdapter(frameList);
        recyclerView.setAdapter(adapter);
    }

    private void InitializeFrameList()
    {
        frameList = new ArrayList<Frame>();
        frameList.add(new Frame("Model 1", R.drawable.catalogue_image_1,0.5f));
        frameList.add(new Frame("Model 2", R.drawable.catalogue_image_2,0.5f));
        frameList.add(new Frame("Model 3", R.drawable.catalogue_image_3,0.5f));
    }


}
