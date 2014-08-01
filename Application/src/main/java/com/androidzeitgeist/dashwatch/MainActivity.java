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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.androidzeitgeist.dashwatch.muzei.SourceManager;

import java.util.List;

import javax.xml.transform.Source;

public class MainActivity extends Activity {
    private static final String TAG = "DashWatch/MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(com.androidzeitgeist.dashwatch.R.layout.activity_main);

        selectDefaultSource();
        subscribeToExtensions();
    }

    private void selectDefaultSource() {
        SourceManager sourceManager = SourceManager.getInstance(this);
        List<SourceManager.SourceListing> availableSources = sourceManager.getAvailableSources();

        SourceManager.SourceListing source = null;

        for (SourceManager.SourceListing listing : availableSources) {
            Log.d(TAG, "Source: " + listing.title + " (" + listing.componentName + ")");

            // TODO: I like space pictures *hack*
            if (listing.title.contains("Nasa")) {
                source = listing;
            }
        }

        if (source == null && availableSources.size() > 0) {
            source = availableSources.get(0);
        }

        if (source != null) {
            Log.d(TAG, "Selecting and subscribing to source: " + source.title);

            sourceManager.selectSource(source.componentName);
            sourceManager.subscribeToSelectedSource();
        }
    }

    private void subscribeToExtensions() {
        Intent intent = new Intent(this, DashWatchService.class);
        startService(intent);
    }
}
