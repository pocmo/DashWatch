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

package com.androidzeitgeist.dashwatch.common;

/**
 * Constants that are used in both the Application and the Wearable modules.
 */
public final class Constants {
    private Constants() {};

    public static final String PATH_EXTENSION_UPDATE = "/extension/update";
    public static final String PATH_ARTWORK_UDPATE = "/artwork/update";

    public static final String PATH_INTENT = "/intent";
    public static final String PATH_SETUP = "/setup";

    public static final String EXTRA_INTENT_URI = "intentUri";
    public static final String EXTRA_EXTENSION_COMPONENT = "extension_component";

    public static final String KEY_ARTWORK_ASSET = "artwork_asset";

    public static final String ACTION_DISMISS = "com.androidzeitgeist.dashwatch.notification.DISMISS";
}
