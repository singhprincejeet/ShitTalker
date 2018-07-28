package com.example.princejeetsinghsan.shittalker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import me.aflak.arduino.Arduino;
import me.aflak.arduino.ArduinoListener;

public class MainActivity extends AppCompatActivity {
    Arduino arduino ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arduino = new Arduino(getApplicationContext());
        arduino.addVendorId(6790);
        checkPermission();

        final TextView speechTextView = findViewById(R.id.speechTextView);

        final SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent speechRecognizerIntent = getSpeechRecognizerIntent();

        startListening(speechRecognizer);

        findViewById(R.id.micButton).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        speechRecognizer.stopListening();
                        speechTextView.setHint("You will see input here");
                        break;

                    case MotionEvent.ACTION_DOWN:
                        speechRecognizer.startListening(speechRecognizerIntent);
                        speechTextView.setText("");
                        speechTextView.setHint("Listening...");
                        //fixme
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        arduino.setArduinoListener(new ArduinoListener() {
            @Override
            public void onArduinoAttached(UsbDevice device) {
                arduino.open(device);
            }

            @Override
            public void onArduinoDetached() {
                // arduino detached from phone
            }

            @Override
            public void onArduinoMessage(byte[] bytes) {
                String message = new String(bytes);
                // new message received from arduino
            }

            @Override
            public void onArduinoOpened() {
                // you can start the communication
                String str = "Hello Arduino !";
                arduino.send(str.getBytes());
            }

            @Override
            public void onUsbPermissionDenied() {

            }
        });

    }

    private void startListening(SpeechRecognizer speechRecognizer) {
        final TextView speechTextView = findViewById(R.id.speechTextView);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

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
                //getting all the matches
                ArrayList<String> matches = bundle
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                //displaying the first match
                if (matches != null){
                    speechTextView.setText(matches.get(0));
                    proccessAudio(matches.get(0)) ;

                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
    }

    private void proccessAudio(String s) {
        String processed = s.toLowerCase() ;
        if (processed.contains("go")){
            processSpeedRequest(processed.substring(processed.indexOf("go")));
        }
        if(processed.contains("say")){
            processSpeachRequest(processed.substring(processed.indexOf("say")));
        }
        if(processed.contains("mario")){
            startEasterEgg("mario") ; 
        }
        arduino.send(s.getBytes());
    }

    private void startEasterEgg(String mario) {
    }

    public static final String FORWARD = "F" ; 
    
    private void processSpeachRequest(String speach) {
        if(speach.contains("forward")){
            setRobotSpeed(FORWARD) ; 
        }
    }

    private void setRobotSpeed(String forward) {
    }

    private void processSpeedRequest(String speed) {
    }

    private Intent getSpeechRecognizerIntent() {
        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());
        return speechRecognizerIntent;
    }


    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        arduino.unsetArduinoListener();
        arduino.close();
    }

}