package com.example.oalam.notificationschedule;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    JobScheduler mScheduler;
    int JOB_ID = 0;
    Switch mDeviceIdle, mDeviceCharging,mPeriodicSwitch;
    SeekBar mSeekBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDeviceIdle = (Switch) findViewById(R.id.idleSwitch);
        mDeviceCharging = (Switch) findViewById(R.id.chargingSwitch);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        final TextView label = (TextView) findViewById(R.id.seekBarLabel);
        final TextView mSeekBarProgress = (TextView) findViewById(R.id.seekBarProgress);
        mPeriodicSwitch = (Switch) findViewById(R.id.periodicSwitch);
        mPeriodicSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    label.setText("Periodic Interval:");
                } else {
                    label.setText("Override Deadline: ");
                }
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i > 0){
                    mSeekBarProgress.setText(String.valueOf(i) + " s");
                }else {
                    mSeekBarProgress.setText("Not Set");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void scheduleJob(View view) {
        RadioGroup networkOptions = (RadioGroup) findViewById(R.id.networkOptions);
        mScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int selectedNetworkID = networkOptions.getCheckedRadioButtonId();
        int selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
        int seekBarInteger = mSeekBar.getProgress();
        boolean seekBarSet = seekBarInteger > 0;

        switch (selectedNetworkID) {
            case R.id.noNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
                break;
            case R.id.anyNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_ANY;

                break;
            case R.id.wifiNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_UNMETERED;

                break;

        }
        ComponentName serviceName = new ComponentName(getPackageName(), NotificationJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceName)
                .setRequiredNetworkType(selectedNetworkOption)
                .setRequiresDeviceIdle(mDeviceIdle.isChecked())
                .setRequiresCharging(mDeviceCharging.isChecked());
        if (mPeriodicSwitch.isChecked()){
            if (seekBarSet){
                builder.setPeriodic(seekBarInteger * 1000);
            } else {
                Toast.makeText(MainActivity.this,
                        "Please set a periodic interval", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (seekBarSet){
                builder.setOverrideDeadline(seekBarInteger * 1000);
            }
        }
        boolean constraintSet = selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE
                || mDeviceCharging.isChecked() || mDeviceIdle.isChecked()
                || seekBarSet;
        if (constraintSet) {
            JobInfo jobInfo = builder.build();
            mScheduler.schedule(jobInfo);
            Toast.makeText(this, "Job Scheduled", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "Set at least one constraint", Toast.LENGTH_SHORT).show();

        }

    }

    public void cancelJobs(View view) {
        if (mScheduler != null) {
            mScheduler.cancelAll();
            mScheduler = null;
            Toast.makeText(this, "Job Cancelled", Toast.LENGTH_SHORT).show();
        }
    }
}
