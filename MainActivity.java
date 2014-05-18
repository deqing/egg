package com.dqtools.egg;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

    private int mCount;
    private String mTask;
    private boolean mbSound;
    private boolean mbStopped;
    private boolean mbTest;
    private boolean mbVibrate;
    private int mMins;

    private CountDownTimer mWaitTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }

        mbSound = true;
        mbTest = mbVibrate = false;
        CheckBox cb = (CheckBox) findViewById(R.id.cbSound);   cb.setChecked(mbSound);
                 cb = (CheckBox) findViewById(R.id.cbVibrate); cb.setChecked(mbVibrate);
                 cb = (CheckBox) findViewById(R.id.cbTest);    cb.setChecked(mbTest);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        ((TextView)findViewById(R.id.tv)).setText("");

        addListeners();
        runMins(2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    ////////// Text updater
    private void say(String s) {
        TextView tv = (TextView)findViewById(R.id.tv);
        tv.append(s+"\n");
    }

    private String chop(String s) { return s.substring(0, s.length()-1); }

    private String getLastLine() {
        TextView tv = (TextView)findViewById(R.id.tv);
        String s = chop(tv.getText().toString());
        return s.substring(s.lastIndexOf('\n')+1);
    }

    private void updateLastLine(String last) {
        TextView tv = (TextView)findViewById(R.id.tv);
        String s = chop(tv.getText().toString());
        tv.setText(s.substring(0, s.lastIndexOf('\n')+1) + last + "\n");
    }

    private void updateLastLine() {
        updateLastLine(String.valueOf(++mCount) + " times for " + mTask + " (" + String.valueOf(mMins) + " mins)");
    }

    /////////// start timer
    private void go() {
        int seconds = mbTest ? 2 : 60 * mMins;
        mWaitTimer = new CountDownTimer(seconds*1000, 300) {

            @SuppressLint("SimpleDateFormat")
            public void onTick(long millisUntilFinished) {
                TextView tv = (TextView)findViewById(R.id.tvTime);
                tv.setText("Remaining: " + new SimpleDateFormat("mm:ss").format(new Date(millisUntilFinished)));

                // Update task, it might be changed anytime by user
                EditText ed = (EditText)findViewById(R.id.editSprint);
                mTask = ed.getText().toString();
            }

            public void onFinish() {
                if (mbStopped) return;

                if (mbSound) { // Play a sound
                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext()).setSound(soundUri);
                    notificationManager.notify(0, mBuilder.build());
                }
                if (mbVibrate) ((Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(500); // 500 milliseconds

                updateLastLine(); go(); // Go again
            }
        }.start();
    }

    /////////
    private void addListeners() {

        /// CheckBox Handlers
        final CheckBox cb1 = (CheckBox) findViewById(R.id.cbTest);
        final CheckBox cb2 = (CheckBox) findViewById(R.id.cbSound);
        final CheckBox cb3 = (CheckBox) findViewById(R.id.cbVibrate);
        cb1.setOnClickListener(new OnClickListener() { @Override public void onClick(View v) { mbTest = ((CheckBox) v).isChecked(); }});
        cb2.setOnClickListener(new OnClickListener() { @Override public void onClick(View v) { mbSound = ((CheckBox) v).isChecked(); }});
        cb3.setOnClickListener(new OnClickListener() { @Override public void onClick(View v) { mbVibrate = ((CheckBox) v).isChecked(); }});

        /// Button Handlers

        Button btn = (Button) this.findViewById(R.id.btn2);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                runMins(2);
            }
        });

        btn = (Button) this.findViewById(R.id.btn5);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                runMins(5);
            }
        });

        btn = (Button) this.findViewById(R.id.btnStop);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                 if (mWaitTimer != null) {
                     mWaitTimer.cancel();
                     mWaitTimer = null;
                     updateLastLine();
                 }
                mbStopped = true;
                updateBtns();
            }
        });
        updateBtns();

        btn = (Button) this.findViewById(R.id.btnClean);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                TextView tv = (TextView)findViewById(R.id.tv);
                tv.setText(getLastLine() + "\n");
            }
        });
    }

    private void runMins(int m) {
        say(String.valueOf(m) + "mins Countdown!"); mMins = m;
        mCount = 0; mbStopped = false; updateBtns(); go();
    }

    private void updateBtns() {
        final Button btn2 = (Button) this.findViewById(R.id.btn2);
        final Button btn5 = (Button) this.findViewById(R.id.btn5);
        final Button btnS = (Button) this.findViewById(R.id.btnStop);
        btn2.setVisibility(mbStopped ? View.VISIBLE : View.INVISIBLE);
        btn5.setVisibility(mbStopped ? View.VISIBLE : View.INVISIBLE);
        btnS.setVisibility(mbStopped ? View.INVISIBLE : View.VISIBLE);
    }
}
