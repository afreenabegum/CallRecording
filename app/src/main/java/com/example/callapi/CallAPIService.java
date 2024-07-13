package com.example.callapi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CallAPIService extends Service {

    MediaRecorder mediaRecorder;
    boolean isRecording = false;
    private static final String CHANNEL_ID = "CallRecordingChannel";
    private String number ;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaRecorder = new MediaRecorder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String incomingNumber = intent.getStringExtra("IncomingNumber");
        number=incomingNumber;
        startForegroundService();
        startRecording(incomingNumber);
        return START_STICKY;
    }

    private void startForegroundService() {
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Call Recording")
                .setContentText("Recording call in progress")
                .build();
        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Call Recording Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }


    private void startRecording(String incomingNumber) {

        // Todo check the logic
        if (isRecording) {
            Log.d("TAG", "Already recording, return");
            return;
        }


        try{
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mediaRecorder.setOutputFile(pathToSaveFile());

                mediaRecorder.prepare();
                mediaRecorder.start();
                isRecording = true;
                Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show();
                Log.d("RecordStart", "started Recording: ");
            } catch (Exception e) {
                Log.d("RecordFailed", "Recording Failed " + e);

            }
    }

    private String pathToSaveFile(){

            File file_Directory;

            ContextWrapper contextWrapper =  new ContextWrapper(getApplicationContext());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                file_Directory  = Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_DOWNLOADS);

                // contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS); other paths to save file
            }else {
                file_Directory = contextWrapper.getFilesDir();
            }
            File file = new File(file_Directory,"testRecording"+ number +".mp3");
            System.currentTimeMillis();
            return file.getPath();
    }

    private void stopRecording() {
        if(isRecording){
            mediaRecorder.stop();
            mediaRecorder.reset();
            isRecording = false;
            Toast.makeText(this, "Recording Stopped", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecording();
    }
}

