//Transmitter
package com.example.audiorecorder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.audiorecorder.application.SignalConstructor;

import java.util.ArrayList;

//Receiver
public class MainActivity2 extends AppCompatActivity {

    public static float duration = (float) 0.3; // duration of a bit in sec
    public static int sampleRate = 44100; // Hz
    public static int freq = 500; // Hz
    public static int numSamples = (int) (sampleRate * duration);

    SignalConstructor signalConstructor;

    SetTransmitterSettingCallbackHere setTransmitterSettingCallback;

    public class SetTransmitterSettingCallbackHere implements SetTransmitterSettingCallback
    {
        @Override
        public void deleteSetting(int Position)
        {
            transmitterSettingsList.remove(Position);
            transmitterSettingsAdapter.notifyDataSetChanged();
        }

        @Override
        public void checkSetting(int Position)
        {
            for (transmitterSetting TS : transmitterSettingsList)
            {
                TS.setCheckedF(false);
            }
            transmitterSettingsList.get(Position).setCheckedF(true);
            transmitterSettingsAdapter.notifyDataSetChanged();
        }

        @Override
        public void setBitDuration(float BD)
        {
            duration = BD;
        }

        @Override
        public void setFrequency(int frequency)
        {
            freq = frequency;
        }

        @Override
        public void newBDSetting(float typedBD, int Position)
        {
            transmitterSettingsList.get(Position).setDuration(typedBD);
        }

        @Override
        public void newFrequencySetting(int typedFrequency, int Position)
        {
            transmitterSettingsList.get(Position).setFrequency(typedFrequency);
        }
    }

    static ArrayList<transmitterSetting> transmitterSettingsList = new ArrayList<transmitterSetting>();
    transmitterSettingsAdapter transmitterSettingsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //take all the text to transmit
        TextView textToTransmit = findViewById(R.id.textToTransmit);
        Button btnTransmit = findViewById(R.id.Transmit);

        View.OnClickListener oclbtnTransmit = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //clear previous massage
                StringBuffer textBuffer = new StringBuffer();
                //take new massage
                textBuffer.append(textToTransmit.getText().toString());

                signalConstructor = new SignalConstructor(textBuffer, true, sampleRate, freq, duration);
                signalConstructor.start();
            }
        };
        btnTransmit.setOnClickListener(oclbtnTransmit);

        transmitterSettingsAdapter = new transmitterSettingsAdapter(this, transmitterSettingsList, setTransmitterSettingCallback);
        ListView lvTransmitters = findViewById(R.id.lvTransmitters);
        lvTransmitters.setAdapter(transmitterSettingsAdapter);
    }

    public void stopTransmitting(View view)
    {
        if (signalConstructor != null)
        {
            signalConstructor.stopPlaying();
            signalConstructor.interrupt();
        }
    }

    public void addSetting(View view)
    {
        transmitterSettingsList.add(new transmitterSetting(1000,(float) 0.3));
        transmitterSettingsAdapter.notifyDataSetChanged();
    }

    public void toReceive(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void toTransmit(View view)
    {
    }

}