package com.example.audiorecorder;

import android.Manifest;
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

public class SettingsActivity extends AppCompatActivity {

    public static float duration = (float) 0.3; // duration of a bit in sec
    public static int sampleRate = 44100; // Hz
    public static int freq = 500; // Hz

    int REQUEST_WRITE_STORAGE_REQUEST_CODE = 1;
    int REQUEST_READ_STORAGE_REQUEST_CODE = 2;
    int RECORD_AUDIO_REQUEST_CODE = 3;

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

        recordStart();//Start "listening" to

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

        h = new Handler(Looper.getMainLooper()) {
            public void handleMessage(android.os.Message msg)
            {
                if(msg.what == amplitudeReader.THREAD_END)
                {
                    amplitudeReader.interrupt();
                }
                else
                {
                    addReceivedMessage((String) msg.obj);
                }
            }
        };
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



    public void getMicPermission(View view) {
        ActivityCompat.requestPermissions(this,
                new String[]
                        {
                                Manifest.permission.RECORD_AUDIO
                        }, RECORD_AUDIO_REQUEST_CODE); // your request code
    }

    public void getStoragePermission(View view) {
        ActivityCompat.requestPermissions(this,
                new String[]
                        {
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }, REQUEST_WRITE_STORAGE_REQUEST_CODE); // your request code
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == REQUEST_WRITE_STORAGE_REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(SettingsActivity.this, "WRITE_STORAGE Permission Granted", Toast.LENGTH_SHORT) .show();
            }
            else
            {
                Toast.makeText(SettingsActivity.this, "WRITE_STORAGE Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        }
        else if (requestCode == REQUEST_READ_STORAGE_REQUEST_CODE)
        {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(SettingsActivity.this, "READ_STORAGE Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(SettingsActivity.this, "READ_STORAGE Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == RECORD_AUDIO_REQUEST_CODE)
        {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(SettingsActivity.this, "RECORD_AUDIO_REQUEST_CODE Permission Granted", Toast.LENGTH_SHORT).show();
            } else
            {
                Toast.makeText(SettingsActivity.this, "RECORD_AUDIO_REQUEST_CODE Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}