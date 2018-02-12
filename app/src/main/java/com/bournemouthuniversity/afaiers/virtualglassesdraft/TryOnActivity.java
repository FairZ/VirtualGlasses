package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class TryOnActivity extends AppCompatActivity {

    private static final String TAG = "TryOn";

    //layout setup
    private Button m_captureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try_on);

        //get and setup capture button
        m_captureButton = (Button)findViewById(R.id.capture_button);
        assert m_captureButton != null;

    }
}