//Receiver
package com.example.audiorecorder;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import java.util.ArrayList;

import ddf.minim.effects.BandPass;


public class MainActivity extends Activity {
    private TextView BitDuration;
    private TextView BandWidthInput;
    private TextView frequencyFilter;
    private TextView dataPacketsCounter;
    private TextView messageOutput;
    private TextView messageData;

    private String fileName;
    private String filteredValues;
    private String processedValues;
    int howManyTimes=0;
    int REQUEST_WRITE_STORAGE_REQUEST_CODE = 1;
    int REQUEST_READ_STORAGE_REQUEST_CODE = 2;
    int RECORD_AUDIO_REQUEST_CODE = 3;
    AmplitudeReader thread;
    BandPass bandpass;
    float[] floatedValues;
    float[] movingAverageValues;
    String recordedValues;
    StringBuilder showPacketsSB;
    StringBuilder showMessageSB;
    StringBuilder showMessageDataSB;
    int datum = 0;

    int dataSampleRate = MainActivity2.sampleRate;
    int filterFrequency = 500;
    int filterBW = 100;
    float movingAverageAccumulatorPrevious = 0; //sum of 'window' elements
    float duration = (float) 0.3;//=300ms

    ArrayList<Boolean> bitsInMessage1 = new ArrayList<>(1000);
    ArrayList<Boolean> bitsInMessage2 = new ArrayList<>(1000);
    int bitIndex1 = 0;//the last and current index
    int bitIndex2 = 0;//the last and current index for shifted measurement

    int sampleIndex = 0;//the last and current index when preambula is detected
    int sampleIndex1 = 0;//the last and current index for non-shifted measurement
    int sampleIndex2 = 0;//the last and current index for shifted measurement
    float bitThreshold = 1000;//to compare with
    int numberToEnd = 0;//between the last measurement and bufLength
    int numSamples = 0;//between the measurements
    boolean isPreambula = false;// if preambula is detected = true

    boolean isBufferEnd = false;// if buffer end is riched = true
    boolean isBufferEnd1 = false;
    boolean isBufferEnd2 = false;

    int preambulaCounter1 = 0;
    int preambulaCounter2 = 0;

    Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileName = "obtainedValues.txt";
        filteredValues = "filteredValues.txt";
        processedValues = "processedValues.txt";

        BitDuration = (TextView) findViewById(R.id.BitDuration);
        BandWidthInput = (TextView) findViewById(R.id.BandWidthInput);
        frequencyFilter = (TextView) findViewById(R.id.frequencyFilter);
        dataPacketsCounter = (TextView) findViewById(R.id.packetsCounter);
        messageData = (TextView) findViewById(R.id.messageData);

        messageOutput = (TextView) findViewById(R.id.messageOutput);
        showMessageDataSB = new StringBuilder();

        bandpass = new BandPass(filterFrequency,filterBW,dataSampleRate);

        showMessageDataSB = new StringBuilder();

        numSamples = MainActivity2.numSamples;
        sampleIndex2 = numSamples/2;

        //booleanList initialization
        for (int i = 0; i < bitsInMessage1.size(); ++i){
            bitsInMessage1.add(false);
        }
        for (int i = 0; i < bitsInMessage2.size(); ++i){
            bitsInMessage2.add(false);
        }

