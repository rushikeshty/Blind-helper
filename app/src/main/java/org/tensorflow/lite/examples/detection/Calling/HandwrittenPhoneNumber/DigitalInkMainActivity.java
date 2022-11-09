package org.tensorflow.lite.examples.detection.Calling.HandwrittenPhoneNumber;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.collect.ImmutableSortedSet;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier;

import org.tensorflow.lite.examples.detection.R;

import java.util.Locale;
import java.util.Set;

/** Main activity which creates a StrokeManager and connects it to the DrawingView. */
public class DigitalInkMainActivity extends AppCompatActivity
        implements StrokeManager.DownloadedModelsChangedListener, TextToSpeech.OnUtteranceCompletedListener, RecognitionListener {
    @VisibleForTesting
    //in strokemanger we have created different interfaces and methods to perform the recognize, clear draw
    //and download the model.
    final StrokeManager strokeManager = new StrokeManager();
    //languageadapter is used for selecting language that we want to write
    private ArrayAdapter<ModelLanguageContainer> languageAdapter;
    static TextToSpeech textToSpeech;
    static Toast toast;
    static DrawingView drawingView;
    static SpeechRecognizer speechRecognizer;
    static String languageCode;

    public static void Toaststring(String text) {
        toast = Toast.makeText(drawingView.getContext(),text ,Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handwritphonenumber);

        Spinner languageSpinner = findViewById(R.id.languages_spinner);
        //model will download automatically but if incase it not downloaded then click on download button.
        findViewById(R.id.download_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strokeManager.download();
            }
        });

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);

        StatusTextView statusTextView = findViewById(R.id.status_text_view);
        StatusTextView1 statusTextView1 = findViewById(R.id.status_text_view2);
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.getDefault());
                    textToSpeech.setSpeechRate((float) 0.7);
                    //check model is downloaded or not
                    if(!ModelLanguageContainer.downloaded){
                        textToSpeech.speak("Writing model is downloading... please wait",TextToSpeech.QUEUE_FLUSH,null);
                     }
                    else {
                        textToSpeech.speak("write phone number", TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
        });
        drawingView =findViewById(R.id.drawing_view);


        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, PackageManager.PERMISSION_GRANTED);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 0);
        }
        findViewById(R.id.clear_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strokeManager.reset();
                DrawingView drawingView =findViewById(R.id.drawing_view);
                drawingView.clear();

            }
        });

        findViewById(R.id.recognize_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strokeManager.recognize();
            }
        });
        DrawingView drawingView = findViewById(R.id.drawing_view);
        drawingView.setStrokeManager(strokeManager);
        statusTextView.setStrokeManager(strokeManager);
        strokeManager.setStatusChangedListener(statusTextView);
        statusTextView1.setStrokeManager(strokeManager);
        strokeManager.setStatusChangedListener1(statusTextView1);
        strokeManager.setContentChangedListener(drawingView);
        strokeManager.setDownloadedModelsChangedListener(this);
        strokeManager.setClearCurrentInkAfterRecognition(true);
        strokeManager.setTriggerRecognitionAfterInput(false);

        languageAdapter = populateLanguageAdapter();
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageAdapter);
        strokeManager.refreshDownloadedModelsStatus();
        textToSpeech.setOnUtteranceCompletedListener(this);
        languageCode = languageAdapter.getItem(0).getLanguageTag();
        strokeManager.setActiveModel(languageCode);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!ModelLanguageContainer.downloaded) {
                    strokeManager.download();
                    Toast.makeText(getApplicationContext(), "Downloading...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Toast.makeText(getApplicationContext(), languageCode, Toast.LENGTH_SHORT).show();

        strokeManager.reset();
    }



    public static void cleardraw(){

        drawingView.clear();

    }

    public  void startvoice(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
        speechRecognizer.startListening(intent);
    }

    @Override
    public void onUtteranceCompleted(String s) {

        startvoice();
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Toast.makeText(this,"Listening...",Toast.LENGTH_LONG).show();
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

    }

    @Override
    public void onResults(Bundle bundle) {

    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }



    private static class ModelLanguageContainer implements Comparable<ModelLanguageContainer> {
        private final String label;
        @Nullable private final String languageTag;
        static boolean downloaded;

        private ModelLanguageContainer(String label, @Nullable String languageTag) {
            this.label = label;
            this.languageTag = languageTag;
        }

        /**
         * Populates and returns a real model identifier, with label, language tag and downloaded
         * status.
         */
        public static ModelLanguageContainer createModelContainer(String label, String languageTag) {
            // Offset the actual language labels for better readability
            return new ModelLanguageContainer(label, languageTag);
        }

        /** Populates and returns a label only, without a language tag. */
        public static ModelLanguageContainer createLabelOnly(String label) {
            return new ModelLanguageContainer(label, null);
        }

        public String getLanguageTag() {
            return languageTag;
        }

        public void setDownloaded(boolean downloaded) {
            ModelLanguageContainer.downloaded = downloaded;
        }

        @NonNull
        @Override
        public String toString() {
            if (languageTag == null) {
                return label;
            } else if (downloaded) {
                textToSpeech.speak("Model download succesfully, write the phone number",TextToSpeech.QUEUE_FLUSH,null);
                return "   [D] " + label;
            } else {
                return "   " + label;
            }
        }

        @Override
        public int compareTo(ModelLanguageContainer o) {
            return label.compareTo(o.label);
        }
    }

    @Override
    public void onDownloadedModelsChanged(Set<String> downloadedLanguageTags) {
        for (int i = 0; i < languageAdapter.getCount(); i++) {
            ModelLanguageContainer container = languageAdapter.getItem(i);
            container.setDownloaded(downloadedLanguageTags.contains(container.languageTag));

        }
        languageAdapter.notifyDataSetChanged();
    }

//display all model languages in spinner but we write only number so display any laguage so here we display "English en US"
    private ArrayAdapter<ModelLanguageContainer> populateLanguageAdapter() {
        ArrayAdapter<ModelLanguageContainer> languageAdapter =
                new ArrayAdapter<ModelLanguageContainer>(this, android.R.layout.simple_spinner_item);

        // Manually add non-text models first
        ImmutableSortedSet.Builder<ModelLanguageContainer> textModels =
                ImmutableSortedSet.naturalOrder();
        for (DigitalInkRecognitionModelIdentifier modelIdentifier :
                DigitalInkRecognitionModelIdentifier.forRegionSubtag("US")) {
            if (modelIdentifier.getLanguageSubtag().equals("en")) {
                StringBuilder label = new StringBuilder();
                label.append(new Locale(modelIdentifier.getLanguageSubtag()).getDisplayName());

                if (modelIdentifier.getRegionSubtag() != null) {
                    label.append(" (").append(modelIdentifier.getRegionSubtag()).append(")");
                }

                if (modelIdentifier.getScriptSubtag() != null) {
                    label.append(", ").append(modelIdentifier.getScriptSubtag()).append(" Script");
                }
                textModels.add(
                        ModelLanguageContainer.createModelContainer(
                                label.toString(), modelIdentifier.getLanguageTag()));
            }
            languageAdapter.addAll(textModels.build());
        }
        return languageAdapter;
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
        StrokeManager.getphoneno().clear();
    }

}
