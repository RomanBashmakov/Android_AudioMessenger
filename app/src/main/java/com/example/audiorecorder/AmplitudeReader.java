package com.example.audiorecorder;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

public class AmplitudeReader extends Thread {

    private AudioRecord audioRecord;
    private int bufflen;
    private static final int SAMPPERSEC = 44100;
    private final int BUFF_COUNT = 32;

    boolean mIsRunning;
    final int MSG_DATA = 101;
    final int THREAD_END = 102;

    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    private List<Handler> handlers = new ArrayList<>();

    public AmplitudeReader()
    {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        bufflen = AudioRecord.getMinBufferSize(SAMPPERSEC, channelConfiguration, audioEncoding);
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
        audioRecord.startRecording();
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
            sendMsg(buffers[count]);

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
            audioRecord.release();
            audioRecord = null;
        }
    }

    private void sendMsg(short[] data)
    {
        for(Handler handler : handlers)
        {
            handler.sendMessage(handler.obtainMessage(MSG_DATA, data));
        }
    }



}