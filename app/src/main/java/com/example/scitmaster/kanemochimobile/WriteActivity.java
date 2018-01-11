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

public class WriteActivity extends AppCompatActivity {

    private String jsonText;
    private String id;
    private String day;
    private TextView dayView;
    private Spinner spinner;
    private String category;
    private EditText editText;
    private String kane;
    private EditText editText2;
    private String tag;
    private String payment;
    private String[] items = {"burger","ramen","sushi","cafe","dessert","beer","cvs","movie","clothes","hair","hospital","book","bus","bank"};
    private Button confirm;
    private Button cancel;
    private RadioGroup radioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        dayView = (TextView) findViewById(R.id.dayView);
        //캘린더로 부터 날짜랑 ID값을 받아옴.
        Intent intent = getIntent();
        id = intent.getExtras().getString("id");
        day = intent.getExtras().getString("day");
        dayView.setText(day);
        editText = (EditText) findViewById(R.id.Kane);
        editText2 = (EditText) findViewById(R.id.Tag);
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
        confirm = (Button) findViewById(R.id.Confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kane = editText.getText().toString();
                tag = editText2.getText().toString();
                Log.i("",kane);

                if(day != "" && category != "" && kane.length() > 0  && tag.length() > 0 && payment  != null ){
                    // 스레드 시작
                    AnotherThread thread = new AnotherThread("http://10.10.17.26:8089/kanemochi/android/insertHouse");
                    thread.start();
                } else {
                    Toast.makeText(getApplicationContext(), "모든 입력값을 입력하시오",Toast.LENGTH_LONG).show();
                };

            }
        });

        cancel = (Button) findViewById(R.id.Cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.cash){
                payment = "cash";
            }

            else if (checkedId == R.id.card){
                payment = "card";
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
                    outputStream.write((id+","+day+","+category+","+tag+","+kane+","+payment).getBytes("utf-8")); // 보내는 방식이 조금 특이
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
                Toast.makeText(WriteActivity.this, "스레드 에러", Toast.LENGTH_SHORT).show();
                //e.printStackTrace();
            }
            return sb.toString();
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            jsonText =  (String)msg.obj;
        }
    };
}