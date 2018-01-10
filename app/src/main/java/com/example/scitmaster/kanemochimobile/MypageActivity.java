package com.example.scitmaster.kanemochimobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.TextView;
import android.widget.ImageView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

public class MypageActivity extends AppCompatActivity{

    private TextView userId;
    private TextView userLevel;
    private ImageView userAvatar;
    private DiscreteSeekBar level_bar;
    private Intent intent;
    private String id;
    private String img;
    private String level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        userId = (TextView) findViewById(R.id.userId);
        userLevel = (TextView) findViewById(R.id.userLevel);
        userAvatar = (ImageView) findViewById(R.id.userAvatar);
        level_bar = (DiscreteSeekBar) findViewById(R.id.level_bar);

        intent = getIntent();
        id = intent.getExtras().getString("id");
        img = intent.getExtras().getString("img");
        level = intent.getExtras().getString("level");

        userId.setText(id + " 님");
        userLevel.setText("Level. " + level);

        if(img.equals("catGirl")) userAvatar.setImageResource(R.drawable.cat_girl);
        if(img.equals("chineseGirl")) userAvatar.setImageResource(R.drawable.chinese_girl);
        if(img.equals("englishBoy")) userAvatar.setImageResource(R.drawable.english_boy);
        if(img.equals("pinkGirl")) userAvatar.setImageResource(R.drawable.pink_girl);
        if(img.equals("coolBoy")) userAvatar.setImageResource(R.drawable.cool_boy);
        if(img.equals("Usoku")) userAvatar.setImageResource(R.drawable.usoku);

        Log.i("id2", id);
        Log.i("img2", img);
        Log.i("level2", level);
    }

    public void calendar(View view){
        Intent intent = new Intent(MypageActivity.this, CalendarActivity.class);
        intent.putExtra("id", id);
        finish();
        startActivity(intent);
    }

    public void chart(View view){
        Intent intent = new Intent(MypageActivity.this, ChartActivity.class);
        startActivityForResult(intent,200);
    }

    public void logout(View view){
        finish();
        Intent intent = new Intent(MypageActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}