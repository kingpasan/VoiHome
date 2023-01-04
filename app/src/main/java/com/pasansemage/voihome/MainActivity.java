package com.pasansemage.voihome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton btnTalkMic;
    private RecyclerView chatRecylerView;
    private MessagesAdapter messagesAdapter;
    private ArrayList<Message> messageArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        btnTalkMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText( getApplicationContext(),"click complete", Toast.LENGTH_LONG).show();
                Message msg = new Message("Hello Pasan", 1);
                messageArrayList.add(msg);
                messagesAdapter.notifyDataSetChanged();
            }
        });
    }

    public void init(){
        btnTalkMic = findViewById(R.id.btnTalkMic);
        chatRecylerView = findViewById(R.id.chatBoxRecyclerView);
        messageArrayList = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        chatRecylerView.setLayoutManager(linearLayoutManager);
        messagesAdapter = new MessagesAdapter(MainActivity.this, messageArrayList);
        chatRecylerView.setAdapter(messagesAdapter);

    }
}