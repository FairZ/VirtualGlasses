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

/*
    Fragment which handles the display and functions of the catalogue tab in the main activity
*/
public class Catalogue extends Fragment {

    //used to store a list of available frames which is assigned to the recyclerView
    private List<Frame> m_frameList;

    public Catalogue() {

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
        RecyclerView recyclerView = view.findViewById(R.id.catalogue_view);
        recyclerView.setHasFixedSize(true);

        //create a new grid layout manager and assign it to the recycler view
        GridLayoutManager gridLayout = new GridLayoutManager(getContext(),2);
        recyclerView.setLayoutManager(gridLayout);

        //initialise the frame list
        InitialiseFrameList();

        //initialise and set the adapter for frames
        FrameAdapter adapter = new FrameAdapter(m_frameList);
        recyclerView.setAdapter(adapter);
    }

    private void InitialiseFrameList()
    {
        //initialise the frame list and assign all available glasses to it as well as their data
        m_frameList = new ArrayList<Frame>();
        m_frameList.add(new Frame("Model 1", R.drawable.catalogue_image_1, new float[] {0.5f,0.5f,0.5f,1.0f},new float[] {0.5f,0.5f,0.5f,1.0f},
                new float[] {0.5f,0.5f,0.5f,1.0f},new float[] {0,0,0,0},R.raw.final1));
        m_frameList.add(new Frame("Model 2", R.drawable.catalogue_image_2, new float[] {1.0f,0.5f,0.5f,1.0f},new float[] {0.5f,0.5f,0.5f,1.0f},
                new float[] {0.5f,0.5f,0.5f,1.0f},new float[] {0,0,0,0},R.raw.final2));
        m_frameList.add(new Frame("Model 3", R.drawable.catalogue_image_3, new float[] {0.5f,0.5f,1.0f,1.0f},new float[] {0.5f,0.5f,0.5f,1.0f},
                new float[] {0.5f,0.5f,0.5f,1.0f},new float[] {0,0,0,0},R.raw.final3));
    }


}
