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

package com.androidzeitgeist.dashwatch.common;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;

import java.net.URISyntaxException;

public class ExtensionUpdate {
    private static final String TAG = "ExtensionUpdate";

    private static final String KEY_POSITION = "position";
    private static final String KEY_TITLE = "title";
    private static final String KEY_TEXT = "text";
    private static final String KEY_COMPONENT = "component";
    private static final String KEY_INTENT = "intent";
    private static final String KEY_STATUS = "status";
    private static final String KEY_VISIBLE = "visible";
    private static final String KEY_ICON = "icon";

    private int position;
    private String title;
    private String text;
    private String component;
    private Intent intent;
    private String status;
    private boolean visible;
    private Bitmap icon;

    public static ExtensionUpdate fromDataMap(Context context, DataMap dataMap) {
        ExtensionUpdate update = new ExtensionUpdate();

        update.setPosition(dataMap.getInt(KEY_POSITION));
        update.setTitle(dataMap.getString(KEY_TITLE));
        update.setText(dataMap.getString(KEY_TEXT));
        update.setComponent(dataMap.getString(KEY_COMPONENT));
        update.setStatus(dataMap.getString(KEY_STATUS));
        update.setVisible(dataMap.getBoolean(KEY_VISIBLE));

        if (dataMap.containsKey(KEY_ICON)) {
            update.setIcon(AssetHelper.loadBitmapFromAsset(context, dataMap.getAsset(KEY_ICON)));
        }

        if (dataMap.containsKey(KEY_INTENT)) {
            try {
                update.setIntent(Intent.parseUri(dataMap.getString(KEY_INTENT), 0));
            } catch (URISyntaxException e) {
                Log.w(TAG, "Unable to parse intent uri", e);
            }
        }

        return update;
    }

    public void writeToDataMap(DataMap dataMap) {
        dataMap.putInt(KEY_POSITION, position);
        dataMap.putString(KEY_TITLE, title);
        dataMap.putString(KEY_TEXT, text);
        dataMap.putString(KEY_COMPONENT, component);
        dataMap.putString(KEY_STATUS, status);
        dataMap.putBoolean(KEY_VISIBLE, visible);

        if (icon != null) {
            dataMap.putAsset(KEY_ICON, AssetHelper.createAssetFromBitmap(icon));
        } else {
            dataMap.remove(KEY_ICON);
        }

        if (hasIntent()) {
            dataMap.putString(KEY_INTENT, intent.toUri(0));
        } else {
            dataMap.remove(KEY_INTENT);
        }
    }

    public String getContentHash() {
        return Hash.sha1(String.format(
            "%s-%s-%s-%s",
            title,
            text,
            component,
            intent != null ? intent.toUri(0) : ""
        ));
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setText(String content) {
        this.text = content;
    }

    public String getText() {
        return text;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getComponent() {
        return component;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public Intent getIntent() {
        return intent;
    }

    public boolean hasIntent() {
        return intent != null;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public Bitmap getIcon() {
        return icon;
    }
}
