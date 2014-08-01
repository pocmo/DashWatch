/*
 * Copyright (C) 2014 Sebastian Kaspari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidzeitgeist.dashwatch;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.androidzeitgeist.dashwatch.dashclock.ExtensionHost;
import com.androidzeitgeist.dashwatch.dashclock.ExtensionManager;
import com.google.android.apps.dashclock.api.DashClockExtension;

import java.util.List;

public class DashWatchService extends Service implements ExtensionManager.OnChangeListener {
    private static final String TAG = "DashWatchService";

    private Handler mUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "Extensions changed: " + (msg.obj != null ? "extension " + msg.obj : "DashClock"));

            // TODO: Replace this with sending just the extension data of the updated extension?
            mWearableManager.sendExtensionDataToWearable();
        }
    };

    /**
     * The maximum duration for the wakelock.
     */
    private static final long UPDATE_WAKELOCK_TIMEOUT_MILLIS = 30 * 1000;

    private WearableManager mWearableManager;
    private ExtensionManager mExtensionManager;
    private ExtensionHost mExtensionHost;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        mWearableManager = WearableManager.getInstance(this);

        mExtensionManager = ExtensionManager.getInstance(this);
        mExtensionManager.addOnChangeListener(this);
        mExtensionHost = new ExtensionHost(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        mUpdateHandler.removeCallbacksAndMessages(null);
        mExtensionManager.removeOnChangeListener(this);
        mExtensionHost.destroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: " + (intent != null ? intent.toString() : "no intent"));

        if (intent != null) {
            updateExtensions();

            // If started by a wakeful broadcast receiver, release the wake lock it acquired.
            WakefulBroadcastReceiver.completeWakefulIntent(intent);
        }

        return START_STICKY;
    }

    @Override
    public void onExtensionsChanged(ComponentName sourceExtension) {
        mUpdateHandler.removeCallbacksAndMessages(null);
        mUpdateHandler.sendMessageDelayed(
                mUpdateHandler.obtainMessage(0, sourceExtension),
                ExtensionHost.UPDATE_COLLAPSE_TIME_MILLIS);
    }

    /**
     * Asks extensions to provide data updates.
     */
    private void updateExtensions() {
        PowerManager pwm = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock lock = pwm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        lock.acquire(UPDATE_WAKELOCK_TIMEOUT_MILLIS);

        int reason = DashClockExtension.UPDATE_REASON_INITIAL;

        try {
            for (ComponentName cn : mExtensionManager.getActiveExtensionNames()) {
                mExtensionHost.execute(
                        cn,
                        ExtensionHost.UPDATE_OPERATIONS.get(reason),
                        ExtensionHost.UPDATE_COLLAPSE_TIME_MILLIS,
                        reason
                );
            }
        } finally {
            lock.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
