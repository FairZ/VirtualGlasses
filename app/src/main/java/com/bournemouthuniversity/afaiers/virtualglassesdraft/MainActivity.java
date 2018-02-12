package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.content.Intent;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private ViewPager m_viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + R.string.folder_name;
        File f = new File(path);
        if (!f.exists())
        {
            f.mkdirs();
        }

        m_viewPager = (ViewPager) findViewById(R.id.container);

        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.AddFragment(new Catalogue(), "Catalogue");
        adapter.AddFragment(new Gallery(), "Gallery");

        m_viewPager.setAdapter(adapter);

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(m_viewPager);
    }

    public void SwitchToCamera(View view)
    {
        Intent intent = new Intent(this,TryOnActivity.class);
        startActivity(intent);
    }
}