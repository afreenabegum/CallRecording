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
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

        if (isRecording) {
            Log.d("TAG", "Already recording, return");
            return;
        }

        try{
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mediaRecorder.setOutputFile(pathToSaveFile(number));
            mediaRecorder.prepare();
            mediaRecorder.start();

                isRecording = true;
                Toast.makeText(this, "RecordingStarted", Toast.LENGTH_SHORT).show();
                Log.d("RecordStart", "started Recording: ");
            }
            catch (IOException e) {
                Log.e("CallRecordingService", "Recording failed", e);
                stopSelf(); // Stop the service if recording fails
             }

            catch (IllegalStateException e) {
                Log.e("CallRecordingService", "Recording failed due to illegal state", e);
                stopSelf(); // Stop the service if recording fails
        }
    }

    private String pathToSaveFile(String number){

            File file_Directory;

            ContextWrapper contextWrapper =  new ContextWrapper(getApplicationContext());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                file_Directory  = Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_DOWNLOADS);

            }
            else {
                file_Directory = contextWrapper.getFilesDir();
            }
//            String timeStamp =
//                    new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
//            String fileName = "Recording_" + timeStamp;
//            File file = new File(file_Directory,fileName + this.number +".m4a");
//            return file.getPath();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File file = new File(file_Directory, "Recording_" + number + "_" + timeStamp +
                ".mp3");
        return file.getPath();
    }

    public void onDestroy() {
        super.onDestroy();
        if (isRecording) {
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                Log.e("RecordStop", "Stopped Recording: ", e);
            }
            mediaRecorder.release();
            isRecording = false;
        }
    }
}

