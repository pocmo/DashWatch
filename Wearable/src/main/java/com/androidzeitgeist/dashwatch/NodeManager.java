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

import android.content.Context;
import android.util.Log;

import com.androidzeitgeist.dashwatch.common.ConnectionUtil;
import com.androidzeitgeist.dashwatch.common.Constants;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NodeManager {
    private static final String TAG = "DashWatch/NodeManager";

    private static NodeManager sInstance;

    private Context mApplicationContext;
    private GoogleApiClient mGoogleApiClient;
    private ExecutorService mExecutorService;

    public static NodeManager getsInstance(Context context) {
        if (sInstance == null) {
            sInstance = new NodeManager(context);
        }

        return sInstance;
    }

    private NodeManager(Context context) {
        mExecutorService = Executors.newSingleThreadExecutor();
        mApplicationContext = context.getApplicationContext();
        mGoogleApiClient = new GoogleApiClient.Builder(mApplicationContext)
            .addApi(Wearable.API)
            .build();
    }

    public void sendSetupMessage() {
        Log.i(TAG, "sendSetupMessage()");

        sendMessageToNodesInBackground(Constants.PATH_SETUP, null);
    }

    public Future<?> sendIntentMessage(String intentUri) {
        Log.i(TAG, "sendIntentMessage()");

        try {
            return sendMessageToNodesInBackground(Constants.PATH_INTENT, intentUri.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            AssertionError error = new AssertionError("This device has no clue what UTF-8 is..");
            error.initCause(e);
            throw error;
        }
    }

    private Future<?> sendMessageToNodesInBackground(final String path, final byte[] data) {
        return mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                sendMessageToNodes(path, data);
            }
        });
    }

    private void sendMessageToNodes(String path, byte[] data) {
        if (!ConnectionUtil.validateConnection(mGoogleApiClient)) {
            Log.w(TAG, "No connection available");
            return;
        }

        List<Node> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes();
        for (Node node : nodes) {
            Log.d(TAG, String.format("Sending message to node %s", node.getId()));

            PendingResult<MessageApi.SendMessageResult> pendingResult = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, data);
            pendingResult.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(MessageApi.SendMessageResult result) {
                    Log.d(TAG, String.format("onResult(): requestedId=%d", result.getRequestId()));
                }
            });
        }
    }
}
