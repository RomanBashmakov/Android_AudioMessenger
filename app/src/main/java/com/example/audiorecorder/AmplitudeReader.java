package com.example.audiorecorder;

import android.annotation.SuppressLint;

import android.content.ContextWrapper;
import android.content.*;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ddf.minim.effects.BandPass;

public class AmplitudeReader extends Thread {

//From the previous receiver
    BandPass bandpass;
    float[] floatedValues;
    float[] movingAverageValues;
    StringBuilder showMessageDataSB;
    int datum = 0;

    int filterFrequency = 500;
    int filterBW = 100;
    float movingAverageAccumulatorPrevious = 0; //sum of 'window' elements
    float duration = (float) 0.3;//=300ms
    int bitIndex1 = 0;//the last and current index

    int sampleIndex = 0;//the last and current index when preambula is detected
    int sampleIndex1 = 0;//the last and current index for non-shifted measurement
    int sampleIndex2 = 0;//the last and current index for shifted measurement
    float bitThreshold = 1000;//to compare with
    int numberToEnd = 0;//between the last measurement and bufLength
    boolean isPreambula = false;// if preambula is detected = true

    boolean isBufferEnd = false;// if buffer end is riched = true
    boolean isBufferEnd1 = false;
    boolean isBufferEnd2 = false;

    int preambulaCounter1 = 0;
    int preambulaCounter2 = 0;
//From the previous receiver

    private AudioRecord audioRecord;
    private int bufflen;
    private static final int SAMPPERSEC = 44100;
    private final int BUFF_COUNT = 32;

    int dataSampleRate = SAMPPERSEC;
    int numSamples = (int) (duration * SAMPPERSEC);//between the measurements

    static boolean mIsRunning;
    final int MSG_DATA = 101;
    final int THREAD_END = 102;
    final int MSG_ERROR = 113;

    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    private List<Handler> handlers = new ArrayList<>();

    public AmplitudeReader(ReceiverSetting receiverSetting)
    {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        bufflen = AudioRecord.getMinBufferSize(SAMPPERSEC, channelConfiguration, audioEncoding);

        showMessageDataSB = new StringBuilder();

        if (receiverSetting != null)
        {
            duration = receiverSetting.duration;
            filterFrequency = receiverSetting.frequency;
            numSamples = (int) (duration * SAMPPERSEC);
        }
    }

    public void addHandler(Handler handler)
    {
        handlers.add(handler);
    }

    public void stopRecording()
    {
        mIsRunning = false;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void run()
    {
        // циклический буфер буферов. Чтобы не затереть данные,
        // пока главный поток их обрабатывает
        short[][] buffers = new short[BUFF_COUNT][bufflen];

        audioRecord = new AudioRecord(
                android.media.MediaRecorder.AudioSource.MIC,
                SAMPPERSEC,
                channelConfiguration,
                audioEncoding,
                bufflen*10
        );

        mIsRunning = true;
        try
        {
            audioRecord.startRecording();
        }
        catch (IllegalStateException i)
        {
            messageError();
            stopRecording();
        }


        int count = 0;
        while(mIsRunning)
        {
            int samplesRead = audioRecord.read(buffers[count], 0, buffers[count].length);
            if(samplesRead == AudioRecord.ERROR_INVALID_OPERATION)
            {
                System.err.println("read() returned ERROR_INVALID_OPERATION");
                return;
            }

            if(samplesRead == AudioRecord.ERROR_BAD_VALUE)
            {
                System.err.println("read() returned ERROR_BAD_VALUE");
                return;
            }

            // посылаем оповещение обработчикам
            // Log.d("myTag", "saveFile " + Integer.toString(count));
            saveFile(buffers[count]);//!!! новое

            count = (count + 1) % BUFF_COUNT;
        }
        try
        {
            audioRecord.stop();
            if(!mIsRunning)
            {
                for(Handler handler : handlers)
                {
                    handler.sendMessage(handler.obtainMessage(THREAD_END));
                }
            }
        }
        catch(IllegalStateException e)
        {
            e.printStackTrace();
        }
        finally
        {
            // освобождаем ресурсы
            //Log.d("myTag", "finally audioRecord.release");
            audioRecord.release();
            audioRecord = null;
        }
    }

    private void sendMsg(String data)
    {
        Log.d("msg", "msg 1");
        for(Handler handler : handlers)
        {
            handler.sendMessage(handler.obtainMessage(MSG_DATA, data));
        }
    }

    void saveFile(short[] file)
    {
        bandpass = new BandPass(filterFrequency,filterBW,dataSampleRate);

        //FILTERED
        floatedValues=new float[file.length];
        for (int i=0; i<file.length; i++)
        {
            floatedValues[i]=file[i];
        }
        bandpass.process(floatedValues);

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
        int movingAverageW = 200; //window
        float movingAverageAccumulator = 0; //sum of 'window' elements
        for (int i = 0; i < (floatedValues.length); i++)
        {
            if((( i-movingAverageW/2 ) >= 0) && (( i + movingAverageW/2) < floatedValues.length))
            {
                for(int w=0; w<=movingAverageW; w++)
                {
                    movingAverageAccumulator += floatedValues [i - movingAverageW/2 + w];
                }
                movingAverageValues[i] = movingAverageAccumulator/movingAverageW;
                movingAverageAccumulator = 0; //sum of 'window' elements
                movingAverageAccumulatorPrevious = movingAverageValues[i];
            }
            else
            {
                movingAverageValues[i] = movingAverageAccumulatorPrevious;
            }
        }

        //1 Looking for preambula. Non-shifted
        if ( !isPreambula )
        {
            while( !isBufferEnd1 && !isPreambula )
            {
                if ((preambulaCounter1 % 2) == 0)
                {
                    if ( sampleIndex1 < file.length )
                    {
                        if ( movingAverageValues[sampleIndex1] > bitThreshold )
                        {
                            preambulaCounter1++;
                            //Log.d("Preambula", "Preambula1 " + Integer.toString(preambulaCounter1));
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
                    if ( sampleIndex1 < file.length )
                    {
                        if ( movingAverageValues[sampleIndex1] < bitThreshold )
                        {
                            preambulaCounter1++;
                            //Log.d("Preambula", "Preambula1 " + Integer.toString(preambulaCounter1));
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
                        if (movingAverageValues[sampleIndex2] > bitThreshold)
                        {
                            preambulaCounter2++;
                            //Log.d("Preambula", "Preambula2 " + Integer.toString(preambulaCounter2));
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
                else
                {
                    if ( sampleIndex2 < file.length)
                    {
                        if(movingAverageValues[sampleIndex2] < bitThreshold)
                        {
                            preambulaCounter2++;
                            //Log.d("Preambula", "Preambula2 " + Integer.toString(preambulaCounter2));
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
                        datum = datum | (1 << (bitIndex1 - 1));
                    }
                    bitIndex1++;
                    if ( bitIndex1 == 8)
                    {
                        showMessageDataSB.append( (char) datum );

                        messageBuilder(showMessageDataSB);//!!! НОВОЕ
                        //Log.d("myTag", String.valueOf(showMessageDataSB));

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
            //if preambula has not detected yet
        }
        isBufferEnd=false;
    }

    void messageBuilder(StringBuilder inputSB)
    {
        if ( inputSB.length() >= 3 )
        {
            Log.d("myTag", String.valueOf(inputSB));
            sendMsg(inputSB.toString());
            isPreambula = false;
            showMessageDataSB = new StringBuilder();
        }
    }

    void messageError()
    {
        for(Handler handler : handlers)
        {
            handler.sendMessage(handler.obtainMessage(MSG_ERROR));
        }
    }

}