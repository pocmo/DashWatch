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

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.androidzeitgeist.dashwatch.common.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MessageSenderService extends IntentService {
    private static final String TAG = "MessageSenderService";

    private NodeManager mNodeManager;

    public MessageSenderService() {
        super(TAG);

        mNodeManager = NodeManager.getsInstance(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate()");
    }

    @Override
    protected synchronized void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent()");

        String intentUri = intent.getStringExtra(Constants.EXTRA_INTENT_URI);

        try {
            mNodeManager.sendIntentMessage(intentUri).get();
        } catch (InterruptedException e) {
            Log.w(TAG, "Sending intent has been interrupted", e);
        } catch (ExecutionException e) {
            Log.w(TAG, "Sending intent caused an exception", e);
        }
    }
}
