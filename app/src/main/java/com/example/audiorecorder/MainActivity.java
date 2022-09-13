package com.example.audiorecorder;

import android.Manifest;
import android.app.Activity;
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

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

public class MainActivity extends Activity {
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String fileName;
    int REQUEST_WRITE_STORAGE_REQUEST_CODE=1;
    int REQUEST_READ_STORAGE_REQUEST_CODE=2;
    int RECORD_AUDIO_REQUEST_CODE=3;
    AmplitudeReader thread;

    Handler h;

    void saveFile(short[] file) {
        String recordedValues= Arrays.toString(file);
        try
        {
            // отрываем поток для записи
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    openFileOutput("recordedValues.txt", MODE_APPEND)));
            // пишем данные
            bw.write("Содержимое файла");
            // закрываем поток
            bw.close();
            Toast.makeText(MainActivity.this, "Data has been saved", Toast.LENGTH_SHORT).show();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        h = new Handler(Looper.getMainLooper()) {
            public void handleMessage(android.os.Message msg)
            {
                saveFile((short[]) msg.obj);
            }
        };

        thread = new AmplitudeReader();
        thread.addHandler(h);
    }

    public void recordStart(View v)
    {
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