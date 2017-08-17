package com.example.kwonwanbin.alarm.alert;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.example.kwonwanbin.alarm.Alarm;
import com.example.kwonwanbin.pro_bulb.RaspberryConnection;
import com.example.kwonwanbin.pro_bulb.R;

public class AlarmAlertActivity extends Activity {
	private Alarm alarm;
	private MediaPlayer mediaPlayer;
	private Vibrator vibrator;

	private boolean alarmActive;
	private SeekBar alarmOff;
	private int progress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		setContentView(R.layout.alarm_alert);

		Bundle bundle = this.getIntent().getExtras();
		alarm = (Alarm) bundle.getSerializable("alarm");
		alarmOff = (SeekBar) findViewById(R.id.alarm_off);

		this.setTitle(alarm.getAlarmName());

		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);

		PhoneStateListener phoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (state) {
				case TelephonyManager.CALL_STATE_RINGING:
					Log.d(getClass().getSimpleName(), "Incoming call: "
							+ incomingNumber);
					try {
						mediaPlayer.pause();
					} catch (IllegalStateException e) {

					}
					break;
				case TelephonyManager.CALL_STATE_IDLE:
					Log.d(getClass().getSimpleName(), "Call State Idle");
					try {
						mediaPlayer.start();
					} catch (IllegalStateException e) {

					}
					break;
				}
				super.onCallStateChanged(state, incomingNumber);
			}
		};

		alarmOff.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
						progress = progressValue;
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						if (progress < 80)
							seekBar.setProgress(0);
						else
							finish();
					}
				});

		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_CALL_STATE);

		startAlarm();
	}

	@Override
	protected void onResume() {
		super.onResume();
		alarmActive = true;
	}

	private void startAlarm() {

		if (alarm.getAlarmTonePath() != "") {
			mediaPlayer = new MediaPlayer();
			if (alarm.getVibrate()) {
				vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
				long[] pattern = { 1000, 200, 200, 200 };
				vibrator.vibrate(pattern, 0);
			}
			try {
				mediaPlayer.setVolume(1.0f, 1.0f);
				mediaPlayer.setDataSource(this,
						Uri.parse(alarm.getAlarmTonePath()));
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mediaPlayer.setLooping(true);
				mediaPlayer.prepare();
				mediaPlayer.start();

				RaspberryConnection.sendData((byte)4);

			} catch (Exception e) {
				mediaPlayer.release();
				alarmActive = false;
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (!alarmActive)
			super.onBackPressed();
	}

	@Override
	protected void onPause() {
		super.onPause();
		com.example.kwonwanbin.alarm.alert.StaticWakeLock.lockOff(this);
	}

	@Override
	protected void onDestroy() {
		try {
			if (vibrator != null)
				vibrator.cancel();
		} catch (Exception e) {

		}
		try {
			mediaPlayer.stop();
		} catch (Exception e) {

		}
		try {
			mediaPlayer.release();
		} catch (Exception e) {

		}
		super.onDestroy();
	}
}
