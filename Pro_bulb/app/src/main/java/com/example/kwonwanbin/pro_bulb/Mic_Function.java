package com.example.kwonwanbin.pro_bulb;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by Kwonwanbin on 2016-08-20.
 */
public class Mic_Function {

    AudioRecord recorder;
    private int sampleRate = 44100 ; // 44100 for music
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    boolean status = false;

    public void Mic_On() {
        status = true;
        startStreaming();
    }
    public void Mic_Off() {
        status = false;
        recorder.release();
    }

    public void startStreaming() {
        Thread streamThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                    byte[] buffer = new byte[minBufSize];
                    Log.d("Pro_bulb_test","Buffer created of size " + minBufSize);

                    minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize*10);

                    recorder.startRecording();

                    while(status) {
                        //reading data from MIC into buffer
                        minBufSize = recorder.read(buffer, 0, buffer.length);

                        //putting buffer in the packet
                        //packet = new DatagramPacket (buffer,buffer.length,destination,port);
                        //socket.send(packet);
                        RaspberryConnection.sendData(buffer);
                    }

                    byte[] end_sign = {9};
                    RaspberryConnection.sendData(end_sign);
                } catch(Exception e) {
                    Log.d("Pro_bulb_test", e.toString());
                }
            }
        });
        streamThread.start();
    }
}
