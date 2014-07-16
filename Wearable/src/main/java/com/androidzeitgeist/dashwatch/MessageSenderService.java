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
import java.util.LinkedList;
import java.util.List;

public class MessageSenderService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MessageSenderService";

    private GoogleApiClient mGoogleApiClient;

    private List<Intent> intentQueue;

    public MessageSenderService() {
        super(TAG);

        intentQueue = new LinkedList<Intent>();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate()");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    protected synchronized void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent()");

        if (mGoogleApiClient.isConnected()) {
            sendMessageInBackground(intent);

        } else {
            intentQueue.add(intent);
        }
    }

    private void sendMessageInBackground(final Intent intent) {
        new Thread() {
            public void run() {
                sendMessageBlocking(intent);
            }
        }.start();
    }

    private void sendMessageBlocking(Intent intent) {
        String intentUri = intent.getStringExtra(Constants.EXTRA_INTENT_URI);

        Log.d(TAG, String.format("Sending message for intent: %s", intentUri));

        List<Node> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes();
        Log.d(TAG, "Connected nodes: " + nodes);

        for (Node node : nodes) {
            Log.d(TAG, String.format("Sending message to node %s", node.getId()));

            try {
                PendingResult<MessageApi.SendMessageResult> pendingResult = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), Constants.PATH_INTENT, intentUri.getBytes("UTF-8"));
                pendingResult.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        Log.d(TAG, String.format("onResult(): requestedId=%d", sendMessageResult.getRequestId()));
                    }
                });
            } catch (UnsupportedEncodingException e) {
                Log.w(TAG, "This device has no clue what UTF-8 is..");
            }
        }
    }

    @Override
    public synchronized void onConnected(Bundle bundle) {
        Log.i(TAG, "onConnected()");

        Iterator<Intent> iterator = intentQueue.iterator();
        while (iterator.hasNext()) {
            sendMessageInBackground(iterator.next());

            iterator.remove();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended()");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed()");
    }
}
