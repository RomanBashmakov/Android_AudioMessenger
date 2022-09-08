package com.example.audiorecorder;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends Activity {

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String fileName;
    int REQUEST_WRITE_STORAGE_REQUEST_CODE=1;
    int REQUEST_READ_STORAGE_REQUEST_CODE=2;
    int RECORD_AUDIO_REQUEST_CODE=3;

    //-----------------------------------
    private void requestAppPermissions()
    {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            return;
        }

        ActivityCompat.requestPermissions(this,
            new String[]
                {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_WRITE_STORAGE_REQUEST_CODE); // your request code

    }
    //-----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + System.currentTimeMillis() + ".3gp";
        //requestAppPermissions();

        // Record to the external cache directory for visibility
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.3gp";

    }

    public void recordStart(View v) {

        releaseRecorder();

        File outFile = new File(fileName);
        if (outFile.exists()) {
            outFile.delete();
        }

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mediaRecorder.setOutputFile(fileName);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void recordStop(View v) {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
        }
    }

    public void getPermission(View v) {
        ActivityCompat.requestPermissions(this,
            new String[]
                {
                    Manifest.permission.RECORD_AUDIO
                }, RECORD_AUDIO_REQUEST_CODE); // your request code
    }

    public void playStart(View v) {
        try {
            releasePlayer();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fileName);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playStop(View v) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
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