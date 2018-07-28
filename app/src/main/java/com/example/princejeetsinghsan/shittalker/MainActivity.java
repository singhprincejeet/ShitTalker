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
import android.support.annotation.IntDef;
import android.support.annotation.StringDef;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Locale;

import me.aflak.arduino.Arduino;
import me.aflak.arduino.ArduinoListener;

public class MainActivity extends AppCompatActivity {

    Arduino arduino ;

    @Override protected void onCreate(Bundle savedInstanceState) {
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

    @Override protected void onStart() {
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

    @Override protected void onDestroy() {
        super.onDestroy();
        arduino.unsetArduinoListener();
        arduino.close();
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


    private static final int MAX_LENGTH  = 20 ;

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({"F", "B", "L", "R"}) public @interface Message{}

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SPEED,TURN,EASTER_EGG}) public @interface PrefixTag{}

    private static final int SPEED = 1 ;
    private static final int TURN = 2 ;
    private static final int EASTER_EGG = 3 ;

    private void proccessAudio(String s)
    {
        String processed = s.toLowerCase() ;

        if(processed.contains("f***")){
            if (pissOffLevel < 5){
                pissOffLevel++;
            }
        }

        if (processed.contains("go f*** yourself")) {
            sendArduinoCode(EASTER_EGG, DIRECTIONS[GO_NUTS]);
        }

        if(processed.contains("please")){
            pissOffLevel = 0;
        }

        if (processed.contains("go")) {
            processSpeedRequest(processed.substring(processed.indexOf("go")));
        }
        //Easter Eggs
        else if(processed.contains("mario")){
            startEasterEgg("mario") ;
        }
    }

    private static final int FORWARD = 0;
    private static final int BACKWARD = 1;
    private static final int LEFT = 2;
    private static final int RIGHT = 3;
    private static final int GO_NUTS = 4;

    private static final String[] DIRECTIONS = {"F", "B", "L", "R", "N"};

    private static int pissOffLevel = 0;

    private void processSpeedRequest(String go) {
        int random = (int) (Math.random()*pissOffLevel);
        if (random > 1) {
            int randomDirection = (int) (Math.random()*1);
            sendArduinoCode(SPEED, DIRECTIONS[randomDirection]);
        } else {
            if (go.contains("forward")) {
                sendArduinoCode(SPEED, DIRECTIONS[FORWARD]);
            } else if (go.contains("backward") || go.contains("reverse")
                    || go.contains("back")
                    ) {
                sendArduinoCode(SPEED, DIRECTIONS[BACKWARD]);
            } else if (go.contains("left")) {
                sendArduinoCode(SPEED, DIRECTIONS[LEFT]);
            } else if (go.contains("right")) {
                sendArduinoCode(SPEED, DIRECTIONS[RIGHT]);
            }
        }
    }


    private void startEasterEgg(String easterEgg)
    {
        throw new RuntimeException("Not implemented") ;
//        sendArduinoCode(EASTER_EGG,null);
    }



    private void sendArduinoCode(@PrefixTag  int prefix,@Message String message ){
        if (message.length() > 1){
            message = "$" + message + "&" ;
        }

        if (message.length() > MAX_LENGTH){
            message = message.substring(0,MAX_LENGTH);
        }

        String prefixChar = null ;
        switch (prefix){
            case SPEED:
                prefixChar = "S" ;
                break;
            case TURN:
                prefixChar = "T" ;
                break;
            case EASTER_EGG:
                prefixChar = "E" ;
                break;
        }
        if (prefixChar != null){
            message = prefixChar + message ;
            arduino.send(message.getBytes());
        }

    }


}