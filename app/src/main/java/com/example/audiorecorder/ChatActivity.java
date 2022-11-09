package com.example.audiorecorder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    static ArrayList<transmitterSetting> transmitterSettingsList;
    static transmitterSettingsAdapter transmitterSettingsAdapter;

    static ArrayList<receiverSetting> receiverSettingsList;
    static receiverSettingsAdapter receiverSettingsAdapter;

    ListView lvTransmitters;
    ListView lvReceivers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        transmitterSettingsList = new ArrayList<transmitterSetting>();
        transmitterSettingsAdapter = new transmitterSettingsAdapter(this, transmitterSettingsList);

        lvTransmitters = (ListView) findViewById(R.id.lvTransmitters);
        lvReceivers = (ListView) findViewById(R.id.lvReceivers);

        lvTransmitters.setAdapter(transmitterSettingsAdapter);
    }

    public void addTransmitterSetting(View view)
    {
        transmitterSettingsList.add(new transmitterSetting(1000,(float) 0.3));
        transmitterSettingsAdapter.notifyDataSetChanged();
    }

    public static void checkSetting(int Position)
    {
        for (transmitterSetting TS : transmitterSettingsList)
        {
            TS.setCheckedF(false);
        }
        transmitterSettingsList.get(Position).setCheckedF(true);
        transmitterSettingsAdapter.notifyDataSetChanged();
    }

    static public void deleteSetting(int Position)
    {
        transmitterSettingsList.remove(Position);
        transmitterSettingsAdapter.notifyDataSetChanged();
    }

    public void addReceiverSetting(View view)
    {
        receiverSettingsList.add(new receiverSetting(1000,(float) 0.3));
        receiverSettingsAdapter.notifyDataSetChanged();
    }
}