package com.example.kwonwanbin.pro_bulb;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class RaspberryConnection extends AsyncTask<Void, byte[], Boolean> {
    public static Socket raspSocket;
    static DataInputStream inputStream;
    static DataOutputStream outputStream;
    static String serverIpAddress;
    int escape = 666;
    int count = 0;

    static byte[] buffer = new byte[4];
    public RaspberryConnection(String ip) { serverIpAddress = ip; }

    @Override
    protected void onPreExecute() {
        Log.d("ZZZZZ", "pre");
    }

    class runThread extends Thread {
        public void run() {
            try {
                while (escape != -1) {
                    inputStream.read(buffer);
                    //Log.d("Reading value : " , buffer.toString());
                    // publishProgress(buffer);
                    // after read the data in the buffer, if the buffer's value or nothing is present update this activity

                    if (count == 0) {
                        Log.d("ZZZZZ", "doInBackground loop");
                        Log.d("Port number : ", String.valueOf(raspSocket.getPort()));
                        Log.d("ZZZZZ", "doInBackground after read");
                    }
                    count = 2;
                }
                raspSocket.close();
                inputStream.close();;
                outputStream.close();

                //inputStream.read(buffer);
            }
            catch(Exception e) {
                Log.d("",e.toString());
            }
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result = false;
        try {
            Log.d("ZZZZZ", "doInBackground");
            InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
            raspSocket = new Socket(serverAddr, 10002);

            if (raspSocket.isConnected()) {
                inputStream = new DataInputStream(raspSocket.getInputStream());
                outputStream = new DataOutputStream(raspSocket.getOutputStream());
                Log.d("ZZZZZ", "doInBackground socket connections up" + inputStream.toString() + " what is youout : " + outputStream.toString());
                runThread r = new runThread();
                r.run();

                Log.d("ZZZZZZ", "Out of loop");
            } else {
                Log.d("NotConnected", "ip can't match");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("ZZZZZ", "doInBackground IOException");
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ZZZZZ", "doInBackground Exception");
            result = true;
        } finally {
            Log.d("ZZZZZ", "doInBackground close sockets");
            try {
                inputStream.close();
                outputStream.close();
                raspSocket.close();
                raspSocket = null;
                Log.d("ZZZZZ", "Socket Successfully Closed!!");
            } catch (IOException e) {
                e.printStackTrace();
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
                result = true;
            }
        }
        return result;
    }

    @Override
    public void onCancelled() { super.onCancelled(); }

    public static byte current_Bulb_Status() { return buffer[0]; }

    public static void sendData(short data) {
        try {
            if (raspSocket.isConnected()) {
                outputStream.writeByte(data);
            } else {
                Log.d("ZZZZZ", "SendDirectionToYun: Cannot send message. Socket is closed");
            }
        } catch (Exception e) {
            Log.d("ZZZZZ", "SendDirectionToYun: Message send failed. Caught an exception");
            e.printStackTrace();
        }
    }

    public static void sendData(byte[] data) {
        try {
            if (raspSocket.isConnected()) {
                outputStream.write(data);
            } else {
                Log.d("ZZZZZ", "SendDirectionToYun: Cannot send message. Socket is closed");
            }
        } catch (Exception e) {
            Log.d("ZZZZZ", "SendDirectionToYun: Message send failed. Caught an exception");
            e.printStackTrace();
        }
    }

    @Override
    protected void onProgressUpdate(byte[]... direct) { }

    public static boolean isSocketConnected() {
        if (raspSocket == null || !raspSocket.isConnected()) {
            Log.d("ZZZZZ", "Socket Closed");
            return false;
        }
        Log.d("ZZZZZ", "Socket Open");
        return true;
    }
}
