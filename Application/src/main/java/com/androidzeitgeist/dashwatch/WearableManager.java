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
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.androidzeitgeist.dashwatch.dashclock.ExtensionManager;
import com.androidzeitgeist.dashwatch.common.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class WearableManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "DashWatch/WearableManager";

    private GoogleApiClient mGoogleApiClient;

    public WearableManager(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void connect() {
        mGoogleApiClient.connect();
    }

    public void disconnect() {
        mGoogleApiClient.disconnect();
    }

    public void updateExtensionNotification(ExtensionManager.ExtensionWithData extension) {
        String title = extension.latestData.expandedTitle();
        if (TextUtils.isEmpty(title)) {
            title = extension.latestData.status();
        }

        if (mGoogleApiClient.isConnected()) {
            buildWearableOnlyNotification(
                    title,
                    extension.latestData.expandedBody(),
                    extension.hashCode(),
                    Constants.PATH_NOTIFICATION
            );
        }
    }

    private void buildWearableOnlyNotification(String title, String content, int id,  String path) {
        if (mGoogleApiClient.isConnected()) {
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
            putDataMapRequest.getDataMap().putString(Constants.KEY_CONTENT, content);
            putDataMapRequest.getDataMap().putString(Constants.KEY_TITLE, title);
            putDataMapRequest.getDataMap().putInt(Constants.KEY_NOTIFICATION_ID, id);
            PutDataRequest request = putDataMapRequest.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            if (!dataItemResult.getStatus().isSuccess()) {
                                Log.e(TAG, "buildWatchOnlyNotification(): Failed to set the data, "
                                        + "status: " + dataItemResult.getStatus().getStatusCode());
                            } else {
                                Log.d(TAG, "buildWatchOnlyNotification(): Success");
                            }
                        }
                    });
        } else {
            Log.e(TAG, "buildWearableOnlyNotification(): no Google API Client connection");
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected()");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended()");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed()");
    }
}
