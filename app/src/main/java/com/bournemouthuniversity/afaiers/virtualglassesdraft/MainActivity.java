package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.view.ViewPager;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager m_viewPager;
    private static final String TAG = "Main Act";

    private static final int MULTIPLE_PERMISSIONS = 10;

    private final String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        List<String> requiredPermissions = new ArrayList<>();
        for(String permission:permissions)
        {
            int permissionCheck = ActivityCompat.checkSelfPermission(this, permission);
            if(permissionCheck !=PackageManager.PERMISSION_GRANTED)
            {
                requiredPermissions.add(permission);
            }
        }
        if (!requiredPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, requiredPermissions.toArray(new String[requiredPermissions.size()]), MULTIPLE_PERMISSIONS);
        }

        String path = Environment.getExternalStorageDirectory().toString() + getResources().getString(R.string.folder_name);
        File f = new File(path);
        if (!f.exists())
        {
            f.mkdirs();
        }

        m_viewPager = (ViewPager) findViewById(R.id.container);

        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.AddFragment(new Catalogue(), "Catalogue");
        adapter.AddFragment(new Gallery(), "Gallery");
        Catalogue catalogue = (Catalogue)adapter.getItem(0);
        catalogue.SetContext(this);

        m_viewPager.setAdapter(adapter);

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(m_viewPager);
    }

    public void SwitchToCamera(FrameData _data)
    {
        Intent intent = new Intent(this,TryOnActivity.class);
        intent.putExtra("Frame",_data);
        startActivity(intent);
    }

    public void SwitchToPhoto(String _filePath)
    {
        Intent intent = new Intent(this,PhotoViewer.class);
        intent.putExtra("filePath",_filePath);
        startActivity(intent);
    }
}