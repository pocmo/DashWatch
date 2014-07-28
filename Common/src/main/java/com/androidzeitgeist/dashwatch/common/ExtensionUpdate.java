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

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;

import java.net.URISyntaxException;

public class ExtensionUpdate {
    private static final String TAG = "ExtensionUpdate";

    private static final String KEY_TITLE = "title";
    private static final String KEY_TEXT = "text";
    private static final String KEY_COMPONENT = "component";
    private static final String KEY_INTENT = "intent";

    private String title;
    private String text;
    private String component;
    private Intent intent;

    public static ExtensionUpdate fromDataMap(DataMap dataMap) {
        ExtensionUpdate update = new ExtensionUpdate();

        update.setTitle(dataMap.getString(KEY_TITLE));
        update.setText(dataMap.getString(KEY_TEXT));
        update.setComponent(dataMap.getString(KEY_COMPONENT));

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
        dataMap.putString(KEY_TITLE, title);
        dataMap.putString(KEY_TEXT, text);
        dataMap.putString(KEY_COMPONENT, component);

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
}
