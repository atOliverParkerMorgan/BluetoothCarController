package com.example.bluetoothcarcontroller;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class AnalyzeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View v = new TouchExampleView(this);

        super.onCreate(savedInstanceState);
        setContentView(v);
    }
}
