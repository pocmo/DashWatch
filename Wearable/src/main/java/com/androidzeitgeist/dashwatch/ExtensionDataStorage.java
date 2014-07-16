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

import com.androidzeitgeist.dashwatch.common.ExtensionUpdate;

public class ExtensionDataStorage {
    private static final String PREFERENCE_NAME = "extension_data";

    private static final String KEY_NEXT_EXTENSION_ID = "next_extension_id";
    private static final String KEY_SUFFIX_NOTIFICATION_ID = "notification_id";
    private static final String KEY_SUFFIX_EXTENSION_HASH = "hash";

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

    private static String createKey(ExtensionUpdate update, String keySuffix) {
        return String.format("%s-%s", update.getComponent(), keySuffix);
    }
}
