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

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.androidzeitgeist.dashwatch.common.AssetHelper;
import com.androidzeitgeist.dashwatch.common.ConnectionUtil;
import com.androidzeitgeist.dashwatch.common.ExtensionUpdate;
import com.androidzeitgeist.dashwatch.common.Utils;
import com.androidzeitgeist.dashwatch.dashclock.ExtensionManager;
import com.androidzeitgeist.dashwatch.common.Constants;
import com.androidzeitgeist.dashwatch.muzei.ArtworkCache;
import com.androidzeitgeist.dashwatch.muzei.SourceManager;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WearableManager {
    private static final String TAG = "DashWatch/WearableManager";

    private static WearableManager sInstance;

    private Context mApplicationContext;
    private GoogleApiClient mGoogleApiClient;
    private SourceManager mSourceManager;
    private ArtworkCache mArtworkCache;
    private ExtensionManager mExtensionManager;
    private ExecutorService mExecutorService;

    public static WearableManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new WearableManager(context);
        }

        return sInstance;
    }

    private WearableManager(Context context) {
        mApplicationContext = context.getApplicationContext();

        mExecutorService = Executors.newSingleThreadExecutor();

        mSourceManager = SourceManager.getInstance(mApplicationContext);
        mArtworkCache = ArtworkCache.getInstance(mApplicationContext);
        mExtensionManager = ExtensionManager.getInstance(mApplicationContext);

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    public void sendExtensionData(int position, ExtensionManager.ExtensionWithData extension) {
        Log.d(TAG, String.format("Sending extension (%d): %s", position, extension.listing.title));

        String title = extension.latestData.expandedTitle();
        if (TextUtils.isEmpty(title)) {
            title = extension.latestData.status();
        }

        ExtensionUpdate update = new ExtensionUpdate();
        update.setTitle(title);
        update.setText(extension.latestData.expandedBody());
        update.setComponent(extension.listing.componentName.flattenToString());
        update.setIntent(extension.latestData.clickIntent());
        update.setStatus(extension.latestData.status());
        update.setVisible(extension.latestData.visible());
        update.setPosition(position);

        update.setIcon(
                Utils.loadExtensionIcon(
                    mApplicationContext,
                    extension.listing.componentName,
                    extension.latestData.icon(),
                    extension.latestData.iconUri(),
                    0xFFFFFFFF
                )
        );

        sendToWearable(update);
    }

    public void sendArtworkToWearable() {
        ComponentName componentName = mSourceManager.getSelectedSource();

        if (componentName == null) {
            Log.d(TAG, "No selected source");
            return;
        }

        SourceState sourceState = mSourceManager.getSelectedSourceState();

        if (sourceState == null) {
            Log.d(TAG, "No state for selected source");
            return;
        }

        Artwork artwork = sourceState.getCurrentArtwork();

        if (artwork == null) {
            Log.d(TAG, "No artwork for  selected source");
            return;
        }

        File file = mArtworkCache.getArtworkCacheFile(componentName, artwork);
        if (file == null) {
            Log.d(TAG, "No artwork file for selected source");
            return;
        }

        if (!ConnectionUtil.validateConnection(mGoogleApiClient)) {
            Log.e(TAG, "No Google API Client connection");
            return;
        }

        Log.d(TAG, "Sending file from path: " + file.getPath());

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Constants.PATH_ARTWORK_UDPATE);

        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());

        if (bitmap == null) {
            Log.d(TAG, "Could not load bitmap from path: " + file.getPath());
            return;
        }

        putDataMapRequest.getDataMap().putAsset(
                Constants.KEY_ARTWORK_ASSET,
                AssetHelper.createAssetFromBitmap(bitmap)
        );

        sendToWearableInBackground(putDataMapRequest);
    }

    public void sendExtensionDataToWearable() {
        Log.i(TAG, "sendExtensionDataToWearable()");

        List<ExtensionManager.ExtensionWithData> extensions = mExtensionManager.getActiveExtensionsWithData();

        Log.d(TAG, "Active extensions with data: " + extensions.size());

        int i = 0;
        for (ExtensionManager.ExtensionWithData extension : extensions) {
            sendExtensionData(i, extension);
            i++;
        }
    }

    private void sendToWearable(ExtensionUpdate update) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Constants.PATH_EXTENSION_UPDATE);
        update.writeToDataMap(putDataMapRequest.getDataMap());

        sendToWearableInBackground(putDataMapRequest);
    }

    private void sendToWearableInBackground(final PutDataMapRequest putDataMapRequest) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (!ConnectionUtil.validateConnection(mGoogleApiClient)) {
                    Log.e(TAG, "sendToWearableInBackground(): No Google API Client connection");
                    return;
                }

                PutDataRequest request = putDataMapRequest.asPutDataRequest();
                Wearable.DataApi.putDataItem(mGoogleApiClient, request).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        if (!dataItemResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to set the data - status: " + dataItemResult.getStatus().getStatusCode());
                        }
                    }
                });
            }
        });
    }
}
