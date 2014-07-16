/*
 * Copyright (C) 2014 Sebastian Kaspari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidzeitgeist.dashwatch;

import android.app.Notification;
import android.app.NotificationManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.androidzeitgeist.dashwatch.common.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class NotificationUpdateService extends WearableListenerService
implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "NotificationUpdate";
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");

        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged()");

        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                String content = dataMap.getString(Constants.KEY_CONTENT);
                String title = dataMap.getString(Constants.KEY_TITLE);
                int id = dataMap.getInt(Constants.KEY_NOTIFICATION_ID);

                buildNotification(title, content, id);
            }
        }
    }

    private void buildNotification(String title, String content, int id) {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(com.androidzeitgeist.dashwatch.R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.notification_background));

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(id, builder.build());
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected()");
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}