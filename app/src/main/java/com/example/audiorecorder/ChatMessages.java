package com.example.audiorecorder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.audiorecorder.application.SignalConstructor;

import java.util.ArrayList;

public class ChatMessages extends AppCompatActivity {

    public static float duration = (float) 0.3; // duration of a bit in sec
    public static int sampleRate = 44100; // Hz
    public static int freq = 500; // Hz

    ListView lvMessages;
    Button btnTransmit;
    TextView textToTransmit;

    ArrayList<MyMessage> messagesList;

    SignalConstructor signalConstructor;

    MyMessagesAdapter myMessagesAdapter;

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


        View.OnClickListener oclbtnTransmit = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //clear previous massage
                StringBuffer textBuffer = new StringBuffer();
                //take new massage
                textBuffer.append(textToTransmit.getText().toString());

                addMessage(textBuffer.toString());

                signalConstructor = new SignalConstructor(textBuffer, true, sampleRate, freq, duration);
                signalConstructor.start();
            }
        };
        btnTransmit.setOnClickListener(oclbtnTransmit);
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

    public void addMessage(String newMessage)
    {
        messagesList.add(new MyMessage(newMessage));
        myMessagesAdapter.notifyDataSetChanged();
    }
}