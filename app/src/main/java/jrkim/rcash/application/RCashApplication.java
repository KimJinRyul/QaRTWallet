package jrkim.rcash.application;

import android.app.Application;

import java.util.Timer;
import java.util.TimerTask;

public class RCashApplication extends Application {
    private Timer activityTransitionTimer;
    private TimerTask activityTransitionTimerTask;
    public boolean wasInBackground = true;
    private final long MAX_ACTIVITY_TRANSITION_TIME_MS = 1000;

    public void startActivityTransitionTimer() {
        activityTransitionTimer = new Timer();
        activityTransitionTimerTask = new TimerTask() {
            @Override
            public void run() {
                wasInBackground = true;
            }
        };
        activityTransitionTimer.schedule(activityTransitionTimerTask, MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    public void stopActivityTransitionTimer() {
        if(activityTransitionTimerTask  != null) {
            activityTransitionTimerTask.cancel();
        }

        if(activityTransitionTimer != null) {
            activityTransitionTimer.cancel();;
        }

        wasInBackground = false;
    }
}
