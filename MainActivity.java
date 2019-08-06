//メイン画面(カレンダー表示画面)
package com.example.mytodo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ajd4jp.AJD;
import ajd4jp.AJDException;
import ajd4jp.Holiday;
import ajd4jp.Month;

public class MainActivity extends AppCompatActivity {
    private Button nextMonthButton;     //次の月へボタン
    private Button previousMonthButton; //前の月へボタン
    private TextView headerMonthText;   //年月表示テキストビュー

    private int currentYear = 0;        //現在表示中の年
    private int currentMonth = 0;       //現在表示中の月

    private int nowYear = 0;            //現在の年
    private int nowMonth = 0;           //現在の月
    private int nowDay = 0;             //現在の日

    //日表示テキスト情報リスト
    private ArrayList<DayTextViewInfo> dayTextList = new ArrayList<>();

    //各日付のテキストビューのID
    private final int[][] DAYID = {
            {R.id.tv1Sun, R.id.tv1Mon, R.id.tv1Tue, R.id.tv1Wed, R.id.tv1Thu, R.id.tv1Fri, R.id.tv1Sat},
            {R.id.tv2Sun, R.id.tv2Mon, R.id.tv2Tue, R.id.tv2Wed, R.id.tv2Thu, R.id.tv2Fri, R.id.tv2Sat},
            {R.id.tv3Sun, R.id.tv3Mon, R.id.tv3Tue, R.id.tv3Wed, R.id.tv3Thu, R.id.tv3Fri, R.id.tv3Sat},
            {R.id.tv4Sun, R.id.tv4Mon, R.id.tv4Tue, R.id.tv4Wed, R.id.tv4Thu, R.id.tv4Fri, R.id.tv4Sat},
            {R.id.tv5Sun, R.id.tv5Mon, R.id.tv5Tue, R.id.tv5Wed, R.id.tv5Thu, R.id.tv5Fri, R.id.tv5Sat},
            {R.id.tv6Sun, R.id.tv6Mon, R.id.tv6Tue, R.id.tv6Wed, R.id.tv6Thu, R.id.tv6Fri, R.id.tv6Sat}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //前月と翌月のカレンダーを表示するボタンの実装
        touchMonthButtonClickListener listener = new touchMonthButtonClickListener();
        nextMonthButton = findViewById(R.id.next_month_id);
        nextMonthButton.setOnClickListener(listener);
        previousMonthButton = findViewById(R.id.previous_month_button);
        previousMonthButton.setOnClickListener(listener);

        //当月のカレンダーの初期設定
        initializeCalendar();


    }

