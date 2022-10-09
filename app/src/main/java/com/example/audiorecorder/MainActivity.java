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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ddf.minim.effects.BandPass;


public class MainActivity extends Activity {
    private TextView BitDuration;
    private TextView BandWidthInput;
    private TextView frequencyFilter;
    private TextView dataPacketsCounter;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String fileName;
    private String filteredValues;
    private String processedValues;
    int howManyTimes=0;
    int REQUEST_WRITE_STORAGE_REQUEST_CODE=1;
    int REQUEST_READ_STORAGE_REQUEST_CODE=2;
    int RECORD_AUDIO_REQUEST_CODE=3;
    AmplitudeReader thread;
    BandPass bandpass;
    float[] floatedValues;
    String recordedValues;
    StringBuilder showPacketsSB;

    int filterFrequency=1000;
    int filterBW=100;
    float movingAverageAccumulatorPrevious=0; //sum of 'window' elements
    float duration=(float) 0.1;

    Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileName = "obtainedValues.txt";
        filteredValues= "filteredValues.txt";
        processedValues= "processedValues.txt";

        BitDuration=(TextView) findViewById(R.id.BitDuration);
        BandWidthInput=(TextView) findViewById(R.id.BandWidthInput);
        frequencyFilter=(TextView) findViewById(R.id.frequencyFilter);
        dataPacketsCounter=(TextView) findViewById(R.id.packetsCounter);

        bandpass=new BandPass(filterFrequency,filterBW,44100);

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

    public void playStart(View v)
    {
    }

    public void setHz(View v)
    {
        filterFrequency = Integer.parseInt(frequencyFilter.getText().toString()); // duration of sound in ms
    }

    public void setBW(View v)
    {
        filterBW = Integer.parseInt(BandWidthInput.getText().toString()); // duration of sound in ms
    }

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

    public void getMicPermission(View v) {
        ActivityCompat.requestPermissions(this,
                new String[]
                        {
                                Manifest.permission.RECORD_AUDIO
                        }, RECORD_AUDIO_REQUEST_CODE); // your request code
    }

    public void getStoragePermission(View v) {
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

    void saveFile(short[] file)
    {
        Context context = GlobalApplication.getAppContext();

        //NOT FILTERED
        StringBuilder sb = new StringBuilder(file.length);
        for (int i = 0; i < file.length; i++)
        {
            if (i > 0)
            {
                sb.append(" ");
            }
            sb.append(Short.toString(file[i]));
        }
        String recordedValues = sb.toString();
        try
        {
            FileWriter out = new FileWriter(new File(context.getExternalFilesDir(null), fileName), true);
            out.write(recordedValues);
            out.close();
            howManyTimes++;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        //print packets number
        showPacketsSB = new StringBuilder();
        showPacketsSB.append(this.getString(R.string.Packets));
        showPacketsSB.append(": ");
        showPacketsSB.append(String.valueOf(howManyTimes));
        dataPacketsCounter.setText(showPacketsSB.toString());

        //FILTERED
        floatedValues=new float[file.length];
        for (int i=0; i<file.length; i++)
        {
            floatedValues[i]=(float) file[i];
        }
        bandpass.process(floatedValues);

        StringBuilder sbFiltered = new StringBuilder(file.length);
        for (int i = 0; i < file.length; i++)
        {
            if (i > 0)
            {
                sbFiltered.append(" ");
            }
            sbFiltered.append(floatedValues[i]);
        }
        recordedValues = sbFiltered.toString();
        try
        {
            FileWriter out = new FileWriter(new File(context.getExternalFilesDir(null), filteredValues), true);
            out.write(recordedValues);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //PROCESSED
        for (int i=0; i<floatedValues.length; i++)
        {
            if(floatedValues[i]<0)
            {
                floatedValues[i]=-floatedValues[i];
            }
        }

        //moving average for the filtered data
        int movingAverageW=100; //window
        float movingAverageAccumulator=0; //sum of 'window' elements
        for (int i=0; i<(floatedValues.length); i++)
        {
            if(((i-movingAverageW/2)>=0)&&((i+movingAverageW/2)<floatedValues.length))
            {
                for(int w=0; w<=movingAverageW; w++)
                {
                    movingAverageAccumulator+=floatedValues[i-movingAverageW/2+w];
                }
                floatedValues[i]=movingAverageAccumulator/movingAverageW;
                movingAverageAccumulator=0; //sum of 'window' elements
                movingAverageAccumulatorPrevious=floatedValues[i];//
            }
            else
            {
                floatedValues[i]=movingAverageAccumulatorPrevious;
            }
        }

        StringBuilder sbProcessed = new StringBuilder(file.length);
        for (int i = 0; i < floatedValues.length; i++)
        {
            if (i > 0)
            {
                sbProcessed.append(" ");
            }
            sbProcessed.append(floatedValues[i]);
        }
        recordedValues = sbProcessed.toString();
        try
        {
            FileWriter out = new FileWriter(new File(context.getExternalFilesDir(null), processedValues), true);
            out.write(recordedValues);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}