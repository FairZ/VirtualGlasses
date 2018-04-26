package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
    Fragment to handle the display and functions of the Gallery tab of the main activity
*/
public class Gallery extends Fragment {

    private List<Photo> photoList = null;
    private PhotoAdapter adapter = null;
    private RecyclerView recyclerView = null;
    private Button deleteButton = null;

    public Gallery() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //get the RecyclerView and fix its size
        recyclerView = view.findViewById(R.id.gallery_view);
        recyclerView.setHasFixedSize(true);

        //create a new grid layout manager and assign it to the recycler view
        GridLayoutManager gridLayout = new GridLayoutManager(getContext(),2);
        recyclerView.setLayoutManager(gridLayout);

        //initialise the frame list
        InitializePhotoList();

        //initialise and set the adapter for frames
        adapter = new PhotoAdapter(photoList);
        recyclerView.setAdapter(adapter);

        deleteButton = view.findViewById(R.id.gallery_delete);
        //assign the onClick callback function to call delete photos
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeletePhotos();
            }
        });
    }

    private void InitializePhotoList()
    {
        photoList = new ArrayList<Photo>();
        //get all files in the app's directory and create a list of them
        String path = Environment.getExternalStorageDirectory().toString() + getResources().getString(R.string.folder_name);
        File f = new File(path);
        File[] files = f.listFiles();
        if(files != null) {
            for (int i = 0; i < files.length; i++) {
                photoList.add(new Photo(files[i].getName(), files[i].getAbsolutePath()));
            }
        }
    }

    private void RemakePhotoList(){
        photoList = new ArrayList<Photo>();
        //get all files in the app's directory and create a list of them
        String path = Environment.getExternalStorageDirectory().toString() + getResources().getString(R.string.folder_name);
        File f = new File(path);
        File[] files = f.listFiles();
        if(files!= null) {
            for (int i = 0; i < files.length; i++) {
                photoList.add(new Photo(files[i].getName(), files[i].getAbsolutePath()));
            }
        }
        //update the recycler view to show all new photos
        adapter.UpdateList(photoList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        //recreate the photo list when re-entering the fragment
        RemakePhotoList();
    }

    public void DeletePhotos()
    {
        //delete all photos saved by the app and update the recycler view
        String path = Environment.getExternalStorageDirectory().toString() + getResources().getString(R.string.folder_name);
        File f = new File(path);
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            files[i].delete();
        }
        RemakePhotoList();
    }
}
