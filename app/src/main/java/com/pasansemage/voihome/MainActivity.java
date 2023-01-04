package com.pasansemage.voihome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pasansemage.voihome.adapters.MessagesAdapter;
import com.pasansemage.voihome.models.Message;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton btnTalkMic;
    private RecyclerView chatRecyclerView;
    private MessagesAdapter messagesAdapter;
    private ArrayList<Message> messageArrayList;

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private Intent speechIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        btnTalkMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speechRecognizer.startListening(speechIntent);
            }
        });
    }

    public void init() {
        btnTalkMic = findViewById(R.id.btnTalkMic);
        chatRecyclerView = findViewById(R.id.chatBoxRecyclerView);

        initSpeechRecognizer();
        initTextToSpeech();
        initChatRecyclerView();
    }


    private void initSpeechRecognizer() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkRecordAudioPermission();
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                btnTalkMic.setEnabled(false);
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
                btnTalkMic.setEnabled(true);
            }

            @Override
            public void onError(int i) {
                btnTalkMic.setEnabled(true);

                Message msg = new Message("I didn't get it, please say it again", 2);
                messageArrayList.add(msg);

                messagesAdapter.notifyDataSetChanged();

                textToSpeech.speak("i didn't get it, please say it again", TextToSpeech.QUEUE_FLUSH, null);
            }

            @Override
            public void onResults(Bundle bundle) {

                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                Message msg = new Message(data.remove(0),1 );
                messageArrayList.add(msg);

                messagesAdapter.notifyDataSetChanged();

                btnTalkMic.setEnabled(true);
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });
    }

    private void initChatRecyclerView() {
        messageArrayList = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(linearLayoutManager);

        messagesAdapter = new MessagesAdapter(MainActivity.this, messageArrayList);
        chatRecyclerView.setAdapter(messagesAdapter);
    }

    private void checkRecordAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
    }
}