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

import android.util.Log;

import com.androidzeitgeist.dashwatch.common.ExtensionUpdate;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class StatusManager {
    private static final String TAG = "DashWatch/StatusManager";

    private List<ExtensionUpdate> updates;

    public StatusManager() {
        updates = new LinkedList<ExtensionUpdate>();
    }

    public synchronized void update(ExtensionUpdate update) {
        if (update.isVisible()) {
            add(update);
        } else {
            remove(update);
        }
    }

    // TODO: This should actually rank the extensions by visibility and
    public synchronized ExtensionUpdate[] getRankedExtensions() {
        Log.d(TAG, "Available extensions: " + updates.size());

        return new ExtensionUpdate[] {
            updates.size() > 0 ? updates.get(0) : null,
            updates.size() > 1 ? updates.get(1) : null,
            updates.size() > 2 ? updates.get(2) : null
        };
    }

    private void add(ExtensionUpdate update) {
        // First try to replace existing extension
        for (int i = 0; i < updates.size(); i++) {
            if (updates.get(i).getComponent().equals(update.getComponent())) {
                updates.set(i, update);
                return;
            }
        }

        // It's a new extension
        updates.add(update);
    }

    private void remove(ExtensionUpdate update) {
        Iterator<ExtensionUpdate> iterator = updates.iterator();

        while (iterator.hasNext()) {
            if (iterator.next().getComponent().equals(update.getComponent())) {
                iterator.remove();
                return;
            }
        }
    }
}
