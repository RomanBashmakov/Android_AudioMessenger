package com.example.audiorecorder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.audiorecorder.application.SignalConstructor;

import java.util.ArrayList;

public class ChatMessagesActivity extends AppCompatActivity {

    public static float duration = (float) 0.3; // duration of a bit in sec
    public static int sampleRate = 44100; // Hz
    public static int freq = 500; // Hz

    ListView lvMessages;
    Button btnTransmit;
    TextView textToTransmit;

    ArrayList<MyMessage> messagesList;

    SignalConstructor signalConstructor;

    MyMessagesAdapter myMessagesAdapter;

    AmplitudeReader amplitudeReader;

    Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_messages);

        //take all the text to transmit
        textToTransmit = findViewById(R.id.textToTransmit);
        btnTransmit = findViewById(R.id.Transmit);
        lvMessages = findViewById(R.id.lvMessages);

        messagesList = new ArrayList<>();

        myMessagesAdapter = new MyMessagesAdapter(this, messagesList, new MyMessagesAdapterCallbackHere());
        lvMessages.setAdapter(myMessagesAdapter);

        h = new Handler(Looper.getMainLooper()) {
            public void handleMessage(android.os.Message msg)
            {
                if(msg.what == amplitudeReader.THREAD_END)
                {
                    amplitudeReader.interrupt();
                }
                else if(msg.what == amplitudeReader.MSG_ERROR)
                {
                    Toast.makeText(ChatMessagesActivity.this, "RECORD_AUDIO Permission not Granted. Input messages cannot be recognized", Toast.LENGTH_LONG).show();
                }
                else
                {
                    addReceivedMessage((String) msg.obj);
                }
            }
        };

        try
        {
            recordStart();//Start "listening" to
        }
        catch (Throwable t)
        {
            Toast.makeText(ChatMessagesActivity.this, "RECORD_AUDIO Permission not Granted", Toast.LENGTH_LONG).show();
            toMenu();
        }

        View.OnClickListener oclbtnTransmit = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //clear previous massage
                StringBuffer textBuffer = new StringBuffer();
                //take new massage
                textBuffer.append(textToTransmit.getText().toString());

                addTransmittedMessage(textBuffer.toString());

                signalConstructor = new SignalConstructor(textBuffer, true, sampleRate, freq, duration);
                signalConstructor.start();
            }
        };
        btnTransmit.setOnClickListener(oclbtnTransmit);
    }

    public void addReceivedMessage (String newMessage)
    {
        messagesList.add(new MyMessage(newMessage));
        myMessagesAdapter.notifyDataSetChanged();
    }

    public void addTransmittedMessage(String newMessage)
    {
        messagesList.add(new MyMessage(newMessage));
        myMessagesAdapter.notifyDataSetChanged();
    }

    public void recordStart()
    {
        amplitudeReader = new AmplitudeReader();
        amplitudeReader.addHandler(h);
        amplitudeReader.start();
    }

    public void recordStop(View v) {
        if (amplitudeReader != null)
        {
            amplitudeReader.stopRecording();
        }
    }

    public void stopTransmitting(View view)
    {
        if (signalConstructor != null)
        {
            signalConstructor.stopPlaying();
            signalConstructor.interrupt();
        }
    }

    public class MyMessagesAdapterCallbackHere implements MyMessagesAdapterCallback {
        @Override
        public void deleteItem(int Position) {
            messagesList.remove(Position);
            myMessagesAdapter.notifyDataSetChanged();
        }
    }

    public void toMenu()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

}