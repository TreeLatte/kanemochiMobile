package com.example.scitmaster.kanemochimobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MypageActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);
    }

    public void calendar(View view){
        Intent intent = new Intent(MypageActivity.this, CalendarActivity.class);
        startActivityForResult(intent,100);
    }

    public void chart(View view){
        Intent intent = new Intent(MypageActivity.this, ChartActivity.class);
        startActivityForResult(intent,200);
    }

}