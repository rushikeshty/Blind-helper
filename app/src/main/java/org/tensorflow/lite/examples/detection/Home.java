package org.tensorflow.lite.examples.detection;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

 import org.tensorflow.lite.examples.detection.Calling.CallActivity;
import org.tensorflow.lite.examples.detection.Location.LocationActivity;
import org.tensorflow.lite.examples.detection.Message.MessageReader;
import org.tensorflow.lite.examples.detection.Navigation.Navigation;
import org.tensorflow.lite.examples.detection.Translate.TranslateActivity;

import java.util.ArrayList;
import java.util.Locale;

public class Home extends AppCompatActivity {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private static int firstTime = 0;
    private TextView mVoiceInputTv;
    float x1, x2, y1, y2;
    private static TextToSpeech textToSpeech;
     static String Readmessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(checkIfAlreadyhavePermission()){
            Toast.makeText(getApplicationContext(), "Permission is granted", Toast.LENGTH_SHORT).show();
        }else {
            ActivityCompat.requestPermissions(Home.this,
                    new String[]{Manifest.permission.READ_SMS},
                    1);
        }
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                    textToSpeech.setSpeechRate(1f);
                    if (firstTime == 0)
                        textToSpeech.speak("Welcome to Blind App. Swipe right to listen the features of the app and swipe left and say what you want", TextToSpeech.QUEUE_FLUSH, null);
                    //when user return from another activities to main activities.
                    if(firstTime!=0)
                        textToSpeech.speak("you are in main menu. just swipe left and say what you want", TextToSpeech.QUEUE_FLUSH, null);

                }
            }
        });


        mVoiceInputTv = (TextView) findViewById(R.id.voiceInput);

    }
    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public boolean onTouchEvent(MotionEvent touchEvent) {
        firstTime = 1;
        switch (touchEvent.getAction()) {

            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                y2 = touchEvent.getY();
                if (x1 < x2) {
                    firstTime = 1;
                    Intent intent = new Intent(Home.this, Features.class);
                    startActivity(intent);

                }
                if (x1 > x2) {
                    firstTime = 1;
                    startVoiceInput();

                    break;
                }


                break;
        }

        return false;
    }


    private void startVoiceInput() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            a.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                mVoiceInputTv.setText(result.get(0));
                if (mVoiceInputTv.getText().toString().contains("exit")) {
                    finishAffinity();
                    System.exit(0);
                }
               else if (mVoiceInputTv.getText().toString().endsWith("want to read")) {
                    Intent intent = new Intent(getApplicationContext(), OCRReader.class);
                    startActivity(intent);
                     mVoiceInputTv.setText(null);
                }
               else if (mVoiceInputTv.getText().toString().contains("calculator")) {
                    Intent intent = new Intent(getApplicationContext(), Calculator.class);
                    startActivity(intent);
                    mVoiceInputTv.setText(null);
                }
               else if (mVoiceInputTv.getText().toString().contains("time and date")) {
                    Intent intent = new Intent(getApplicationContext(), DateAndTime.class);
                    startActivity(intent);
                    mVoiceInputTv.setText(null);
                }
                else if (mVoiceInputTv.getText().toString().contains("weather")) {
                    Intent intent = new Intent(getApplicationContext(), Weather.class);
                    startActivity(intent);
                    mVoiceInputTv.setText(null);
                }
               else if (mVoiceInputTv.getText().toString().contains("navigation")) {
                    Intent intent = new Intent(getApplicationContext(), Navigation.class);
                    startActivity(intent);
                    mVoiceInputTv.setText(null);
                }
               else if (mVoiceInputTv.getText().toString().contains("Translator")) {
                    Intent intent = new Intent(getApplicationContext(), TranslateActivity.class);
                    startActivity(intent);
                    mVoiceInputTv.setText(null);
                }
               else if (mVoiceInputTv.getText().toString().contains("translat")) {
                    Intent intent = new Intent(getApplicationContext(), TranslateActivity.class);
                    startActivity(intent);
                    mVoiceInputTv.setText(null);
                }
                else if (mVoiceInputTv.getText().toString().contains("battery")) {
                    Intent intent = new Intent(getApplicationContext(), Battery.class);
                    startActivity(intent);
                    mVoiceInputTv.setText(null);
                }
               else if (mVoiceInputTv.getText().toString().contains("yes")) {
                    textToSpeech.speak("  Say Read for reading,  calculator for calculator,  time and date,  weather for weather,  battery for battery. Do you want to listen again", TextToSpeech.QUEUE_FLUSH, null);
                    mVoiceInputTv.setText(null);
                } else if ((mVoiceInputTv.getText().toString().contains("no"))) {
                    textToSpeech.speak("then Swipe right and say what you want", TextToSpeech.QUEUE_FLUSH, null);

                } else if (mVoiceInputTv.getText().toString().contains("location")) {

                    Intent intent = new Intent(getApplicationContext(), LocationActivity.class);
                    startActivity(intent);
                    mVoiceInputTv.setText(null);
                }
               else if(mVoiceInputTv.getText().toString().contains("read message")){
                    Readmessage = "read message";
                     Intent i = new Intent(Home.this, MessageReader.class);
                    i.putExtra("read message", Readmessage);
                    textToSpeech.speak("Getting messages , Please wait", TextToSpeech.QUEUE_FLUSH, null);
                    startActivity(i);

                }
              else   if(mVoiceInputTv.getText().toString().contains("unread")){
                    Readmessage = "unread message";
                    Intent i = new Intent(Home.this, MessageReader.class);
                    i.putExtra("unread message",Readmessage);
                    startActivity(i);

                }
               else if(mVoiceInputTv.getText().toString().contains("call")){
                     Intent i = new Intent(Home.this, CallActivity.class);
                     startActivity(i);

                }

                else if(mVoiceInputTv.getText().toString().contains("Android message")){
                    Readmessage = "unread message";
                    Intent i = new Intent(Home.this, MessageReader.class);
                    i.putExtra("unread message",Readmessage);
                    startActivity(i);

                }
               else if(mVoiceInputTv.getText().toString().contains("yesterday")){
                    Readmessage = "yesterday message";
                    Intent i = new Intent(Home.this, MessageReader.class);
                    i.putExtra("yesterday message",Readmessage);
                    startActivity(i);

                }
  if (mVoiceInputTv.getText().toString().contains("exit")) {
                        mVoiceInputTv.setText(null);
                        finishAffinity();
                    }
  else {
      textToSpeech.speak("Do not understand Swipe left Say again", TextToSpeech.QUEUE_FLUSH, null);
  }

                }
            }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == 1) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(getApplicationContext(), "permission granted ... Reading messages", Toast.LENGTH_SHORT).show();
            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(Home.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
            }
            return;

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void onPause() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        super.onPause();

    }
}
