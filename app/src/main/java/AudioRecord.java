import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

class AmplitudeReader extends Thread {

    private AudioRecord audioRecord;
    private int bufflen;
    private static final int SAMPPERSEC = 44100;
    private final int BUFF_COUNT = 32;
    final int MSG_DATA = 101;
    private boolean mIsRunning;

    private List<Handler> handlers;

    @SuppressLint("MissingPermission")
    public AmplitudeReader() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        bufflen = AudioRecord.getMinBufferSize(SAMPPERSEC, channelConfiguration, audioEncoding);
        handlers = new ArrayList<Handler>();

        audioRecord = new AudioRecord(
                android.media.MediaRecorder.AudioSource.MIC,
                SAMPPERSEC,
                channelConfiguration,
                audioEncoding,
                bufflen
        );
        audioRecord.startRecording();
    }

    private short getMax(short[] arr, int count) {
        short m = 0;
        for (int i = 0; i < count; i++) {
            short c = (short) Math.abs(arr[i]);
            if (m < c) {
                m = c;
            }
        }
        return m;
    }

    // циклический буфер буферов. Чтобы не затереть данные,
    // пока главный поток их обрабатывает
    short[][] buffers = new short[BUFF_COUNT][bufflen >> 1];


    @Override
    public void run()
    {
        mIsRunning = true;
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


        for(int i=0; i<11; i++) {
            short[] curr = new short[bufflen];
            int curread = audioRecord.read(curr, 0, bufflen);
            System.err.println("current amplitude is:" + getMax(curr, curread));
        }

        try
        {
            try
            {
                audioRecord.stop();
            }
            catch(IllegalStateException e)
            {
                e.printStackTrace();
            }
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