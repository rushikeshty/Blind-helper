package org.tensorflow.lite.examples.detection;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import java.text.SimpleDateFormat;
import java.util.Locale;

public class DateAndTime extends AppCompatActivity {
    private TextToSpeech textToSpeech;
    private TextView format7;
    float x1,x2,y1,y2;

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        String dateTime = null;
        Calendar calendar = null;
        SimpleDateFormat simpleDateFormat;
        format7 = (TextView) findViewById(R.id.format7);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            calendar = Calendar.getInstance();
        }
        simpleDateFormat = new SimpleDateFormat("'date is' dd-LLLL-yyyy 'and time is' KK:mm aaa ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dateTime = simpleDateFormat.format(calendar.getTime()).toString();
        }
        format7.setText(dateTime);
        format7.getText().toString();

        String finalDateTime = dateTime;
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    textToSpeech.setSpeechRate(1f);
                    textToSpeech.speak(finalDateTime, TextToSpeech.QUEUE_FLUSH, null);
                    textToSpeech.speak("swipe left to listen again and swipe right to return back in main menu", TextToSpeech.QUEUE_ADD, null);

                }
            }
        });
    }
 public boolean onTouchEvent(MotionEvent touchEvent) {

    switch (touchEvent.getAction()) {

        case MotionEvent.ACTION_DOWN:
            x1 = touchEvent.getX();
            y1 = touchEvent.getY();
            break;
        case MotionEvent.ACTION_UP:
            x2 = touchEvent.getX();
            y2 = touchEvent.getY();
            if (x1 > x2) {
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(DateAndTime.this, Home.class);
                        startActivity(intent);

                    }
                }, 1000);

            }

            if(x1<x2) {
                String dateTime = null;
                Calendar calendar = null;
                SimpleDateFormat simpleDateFormat;
                format7 = (TextView) findViewById(R.id.format7);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    calendar = Calendar.getInstance();
                }
                simpleDateFormat = new SimpleDateFormat("'date is' dd-LLLL-yyyy 'and time is' KK:mm aaa ");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    dateTime = simpleDateFormat.format(calendar.getTime()).toString();
                }
                format7.setText(dateTime);
                format7.getText().toString();

                String finalDateTime = dateTime;
                textToSpeech.speak(finalDateTime, TextToSpeech.QUEUE_FLUSH, null);
                textToSpeech.speak("swipe left to listen again and swipe right to return back in main menu", TextToSpeech.QUEUE_ADD, null);
            }
    }

    return false;
}

    public void onPause() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        super.onPause();

    }
}