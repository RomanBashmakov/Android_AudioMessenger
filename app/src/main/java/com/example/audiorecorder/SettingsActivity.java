package com.example.audiorecorder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    public static float duration = (float) 0.3; // duration of a bit in sec
    public static int freq = 500; // Hz

    ArrayList<TransmitterSetting> transmitterSettingsList;
    transmitterSettingsAdapter transmitterSettingsAdapter;

    ArrayList<ReceiverSetting> receiverSettingsList;
    receiverSettingsAdapter receiverSettingsAdapter;

    ListView lvTransmitters;
    ListView lvReceivers;

    SetTransmitterSettingCallbackHere setTransmitterSettingCallback;
    SetReceiverSettingCallbackHere setReceiverSettingCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        setTransmitterSettingCallback = new SetTransmitterSettingCallbackHere();
        transmitterSettingsList = new ArrayList<TransmitterSetting>();
        lvTransmitters = (ListView) findViewById(R.id.lvTransmitters);
        transmitterSettingsAdapter = new transmitterSettingsAdapter(this, transmitterSettingsList, setTransmitterSettingCallback);
        lvTransmitters.setAdapter(transmitterSettingsAdapter);

        setReceiverSettingCallback = new SetReceiverSettingCallbackHere();
        receiverSettingsList = new ArrayList<ReceiverSetting>();
        lvReceivers = (ListView) findViewById(R.id.lvReceivers);
        receiverSettingsAdapter = new receiverSettingsAdapter(this, receiverSettingsList, setReceiverSettingCallback);
        lvReceivers.setAdapter(receiverSettingsAdapter);
    }

    public void addTransmitterSetting(View view)
    {
        transmitterSettingsList.add(new TransmitterSetting(1000,(float) 0.3));
        transmitterSettingsAdapter.notifyDataSetChanged();
    }

    public void addReceiverSetting(View view)
    {
        receiverSettingsList.add(new ReceiverSetting(1000,(float) 0.3));
        receiverSettingsAdapter.notifyDataSetChanged();
    }

    public void toMenu(View view)
    {
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }

    public class SetTransmitterSettingCallbackHere implements SetTransmitterSettingCallback{

        @Override
        public void deleteSetting(int Position) {
            transmitterSettingsList.remove(Position);
            transmitterSettingsAdapter.notifyDataSetChanged();
        }

        @Override
        public void checkSetting(int Position) {
            for (TransmitterSetting TS : transmitterSettingsList){
                TS.setCheckedF(false);
            }
            transmitterSettingsList.get(Position).setCheckedF(true);
            transmitterSettingsAdapter.notifyDataSetChanged();
        }

        @Override
        public void setBitDuration(float BD) {
            duration = BD;
        }

        @Override
        public void setFrequency(int frequency) {
            freq = frequency;
        }

        @Override
        public void newBDSetting(float typedBD, int Position) {
            transmitterSettingsList.get(Position).setDuration(typedBD);
        }

        @Override
        public void newFrequencySetting(int typedFrequency, int Position) {
            transmitterSettingsList.get(Position).setFrequency(typedFrequency);
        }
    }

    public class SetReceiverSettingCallbackHere implements SetReceiverSettingCallback{

        @Override
        public void deleteSetting(int Position) {
            receiverSettingsList.remove(Position);
            receiverSettingsAdapter.notifyDataSetChanged();
        }

        @Override
        public void checkSetting(int Position) {
            for (ReceiverSetting RS : receiverSettingsList){
                RS.setCheckedF(false);
            }
            receiverSettingsList.get(Position).setCheckedF(true);
            receiverSettingsAdapter.notifyDataSetChanged();
        }

        @Override
        public void setBitDuration(float BD) {
            duration = BD;
        }

        @Override
        public void setFrequency(int frequency) {
            freq = frequency;
        }

        @Override
        public void newBDSetting(float typedBD, int Position) {
            receiverSettingsList.get(Position).setDuration(typedBD);
        }

        @Override
        public void newFrequencySetting(int typedFrequency, int Position) {
            receiverSettingsList.get(Position).setFrequency(typedFrequency);
        }
    }
}