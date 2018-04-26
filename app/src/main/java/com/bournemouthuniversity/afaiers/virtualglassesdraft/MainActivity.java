package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
    Main activity of the app, holds the tab layout to display both the gallery and catalogue fragments
    also handles switching to the two other activities and the getting of permissions for the app
*/
public class MainActivity extends AppCompatActivity {

    private ViewPager m_viewPager;

    private static final int MULTIPLE_PERMISSIONS = 10;

    private final String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<String> requiredPermissions = new ArrayList<>();
        //Check through permissions to see which have been granted
        for(String permission:permissions)
        {
            int permissionCheck = ActivityCompat.checkSelfPermission(this, permission);
            if(permissionCheck !=PackageManager.PERMISSION_GRANTED)
            {
                requiredPermissions.add(permission);
            }
        }
        //if not all permissions have been granted request the permissions which have not been
        if (!requiredPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, requiredPermissions.toArray(new String[requiredPermissions.size()]), MULTIPLE_PERMISSIONS);
        }

        m_viewPager = findViewById(R.id.container);

        //add fragments to the tab adapter
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.AddFragment(new Catalogue(), "Catalogue");
        adapter.AddFragment(new Gallery(), "Gallery");

        //assign the adapter to the view
        m_viewPager.setAdapter(adapter);

        //setup the tabs on the layout
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(m_viewPager);
    }

    public void SwitchToCamera(FrameData _data)
    {
        //open the try on activity and send the frame data to it
        Intent intent = new Intent(this,TryOnActivity.class);
        intent.putExtra("Frame",_data);
        startActivity(intent);
    }

    public void SwitchToPhoto(String _filePath)
    {
        //open the photo viewer activity and send the file path to it
        Intent intent = new Intent(this,PhotoViewer.class);
        intent.putExtra("filePath",_filePath);
        startActivity(intent);
    }
}