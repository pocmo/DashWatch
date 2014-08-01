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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.util.Log;

import com.androidzeitgeist.dashwatch.common.Constants;
import com.androidzeitgeist.dashwatch.dashclock.ExtensionManager;
import com.androidzeitgeist.dashwatch.muzei.SourceManager;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

public class MessageReceiverService extends WearableListenerService {
    private static final String TAG = "MessageReceiverService";

    private SourceManager mSourceManager;
    private ExtensionManager mExtensionManager;
    private WearableManager mWearableManager;

    public MessageReceiverService() {
        mSourceManager = SourceManager.getInstance(this);
        mExtensionManager = ExtensionManager.getInstance(this);
        mWearableManager = WearableManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i(TAG, String.format("onMessageReceived(): %s", messageEvent.getPath()));

        if (Constants.PATH_INTENT.equals(messageEvent.getPath())) {
            fireIntent(messageEvent.getData());
        } else if (Constants.PATH_SETUP.equals(messageEvent.getPath())) {
            setupWatchFace();
        }
    }

    private void fireIntent(byte[] data) {
        Log.i(TAG, "fireIntent()");

        try {
            Intent intent = Intent.parseUri(new String(data, "UTF-8"), 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (URISyntaxException e) {
            Log.w(TAG, "Can't parse intent uri", e);
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "This device does not know what UTF-8 is..", e);
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "Activity could not be found", e);
        }
    }

    private void setupWatchFace() {
        Log.i(TAG, "setupWatchFace()");

        mWearableManager.sendExtensionDataToWearable();
        mWearableManager.sendArtworkToWearable();
    }
}
