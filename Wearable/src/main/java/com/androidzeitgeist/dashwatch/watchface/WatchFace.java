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

package com.androidzeitgeist.dashwatch.watchface;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidzeitgeist.dashwatch.NodeManager;
import com.androidzeitgeist.dashwatch.R;
import com.androidzeitgeist.dashwatch.WearableCache;
import com.androidzeitgeist.dashwatch.common.ExtensionUpdate;
import com.androidzeitgeist.dashwatch.event.ArtworkUpdate;
import com.androidzeitgeist.dashwatch.event.BusProvider;
import com.squareup.otto.Subscribe;

import java.util.List;

public class WatchFace extends Activity {
    private static final String TAG = "DashWatch/WatchFace";

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive(" + intent.getAction() + ")");

            updateTime();
        }
    };

    private StatusManager mStatusManager;
    private WearableCache mCache;

    private ImageView mBackgroundView;
    private TextView mTimeView;
    private TextView mDateView;
    private TextView[] mStatusViews;
    private ImageView[] mIconViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");

        super.onCreate(savedInstanceState);

        mCache = WearableCache.getInstance(this);

        setContentView(R.layout.activity_watch_face);

        initializeViews();
        loadArtworkFromCache();
        updateTime();
        registerReceiver();

        BusProvider.getBus().register(this);

        NodeManager.getsInstance(this).sendSetupMessage();
    }

    private void initializeViews() {
        mStatusViews = new TextView[] {
                (TextView) findViewById(R.id.status1),
                (TextView) findViewById(R.id.status2),
                (TextView) findViewById(R.id.status3)
        };

        mIconViews = new ImageView[] {
                (ImageView) findViewById(R.id.icon1),
                (ImageView) findViewById(R.id.icon2),
                (ImageView) findViewById(R.id.icon3)
        };

        this.mStatusManager = new StatusManager();
        this.mTimeView = (TextView) findViewById(R.id.time);
        this.mDateView = (TextView) findViewById(R.id.date);
        this.mBackgroundView = (ImageView) findViewById(R.id.background);
    }

    private void loadArtworkFromCache() {
        Bitmap bitmap = mCache.fetchArtwork();

        if (bitmap != null) {
            mBackgroundView.setImageBitmap(bitmap);
        }
    }

    private void registerReceiver() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");

        super.onDestroy();

        BusProvider.getBus().unregister(this);

        unregisterReceiver();
    }

    private void unregisterReceiver() {
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume()");

        super.onResume();

        mBackgroundView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause()");

        super.onPause();

        mBackgroundView.setVisibility(View.GONE);
    }

    @Subscribe
    public void onStatusUpdate(ExtensionUpdate update) {
        Log.d(TAG, "Received update for extension: " + update.getComponent());

        mStatusManager.update(update);

        updateExtensionsStatus();
    }

    @Subscribe
    public void onArtworkUpdate(ArtworkUpdate update) {
        mBackgroundView.setImageBitmap(update.getBitmap());
    }

    private void updateTime() {
        Log.d(TAG, "updateTime()");

        mDateView.setText(
            DateUtils.formatDateTime(
                this,
                System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_ALL
            )
        );

        mTimeView.setText(
            DateUtils.formatDateTime(
                this,
                System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME
            )
        );
    }

    private void updateExtensionsStatus() {
        Log.i(TAG, "updateExtensionsStatus()");

        List<ExtensionUpdate> extensions = mStatusManager.getRankedExtensions();

        // TODO: This is just a fast and ugly hack. The layout should be more dynamically.
        updateExtensionView(extensions.get(0), mStatusViews[0], mIconViews[0]);
        updateExtensionView(extensions.get(1), mStatusViews[1], mIconViews[1]);
        updateExtensionView(extensions.get(2), mStatusViews[2], mIconViews[2]);
    }

    private void updateExtensionView(ExtensionUpdate extension, TextView statusView, ImageView iconView) {
        if (extension == null || !extension.isVisible()) {
            statusView.setVisibility(View.GONE);
            iconView.setVisibility(View.GONE);
        } else {
            statusView.setVisibility(View.VISIBLE);
            iconView.setVisibility(View.VISIBLE);

            String status = extension.getStatus();

            statusView.setText(status);
            iconView.setImageBitmap(extension.getIcon());

            if (status.length() >= 4) {
                statusView.setTextSize(14);
            } else {
                statusView.setTextSize(18);
            }
        }
    }
}
