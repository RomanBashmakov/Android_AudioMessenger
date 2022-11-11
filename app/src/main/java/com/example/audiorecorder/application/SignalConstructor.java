package com.example.audiorecorder.application;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class SignalConstructor extends Thread {
    boolean preambulaF;
    boolean mIsTransmitting;
    public int sampleRate;
    public int freq; // Hz
    public float duration;
    public int numSamples;
    double[] samples;
    short[] buffer;
    short[] audioBufferShort;
    byte[] bytes;

    StringBuffer textBuffer;
    AudioTrack audioTrack;
    ArrayList<Boolean> booleanList;//bits number
    BitSet bitBuffer;

    private List<Handler> handlers = new ArrayList<>();

    public SignalConstructor (StringBuffer _textBuffer,
                                   boolean _preambulaF,
                                   int _sampleRate,
                                   int _freq,
                                   float _duration)
    {
        this.textBuffer = _textBuffer;
        this.preambulaF = _preambulaF;
        this.sampleRate = _sampleRate;
        this.freq = _freq;
        this.duration = _duration;

        numSamples = (int) (sampleRate * duration);
        samples = new double[numSamples];
        buffer = new short[numSamples];
        booleanList = new ArrayList<Boolean>(1000);//bits number
    }

    public void addHandler(Handler handler)
    {
        handlers.add(handler);
    }

    public void stopPlaying()
    {
        if (audioTrack != null){
            if(audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
            {
                audioTrack.stop();
                audioTrack.flush();
            }
        }
    }

    //Execute audio
    @Override
    public void run()
    {
        //one bit sine array initialization
        for (int i = 1; i < numSamples; ++i)
        {
            samples[i] = Math.sin(2 * Math.PI * i / (sampleRate / freq)); // Sine wave
            buffer[i] = (short) (samples[i] * Short.MAX_VALUE);  // Higher amplitude increases volume
        }

        //booleanList initialization
        for (int i = 0; i < 1000; ++i){
            booleanList.set(i, false);
        }

        //text into bytes
        bytes = textBuffer.toString().getBytes();

        //bytes into bitSet
        bitBuffer = BitSet.valueOf(bytes);

        //1=true, 0=false
        for (int i = bitBuffer.nextSetBit(0); i >= 0; i = bitBuffer.nextSetBit(i+1))
        {
            booleanList.set((i),true);
        }

        if (preambulaF)
        {
            //Add the preambula at the beginning
            //1010101010
            for (int i=0; i<=9; i++)
            {
                if(i%2==0)
                {
                    booleanList.add(0,false);
                }
                else
                {
                    booleanList.add(0,true);
                }
            }
        }

        //Initialize audioBuffer everytime to avoid corrupted wave
        ArrayList<Short> audioBuffer = new ArrayList<>();//sine to play

        //Wave to output. 1-sine, 0-zero
        for (int i=0; i<booleanList.size(); i++)
        {
            if(booleanList.get(i))
            {
                for (int k=0;k<buffer.length;k++)
                {
                    audioBuffer.add(buffer[k]);
                }
            }
            else
            {
                for (int k=0;k<buffer.length;k++)
                {
                    audioBuffer.add((short)0);
                }
            }
        }

        //Short-to-short
        audioBufferShort = new short[audioBuffer.size()];
        for(int sh=0;sh<audioBuffer.size(); sh++)
        {
            audioBufferShort[sh]=audioBuffer.get(sh);
        }

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
            sampleRate, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT, audioBufferShort.length,
            AudioTrack.MODE_STATIC);
        if(audioTrack.getPlayState()==AudioTrack.PLAYSTATE_PLAYING)
        {
            audioTrack.stop();
            audioTrack.flush();
        }
        audioTrack.write(audioBufferShort, 0, audioBufferShort.length);
        audioTrack.play();
    }
}
