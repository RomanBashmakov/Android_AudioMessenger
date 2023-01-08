package com.example.audiorecorder;

import android.os.Handler;

import com.casualcoding.reedsolomon.EncoderDecoder;

import java.util.ArrayList;
import java.util.List;

public class SignalProcessing extends Thread {

    private List<Handler> handlers = new ArrayList<>();
    final int MSG_DATA = 103;

    public void addHandler(Handler handler)
    {
        handlers.add(handler);
    }

    @Override
    public void run()
    {

    }

    private void sendMsg(short[] data)
    {
        for(Handler handler : handlers)
        {
            handler.sendMessage(handler.obtainMessage(MSG_DATA, data));
        }
    }

}
