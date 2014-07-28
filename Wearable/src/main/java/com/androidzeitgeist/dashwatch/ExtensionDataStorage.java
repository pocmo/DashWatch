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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.androidzeitgeist.dashwatch.common.ExtensionUpdate;

public class ExtensionDataStorage {
    private static final String TAG = "DashWatch/ExtensionDataStorage";

    private static final String PREFERENCE_NAME = "extension_data";

    private static final String KEY_NEXT_EXTENSION_ID = "next_extension_id";
    private static final String KEY_SUFFIX_NOTIFICATION_ID = "notification_id";
    private static final String KEY_SUFFIX_EXTENSION_HASH = "hash";
    private static final String KEY_SILENT_SINCE = "silent_since";

    private static final int SILENCE_PERIOD = 1000 * 60 * 60 * 12;

    private SharedPreferences preferences;
    private int nextExtensionId;

    public ExtensionDataStorage(Context context) {
        preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        nextExtensionId = preferences.getInt(KEY_NEXT_EXTENSION_ID, 1);
    }

    public synchronized int getNotificationId(ExtensionUpdate update) {
        String key = createKey(update, KEY_SUFFIX_NOTIFICATION_ID);

        int extensionId = preferences.getInt(key, nextExtensionId);

        if (extensionId == nextExtensionId) {
            nextExtensionId++;

            preferences.edit()
                .putInt(KEY_NEXT_EXTENSION_ID, nextExtensionId)
                .putInt(key, extensionId)
                .apply();
        }

        return nextExtensionId;
    }

    public synchronized boolean isNew(ExtensionUpdate update) {
        String key = createKey(update, KEY_SUFFIX_EXTENSION_HASH);

        String contentHash = update.getContentHash();
        String lastKnownHash = preferences.getString(key, null);

        if (lastKnownHash == null || !contentHash.equals(lastKnownHash)) {
            preferences.edit().putString(key, contentHash).apply();
            return true;
        }

        return false;
    }

    public synchronized void shutUpNotification(String notificationComponent) {
        String key = createKey(notificationComponent, KEY_SILENT_SINCE);

        long now = System.currentTimeMillis();
        long silentUntil = SILENCE_PERIOD + now;

        Log.d(TAG, String.format("Extension %s should shut up at least until %d", notificationComponent, silentUntil));

        preferences.edit()
            .putLong(key, now)
            .apply();
    }

    public synchronized boolean shouldShutUp(ExtensionUpdate update) {
        long silentSince = preferences.getLong(createKey(update, KEY_SILENT_SINCE), 0);

        return silentSince != 0 && System.currentTimeMillis() < silentSince + SILENCE_PERIOD;
    }

    private static String createKey(ExtensionUpdate update, String keySuffix) {
        return createKey(update.getComponent(), keySuffix);
    }

    private static String createKey(String extensionComponent, String keySuffix) {
        return String.format("%s-%s", extensionComponent, keySuffix);
    }
}
