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

import com.google.android.gms.wearable.DataMap;

public class ExtensionUpdate {
    private static final String KEY_TITLE = "title";
    private static final String KEY_TEXT = "text";
    private static final String KEY_COMPONENT = "component";

    private String title;
    private String text;
    private String component;

    public static ExtensionUpdate fromDataMap(DataMap dataMap) {
        ExtensionUpdate update = new ExtensionUpdate();

        update.setTitle(dataMap.getString(KEY_TITLE));
        update.setText(dataMap.getString(KEY_TEXT));
        update.setComponent(dataMap.getString(KEY_COMPONENT));

        return update;
    }

    public void writeToDataMap(DataMap dataMap) {
        dataMap.putString(KEY_TITLE, title);
        dataMap.putString(KEY_TEXT, text);
        dataMap.putString(KEY_COMPONENT, component);
    }

    public String getContentHash() {
        return Hash.sha1(String.format(
            "%s-%s-%s",
            title,
            text,
            component
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
}
