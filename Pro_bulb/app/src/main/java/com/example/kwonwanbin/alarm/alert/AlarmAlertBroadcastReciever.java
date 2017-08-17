package com.example.kwonwanbin.alarm.alert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.kwonwanbin.alarm.Alarm;
import com.example.kwonwanbin.alarm.service.AlarmServiceBroadcastReciever;

public class AlarmAlertBroadcastReciever extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent alarmService = new Intent(context, AlarmServiceBroadcastReciever.class);
		context.sendBroadcast(alarmService, null);

		com.example.kwonwanbin.alarm.alert.StaticWakeLock.lockOn(context);
		Bundle bundle = intent.getExtras();
		final Alarm alarm = (Alarm) bundle.getSerializable("alarm");

		Intent alarmAlertIntent = new Intent(context, AlarmAlertActivity.class);
		alarmAlertIntent.putExtra("alarm", alarm);
		alarmAlertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(alarmAlertIntent);
	}

}
