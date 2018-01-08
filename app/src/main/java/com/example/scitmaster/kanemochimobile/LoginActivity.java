package com.example.scitmaster.kanemochimobile;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity {

    private String jsonText;
    private String id;
    private String pwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void test(View view){
        switch (view.getId()){
            case R.id.loginButton:
                AnotherThread thread = new AnotherThread("http://10.10.17.67:8888/kanemochi/android/login");
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

        }
    };
}
