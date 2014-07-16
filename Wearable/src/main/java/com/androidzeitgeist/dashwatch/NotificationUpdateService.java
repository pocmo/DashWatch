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
import android.util.Log;

import com.androidzeitgeist.dashwatch.common.ExtensionUpdate;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

public class NotificationUpdateService extends WearableListenerService {
    private static final String TAG = "NotificationUpdate";

    private ExtensionDataStorage storage;

    @Override
    public void onCreate() {
        super.onCreate();

        storage = new ExtensionDataStorage(this);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged()");

        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();

                ExtensionUpdate update = ExtensionUpdate.fromDataMap(dataMap);

                if (storage.isNew(update)) {
                    buildNotification(update);
                } else {
                    Log.d(TAG, String.format("Notification of component %s is not new. Ignoring.", update.getComponent()));
                }
            }
        }
    }

    private void buildNotification(ExtensionUpdate update) {
        int id = storage.getNotificationId(update);

        Log.d(TAG, String.format("Building notification for component %s with id %d", update.getComponent(), id));

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(com.androidzeitgeist.dashwatch.R.drawable.ic_launcher)
                .setContentTitle(update.getTitle())
                .setContentText(update.getText())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.notification_background));

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(id, builder.build());
    }
}