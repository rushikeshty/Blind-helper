package org.tensorflow.lite.examples.detection.Navigation;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.tensorflow.lite.examples.detection.Location.GetAllData;
import org.tensorflow.lite.examples.detection.R;

import java.util.ArrayList;
import java.util.Locale;

public class Navigation extends AppCompatActivity  {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private TextToSpeech texttospeech;
    float x1, x2;
    private FusedLocationProviderClient fusedLocationClient;//One of the location APIs in google play services
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;//Request Code is used to check which permission called this function. // This request code is provided when the user is prompt for permission.
    private LocationCallback locationCallback;

    private LocationAddressResultReceiver addressResultReceiver;//receives the address results
    static String currentAdd,destinationname;
     protected Context context;
    private  android.location.Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation);
        addressResultReceiver = new LocationAddressResultReceiver(new Handler());

        texttospeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    texttospeech.setLanguage(Locale.US);
                    texttospeech.setSpeechRate(1f);
                    texttospeech.speak("welcome to blind navigation, swipe right and tell me the name of destination, where you want to go. or swipe left, to know your current location.", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                currentLocation = locationResult.getLocations().get(0);
                getAddress();
            }
        };
        startLocationUpdates();//call this function to check location permission



    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new
                            String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(2000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }


    private void getAddress() {
        if (!Geocoder.isPresent()) {
            Toast.makeText(Navigation.this, "Can't find current address, ",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, GetAllData.class);
        intent.putExtra("add_receiver", addressResultReceiver);
        intent.putExtra("add_location", currentLocation);
        startService(intent);
    }
    private void turnGPSOn(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps")){ //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
    }


    private class LocationAddressResultReceiver extends ResultReceiver {
        LocationAddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == 0) {
                 getAddress();
            }
            if (resultCode == 1) {
                Toast.makeText(Navigation.this, "Address not found, ", Toast.LENGTH_SHORT).show();
            }
             currentAdd = resultData.getString("address_result");

        }
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

    public boolean onTouchEvent(MotionEvent touchEvent) {
        switch (touchEvent.getAction()) {

            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                if (x1 < x2) {
        texttospeech.speak("you location is at"+currentAdd, TextToSpeech.QUEUE_FLUSH, null);

                }
                if (x1 > x2) {
                    texttospeech.stop();
                    startVoiceInput();
                    break;
                }

                break;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                destinationname = result.get(0);
                texttospeech.speak("getting direction please wait",TextToSpeech.QUEUE_FLUSH,null);
                Intent intent = new Intent(this, MapsActivity.class);
                intent.putExtra("name", destinationname);
                startActivity(intent);
                /**User will go to google maps application for direction from its location to the destination
                *and our application will closed.
                 * */
                final Handler h = new Handler(Looper.getMainLooper());
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finishAffinity();
                        System.exit(0);
                    }
                },2000);

                if(result.get(0).contains("location")){
                    texttospeech.speak("your location is "+currentAdd, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
         }

    }
    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }


}