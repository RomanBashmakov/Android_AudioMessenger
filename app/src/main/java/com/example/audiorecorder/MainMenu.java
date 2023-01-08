package com.example.audiorecorder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.casualcoding.reedsolomon.EncoderDecoder;
import com.google.zxing.common.reedsolomon.ReedSolomonException;
import com.google.zxing.common.reedsolomon.Util;

public class MainMenu extends AppCompatActivity {

    EncoderDecoder encoderDecoder = new EncoderDecoder();
    String message = new String("EncoderDecoder Example");
    byte[] data = message.getBytes();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        checkEncoderDecoder();
    }

    public void toMessenger(View view)
    {
        Intent intent = new Intent(this, ChatMessagesActivity.class);
        startActivity(intent);
    }

    public void toChatActivity(View view)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void checkEncoderDecoder()
    {

    }
}