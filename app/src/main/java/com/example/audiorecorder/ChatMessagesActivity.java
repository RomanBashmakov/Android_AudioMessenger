package com.example.audiorecorder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.audiorecorder.application.SignalConstructor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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

        loadTransmitterSetting();

        //take all the text to transmit
        textToTransmit = findViewById(R.id.textToTransmit);
        btnTransmit = findViewById(R.id.Transmit);
        lvMessages = findViewById(R.id.lvMessages);

        loadChat();

        myMessagesAdapter = new MyMessagesAdapter(this, messagesList, new MyMessagesAdapterCallbackHere());
        lvMessages.setAdapter(myMessagesAdapter);

        h = new Handler (Looper.getMainLooper()) {
            public void handleMessage(android.os.Message msg)
            {
                Log.d("msg", "msg 2");
                if (msg.what == amplitudeReader.THREAD_END)
                {
                    amplitudeReader.interrupt();
                }
                else if (msg.what == amplitudeReader.MSG_ERROR)
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
            recordStart();//Start "listening"
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

    @Override
    public void onPause()
    {
        super.onPause();
        recordStop();
        saveChat();
    }

    public void saveChat()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("saved chat messages", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(messagesList);
        editor.putString("saved messages", json);
        editor.apply();
    }

    public void loadChat()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("saved chat messages", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("saved messages", null);
        Type type = new TypeToken<ArrayList<MyMessage>>() {}.getType();
        messagesList = gson.fromJson(json, type);

        if (messagesList == null) {
            messagesList = new ArrayList<>();
        }
    }

    public ReceiverSetting loadReceiverSetting() {
        SharedPreferences sharedPreferences = getSharedPreferences("saved receiver setting", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("receiver setting", null);
        Type type = new TypeToken<ReceiverSetting>() {}.getType();
        ReceiverSetting receiverSetting = gson.fromJson(json, type);
        return receiverSetting;
    }

    public void loadTransmitterSetting()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("saved transmitter setting", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("transmitter setting", null);
        Type type = new TypeToken<TransmitterSetting>() {}.getType();
        TransmitterSetting transmitterSetting = gson.fromJson(json, type);

        if (transmitterSetting != null)
        {
            duration = transmitterSetting.duration;
            freq = transmitterSetting.frequency;
        }
    }

    public void addReceivedMessage (String newMessage)
    {
        Log.d("msg", "msg 3" + newMessage);

        String currentTime = new SimpleDateFormat("HH:mm' 'dd.MM.yyyy", Locale.getDefault()).format(new Date());
        messagesList.add(new MyMessage(newMessage, false, currentTime));
        myMessagesAdapter.notifyDataSetChanged();
    }

    public void addTransmittedMessage(String newMessage)
    {
        String currentTime = new SimpleDateFormat("HH:mm' 'dd.MM.yyyy", Locale.getDefault()).format(new Date());
        messagesList.add(new MyMessage(newMessage, true, currentTime));
        myMessagesAdapter.notifyDataSetChanged();
    }

    public void recordStart()
    {
        if (AmplitudeReader.mIsRunning == false)
        {
            amplitudeReader = new AmplitudeReader(loadReceiverSetting());
            amplitudeReader.addHandler(h);
            amplitudeReader.start();
        }
    }

    public void recordStop() {
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