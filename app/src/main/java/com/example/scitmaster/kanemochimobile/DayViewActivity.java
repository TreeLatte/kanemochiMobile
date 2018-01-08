package com.example.scitmaster.kanemochimobile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DayViewActivity extends AppCompatActivity {

    private TextView money;
    private TextView tag;
    private TextView category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dayview);

    }
}