        h = new Handler(Looper.getMainLooper()) {
            public void handleMessage(android.os.Message msg)
            {
                if(msg.what == thread.THREAD_END)
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
        isPreambula=false;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            sb.append(Short.toString(file[i]));
            sb.append(" ");
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
            floatedValues[i]=file[i];
        }
        bandpass.process(floatedValues);

        StringBuilder sbFiltered = new StringBuilder(file.length);
        for (int i = 0; i < file.length; i++)
        {
            sbFiltered.append(floatedValues[i]);
            sbFiltered.append(" ");
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
        movingAverageValues=new float[file.length];
        for (int i=0; i<floatedValues.length; i++)
        {
            if(floatedValues[i]<0)
            {
                floatedValues[i]=-floatedValues[i];
            }
        }

        //moving average for the filtered data
        int movingAverageW=200; //window
        float movingAverageAccumulator=0; //sum of 'window' elements
        for (int i=0; i<(floatedValues.length); i++)
        {
            if(((i-movingAverageW/2)>=0)&&((i+movingAverageW/2)<floatedValues.length))
            {
                for(int w=0; w<=movingAverageW; w++)
                {
                    movingAverageAccumulator+=floatedValues[i-movingAverageW/2+w];
                }
                movingAverageValues[i]=movingAverageAccumulator/movingAverageW;
                movingAverageAccumulator=0; //sum of 'window' elements
                movingAverageAccumulatorPrevious=movingAverageValues[i];
            }
            else
            {
                movingAverageValues[i]=movingAverageAccumulatorPrevious;
            }
        }

        StringBuilder sbProcessed = new StringBuilder(file.length);
        for (int i = 0; i < movingAverageValues.length; i++)
        {
            sbProcessed.append(movingAverageValues[i]);
            sbProcessed.append(" ");
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

        //1 Looking for preambula. Non-shifted
        if(!isPreambula)
        {
            while( !isBufferEnd1 && !isPreambula )
            {
                if ((preambulaCounter1 % 2) == 0)
                {
                    if ( sampleIndex1 < file.length)
                    {
                        if (movingAverageValues[sampleIndex1] > bitThreshold)
                        {
                            preambulaCounter1++;
                        }
                        else
                        {
                            preambulaCounter1 = 0;
                        }
                    }
                    //если индекс не помещается в этом буфере
                    else
                    {
                        sampleIndex1 = sampleIndex1 - file.length;
                        isBufferEnd1 = true;
                    }
                }
                else
                {
                    if ( sampleIndex1 < file.length)
                    {
                        if (movingAverageValues[sampleIndex1] < bitThreshold)
                        {
                            preambulaCounter1++;
                        }
                        else
                        {
                            preambulaCounter1 = 0;
                        }
                    }
                    //если индекс не помещается в этом буфере
                    else
                    {
                        sampleIndex1 = sampleIndex1 - file.length;
                        isBufferEnd1 = true;
                    }
                }

                //IF sampleIndex bigger than buffer size THEN interrupt 'while'
                if(isBufferEnd1) break;

                //if needed sample in this(current) massive
                if ( (sampleIndex1 + numSamples) < file.length)
                {
                    sampleIndex1 = sampleIndex1 + numSamples;
                }
                //if needed sample is in the next massive
                else
                {
                    numberToEnd = file.length - sampleIndex1;
                    sampleIndex1 = numSamples - numberToEnd;
                    isBufferEnd1=true;
                }

                if (preambulaCounter1 == 9)
                {
                    isPreambula = true;
                    sampleIndex = sampleIndex1;
                    isBufferEnd = isBufferEnd1;

                    //print Preambula
                    showMessageSB = new StringBuilder();
                    showMessageSB.append("Preambula has been detected");
                    messageOutput.setText(showMessageSB.toString());
                }
            }
            isBufferEnd1 = false;
        }

        //2 Looking for preambula. Shifted numSamples/2 to the right
        if( !isPreambula )
        {
            while( !isBufferEnd2 && !isPreambula )
            {
                if( (preambulaCounter2 % 2) == 0)
                {
                    if ( sampleIndex2 < file.length)
                    {
                        if (movingAverageValues[sampleIndex2] > bitThreshold) {
                            preambulaCounter2++;
                        } else {
                            preambulaCounter2 = 0;
                        }
                    }
                    //если индекс не помещается в этом буфере
                    else
                    {
                        sampleIndex2 = sampleIndex2 - file.length;
                        isBufferEnd2 = true;
                    }
                }
                else
                {
                    if ( sampleIndex2 < file.length)
                    {
                        if(movingAverageValues[sampleIndex2] < bitThreshold)
                        {
                            preambulaCounter2++;
                        }
                        else
                        {
                            preambulaCounter2 = 0;
                        }
                    }
                    //если индекс не помещается в этом буфере
                    else
                    {
                        sampleIndex2 = sampleIndex2 - file.length;
                        isBufferEnd2 = true;
                    }
                }

                //IF sampleIndex bigger than buffer size THEN interrupt 'while'
                if(isBufferEnd2) break;

                //if needed sample in this(current) massive
                if((sampleIndex2 + numSamples) < file.length)
                {
                    sampleIndex2 = sampleIndex2 + numSamples;
                }
                //if needed sample is in the next massive
                else
                {
                    numberToEnd = file.length - sampleIndex2;
                    sampleIndex2 = numSamples - numberToEnd;
                    isBufferEnd2 = true;
                }

                if(preambulaCounter2 == 9)
                {
                    isPreambula = true;
                    sampleIndex = sampleIndex2;
                    isBufferEnd = isBufferEnd2;

                    //print Preambula
                    showMessageSB = new StringBuilder();
                    showMessageSB.append("Preambula has been detected");
                    messageOutput.setText(showMessageSB.toString());
                }
            }
            isBufferEnd2 = false;
        }

        //3 Reading the message
        if( isPreambula )
        {
            while(!isBufferEnd)
            {
                if ( sampleIndex < file.length )
                {
                    if (movingAverageValues[sampleIndex] > bitThreshold)
                    {
                        //showMessageDataSB.append("1");
                        //messageData.setText(showMessageDataSB.toString());
                        datum = datum | (1 << (bitIndex1-1));
                    }
                    else
                    {
                        //showMessageDataSB.append("0");
                        //messageData.setText(showMessageDataSB.toString());
                    }
                    bitIndex1++;
                    if ( bitIndex1 == 8)
                    {
                        showMessageDataSB.append( (char) datum );
                        messageData.setText(showMessageDataSB.toString());
                        bitIndex1 = 0;
                        datum = 0;
                    }
                }
                //если индекс не помещается в этом буфере
                else
                {
                    sampleIndex = sampleIndex - file.length;
                    isBufferEnd = true;
                }

                //IF sampleIndex is bigger than buffer size THEN interrupt 'while'
                if (isBufferEnd) break;

                //if needed sample in this(current) massive
                if( (sampleIndex + numSamples) < file.length)
                {
                    sampleIndex = sampleIndex + numSamples;
                }
                //if needed sample is in the next massive
                else
                {
                    numberToEnd = file.length - sampleIndex;
                    sampleIndex = numSamples - numberToEnd;
                    isBufferEnd = true;
                }
            }
            isBufferEnd = false;
        }
        else
        {
            showMessageSB = new StringBuilder();
            showMessageSB.append(String.valueOf(preambulaCounter1));
            showMessageSB.append(String.valueOf(preambulaCounter2));
            messageOutput.setText(showMessageSB.toString());
        }
        isBufferEnd=false;
    }
}