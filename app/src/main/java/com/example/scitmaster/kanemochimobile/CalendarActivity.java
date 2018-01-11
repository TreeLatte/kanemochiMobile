package com.example.scitmaster.kanemochimobile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CalendarActivity extends AppCompatActivity {

    private MaterialCalendarView calendar;
    private ScrollView scrollView;
    private LinearLayout linearLayout;
    private int beforeDay;
    private ServerThread serverThread;
    private int i=0;
    private String id;
    private String dayString;

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendar = (MaterialCalendarView) findViewById(R.id.calendarView);
        LinearLayout inputLayout = (LinearLayout)findViewById(R.id.inputLayout);
        scrollView = (ScrollView)findViewById(R.id.data_scroll_view);
        linearLayout = (LinearLayout)findViewById(R.id.scroll_in_layout);
        Intent intent = getIntent();
        id = intent.getExtras().getString("id");
        inputLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CalendarActivity.this,WriteActivity.class);
                intent.putExtra("id",id);
                intent.putExtra("day",dayString);
                startActivity(intent);
            }
        });
        calendar.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                int year = date.getYear();
                int month = date.getMonth()+1;
                String month2;
                if(month <10){
                    month2 = "0"+month;
                }
                else{
                    month2 = String.valueOf(month);
                }
                int day = date.getDay();
                String day2;
                if(day<10) day2 = "0"+day;
                else day2 = String.valueOf(day);

                dayString = year+"-"+month2+"-"+day2;
                serverThread = new ServerThread("http://10.10.17.27:8089/kanemochi/android/getCalendar",id,dayString);
                serverThread.start();
            }
        });

    }
//    private void openWrite(String id, String dayString ){
//        Intent intent = new Intent(CalendarActivity.this,WriteActivity.class);
//        intent.putExtra("id",id);
//        intent.putExtra("day",dayString);
//        startActivity(intent);
//    }

    private class ServerThread extends Thread{
        String addr;
        String id;
        String dayString;
        //주소,마이페이지에서 넘어온 id,내가 클릭한 달력의 날(내가 변형시켜놨음)
        public ServerThread(String addr,String id,String dayString){
            this.addr = addr;
            this.id = id;
            this.dayString = dayString;
        }

        @Override
        public void run() {
            super.run();

            String result = process(addr); // 서버에 메세지 보내고 받음.
            Message message = handler.obtainMessage();
            message.obj = result;
            handler.sendMessage(message);
        }

        // 네트워크 로직 작성
        // 서버에 요청 보냄. (서버 주소, 프로젝트 명)
        // 메소드 따로 작성
        private String process(String addr){
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
                    //미리 id와 현재 날짜의 문자열을 여기서 합쳐놓고 이것을 보냄 그리고 spring controller 가서 이를 다시 분리한다.
                    Log.i("id",id);
                    Log.i("day",dayString);
                    outputStream.write((id+","+dayString).getBytes("utf-8")); // 보내는 방식이 조금 특이
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
                Toast.makeText(CalendarActivity.this, "스레드 에러", Toast.LENGTH_SHORT).show();
                //e.printStackTrace();
            }
            return sb.toString();
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String result = (String)msg.obj;
            LinearLayout dataLinearView;
            linearLayout = (LinearLayout) findViewById(R.id.scroll_in_layout);
            //스크롤뷰 안에 있는 레이아웃이 가진 모든 뷰(텍스트 뷰들)을 날림.
            linearLayout.removeAllViews();
            if(result == null) return;
            try {
                JSONArray jsonArray = new JSONArray(result);
                JSONObject jsonObject = null;
                //sql의 결과의 길이 만큼 반복.
                for(int i=0; i<jsonArray.length() ;i++) {
                    //스크롤 뷰에다가 추가하기 위해서 사용함.
                    dataLinearView = (LinearLayout) View.inflate(CalendarActivity.this,R.layout.activity_dayview,null);
                    //스크롤뷰에 추가될 레이아웃에 이미 선언 되어있는 TextView들.
                    TextView moneyView = (TextView)dataLinearView.findViewById(R.id.textMoney);
                    TextView categoryView = (TextView)dataLinearView.findViewById(R.id.textCategory);
                    TextView tagView = (TextView)dataLinearView.findViewById(R.id.textTag);
                    TextView payView = (TextView)dataLinearView.findViewById(R.id.textPayment);
                    jsonObject = jsonArray.getJSONObject(i);
                    //스크롤뷰에 생성될 레이아웃이 가지고 있는 textview들에 값을 넣는다.
                    categoryView.setText(jsonObject.getString("category"));
                    tagView.setText(jsonObject.getString("record_tag"));
                    moneyView.setText("￥"+jsonObject.getString("record_price"));
                    payView.setText(jsonObject.getString("record_pay"));
                    //추가 될 때마다 색 변환(빨주노초파남보를 조금 연하게 처리함)
                    switch (linearLayout.getChildCount() % 7){
                        case 0:
                            dataLinearView.setBackgroundColor(Color.rgb(240,128,128));
                            break;
                        case 1:
                            dataLinearView.setBackgroundColor(Color.rgb(255,165,0));
                            break;
                        case 2:
                            dataLinearView.setBackgroundColor(Color.rgb(240,230,90));
                            break;
                        case 3:
                            dataLinearView.setBackgroundColor(Color.rgb(50,205,50));
                            break;
                        case 4:
                            dataLinearView.setBackgroundColor(Color.rgb(0,191,255));
                            break;
                        case 5:
                            dataLinearView.setBackgroundColor(Color.rgb(0,0,128));
                            break;
                        case 6:
                            dataLinearView.setBackgroundColor(Color.rgb(128,0,128));
                            break;
                    }
                    linearLayout.addView(dataLinearView);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
