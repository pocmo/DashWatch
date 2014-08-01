/*
 * Copyright (C) 2014 Google Inc.
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

package com.androidzeitgeist.dashwatch.muzei;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.androidzeitgeist.dashwatch.common.AssetHelper;
import com.androidzeitgeist.dashwatch.common.Constants;
import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.internal.SourceState;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.google.android.apps.muzei.api.internal.ProtocolConstants.ACTION_PUBLISH_STATE;
import static com.google.android.apps.muzei.api.internal.ProtocolConstants.EXTRA_STATE;
import static com.google.android.apps.muzei.api.internal.ProtocolConstants.EXTRA_TOKEN;

public class SourceSubscriberService extends IntentService {
    private static final String TAG = "DashWatch/SourceSubscriberService";
    private static final long CLIENT_CONNECTION_TIMEOUT = 10000;

    private ArtworkCache mArtworkCache;
    private SourceManager mSourceManager;

    public SourceSubscriberService() {
        super("SourceSubscriberService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mSourceManager = SourceManager.getInstance(this);
        mArtworkCache = ArtworkCache.getInstance(this);

        if (intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();

        Log.d(TAG, "onHandleIntent(" + action + ")");

        if (ACTION_PUBLISH_STATE.equals(action)) {
            // Handle API call from source
            String token = intent.getStringExtra(EXTRA_TOKEN);

            SourceState state = null;
            if (intent.hasExtra(EXTRA_STATE)) {
                Bundle bundle = intent.getBundleExtra(EXTRA_STATE);
                if (bundle != null) {
                    state = SourceState.fromBundle(bundle);
                }
            }

            mSourceManager.handlePublishState(token, state);

            mArtworkCache.maybeDownloadCurrentArtworkSync();
            sendArtworkToWearable(state);
        }
    }

    private void sendArtworkToWearable(SourceState state) {
        ComponentName componentName = mSourceManager.getSelectedSource();
        Artwork artwork = state.getCurrentArtwork();

        if (componentName == null || artwork == null) {
            Log.d(TAG, "No artwork :(");
            return;
        }

        File file = mArtworkCache.getArtworkCacheFile(componentName, state.getCurrentArtwork());
        if (file == null) {
            Log.d(TAG, "No artwork file :(");
            return;
        }

        GoogleApiClient apiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult result =
                apiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return;
        }

        Log.d(TAG, "Sending file from path: " + file.getPath());

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Constants.PATH_ARTWORK_UDPATE);

        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());

        if (bitmap == null) {
            Log.d(TAG, "Could not load bitmap :(");
            return;
        }

        putDataMapRequest.getDataMap().putAsset(
            Constants.KEY_ARTWORK_ASSET,
            AssetHelper.createAssetFromBitmap(bitmap)
        );

        putDataMapRequest.getDataMap().putLong("foo", System.currentTimeMillis());

        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(apiClient, request).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                if (!dataItemResult.getStatus().isSuccess()) {
                    Log.e(TAG, "sendArtworkToWearable(): Failed to set the data, "
                            + "status: " + dataItemResult.getStatus().getStatusCode());
                } else {
                    Log.d(TAG, "sendArtworkToWearable(): Success");
                }
            }
        });
    }
}