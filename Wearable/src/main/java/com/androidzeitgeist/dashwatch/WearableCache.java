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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.androidzeitgeist.dashwatch.common.IOUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Cache implementation for caching data and artwork on the wearable.
 */
public class WearableCache {
    private static final String TAG = "DashWatch/WearableCache";

    private static WearableCache sInstance;

    private Context mApplicationContext;

    public synchronized static WearableCache getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new WearableCache(context);
        }

        return sInstance;
    }

    private WearableCache(Context context) {
        mApplicationContext = context.getApplicationContext();
    }

    public Bitmap fetchArtwork() {
        File file = getArtworkFile();

        if (!file.exists()) {
            return null;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        if (bitmap != null) {
            Log.d(TAG, String.format("Successfully loaded artwork: %s", file.getAbsolutePath()));
        } else {
            Log.d(TAG, "Could not load artwork from cache");
        }

        return bitmap;
    }

    public void putArtwork(Bitmap bitmap) {
        File file = getArtworkFile();

        if (file.exists() && !file.delete()) {
            Log.w(TAG, "Could not remove cached artwork");
            return;
        }

        FileOutputStream stream = null;

        try {
            stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            Log.d(TAG, String.format("Artwork cached: %s", file.getAbsolutePath()));
        } catch (IOException exception) {
            Log.w(TAG, "Could not write artwork to cache file due to IOException", exception);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // Don't care..
                }
            }
        }
    }

    private File getArtworkFile() {
        return new File(IOUtil.getBestAvailableCacheRoot(mApplicationContext), "artwork.cache");
    }
}
