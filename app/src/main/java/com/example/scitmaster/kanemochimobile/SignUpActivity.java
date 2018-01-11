package com.example.scitmaster.kanemochimobile;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SignUpActivity extends AppCompatActivity {

    private String jsonText;
    private EditText editText1;
    private String id;
    private EditText editText2;
    private String password;
    private EditText editText3;
    private String name;
    private EditText editText4;
    private String email;
    private EditText editText5;
    private String tel;
    private Spinner spinner;
    private String category;
    private String[] items = {"catGirl","chineseGirl","englishBoy","pinkGirl","coolBoy","usoku"};
    private Button confirm;
    private Button cancel;
    private Button check;
    private RadioGroup radioGroup;
    private String gender;
    private String result;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //findView
        editText1 = (EditText) findViewById(R.id.userId);
        editText2 = (EditText) findViewById(R.id.userPassword);
        editText3 = (EditText) findViewById(R.id.userName);
        editText4 = (EditText) findViewById(R.id.userEmail);
        editText5 = (EditText) findViewById(R.id.userTel);


        //스피너에 카테고리 집어넣음
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
        spinner.setAdapter(adapter);

        //spineer선택시 이벤트
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = items[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                category = "";
            }
        });

        //리디오 그룹 설정
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);

        //확인버튼을 눌렀을 경우
        confirm = (Button) findViewById(R.id.co);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id = editText1.getText().toString();
                password = editText2.getText().toString();
                name = editText3.getText().toString();
                email = editText4.getText().toString();
                tel = editText5.getText().toString();


                if(!category.equals("") && id.length() > 0  && password.length() > 0 && name.length() > 0 && email.length() > 0 && tel.length() > 0 ){
                    // 스레드 시작
                    AnotherThread thread = new AnotherThread("http://10.10.17.26:8089/kanemochi/android/signUp");
                    thread.start();
                } else {
                    Toast.makeText(getApplicationContext(), "입력값을 모두 입력하시오",Toast.LENGTH_LONG).show();
                };

            }
        });

        //취소버튼
        cancel = (Button) findViewById(R.id.ca);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //체크버튼
        check = (Button) findViewById(R.id.Check);
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "사용할 수 있는 아이디입니다.",Toast.LENGTH_LONG).show();
            }
        });

    }

    RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.man){
                gender = "man";
            }

            else if (checkedId == R.id.woman){
                gender = "woman";
            }
        }
    };

    private class AnotherThread extends Thread{
        String addr;

        public AnotherThread(String addr){
            this.addr = addr;
        }

        @Override
        public void run() {
            super.run();
            String result = test2(addr); // 서버에 메세지 보내고 받음.
            Message message = handler.obtainMessage();
            message.obj = result;
            handler.sendMessage(message);
        }

        // 네트워크 로직 작성
        // 서버에 요청 보냄. (서버 주소, 프로젝트 명)
        // 메소드 따로 작성
        private String test2(String addr){
            StringBuilder sb = new StringBuilder();

            try {
                URL url = new URL(addr);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                if(httpURLConnection != null){
                    // 서버에 보낼 준비 작업
                    // 서버에 요청 보냈지만 응답 없을 때, 몇초까지 기다릴 것인가.
                    httpURLConnection.setConnectTimeout(10000);
                    httpURLConnection.setUseCaches(false); // 캐시 사용 여부
                    httpURLConnection.setRequestMethod("POST"); // get 또는 post 방식

                    //추가
                    httpURLConnection.setRequestProperty("Content-Type", "text/plain;charset=UTF-8");


                    // 서버에 메세지 보내기
                    OutputStream outputStream = httpURLConnection.getOutputStream(); // 서버에 보낼 문자열
                    outputStream.write((id+","+password+","+name+","+email+","+tel+","+category+","+gender).getBytes("utf-8")); // 보내는 방식이 조금 특이
                    outputStream.close(); // 자원 봔환; 메모리 반환

                    // 서버에 응답했을 때 로직
                    if(httpURLConnection.getResponseCode() == httpURLConnection.HTTP_OK){
                        InputStreamReader is = new InputStreamReader(httpURLConnection.getInputStream());
                        // Reader 가 붙으면 문자열로 인풋, 아웃풋 스트림으로 주고받음..

                        int ch;
                        while((ch = is.read()) != -1){
                            sb.append((char)ch); // 한 글자씩 붙여줌
                        }
                        is.close(); // 작업 끝나면 close
                        // jsonText = sb.toString(); // streamBuilder 를 문자열로 받아서 잠시 jsonText 에 담아 둠
                    }
                    httpURLConnection.disconnect(); // 더이상 필요하지 않으므로 통신 끊어줌.
                }

            } catch (Exception e) {
                Toast.makeText(SignUpActivity.this, "스레드 에러", Toast.LENGTH_SHORT).show();

                //e.printStackTrace();
            }
            return sb.toString();
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            result =  (String)msg.obj;
            Log.i("result",result);

            if (result.equals("1")){
                finish();
            } else if (result.equals("0")) {
                Toast.makeText(getApplicationContext(), "가입이 정상적으로 처리되지 않았습니다." , Toast.LENGTH_LONG).show();
            }

        }
    };
}