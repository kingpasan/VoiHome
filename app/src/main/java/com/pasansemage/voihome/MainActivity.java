package com.pasansemage.voihome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pasansemage.voihome.adapters.MessagesAdapter;
import com.pasansemage.voihome.models.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.RecursiveTask;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

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
                btnTalkMic.setImageResource(R.drawable.microphone_on);
                speechRecognizer.startListening(speechIntent);
            }
        });
    }

    public void init() {
        btnTalkMic = findViewById(R.id.btnTalkMic);
        chatRecyclerView = findViewById(R.id.chatBoxRecyclerView);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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
                btnTalkMic.setImageResource(R.drawable.microphone);
                btnTalkMic.setEnabled(true);
            }

            @Override
            public void onError(int i) {
                btnTalkMic.setImageResource(R.drawable.microphone);
                btnTalkMic.setEnabled(true);

                Message msg = new Message("I didn't get it, please say it again", 2);
                messageArrayList.add(msg);

                messagesAdapter.notifyDataSetChanged();

                textToSpeech.speak("i didn't get it, please say it again", TextToSpeech.QUEUE_FLUSH, null);
            }

            @Override
            public void onResults(Bundle bundle) {

                btnTalkMic.setEnabled(true);
                btnTalkMic.setImageResource(R.drawable.microphone);

                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                Message msg = new Message(data.remove(0), 1);

                messageArrayList.add(msg);


                conversationHandling(msg);

                messagesAdapter.notifyDataSetChanged();

            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
    }

    private void conversationHandling(Message msg) {

        if(msg.getMessage().toLowerCase().contains("hi") || msg.getMessage().toLowerCase().contains("hey") || msg.getMessage().toLowerCase().contains("hello")){
            Message repMsg = new Message("Hi, What i can do for you?", 2);
            messageArrayList.add(repMsg);
            textToSpeech.speak(repMsg.getMessage(), TextToSpeech.QUEUE_FLUSH, null);
        }

        if (msg.getMessage().toLowerCase().contains("switch") || msg.getMessage().toLowerCase().contains("turn") ) {

            if (msg.getMessage().toLowerCase().contains("on")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SSHCommand("tdtool --on 3");
                    }
                }).start();

                Message repMsg = new Message("Turning on light", 2);
                messageArrayList.add(repMsg);
                textToSpeech.speak(repMsg.getMessage(), TextToSpeech.QUEUE_FLUSH, null);


            }

            if (msg.getMessage().toLowerCase().contains("off")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SSHCommand("tdtool --off 3");
                    }
                }).start();
                Message repMsg = new Message("Turning off light", 2);
                messageArrayList.add(repMsg);
                textToSpeech.speak(repMsg.getMessage(), TextToSpeech.QUEUE_FLUSH, null);
            }
        }

        if (msg.getMessage().toLowerCase().contains("check") || msg.getMessage().toLowerCase().contains("current") || msg.getMessage().toLowerCase().contains("what") ) {
            if (msg.getMessage().toLowerCase().contains("humidity")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        String data = SSHCommand("tdtool --list-sensors");

                        int removeIndex = data.indexOf("]") - 1;

                        StringBuilder newData = new StringBuilder(data);
                        newData.deleteCharAt(removeIndex);

                        try {

                            JSONObject object = getCorrectLatestReadings(newData.toString());

                            String txtHumidity = "Current humidity is " + object.get("humidity").toString() + "%";
                            Message msgRep = new Message(txtHumidity, 2);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    messageArrayList.add(msgRep);
                                    textToSpeech.speak(msgRep.getMessage(), TextToSpeech.QUEUE_FLUSH, null);
                                    messagesAdapter.notifyDataSetChanged();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }

            if (msg.getMessage().toLowerCase().contains("temperature")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        String data = SSHCommand("tdtool --list-sensors");

                        int removeIndex = data.indexOf("]") - 1;

                        StringBuilder newData = new StringBuilder(data);
                        newData.deleteCharAt(removeIndex);

                        try {

                            JSONObject object = getCorrectLatestReadings(newData.toString());

                            String txtHumidity = "Current temperature is celsius " + object.get("temperature").toString();
                            Message msgRep = new Message(txtHumidity, 2);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    messageArrayList.add(msgRep);
                                    textToSpeech.speak(msgRep.getMessage(), TextToSpeech.QUEUE_FLUSH, null);
                                    messagesAdapter.notifyDataSetChanged();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        }
    }

    private JSONObject getCorrectLatestReadings(String jsonQuery) throws JSONException {
        JSONArray obj = new JSONArray(jsonQuery);

        int ageLowest = Integer.parseInt(obj.getJSONObject(0).get("age").toString());
        ;
        int objectID = 0;

        for (int a = 0; a < obj.length(); a++) {
            int nextAge = Integer.parseInt(obj.getJSONObject(a).get("age").toString());
            if (ageLowest > nextAge) {
                ageLowest = nextAge;
                objectID = a;
            }
        }

        return obj.getJSONObject(objectID);
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

    private String SSHCommand(String command) {

        String hostName = "192.168.10.221";
        String userName = "pi";
        String password = "IoT@2021";

        try {
            Connection connection = new Connection(hostName);
            connection.connect();

            boolean isAuthenticated = connection.authenticateWithPassword(userName, password);

            if (isAuthenticated == false) {
                throw new IOException("Authentication Failed");
            }

            Session session = connection.openSession();
            session.execCommand(command);

            InputStream inputStream = new StreamGobbler(session.getStdout());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String data = null;

            while (true) {
                String line = bufferedReader.readLine();
                if (line == null)
                    break;
                data = line;
            }

            session.close();
            connection.close();

            return data;
        } catch (IOException e) {
            System.out.println("Error on SSHCommand : " + e.getMessage());
            return null;
        }
    }
}