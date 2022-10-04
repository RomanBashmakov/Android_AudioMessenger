package com.example.audiorecorder;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.audiorecorder.application.GlobalApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.logging.Logger;

import ddf.minim.effects.BandPass;


public class MainActivity extends Activity {
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String fileName;
    private String filteredValues;
    int howManyTimes=0;
    int REQUEST_WRITE_STORAGE_REQUEST_CODE=1;
    int REQUEST_READ_STORAGE_REQUEST_CODE=2;
    int RECORD_AUDIO_REQUEST_CODE=3;
    AmplitudeReader thread;
    BandPass bandpass;
    float[] floatedValues;
    String recordedValues;

    int filterFrequency=1000;
    int filterBW=100;
    float duration=(float) 0.1;

    Handler h;

    void saveFile(short[] file) {
        Context context = GlobalApplication.getAppContext();
        //not filtered
        try {
            //recordedValues= Arrays.toString(file);
            StringBuilder sb = new StringBuilder(file.length);
            for (int i = 0; i < file.length; ++i) {
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(Short.toString(file[i]));
            }
            String recordedValues = sb.toString();

            FileWriter out = new FileWriter(new File(context.getExternalFilesDir(null), fileName), true);
            out.write(recordedValues);
            out.close();
            howManyTimes++;
        } catch (IOException e) {
            e.printStackTrace();
        }
        //filtered
        try {
            floatedValues=new float[file.length];
            for (int i=0; i<file.length; i++)
            {
                floatedValues[i]=(float) file[i];
            }
            bandpass.process(floatedValues);


            StringBuilder sb = new StringBuilder(file.length);
            for (int i = 0; i < file.length; ++i) {
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(Float.toString(file[i]));
            }
            recordedValues = sb.toString();
            //recordedValues= Arrays.toString(floatedValues);
            FileWriter outFiltered = new FileWriter(new File(context.getExternalFilesDir(null), filteredValues), true);
            outFiltered.write(recordedValues);
            outFiltered.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        fileName = "obtainedValues.txt";
        filteredValues= "filteredValues.txt";

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bandpass=new BandPass(1000,100,44100);

        h = new Handler(Looper.getMainLooper()) {
            public void handleMessage(android.os.Message msg)
            {
                if(msg.what==thread.THREAD_END)
                {
                    thread.interrupt();
                }
                else
                {
                    saveFile((short[]) msg.obj);
                }
            }
        };
    }

    public void recordStart(View v)
    {
        thread = new AmplitudeReader();
        thread.addHandler(h);
        thread.start();
    }

    public void recordStop(View v) {
        if (thread != null)
        {
            thread.stopRecording();
        }
    }

    public void getPermission(View v) {
        ActivityCompat.requestPermissions(this,
            new String[]
                {
                    Manifest.permission.RECORD_AUDIO
                }, RECORD_AUDIO_REQUEST_CODE); // your request code

        ActivityCompat.requestPermissions(this,
            new String[]
                {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_WRITE_STORAGE_REQUEST_CODE); // your request code
    }

    public void playStart(View v)
    {
    }

    TextView frequencyFilter=(TextView) findViewById(R.id.frequencyFilter);
    public void setHz(View v)
    {
        filterFrequency = Integer.parseInt(frequencyFilter.getText().toString()); // duration of sound in ms
    }

    TextView BandWidthInput=(TextView) findViewById(R.id.BandWidthInput);
    public void setBW(View v)
    {
        filterBW = Integer.parseInt(BandWidthInput.getText().toString()); // duration of sound in ms
    }

    TextView BitDuration=(TextView) findViewById(R.id.BitDuration);
    public void setBD(View v)
    {
        duration = Float.parseFloat(BitDuration.getText().toString())/1000; // duration of sound in ms
    }

    public void playStop(View v)
    {
        Toast.makeText(MainActivity.this, Integer.toString(howManyTimes), Toast.LENGTH_SHORT).show();
    }

    private void releaseRecorder()
    {
    }

    private void releasePlayer()
    {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        releaseRecorder();
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
                Toast.makeText(MainActivity.this, "WRITE_STORAGE Permission Granted", Toast.LENGTH_SHORT) .show();
            }
            else
            {
                Toast.makeText(MainActivity.this, "WRITE_STORAGE Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        }
        else if (requestCode == REQUEST_READ_STORAGE_REQUEST_CODE)
        {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(MainActivity.this, "READ_STORAGE Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(MainActivity.this, "READ_STORAGE Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == RECORD_AUDIO_REQUEST_CODE)
        {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(MainActivity.this, "RECORD_AUDIO_REQUEST_CODE Permission Granted", Toast.LENGTH_SHORT).show();
            } else
            {
                Toast.makeText(MainActivity.this, "RECORD_AUDIO_REQUEST_CODE Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void toTransmit(View view)
    {
        Intent intent = new Intent(this, MainActivity2.class);
        startActivity(intent);
    }

    public void toReceive(View view)
    {
    }

}