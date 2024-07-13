package com.example.callapi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallAPIReceiver extends BroadcastReceiver {

    private static boolean isIncomingCall = false;
    private static boolean isOutgoingCall = false;
    private static String lastState = TelephonyManager.EXTRA_STATE_IDLE;

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (Intent.ACTION_NEW_OUTGOING_CALL.equals(action)) {
            String outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            isOutgoingCall = true;
            Log.d("OutgoingCallReceiver", "Outgoing  Call- Ringing " + outgoingNumber);
        }

        else if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {

            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

            if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                isIncomingCall = true;
                Log.d("IncomingCallReceiver", "Incoming call from: " + incomingNumber);
            }

            else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {

                if(isOutgoingCall){
                    Log.d("OutgoingCallReceiver","Outgoing Call Answered");
                }
                else {
                    isIncomingCall = true;
                    Log.d("IncomingCallReceiver", "Incoming call Answered ");
                }

                // Here, need to Record Call here, when Call is answered
                //Start the service
                Intent serviceIntent = new Intent(context, CallAPIService.class);
                serviceIntent.putExtra("IncomingNumber", incomingNumber);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                }
            }

            else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {

                if (lastState.equals(TelephonyManager.EXTRA_STATE_RINGING) && isIncomingCall) {
                    Log.d("IncomingCallReceiver", "Incoming Call Rejected ");
                }
                else if(lastState.equals(TelephonyManager.EXTRA_STATE_RINGING) && isOutgoingCall){
                    Log.d("OutgoingCallReceiver", "Outgoing Call Rejected ");
                }
                else if(lastState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){

                    if(isOutgoingCall)
                        Log.d("OutgoingCallReceiver", "Outgoing Call Disconnected ");

                    else if(isIncomingCall)
                        Log.d("IncomingCallReceiver", "Incoming Call Disconnected ");

                }

                isIncomingCall = false;
                isOutgoingCall = false;
                //stops the service
                context.stopService(new Intent(context, CallAPIService.class));
            }
            lastState = state;
        }
    }
}


