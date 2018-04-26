package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;

/*
    Activity to handle the showing of the large photo and all functions therein
*/
public class PhotoViewer extends AppCompatActivity {

    private ImageButton m_close;
    private ImageButton m_share;
    private ImageView m_photo;
    private Uri m_contentURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);

        //get the file passed in from the gallery
        String filePath = getIntent().getStringExtra("filePath");
        File photoFile = new File(filePath);
        //create a content Uri for that file (required for sharing of image)
        m_contentURI = FileProvider.getUriForFile(this,"com.bournemouthuniversity.afaiers.virtualglassesdraft.fileprovider",photoFile);
        //set read and write permissions for whatever app recieves the file so that no conflicts occur
        grantUriPermission("com.bournemouthuniversity.afaiers.virtualglassesdraft",m_contentURI,Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        //get the close button and set it to close the photoviewer when pressed
        m_close = findViewById(R.id.photo_close_button);
        m_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Close();
            }
        });

        //get the share button and set it to share the image when pressed
        m_share = findViewById(R.id.photo_share_button);
        m_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Share();
            }
        });

        //set the image to that of the photo
        m_photo = findViewById(R.id.photo_image);
        m_photo.setImageURI(m_contentURI);
    }

    private void Close()
    {
        //end the activity
        finish();
    }

    private void Share()
    {
        //setup the sharing intent and start it
        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_STREAM,m_contentURI);
        share.setType("image/jpeg");
        startActivity(Intent.createChooser(share,getResources().getText(R.string.send_to)));
    }

}
