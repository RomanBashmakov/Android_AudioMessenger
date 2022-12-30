package com.example.audiorecorder;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    public static float duration = (float) 0.3; // duration of a bit in sec
    public static int freq = 500; // Hz

    int REQUEST_WRITE_STORAGE_REQUEST_CODE = 1;
    int REQUEST_READ_STORAGE_REQUEST_CODE = 2;
    int RECORD_AUDIO_REQUEST_CODE = 3;

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
        setContentView(R.layout.activity_settings);

        setTransmitterSettingCallback = new SetTransmitterSettingCallbackHere();
        loadData();
        lvTransmitters = (ListView) findViewById(R.id.lvTransmitters);
        transmitterSettingsAdapter = new transmitterSettingsAdapter(this, transmitterSettingsList, setTransmitterSettingCallback);
        lvTransmitters.setAdapter(transmitterSettingsAdapter);

        setReceiverSettingCallback = new SetReceiverSettingCallbackHere();
        lvReceivers = (ListView) findViewById(R.id.lvReceivers);
        receiverSettingsAdapter = new receiverSettingsAdapter(this, receiverSettingsList, setReceiverSettingCallback);
        lvReceivers.setAdapter(receiverSettingsAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("transmitter settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(transmitterSettingsList);
        editor.putString("transmitters list", json);
        editor.apply();

        sharedPreferences = getSharedPreferences("receiver settings", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new Gson();
        json = gson.toJson(receiverSettingsList);
        editor.putString("receivers list", json);
        editor.apply();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("transmitter settings", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("transmitters list", null);
        Type type = new TypeToken<ArrayList<TransmitterSetting>>() {}.getType();
        transmitterSettingsList = gson.fromJson(json, type);

        if (transmitterSettingsList == null) {
            transmitterSettingsList = new ArrayList<>();
        }

        sharedPreferences = getSharedPreferences("receiver settings", MODE_PRIVATE);
        gson = new Gson();
        json = sharedPreferences.getString("receivers list", null);
        type = new TypeToken<ArrayList<ReceiverSetting>>() {}.getType();
        receiverSettingsList = gson.fromJson(json, type);

        if (receiverSettingsList == null) {
            receiverSettingsList = new ArrayList<>();
        }
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
        public void saveSetting(int Position) {
            SharedPreferences sharedPreferences = getSharedPreferences("saved transmitter setting", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(transmitterSettingsList.get(Position));
            editor.putString("transmitter setting", json);
            editor.apply();
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
        public void saveSetting(int Position) {
            SharedPreferences sharedPreferences = getSharedPreferences("saved receiver setting", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(receiverSettingsList.get(Position));
            editor.putString("receiver setting", json);
            editor.apply();
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