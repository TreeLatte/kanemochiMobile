package com.example.scitmaster.kanemochimobile;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends AppCompatActivity {

    private String jsonText;
    private String id;
    private String pwd;
    private int backCount=0;
    private Timer timer;
    private final long FINISH_INTERVAL_TIME = 2000;
    private long   backPressedTime = 0;

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
        {
            super.onBackPressed();
        }
        else
        {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "한번 더 뒤로가기 누르면 꺼집니다.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);




    }

    public void test(View view){
        switch (view.getId()){
            case R.id.loginButton:
                AnotherThread thread = new AnotherThread("http://10.10.17.27:8089/kanemochi/android/login");
                thread.start();
                break;
            case R.id.signupButton:
                if(jsonText==null)return;
                JSONArray jsonArray = null;
                JSONObject jsonObject = null;
                try {
                    jsonArray = new JSONArray(jsonText);
                    StringBuilder sb2 = new StringBuilder();
                    for (int i=0;i<jsonArray.length();i++){
                        jsonObject = jsonArray.getJSONObject(i);
                        sb2.append("번호");
                        sb2.append(jsonObject.getString("num"));
                        sb2.append("이름");
                        sb2.append(jsonObject.getString("name"));
                        sb2.append("\n");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private class AnotherThread extends Thread {
        private String addr;
        EditText emailInput;
        EditText pwdInput;


        public AnotherThread(String addr) {
            this.addr = addr;
        }

        @Override
        public void run() {
            super.run();
            String result = test2(addr);
            Log.i("result",result);
            Message message = handler.obtainMessage();
            message.obj = result;
            handler.sendMessage(message);
        }

        private String test2(String addr) {
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(addr);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                emailInput = (EditText)findViewById(R.id.emailInput);
                pwdInput = (EditText)findViewById(R.id.passwordInput);
                id = emailInput.getText().toString();
                pwd = pwdInput.getText().toString();
                if (httpURLConnection != null) {
                    httpURLConnection.setConnectTimeout(10000);
                    httpURLConnection.setUseCaches(false);
                    httpURLConnection.setRequestMethod("POST");
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    String temp = id+","+pwd;
//                    temp = URLEncoder.encode(temp,"utf-8");
                    outputStream.write((id+","+pwd).getBytes("utf-8"));
                    Log.i("temp: ",temp);
                    Log.i("아이디: ",id);
                    Log.i("비번: ",pwd);
                    outputStream.close();

                    if(httpURLConnection.getResponseCode() == httpURLConnection.HTTP_OK){
                        InputStreamReader is = new InputStreamReader(httpURLConnection.getInputStream());
                        int ch;
                        while ((ch = is.read())!=-1){
                            sb.append((char)ch);
                        }
                        is.close();
                        jsonText = sb.toString();
//                        Log.i("jsonText",jsonText);
                    }
                    httpURLConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return sb.toString();
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String result = (String)msg.obj;
//            Log.i("result",result);
            if(result.equals("")){
                Toast.makeText(getApplicationContext(),"로그인 실패",Toast.LENGTH_SHORT).show();
            }
            else{
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    String id = jsonObject.getString("user_id");
                    String img = jsonObject.getString("img_id");
                    String level = jsonObject.getString("level_title");

                    Intent intent = new Intent(LoginActivity.this,MypageActivity.class);
                    intent.putExtra("id", id);
                    intent.putExtra("img", img);
                    intent.putExtra("level", level);
                    finish();
                    startActivity(intent);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
