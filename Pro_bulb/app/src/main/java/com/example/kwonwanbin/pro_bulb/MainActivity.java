package com.example.kwonwanbin.pro_bulb;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.kwonwanbin.alarm.AlarmActivity;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
{
    final static int SECURITY_MODE = 6;
    final static int TEMPARATURE_MODE = 7;
    final static int CLUB_LIGHT_MODE = 5;
    final static byte SECURITY_RECEIVE = 112;

    private SeekBar seekBar;
    private TextView textView;
    private Button centerButton;
    private boolean isConnected = false;
    boolean power = true;
    short  status = 0;
    byte option_Status;
    short progress = 100;
    short r, g, b;
    private Button alarmButton;
    private Button micButton;
    private Button lightningButton;
    private Button connectionButton;
    TextView time;
    TimePicker simpleTimePicker;
    ImageView mic_Image;

    static final int TIME_DIALOG_ID = 0;
    boolean statusisRasp = false;
    boolean Temparature_Mode_Status, Security_Mode_Status;
    private int pHour;
    private int pMinute;
    final Calendar cal = Calendar.getInstance();
    boolean mic_status;
    byte temparature, security;

    Mic_Function mf;
    private RaspberryConnection rasp;

    Handler mHandler;
    Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        seekBar = (SeekBar) findViewById(R.id.BrightnessBar);
        textView = (TextView) findViewById(R.id.SeekValue);
        seekBar.setProgress(100);
        textView.setText(seekBar.getProgress() + "/" + seekBar.getMax());
        centerButton = (Button) findViewById(R.id.center_button);
        ImageView v = (ImageView) findViewById(R.id.Color_Wheel);
        alarmButton = (Button)findViewById(R.id.alarm_button);
        micButton = (Button)findViewById(R.id.mic_button);
        lightningButton = (Button)findViewById(R.id.option_button);
        connectionButton = (Button)findViewById(R.id.connection_button);

        v.setOnTouchListener(mPagerTouch);
        buttonEffect(centerButton);
        alarmEffect(alarmButton);
        mic_Effect(micButton);
        connecting_Raspberry(connectionButton);
        club_lighting_glows_Effect(lightningButton);
        option_Status = RaspberryConnection.current_Bulb_Status();
        pHour = cal.get(Calendar.HOUR_OF_DAY);
        pMinute = cal.get(Calendar.MINUTE);
        mic_Image = (ImageView)findViewById(R.id.mic_Image);
        mic_status = false;
        mf = new Mic_Function();

        Security_Mode_Status = false;
        Temparature_Mode_Status = false;

        mRunnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext()
                        , MainActivity.class);
                startActivity(intent);
            }
        };

        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progressValue, boolean fromUser)
                    {
                        progress = (short)progressValue;

                        textView.setText(progress + "/" + seekBar.getMax());
                        RaspberryConnection.sendData((short)3);
                        if (progress == 100)
                            RaspberryConnection.sendData((progress));
                        else
                            RaspberryConnection.sendData((short)(progress + 1));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
    }

    public void alarmEffect(View button) {
        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent alarm_intent = new Intent(MainActivity.this, AlarmActivity.class);
                startActivity(alarm_intent);
            }

        });
    }

    public void mic_Effect(View button) {
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!mic_status) {
                    RaspberryConnection.sendData((short) 8);
                    mic_status = true;
                    mic_Image.setImageResource(R.drawable.mic_on);
                    mf.Mic_On();

                    Toast toast = Toast.makeText(getApplicationContext(),"MIC ON", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    mic_status = false;
                    mic_Image.setImageResource(R.drawable.mic_off);
                    mf.Mic_Off();

                    Toast toast = Toast.makeText(getApplicationContext(),"MIC OFF", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

        });
    }
    public void club_lighting_glows_Effect(View button) {
        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                RaspberryConnection.sendData((short) CLUB_LIGHT_MODE);
            }
        });
    }

    public void connecting_Raspberry(View button) {
        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                Log.d("are you click", "status : " + String.valueOf(rasp.isSocketConnected()));

                if (!rasp.isSocketConnected())
                {
                    Context mContext = getApplicationContext();
                    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                    final View layout = inflater.inflate(R.layout.custom_dialog, (ViewGroup) findViewById(R.id.layout_root));

                    final AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this);
                    aDialog.setTitle("Enter your IP");
                    aDialog.setView(layout);

                    aDialog.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            EditText edit = (EditText) layout.findViewById(R.id.ip_text);
                            String ip = edit.getText().toString();

                            rasp = new RaspberryConnection(ip);
                            rasp.execute();

                            connectionButton.setText("Disconnect");

                            Log.d("are you connecting", "conneting status : " + String.valueOf(rasp.isSocketConnected()));
                        }
                    });

                    AlertDialog ad = aDialog.create();
                    ad.show();
                }
                else
                {
                    rasp.cancel(true);
                    rasp.escape = -1;
                    rasp.sendData((short) 255);
                    connectionButton.setText("Connect");

                    Log.d("are you disconnecting", "conneting status : " +  String.valueOf(rasp.isSocketConnected()));
                }

            }

        });
    }

    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    pHour = hourOfDay;
                    pMinute = minute;
                }
            };

    protected Dialog onCreateDialog(int id)
    {
        if(id == TIME_DIALOG_ID)
        {
            return new TimePickerDialog(this, mTimeSetListener, pHour, pMinute, false);
        }
        return null;
    }

    public void setAlarm()
    {
        int year, month, day;
        int hour = pHour;
        int minute = pMinute;
        AlarmManager alarm = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        // Intent intent = new Intent(MainActivity.this, AlarmRecever.class);
        // PendingIntent pender = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
    }


    public void buttonEffect(View button) {
        button.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (RaspberryConnection.isSocketConnected()) {
                    //status = RaspberryConnection.current_Bulb_Status();

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.getBackground().setColorFilter(0xe0eaeaea, PorterDuff.Mode.SRC_ATOP);
                        v.invalidate();

                        if (status == 0 || status == 2) {
                            RaspberryConnection.sendData((short) 1);
                            status = 1;
                        } else if (status == 1) {
                            RaspberryConnection.sendData((short) 255);
                            status = 0;
                        }
                        power = !power;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        v.getBackground().clearColorFilter();
                        v.invalidate();
                    }
                }
                return false;
            }
        });
    }

    private ImageView.OnTouchListener mPagerTouch = new ImageView.OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            ImageView tmp = (ImageView) findViewById(R.id.Color_Wheel);
            tmp.buildDrawingCache();
            Bitmap bitmap = tmp.getDrawingCache();
            TextView t = (TextView) findViewById(R.id.RGB);

            //status = RaspberryConnection.current_Bulb_Status();

            if (event.getAction() == MotionEvent.ACTION_MOVE){

                if(status == (short)1 || status == (short)2)
                {
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    int pixel;

                    if (x > bitmap.getWidth() || y > bitmap.getHeight() || x < 0 || y < 0)
                        pixel = 0;
                    else
                        pixel = bitmap.getPixel(x, y);

                    r = (short)Color.red(pixel);
                    g = (short)Color.green(pixel);
                    b = (short)Color.blue(pixel);

                    if(!(r == 0 && g == 0 && b ==0)) {
                        RaspberryConnection.sendData((short)2);
                        RaspberryConnection.sendData(r);
                        RaspberryConnection.sendData(g);
                        RaspberryConnection.sendData(b);
                    }

                    t.setText("X : " + x + " Y : " + y + " R : " + r + " G : " + g + " B : " + b);
                    status = 2;
                }
            }
            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        option_Status = RaspberryConnection.current_Bulb_Status();

        if (id == R.id.Security)
        {
            if(item.isChecked()) {
                item.setChecked(false);
                Security_Mode_Status = false;
                RaspberryConnection.sendData((short) SECURITY_MODE);
            }
            else
            {
                item.setChecked(true);
                Security_Mode_Status = true;
                RaspberryConnection.sendData((short) SECURITY_MODE);
                Security_Thread st = new Security_Thread();
                st.start();
            }
        }
        if(id == R.id.Temperature)
        {
            RaspberryConnection.sendData((short) TEMPARATURE_MODE);
            try {
                Thread.sleep(1500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Toast.makeText(getApplicationContext(), "현재 온도는 " + RaspberryConnection.current_Bulb_Status() + "℃ 입니다.", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    class Security_Thread extends Thread {
        public void run() {
            while(Security_Mode_Status) {
                if(RaspberryConnection.current_Bulb_Status() == SECURITY_RECEIVE ) {
                    pushing();
                    RaspberryConnection.buffer[0] = 0;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void pushing() {
        NotificationManager notificationManager= (NotificationManager)MainActivity.this.getSystemService(MainActivity.this.NOTIFICATION_SERVICE);
        Intent intent1 = new Intent(MainActivity.this.getApplicationContext(),MainActivity.class); //인텐트 생성.

        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(MainActivity.this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setSmallIcon(R.drawable.alert).setTicker("Pro_bulb").setWhen(System.currentTimeMillis())
                .setNumber(1).setContentTitle("Pro_bulb").setContentText("움직임이 감지되었습니다.")
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE).setContentIntent(pendingNotificationIntent).setAutoCancel(true).setOngoing(true);
        notificationManager.notify(1, builder.build()); // Notification send
    }
}
