package org.tensorflow.lite.examples.detection.Calling.HandwrittenPhoneNumber;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.AttributeSet;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Status bar for the test app.
 *
 * <p>It is updated upon status changes announced by the StrokeManager.
 */
public class StatusTextView1 extends androidx.appcompat.widget.AppCompatTextView implements StrokeManager.StatusChangedListener1, RecognitionListener, TextToSpeech.OnUtteranceCompletedListener {
      StrokeManager strokeManager;
    SpeechRecognizer speechRecognizer;
    private static TextToSpeech textToSpeech;
    static int Size;
    static int counter;
    static ArrayList<String> a = new ArrayList<String>();
     int abxc;
     String n;
    static String separated,string;

    public StatusTextView1(@NonNull Context context) {
        super(context);

    }

    public StatusTextView1(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

    }

    void setStrokeManager(StrokeManager strokeManager) {
        this.strokeManager = strokeManager;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onStatusChanged1() {
        this.setText(this.strokeManager.getStatus1());
        Size = Integer.parseInt(this.strokeManager.getStatus1());
        textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                    textToSpeech.setSpeechRate((float) 0.7);
                    textToSpeech.setOnUtteranceCompletedListener(StatusTextView1.this);
                    if(Size==10) {
                        textToSpeech.speak("Phone number is ",TextToSpeech.QUEUE_FLUSH,null);
                        textToSpeech.speak(StrokeManager.getphoneno().toString() + "say yes to confirm ,or no to write the number again.", TextToSpeech.QUEUE_ADD,null,"hh");
                    }
                }
            }
        });

        if(Size==10) {
            StringBuilder strg = new StringBuilder();
            for(int j=0;j<Size;j++){
                strg = strg.append(StrokeManager.getphoneno().get(j));
            }
             separated = strg.toString().replaceAll("[^a-zA-Z0-9]", " ");

            @SuppressLint("Recycle") Cursor cursor = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

            if(cursor.moveToFirst()) {
                do {
                    String Name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String Num = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    a.add(Name.toLowerCase() + "@" + Num);
                } while (cursor.moveToNext());
            }
            for (int i = 0; i < a.size() - 1; i++) {
                if (a.get(i).contains(separated)) {
                    abxc = i;
                    break;
                }
            }
            n = a.get(abxc);
            String[] d = n.split("@");
           String number = "Name is "+d[0] + " \n"+"Phone number is " + d[1];
            String name = d[1];
             string = String.join(",", name.split(""));

            this.setText(number);
            textToSpeech.speak(number,TextToSpeech.QUEUE_FLUSH,null);
            textToSpeech.speak(StrokeManager.getphoneno().toString() + ", say yes to confirm ,or no to write the number again.", TextToSpeech.QUEUE_ADD,null,"hh");

        }
     }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Toast.makeText(getContext(), "Listening", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int i) {
        if(counter<1) {
            textToSpeech.speak("say something", TextToSpeech.QUEUE_FLUSH, null);
            final Handler h = new Handler(Looper.getMainLooper());
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    counter++;
                    startvoice();
                }
            }, 2000);
        }
        else {
            textToSpeech.speak("tap on the screen and say yes to call or double tap to return in main menu",TextToSpeech.QUEUE_FLUSH,null);
        }


    }

    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> res =bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String output = res.get(0);
        if(output.contains("yes")){
            //when person return from call we set count to 0 the user click on layout and say main menu
            DrawingView.count=0;
            textToSpeech.speak(
                    "calling",TextToSpeech.QUEUE_FLUSH,null);
            final Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(Intent.ACTION_CALL);
                    i.setData(Uri.parse("tel:" +string));
                    getContext().startActivity(i);
                }
            },2000);


        }
        if(output.contains("no")){
            StrokeManager.getphoneno().clear();
        }

    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }
    public void startvoice(){
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
        speechRecognizer.setRecognitionListener(StatusTextView1.this);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
        speechRecognizer.startListening(intent);
    }

    @Override
    public void onUtteranceCompleted(String s) {
        final Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                startvoice();
            }
        });
    }
}