    //カレンダーの初期設定
    private void initializeCalendar() {
        headerMonthText = findViewById(R.id.header_month_text);
        DayTextViewInfo info;

        //アレイリストへのtextViewのIDの格納
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                info = new DayTextViewInfo(DAYID[i][j]);
                dayTextList.add(info);
            }
        }

        Calendar cal1 = Calendar.getInstance();
        nowYear = cal1.get(Calendar.YEAR);          //現在の年を取得
        nowMonth = cal1.get(Calendar.MONTH) + 1;    //現在の月を取得
        nowDay = cal1.get(Calendar.DATE);               //現在の日を取得
        //EditActivityから遷移した場合には表示していた年月を格納(default値は現在の年月)
        currentYear = getIntent().getIntExtra("_year",nowYear);
        currentMonth = getIntent().getIntExtra("_month", nowMonth);


        int id = 0;
        for (int i = 0; i < 6; i++) {

            for (int j = 0; j < 7; j++) {
                //日付のテキストビューにリスナーをセット
                TextView tv = findViewById(dayTextList.get(id).getTextViewId());
                touchDayButtonClickListener listener = new touchDayButtonClickListener();
                tv.setOnClickListener(listener);
                tv.setBackgroundResource(R.drawable.text_day_line);
                dayTextList.get(id).setTextObject(tv);
                id++;
            }
        }
        //日付のセット
        this.SetCalendar(0);
    }

    private void SetCalendar(int offset)  {
        try {
            currentMonth = currentMonth + offset;

            if (currentMonth > 12) {
                currentYear = currentYear + 1;
                currentMonth = 1;
            } else if (currentMonth == 0) {
                currentMonth = 12;
                currentYear = currentYear - 1;
            }

            //テキスト表示情報初期化
            for (int i = 0; i < dayTextList.size(); i++) {
                DayTextViewInfo tg = dayTextList.get(i);
                if (tg.isNowDay() || tg.isSelected()) {
                    tg.getTextObject().setBackgroundResource(R.drawable.text_day_line);

                }
                tg.setNowDay(false);
                tg.setDayNum(0);
                tg.setSelected(false);
                tg.getTextObject().setText(tg.getDisplayString());
                tg.getTextObject().setTextColor(Color.BLACK);

            }

            //カレンダーテーブル作成
            CalendarInfo cl = new CalendarInfo(currentYear, currentMonth);
            int row = 0;
            int col = 0;

            for (int i = 0; i < dayTextList.size(); i++) {
                DayTextViewInfo tg = dayTextList.get(i);
                Month mon = new Month(currentYear, currentMonth);

                if (col == 0) {
                    //日曜日のカラーを変更
                    tg.getTextObject().setTextColor(Color.RED);
                }
                if (col == 6) {
                    //土曜日のカラーを変更
                    tg.getTextObject().setTextColor(Color.BLUE);
                }

                if (cl.calendarDate[row][col] != 0) {
                    // 日付表示
                    tg.setDayNum(cl.calendarDate[row][col]);
                    tg.getTextObject().setText(tg.getDisplayString());

                    //祝日か判断し、祝日の場合赤字表示
                    AJD day = mon.getDays()[cl.calendarDate[row][col]-1];
                    Holiday h = Holiday.getHoliday(day);
                    if (h != null) {
                        tg.getTextObject().setTextColor(Color.RED);
                    }


                    if (nowYear == currentYear
                            && nowMonth == currentMonth
                            && cl.calendarDate[row][col] == nowDay) {

                        // 当日日付表示
                        dayTextList.get(i).setNowDay(true);
                        tg.getTextObject().setBackgroundResource(R.drawable.text_today_line);
                    }


                    //データベースを参照し、予定がある日ならば、★マークをカレンダーに追記する
                    //データベースヘルパーオブジェクトを作成。
                    DatabaseHelper helper = new DatabaseHelper(MainActivity.this);
                    //データベースヘルパーオブジェクトからデータベース接続オブジェクトを取得。
                    SQLiteDatabase db = helper.getWritableDatabase();

                    try {
                        // 検索SQL文字列の用意。
                        String sql = "SELECT _id FROM myschedule where day ='" +currentYear+"/"+currentMonth+"/"+cl.calendarDate[row][col]+"'";
                        // SQLの実行。
                        Cursor cursor =db.rawQuery(sql, null);
                        // データベースから取得したidを格納する変数の用意。データがなかった時のための初期値も用意。
                        int id = 0;
                        List<Map<String, Object>> _scList = new ArrayList<>();
                        // スケジュールデータ1個ずつを格納するMapオブジェクトの変数
                        Map<String, Object> sc;

                        //SQL実行の戻り値であるカーソルオブジェクトをループさせてデータベース内のデータを取得。
                        while (cursor.moveToNext()) {
                            //カラムのインデックス値を取得。
                            int idx_id = cursor.getColumnIndex("_id");
                            // スケジュールデータ1個分のMapオブジェクトの用意
                            sc = new HashMap<>();
                            //カラムのインデックス値を元に実際のデータを取得
                            id = cursor.getInt(idx_id);                 // 主キーの値を取得
                            sc.put("id", id);                           // Mapオブジェクトに格納
                            _scList.add(sc);    // スケジュールデータ格納用のListオブジェクトに1行追加
                        }
                        //スケジュールデータが格納されている場合は★を日付の欄に追記
                        if(_scList.size()!=0){
                            String daynum=tg.getTextObject().getText().toString();//日付取得
                            tg.getTextObject().setText(daynum+"\n    ★");
                        }
                    } finally {
                        db.close(); //データベース接続オブジェクトの解放
                    }

                }

                col += 1;
                if (col == 7) {
                    row += 1;
                    col = 0;
                }
            }

            //年月表示
            headerMonthText.setText(currentYear + "年" + currentMonth + "月");
        }catch (AJDException e ){
        }
    }

    //翌月または前月へ進むボタンが押されたときのリスナ
    private class touchMonthButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.next_month_id) {
                SetCalendar(+1);
            } else if (view.getId() == R.id.previous_month_button) {
                SetCalendar(-1);
            }
        }
    }

    //日付がタップされた際のリスナ（予定の表示画面へ遷移）
    private class touchDayButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int tv =view.getId();
            int day=0;
            for(int i=0;i<dayTextList.size();i++){
                    if(tv==dayTextList.get(i).getTextViewId()){
                      day= dayTextList.get(i).getDayNum();
                }
            }

            if(day!=0) {
                // インテントオブジェクトを生成
                Intent intent = new Intent(MainActivity.this, ToDoActivity.class);
                // Todoに送るデータを格納(年月日)
                intent.putExtra("_year", currentYear);
                intent.putExtra("_month", currentMonth);
                intent.putExtra("_day", day);
                // Todo画面の起動
                startActivityForResult(intent, 100);
            }
        }
    }

    //サブメニューの実装
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //サブメニューの項目がタップされたとき
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId=item.getItemId();
        switch (itemId){
            //現在の日付へ移動
            case R.id.optionGoNow:
                currentYear=nowYear;
                currentMonth=nowMonth;
                this.SetCalendar(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
