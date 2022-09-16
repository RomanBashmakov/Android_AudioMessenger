package com.example.audiorecorder;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.view.View;
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


public class MainActivity extends Activity {
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String fileName;
    int howManyTimes=0;
    int REQUEST_WRITE_STORAGE_REQUEST_CODE=1;
    int REQUEST_READ_STORAGE_REQUEST_CODE=2;
    int RECORD_AUDIO_REQUEST_CODE=3;
    AmplitudeReader thread;

    Handler h;

    void saveFile(short[] file) {
        String recordedValues= Arrays.toString(file);
            Context context = GlobalApplication.getAppContext();
            try {
                FileWriter out = new FileWriter(new File(context.getExternalFilesDir(null), fileName), true);
                out.write(recordedValues);
                out.close();
                howManyTimes++;
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //fileName = getExternalCacheDir().getAbsolutePath()+"/obtainedValues.txt";
        fileName = "obtainedValues.txt";

                super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        h = new Handler(Looper.getMainLooper()) {
            public void handleMessage(android.os.Message msg)
            {
                saveFile((short[]) msg.obj);
                if(msg.what==thread.THREAD_END)
                {
                    thread.interrupt();
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

}