//Transmitter
package com.example.audiorecorder;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.BitSet;

//Receiver
public class MainActivity2 extends AppCompatActivity {

    public static float duration = (float) 0.3; // duration of sound
    public static int sampleRate = 44100; // Hz
    public static int freq = 500; // Hz
    public static int numSamples = (int) (sampleRate*duration);// 1/10 - duration
    double samples[] = new double[numSamples];
    short buffer[] = new short[numSamples];
    AudioTrack audioTrack;

    ArrayList<Boolean> booleanList = new ArrayList<Boolean>(1000);//bits number

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //one bit sine array initialization
        for (int i = 1; i < numSamples; ++i)
        {
            samples[i] = Math.sin(2 * Math.PI * i / (sampleRate/freq)); // Sine wave
            buffer[i] = (short) (samples[i] * Short.MAX_VALUE);  // Higher amplitude increases volume
        }

        //booleanList initialization
        for (int i = 0; i < 1000; ++i){
            booleanList.add(false);
        }

        //take all the text to transmit
        TextView textToTransmit=(TextView) findViewById(R.id.textToTransmit);
        Button btnTransmit=(Button)findViewById(R.id.Transmit);

        View.OnClickListener oclbtnTransmit=new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //clear previous massage
                StringBuffer textbuffer = new StringBuffer("");
                //take new massage
                textbuffer.append(textToTransmit.getText().toString());

                //text into bytes
                byte[] bytes = textbuffer.toString().getBytes();

                //bytes into bitSet
                BitSet bitbuffer=BitSet.valueOf(bytes);

                //booleanList initialization
                for (int i = 0; i < 1000; ++i){
                    booleanList.set(i, false);
                }

                //1=true, 0=false
                for (int i = bitbuffer.nextSetBit(0); i >= 0; i = bitbuffer.nextSetBit(i+1))
                {
                    booleanList.set((i),true);
                }

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
                short[] audioBuffershort= new short[audioBuffer.size()];
                for(int sh=0;sh<audioBuffer.size(); sh++)
                {
                    audioBuffershort[sh]=audioBuffer.get(sh);
                }

                //Execute audio
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, audioBuffershort.length,
                        AudioTrack.MODE_STATIC);
                if(audioTrack.getPlayState()==AudioTrack.PLAYSTATE_PLAYING)
                {
                    audioTrack.stop();
                    audioTrack.flush();
                }
                audioTrack.write(audioBuffershort, 0, audioBuffershort.length);
                audioTrack.play();
            }
        };
        btnTransmit.setOnClickListener(oclbtnTransmit);
    }

    public void stopTransmitting(View view)
    {
        if(audioTrack.getPlayState()==AudioTrack.PLAYSTATE_PLAYING)
        {
            audioTrack.stop();
            audioTrack.flush();
        }
    }

    public void setBD(View view)
    {
        TextView setBD=(TextView) findViewById(R.id.BDInput);
        duration = Float.parseFloat(setBD.getText().toString())/1000; // duration of sound in ms
        numSamples = (int) (sampleRate*duration);
        double samples[] = new double[numSamples];
        buffer = new short[numSamples];

        //one bit sine array initialization
        for (int i = 1; i < numSamples; ++i)
        {
            samples[i] = Math.sin(2 * Math.PI * i / (sampleRate/freq)); // Sine wave
            buffer[i] = (short) (samples[i] * Short.MAX_VALUE);  // Higher amplitude increases volume
        }
    }

    public void setFrequency(View view)
    {
        TextView setHz=(TextView) findViewById(R.id.setHz);

        freq = Integer.parseInt(setHz.getText().toString());
        numSamples = (int) (sampleRate*duration);
        double samples[] = new double[numSamples];
        buffer = new short[numSamples];

        //one bit sine array initialization
        for (int i = 1; i < numSamples; ++i)
        {
            samples[i] = Math.sin(2 * Math.PI * i / (sampleRate/freq)); // Sine wave
            buffer[i] = (short) (samples[i] * Short.MAX_VALUE);  // Higher amplitude increases volume
        }
    }


    public void toTransmit(View view)
    {
    }

    public void toReceive(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}