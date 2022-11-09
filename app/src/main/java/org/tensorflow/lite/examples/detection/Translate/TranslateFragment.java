package org.tensorflow.lite.examples.detection.Translate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import org.tensorflow.lite.examples.detection.Home;
import org.tensorflow.lite.examples.detection.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragment view for handling translations
 */
public class TranslateFragment extends Fragment implements TextToSpeech.OnUtteranceCompletedListener, RecognitionListener {

     private TextToSpeech textToSpeech;
    SpeechRecognizer ur,en;
    TextView textView;
    static ArrayList<String> tt = new ArrayList<>();
     private TextToSpeech ktts;
    static String results;
    String urdu,english;
    private boolean isdownload=true;

    public static TranslateFragment newInstance() {
        return new TranslateFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.translate_fragment, container, false);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(checkIfAlreadyhavePermission()){
            Toast.makeText(getContext(), "Permission is granted", Toast.LENGTH_SHORT).show();

        }else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1);
        }

        ur = SpeechRecognizer.createSpeechRecognizer(view.getContext());
        ur.setRecognitionListener(this);
        en = SpeechRecognizer.createSpeechRecognizer(view.getContext());
        en.setRecognitionListener(this);
        textView = requireView().findViewById(R.id.result);
        final TextView textView2 = requireView().findViewById(R.id.tt);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!textToSpeech.isSpeaking()){
                    if(textView2.getText().toString().contains("English to Urdu")){
                        startEnglishVoiceInput();
                        return;
                    }
                    if(textView2.getText().toString().contains("Urdu to English")){
                        startUrduVoiceInput();
                        return;
                    }
                    startEnglishVoiceInput();
                }

            }
        });
        textView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                startActivity(new Intent(getContext(), Home.class));

                return true;
            }
        });

        textToSpeech = new TextToSpeech(requireView().getContext(), new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                            textToSpeech.setLanguage(new Locale("urd"));
                            textToSpeech.setSpeechRate(1f);
                 }
            }

        });
        ktts = new TextToSpeech(requireView().getContext(), new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    ktts.setLanguage(Locale.getDefault());
                    ktts.setSpeechRate(0.9f);
                    ktts.setOnUtteranceCompletedListener(TranslateFragment.this);
                }
            }
        });
        final TextView downloadedModelsTextView = view.findViewById(R.id.downloadedModels);


        final Handler h = new Handler(Looper.getMainLooper());
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!downloadedModelsTextView.getText().toString().contains("ur")){
                    ktts.speak("translating model is downloading. please do not close the app",TextToSpeech.QUEUE_FLUSH,null);
                    final Spinner targetLangSelector = requireView().findViewById(R.id.targetLangSelector);
                    final TranslateViewModel viewModel = ViewModelProviders.of(TranslateFragment.this).get(TranslateViewModel.class);
                    final ArrayAdapter<TranslateViewModel.Language> adapter = new ArrayAdapter<TranslateViewModel.Language>(getContext(), android.R.layout.simple_spinner_dropdown_item, viewModel.getAvailableLanguages());
                    targetLangSelector.setAdapter(adapter);
                    targetLangSelector.setSelection(adapter.getPosition(new TranslateViewModel.Language("ur")));
                    TranslateViewModel.Language language =
                            adapter.getItem(targetLangSelector.getSelectedItemPosition());
                    viewModel.downloadLanguage(language);

                }
                else {
                    ktts.speak("Welcome to Translator, say English to urdu for English to urdu and, say urdu to English for urdu to English, Press long on the screen to return in main menu", TextToSpeech.QUEUE_FLUSH, null, "en");
                    textToSpeech.setOnUtteranceCompletedListener(TranslateFragment.this);
                }
            }
        }, 1000);

         final ToggleButton targetSyncButton = view.findViewById(R.id.buttonSyncTarget);
         final Spinner targetLangSelector = view.findViewById(R.id.targetLangSelector);
        final TranslateViewModel viewModel = ViewModelProviders.of(this).get(TranslateViewModel.class);

        // Get available language list and set up source and target language spinners
        // with default selections.
        final ArrayAdapter<TranslateViewModel.Language> adapter =
                new ArrayAdapter<>(
                        getContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        viewModel.getAvailableLanguages());
         targetLangSelector.setAdapter(adapter);
         targetLangSelector.setSelection(adapter.getPosition(new TranslateViewModel.Language("ur")));

        targetSyncButton.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        TranslateViewModel.Language language =
                                adapter.getItem(targetLangSelector.getSelectedItemPosition());
                        if (isChecked) {
                            viewModel.downloadLanguage(language);
                        } else {
                            viewModel.deleteLanguage(language);
                        }
                    }
                });

        // Update sync toggle button states based on downloaded models list.
        viewModel.availableModels.observe(
                getViewLifecycleOwner(),
                new Observer<List<String>>() {
                    @Override
                    public void onChanged(@Nullable List<String> translateRemoteModels) {
                        String output =
                                requireContext().getString(R.string.downloaded_models_label, translateRemoteModels);
                        downloadedModelsTextView.setText(output);
                        if(downloadedModelsTextView.getText().toString().contains("ur")){
                            ktts.speak("tap on screen and say English to urdu for English to urdu and, say urdu to English for urdu to English, Press long on the screen to return in main menu", TextToSpeech.QUEUE_FLUSH, null);;
                        }
                        targetSyncButton.setChecked(
                                !viewModel.requiresModelDownload(
                                        adapter.getItem(targetLangSelector.getSelectedItemPosition()),
                                        translateRemoteModels));
                    }
                });
    }


    private void startUrduVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ur");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
        ur.startListening(intent);
    }
    private void startEnglishVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
        en.startListening(intent);
    }


    @Override
    public void onUtteranceCompleted(String s) {
        if(s.equals("ur")) {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startUrduVoiceInput();

                }
            });
        }
        if(s.equals("en")) {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final TextView downloadedModelsTextView = requireView().findViewById(R.id.downloadedModels);
                    if(downloadedModelsTextView.getText().toString().contains("ur")){
                        ktts.speak("tap on the screen and say",TextToSpeech.QUEUE_FLUSH,null);
                    }

                }
            });

        }

    }

    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }
     // add toast to check whether it is working or not

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Toast.makeText(getActivity(), "Listening...", Toast.LENGTH_LONG).show();
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
        final TextView textView2 = requireView().findViewById(R.id.tt);
        tt = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
         if(tt.get(0).contains("English to Urdu")){
            textView2.setText(tt.get(0));
            ktts.speak("tap on the screen and tell me the word that you want to translate", TextToSpeech.QUEUE_FLUSH, null);;
        }
        if(tt.get(0).contains("Urdu to English")){
            textView2.setText(tt.get(0));
            textToSpeech.speak("اسکرین پر تھپتھپائیں اور ہمیں وہ لفظ بتائیں جس کا آپ ترجمہ کرنا چاہتے ہیں۔", TextToSpeech.QUEUE_FLUSH, null);
        }
        if(tt.get(0).contains("main menu")){
            startActivity(new Intent(getContext(), Home.class));
        }
        if(textView2.getText().toString().contains("English to Urdu")){
            ArrayList<String> result = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            TranslatorOptions options =
                    new TranslatorOptions.Builder()
                            .setSourceLanguage(TranslateLanguage.ENGLISH)
                            .setTargetLanguage(TranslateLanguage.URDU)
                            .build();

            Translator englishurduTranslator =
                    Translation.getClient(options);
            english = result.get(0).replace("English to Urdu","");
            DownloadConditions conditions = new DownloadConditions.Builder()
                    .requireWifi()
                    .build();

            englishurduTranslator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener(new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object v) {
                            englishurduTranslator.translate(english)
                                    .addOnSuccessListener(
                                            new OnSuccessListener() {
                                                @Override
                                                public void onSuccess(Object translatedText) {
                                                     textView.setText((String) translatedText);
                                                    if(textView.getText().toString().equals("")){
                                                        Toast.makeText(getContext(), translatedText.toString(), Toast.LENGTH_SHORT).show();
                                                    }
                                                    else {
                                                        results = translatedText.toString();
                                                        Toast.makeText(getContext(), translatedText.toString(), Toast.LENGTH_LONG).show();
                                                        textToSpeech.speak(translatedText.toString(), TextToSpeech.QUEUE_FLUSH, null);
                                                        ktts.speak("tap on the screen and say the word",TextToSpeech.QUEUE_ADD,null);
                                                    }
                                                    Log.i("TAG", "Translation is " + (String) translatedText);
                                                }
                                            })
                                    .addOnCanceledListener(new OnCanceledListener() {
                                        @Override
                                        public void onCanceled() {
                                            Toast.makeText(getContext(), "Downloading cancelled...", Toast.LENGTH_SHORT).show();
                                        }
                                    })

                                    .addOnFailureListener(
                                            new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                        }
                    })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Model couldn’t be downloaded or other internal error.
                                    Toast.makeText(getContext(), "Model could n’t be downloaded ", Toast.LENGTH_SHORT).show();

                                }
                            });



        }
        if(textView2.getText().toString().contains("Urdu to English")){
            ArrayList<String> result = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            TranslatorOptions options =
                    new TranslatorOptions.Builder()
                            .setSourceLanguage(TranslateLanguage.URDU)
                            .setTargetLanguage(TranslateLanguage.ENGLISH)
                            .build();

            final Translator englishurduTranslator =
                    Translation.getClient(options);

            urdu = result.get(0).replace("Urdu to English","");


            DownloadConditions conditions = new DownloadConditions.Builder()
                    .requireWifi()
                    .build();


            englishurduTranslator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener(new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object v) {
                            englishurduTranslator.translate(urdu)
                                    .addOnSuccessListener(
                                            new OnSuccessListener() {
                                                @Override
                                                public void onSuccess(Object translatedText) {
                                                    textView.setText(translatedText.toString());
                                                    if(textView.getText().toString().equals("")){
                                                        Toast.makeText(getContext(), translatedText.toString(), Toast.LENGTH_SHORT).show();
                                                    }
                                                    else {
                                                        Toast.makeText(getContext(), translatedText.toString(), Toast.LENGTH_LONG).show();
                                                        ktts.speak((String) translatedText, TextToSpeech.QUEUE_FLUSH, null);
                                                        ktts.speak("tap on the screen and say the word",TextToSpeech.QUEUE_ADD,null);
                                                    }
                                                    Log.i("TAG", "Translation is " + (String) translatedText);
                                                }
                                            })
                                    .addOnFailureListener(
                                            new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    Log.e("Error", "Translation faliled " + e);
                                                }
                                            });
                        }
                    })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Model couldn’t be downloaded or other internal error.
                                    Log.e("Error", "Model could n’t be downloaded " + e);

                                }
                            });

        }





    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], int[] grantResults) {
        if (requestCode == 1) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(getContext(), "permission granted", Toast.LENGTH_SHORT).show();
            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(getContext(), "Permission denied .please allow to record the audio", Toast.LENGTH_SHORT).show();
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
