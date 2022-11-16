package com.example.audiorecorder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    public void toMessenger(View view)
    {
        Intent intent = new Intent(this, ChatMessages.class);
        startActivity(intent);
    }

    public void toChatActivity(View view)
    {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }


}